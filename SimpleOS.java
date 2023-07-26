import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SimpleOS {

    private static final String APPS_FOLDER_PATH = "apps/";
    private static final String APPS_JSON_FILE = "apps.json";
    private static final String PYTHON_SCRIPT_PATH = "parse_apps_json.py"; // Update this path

    public static void main(String[] args) {
        System.out.println("Welcome to SimpleOS!");
        List<AppInfo> apps = readAppInfoFromPythonScript();
        System.out.println("Available apps:");
        for (AppInfo app : apps) {
            System.out.println("- " + app.getName());
        }
        runShell(apps);
    }

    private static void runShell(List<AppInfo> apps) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.print("$ ");
                String command = reader.readLine();
                if (command.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    break;
                } else if (command.startsWith("run ")) {
                    String[] commandParts = command.split(" ");
                    if (commandParts.length >= 2) {
                        String appName = commandParts[1];
                        AppInfo appToRun = getAppInfoByName(apps, appName);
                        if (appToRun != null) {
                            String jarPath = Paths.get(APPS_FOLDER_PATH, appToRun.getFileName()).toString();
                            runJarFile(jarPath);
                        } else {
                            System.out.println("App not found: " + appName);
                        }
                    } else {
                        System.out.println("Usage: run <app_name>");
                    }
                } else {
                    System.out.println("Unknown command: " + command);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<AppInfo> readAppInfoFromPythonScript() {
        List<AppInfo> apps = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("python", PYTHON_SCRIPT_PATH);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read JSON data from Python script output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        String fileName = parts[0];
                        String name = parts[1];
                        apps.add(new AppInfo(fileName, name));
                    }
                }
            }

            int exitCode = process.waitFor();
            System.out.println("Python script exited with code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return apps;
    }

    private static AppInfo getAppInfoByName(List<AppInfo> apps, String name) {
        for (AppInfo app : apps) {
            if (app.getName().equalsIgnoreCase(name)) {
                return app;
            }
        }
        return null;
    }

    private static void runJarFile(String jarPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarPath);
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            System.out.println("Java application exited with code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class AppInfo {
        private final String fileName;
        private final String name;

        public AppInfo(String fileName, String name) {
            this.fileName = fileName;
            this.name = name;
        }

        public String getFileName() {
            return fileName;
        }

        public String getName() {
            return name;
        }
    }
}
