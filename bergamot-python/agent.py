#!/usr/bin/python3

from bergamot.agent import BergamotAgent
from bergamot.agent.handler.ping import *
from bergamot.agent.handler.cpu import *
from bergamot.agent.handler.mem import *
from bergamot.agent.handler.disk import *
from bergamot.agent.handler.net import *
from bergamot.agent.handler.agent import *
from bergamot.agent.handler.os import *
from bergamot.agent.handler.uptime import *
from bergamot.agent.handler.ps import *
from bergamot.config import load_agent_config


agent = BergamotAgent()

agent.register_handler(BergamotAgentPingHandler())
agent.register_handler(BergamotAgentCheckCPUHandler())
agent.register_handler(BergamotAgentCheckMemHandler())
agent.register_handler(BergamotAgentCheckDiskHandler())
agent.register_handler(BergamotAgentCheckNetConHandler())
agent.register_handler(BergamotAgentCheckAgentHandler())
agent.register_handler(BergamotAgentCheckOSHandler())
agent.register_handler(BergamotAgentCheckUptimeHandler())
agent.register_handler(BergamotAgentCheckProcessHandler())

agent.configure(load_agent_config())
agent.run()
