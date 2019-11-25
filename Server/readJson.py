import json

with open('/Users/jguo/Desktop/data.json') as json_file:
    for line in json_file:
        data = json.loads(line)
        # for p in data:
        print(data)
