#!/usr/bin/python3

from bergamot.auth import ClientHeaders, AuthKey
from bergamot.message import *
from bergamot.worker_api import *
import uuid
import websocket
import json
import time


class BergamotWorker:
    def __init__(self):
        self.id = uuid.uuid4()
        self.url = None
        self.engines = {}
        self.worker_pool = None;
        self.host_name = None;
        self.info = '';
        self.auth_key = None;
    
    def configure(self):
        self.url = 'ws://127.0.0.1:14080/proxy'
        self.auth_key = AuthKey('G7EiyxnJRkxGpqvZ5cYg5ygz0eKHFdegce8cqEgEdTCRN0nBc_cqkBWKt82N4MuE9H4')
    
    def register_engine(self, worker):
        self.engines[worker.get_name()] = worker
    
    def start(self):
        for engine in self.engines.values():
            engine.start(BergamotWorkerContext(self))
    
    def stop(self):
        for engine in self.engines.values():
            engine.stop()
    
    def process_check(self, check):
        check.received(int(round(time.time() * 1000)))
        try:
            engine = self.engines.get(check.engine())
            engine.execute(check, BergamotWorkerCheckContext(check, self))
        except:
            print("Error processing check")
    
    def _engine_names(self):
        return self.engines.keys()
    
    def send(self, message):
        self.socket.send(json.dumps(message))

    def on_message(self, ws, message):
        msg = json.loads(message)
        if msg['type'] == 'bergamot.worker.check.execute':
            self.process_check(ExecuteCheck(msg))

    def on_error(self, ws, error):
        print(error)

    def on_close(self, ws):
        print("### closed ###")

    def on_open(self, ws):
        print("### open ###")

    def run(self):
        self.headers = ClientHeaders(self.id).proxy_for_worker().with_engines(self._engine_names()).with_worker_pool(self.worker_pool).with_host_name(self.host_name).with_info(self.info).sign(self.auth_key)
        self.socket = websocket.WebSocketApp(self.url, 
                                             on_open = lambda ws: self.on_open(ws), 
                                             on_message = lambda ws, message: self.on_message(ws, message), 
                                             on_error = lambda ws, error: self.on_error(ws, error), 
                                             on_close = lambda ws: self.on_close(ws), 
                                             header = self.headers)
        self.socket.run_forever(ping_interval = 30)
