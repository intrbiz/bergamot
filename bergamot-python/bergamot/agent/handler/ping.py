from bergamot.agent.api import *
from bergamot.agent.message import *
import time


class BergamotAgentPingHandler(BaseBergamotAgentHandler):
    def get_message_types(self):
        return [ AgentPing.TYPE_NAME ]
    
    def execute(self, message):
        return AgentPong().with_timestamp(int(time.time() * 1000))

