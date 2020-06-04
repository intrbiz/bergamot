#!/usr/bin/python3

from base64 import urlsafe_b64decode, urlsafe_b64encode
from struct import unpack, pack
import uuid
import hashlib
import hmac

import time
import socket

import websocket
import json

class AuthKey:
    def __init__(self, key):
        key = key + ''.join('=' for i in range(len(key) % 4))
        key_binary = urlsafe_b64decode(key)
        magic = unpack('!H', key_binary[0:2]);
        if (magic[0] & 0xFFF0) == 0x1BB0:
            self.version = magic[0] & 0xF
            if self.version == 1:
                self.key_id = uuid.UUID(bytes=key_binary[2:18])
                self.secret = key_binary[18:len(key_binary)]
            else:
                raise Exception("Unsupported version")
        else:
            raise Exception("Bad magic")
    
    def get_version(self):
        return self.version
    
    def get_key_id(self):
        return self.key_id
    
    def get_secret(self):
        return self.secret

    def sign(self, timestamp, id, attrs):
        mac = hmac.new(self.secret, digestmod=hashlib.sha256)
        mac.update(pack('>Q16s', timestamp, id.bytes))
        for key in sorted(attrs.keys()):
            mac.update(key.encode('UTF8'))
            if attrs[key] != None:
                mac.update(attrs[key].encode('UTF8'))
        return urlsafe_b64encode(mac.digest()).decode('UTF8')


class ClientHeaders:
    def __init__(self, id):
        self.attrs = {}
        self.id = id
        self.timestamp = int(time.time())
        self.user_agent = 'Python Bergamot Proxy Client'
    
    def _header_name(self, short_name):
        return 'x-bergamot-' + short_name
    
    def _attr(self, name, value):
        self.attrs[ self._header_name(name) ] = value
        
    def _proxy_for(self, value):
        self._attr('proxy-for', value)
        
    def proxy_for_worker(self):
        self._proxy_for('worker')
        return self
    
    def proxy_for_notifier(self):
        self._proxy_for('notifier')
        return self
    
    def with_engines(self, engines):
        self._attr('engines', ','.join(engines))
        return self
    
    def with_worker_pool(self, worker_pool):
        if worker_pool != None and worker_pool != '':
            self._attr('worker-pool', worker_pool)
        return self
    
    def with_host_name(self, host_name):
        if host_name != None and host_name != '':
            self._attr('host-name', host_name)
        else:
            self._attr('host-name', socket.gethostname())
        return self
    
    def with_info(self, info):
        if info != None:
            self._attr('info', info)
        return self
    
    def with_user_agent(self, user_agent):
        if user_agent != None and user_agent != '':
            self.user_agent = user_agent
        return self

    def sign(self, auth_key):
        auth_sig = auth_key.sign(self.timestamp, self.id, self.attrs)
        headers = {}
        for key in self.attrs.keys():
            headers[key] = self.attrs[key]
        headers[self._header_name('id')] = str(self.id)
        headers[self._header_name('timestamp')] = str(self.timestamp)
        headers[self._header_name('key-id')] = str(auth_key.get_key_id())
        headers['authorization'] = auth_sig
        headers['user-agent'] = self.user_agent
        return headers


class BergamotDummyWorkerEngine:
    def get_name(self):
        return 'dummy'
    
    def start(self, context):
        print("Starting dummy engine")
    
    def execute(self, check, context):
        print("Processing check: %s" % (check));
    
    def stop(self):
        print("Stopping dummy engine")
    

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
    
    def register_worker(self, worker):
        self.engines[worker.get_name()] = worker
    
    def start(self):
        for engine in self.engines.values():
            engine.start(self)
    
    def stop(self):
        for engine in self.engines.values():
            engine.stop()
    
    def process_check(self, check):
        engine = self.engines.get(check.engine)
        if engine != None:
            engine.execute(check, self)
    
    def _engine_names(self):
        return self.engines.keys()

    def on_message(self, ws, message):
        msg = json.loads(message)
        print(msg)

    def on_error(self, ws, error):
        print(error)

    def on_close(self, ws):
        print("### closed ###")

    def on_open(self, ws):
        print("### open ###")

    def run(self):
        websocket.enableTrace(True)
        self.headers = ClientHeaders(self.id).proxy_for_worker().with_engines(self._engine_names()).with_worker_pool(self.worker_pool).with_host_name(self.host_name).with_info(self.info).sign(self.auth_key)
        self.ws = websocket.WebSocketApp(self.url, on_open = self.on_open, on_message = self.on_message, on_error = self.on_error, on_close = self.on_close, header = self.headers)
        self.ws.run_forever(ping_interval = 30)


if __name__ == "__main__":
    worker = BergamotWorker()
    worker.configure()
    worker.run()



