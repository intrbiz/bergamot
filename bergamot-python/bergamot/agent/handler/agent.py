from bergamot.agent.api import *
from bergamot.agent.message import *

import psutil
import platform
import os

class BergamotAgentCheckAgentHandler(BaseBergamotAgentHandler):
    def get_message_types(self):
        return [ CheckAgent.TYPE_NAME ]
    
    def execute(self, message):
        mem = psutil.virtual_memory()
        return AgentStat() \
            .with_processors(psutil.cpu_count()) \
            .with_free_memory(mem.total) \
            .with_total_memory(mem.total) \
            .with_max_memory(mem.total) \
            .with_runtime('Python') \
            .with_runtime_vendor(platform.python_implementation()) \
            .with_runtime_version(platform.python_version()) \
            .with_agent_vendor('Bergamot Monitoring') \
            .with_agent_product('Bergamot Agent') \
            .with_agent_version('4.0.0') \
            .with_user_name(os.getlogin()) \
            .with_os_name(platform.system()) \
            .with_os_version(platform.release() + ' ' + platform.version()) \
            .with_os_arch(platform.machine())
