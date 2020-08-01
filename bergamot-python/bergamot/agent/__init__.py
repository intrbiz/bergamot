from bergamot.auth import ClientHeaders, AuthKey
from bergamot.agent.message import *
from bergamot.agent.api import *
import uuid
import websocket
import json
import time
import socket
from bergamot.log import log

class BergamotAgent:
    def __init__(self):
        self.handlers = []
        self.message_handlers = {}
        self.id = None
        self.url = None
        self.auth_key = None
        self.template_name = None
        self.host_name = None
        self.info = ''
    
    def configure(self, config):
        # required config
        self.id = uuid.UUID(config['agent_id'])
        self.url = config['url']
        self.auth_key = AuthKey(config['auth_key'])
        if not self.auth_key.is_agent_key():
            raise Exception("Incorrect key type, was expecting an agent key")
        # optional config
        self.template_name = config.get('template_name')
        self.host_name = config.get('host_name', socket.gethostname())
        self.info = config.get('info', self.host_name)
    
    def register_handler(self, handler):
        self.handlers.append(handler)
        for type_name in handler.get_message_types():
            self.message_handlers[type_name] = handler
    
    def start(self):
        for handler in self.handlers.values():
            handler.start(BergamotAgentContext(self))
    
    def stop(self):
        for handler in self.handlers.values():
            handler.stop()
    
    def process_message(self, message):
        try:
            handler = self.message_handlers.get(message.get_type_name())
            if handler:
                response = handler.execute(message)
                if response:
                    self.send(response.with_id(str(uuid.uuid4())).with_reply_to(message.id()))
            else:
                raise Exception('No handler')
        except Exception as e:
            log("ERROR", "BergamotAgent", "Error processing message", error=e)
            self.send(GeneralError().with_id(str(uuid.uuid4())).with_reply_to(message.id()).with_message(str(e)))
    
    def send(self, message):
        js = json.dumps(message.to_message())
        self.socket.send(js)

    def on_message(self, ws, message):
        #print(message)
        msg = decode_agent_message(json.loads(message))
        if msg:
            self.process_message(msg)

    def on_error(self, ws, error):
        log("ERROR", "BergamotAgent", "Error occured during error handling", error=error)

    def on_close(self, ws):
        log("INFO", "BergamotAgent", "Connection closed")

    def on_open(self, ws):
        log("INFO", "BergamotAgent", "Connection open")

    def run(self):
        self.headers = ClientHeaders(self.id).with_user_agent("BergamotAgent [Python]").with_host_name(self.host_name).with_info(self.info).with_template_name(self.template_name).sign(self.auth_key)
        self.socket = websocket.WebSocketApp(self.url, 
                                             on_open = lambda ws: self.on_open(ws), 
                                             on_message = lambda ws, message: self.on_message(ws, message), 
                                             on_error = lambda ws, error: self.on_error(ws, error), 
                                             on_close = lambda ws: self.on_close(ws), 
                                             header = self.headers)
        self.socket.run_forever(ping_interval = 30)
