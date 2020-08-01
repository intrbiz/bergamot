from bergamot.agent.api import *
from bergamot.agent.message import *

import psutil

class BergamotAgentCheckMemHandler(BaseBergamotAgentHandler):
    def get_message_types(self):
        return [ CheckMem.TYPE_NAME ]
    
    def execute(self, message):
        mem = psutil.virtual_memory()
        return MemStat() \
            .with_ram(mem.total) \
            .with_total_memory(mem.total) \
            .with_used_memory(mem.used + mem.buffers + mem.cached) \
            .with_free_memory(mem.available) \
            .with_actual_used_memory(mem.used) \
            .with_actual_free_memory(mem.free) \
            .with_used_memory_percentage(((mem.total - (mem.used + mem.buffers + mem.cached)) / mem.total) * 100) \
            .with_free_memory_percentage(((mem.total - mem.available) / mem.total) * 100)
