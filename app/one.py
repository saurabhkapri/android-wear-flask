from flask import Flask
from flask import request
from flask_cors import CORS
import csv
import time

app = Flask(__name__)
CORS(app)


@app.route('/')
def parse_data():
    try:
        url = request.url
        method = request.method
        headers = request.headers
        body = request.body

        return {'message': 'Request parsed sucessfully'}
    except Exception as e:
        return {'error': str(e)}


if __name__ == '__main__':
    app.run()
