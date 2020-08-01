from bergamot.agent.api import *
from bergamot.agent.message import *
import time

import psutil

class BergamotAgentCheckUptimeHandler(BaseBergamotAgentHandler):
    def get_message_types(self):
        return [ CheckUptime.TYPE_NAME ]
    
    def execute(self, message):
        boot = psutil.boot_time()
        now = time.time()
        return UptimeStat().with_uptime(int(now - boot))
