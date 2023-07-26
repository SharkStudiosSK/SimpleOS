import json

def read_app_info_from_json(json_file):
    with open(json_file, 'r') as file:
        apps_data = json.load(file)['apps']
    return apps_data

if __name__ == "__main__":
    apps_info = read_app_info_from_json("apps/apps.json")
    for app in apps_info:
        print(app['file'] + "," + app['name'])
