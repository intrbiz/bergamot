from bergamot.agent.api import *
from bergamot.agent.message import *

import platform

class BergamotAgentCheckOSHandler(BaseBergamotAgentHandler):
    def get_message_types(self):
        return [ CheckOS.TYPE_NAME ]
    
    def execute(self, message):
        uname = platform.uname()
        return OSStat() \
            .with_arch(uname.machine) \
            .with_name(uname.system) \
            .with_description('') \
            .with_machine(uname.machine) \
            .with_version(uname.release) \
            .with_patch_level(uname.version) \
            .with_vendor('') \
            .with_vendor_name('') \
            .with_vendor_code_name('') \
            .with_vendor_version('')
