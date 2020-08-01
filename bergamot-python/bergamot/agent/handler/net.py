from bergamot.agent.api import *
from bergamot.agent.message import *

from socket import SocketKind
from socket import AddressFamily

import psutil

class BergamotAgentCheckNetConHandler(BaseBergamotAgentHandler):
    def get_message_types(self):
        return [ CheckNetCon.TYPE_NAME ]
    
    def execute(self, message):
        cons = psutil.net_connections(kind=BergamotAgentCheckNetConHandler.kind(message))
        stat = NetConStat()
        for con in cons:
            if BergamotAgentCheckNetConHandler.matchesFilter(message, con):
                info = NetConInfo() \
                    .with_protocol(BergamotAgentCheckNetConHandler.protocol(con)) \
                    .with_state(con.status) \
                    .with_local_address(BergamotAgentCheckNetConHandler.local_address(con)) \
                    .with_local_port(BergamotAgentCheckNetConHandler.local_port(con)) \
                    .with_remote_address(BergamotAgentCheckNetConHandler.remote_address(con)) \
                    .with_remote_port(BergamotAgentCheckNetConHandler.remote_port(con))
                stat.with_connections(info)
        return stat

    def kind(message):
        if message.unix():
            return 'all'
        return 'inet'

    def matchesFilter(message, con):
        # ignore time wait cons
        if con.status == 'TIME_WAIT':
            return False
        # protocol filter
        if con.family == AddressFamily.AF_INET or con.family == AddressFamily.AF_INET6:
            if con.type == SocketKind.SOCK_STREAM:
                if not message.tcp():
                    return False
            elif con.type == SocketKind.SOCK_DGRAM:
                if not message.udp():
                    return False
        elif con.family == AddressFamily.AF_UNIX:
            if not message.unix():
                return False
        # match client and or server
        if not ((message.client() and con.status != 'LISTEN') or (message.server() and con.status == 'LISTEN')):
            return False
        # local port match
        if message.local_port() and message.local_port() > 0:
            if message.local_port() != BergamotAgentCheckNetConHandler.local_port(con):
                return False
        # remote port match
        if message.remote_port() and message.remote_port() > 0:
            if message.remote_port() != BergamotAgentCheckNetConHandler.remote_port(con):
                return False
        return True
    
    def protocol(con):
        if con.family == AddressFamily.AF_INET or con.family == AddressFamily.AF_INET6:
            if con.type == SocketKind.SOCK_STREAM:
                return 'tcp'
            elif con.type == SocketKind.SOCK_DGRAM:
                return 'udp'
            return 'inet'
        elif con.family == AddressFamily.AF_UNIX:
            return 'unix'
        return None
    
    def local_address(con):
        try:
            if con.family == AddressFamily.AF_INET or con.family == AddressFamily.AF_INET6:
                return con.laddr.ip
            elif con.family == AddressFamily.AF_UNIX:
                return con.laddr
        except:
            pass
        return None
    
    def local_port(con):
        try:
            if con.family == AddressFamily.AF_INET or con.family == AddressFamily.AF_INET6:
                return con.laddr.port
        except:
            pass
        return None
        
    def remote_address(con):
        try:
            if con.family == AddressFamily.AF_INET or con.family == AddressFamily.AF_INET6:
                return con.raddr.ip
            elif con.family == AddressFamily.AF_UNIX:
                return con.raddr
        except:
            pass
        return None
    
    def remote_port(con):
        try:
            if con.family == AddressFamily.AF_INET or con.family == AddressFamily.AF_INET6:
                return con.raddr.port
        except:
            pass
        return None    
