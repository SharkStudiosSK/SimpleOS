import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SimpleOS {

    private static final String APPS_FOLDER_PATH = "apps/";
    private static final String APPS_XML_FILE = "apps.xml";

    public static void main(String[] args) {
        System.out.println("Welcome to SimpleOS!");
        runShell();
    }

    private static void runShell() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            List<String> appNames = readAppNamesFromXml();
            System.out.println("Available apps:");
            for (String appName : appNames) {
                System.out.println("- " + appName);
            }

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
                        if (appNames.contains(appName)) {
                            String jarPath = APPS_FOLDER_PATH + appName + ".jar";
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

    private static List<String> readAppNamesFromXml() {
        List<String> appNames = new ArrayList<>();
        try {
            File xmlFile = new File(APPS_FOLDER_PATH + APPS_XML_FILE);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("app");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String appName = node.getTextContent();
                    appNames.add(appName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appNames;
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
}
