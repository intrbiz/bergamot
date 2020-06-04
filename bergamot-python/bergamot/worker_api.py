#!/usr/bin/python3

from bergamot.auth import ClientHeaders, AuthKey
from bergamot.message import *
import uuid
import websocket
import json
import time


class BaseBergamotWorkerEngine:
    def get_name(self):
        raise Exception('Unimplemented')
    
    def start(self, context):
        raise Exception('Unimplemented')
    
    def execute(self, check, context):
        raise Exception('Unimplemented')
    
    def stop(self):
        raise Exception('Unimplemented')


class BergamotWorkerContext:
    def __init__(self, worker):
        self.worker = worker
    
    def publish_result(self, result):
        print(result)
        self.worker.send(result.message)


class BergamotWorkerCheckContext:
    def  __init__(self, check, worker):
        self.check = check
        self.worker = worker
    
    def publish_result(self, result):
        result.from_check(self.check)
        result.sent(int(round(time.time() * 1000)))
        print(result)
        self.worker.send(result.message)
