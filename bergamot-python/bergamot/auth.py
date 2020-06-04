#!/usr/bin/python3

from base64 import urlsafe_b64decode, urlsafe_b64encode
from struct import unpack, pack
import uuid
import hashlib
import hmac
import time
import socket


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
