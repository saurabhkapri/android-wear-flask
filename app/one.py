# from flask import Flask, request
# import pandas as pd
# import time
# import csv
# import pandas
# from flask_cors import CORS

# app = Flask(__name__)
# CORS(app)


# @app.route('/', methods=['GET', 'POST'])
# def parse_data():
#     try:
#         body = request.form.to_dict()
#         print('zzzzz')
#         print("body", body)
#         # if (int(body['rate'][:2]) !=0 and )
#         #     with open('zz.csv', 'a+', newline='') as file:
#         #         fieldnames = ['timestamp', 'heart-beat']
#         #         writer = csv.DictWriter(file, fieldnames=fieldnames)
#         #         now = time.strftime('%Y-%m-%d %H:%M:%S')
#         #         writer.writerow({'timestamp': now, 'heart-beat': heartbeat})

#         # df = pd.read_csv("zz.csv")
#         # if(int(body['rate'][:2]) != 0 and df.shape[0] < 60):
#         #     # print('zzzzz')
#         #     new = {
#         #         'time-stamp': time.strftime('%Y-%m-%d %H:%M:%S'),
#         #         'heart-beat': int(body['rate'][:2])}
#         #     df.loc[len(df)] = new

#         # elif(df.shape[0] >= 60):
#         #     print('max capacity reached')
#         #     # df.drop(df.index[0])
#         #     new = {
#         #         'time-stamp': time.strftime('%Y-%m-%d %H:%M:%S'),
#         #         'heart-beat': int(body['rate'][:2])}
#         #     df.loc[len(df)] = new

#         # df.to_csv('zz.csv', index=False)
#         # print('saved to csv')
#         return "hello world"

#     except Exception as e:
#         return "error occured"


# if __name__ == '__main__':
#     df = pd.DataFrame(columns=['time-stamp', 'heart-beat'])
#     df.to_csv("zz.csv", index=False)
#     # df = pd.read_csv("zz.csv")
#     app.run(host="0.0.0.0", port=5000)


from flask import Flask
from flask import request
from flask_cors import CORS

app = Flask(__name__)
CORS(app)


@app.route('/', methods=['GET', 'POST'])
def parse_data():
    try:
        url = request.url
        method = request.method
        headers = request.headers
        body = request.form.to_dict()
        print(body)
        return {'message': 'Request parsed successfully'}
    except Exception as e:
        return {'error': str(e)}


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000)
