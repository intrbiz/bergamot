class BaseBergamotAgentHandler:    
    def get_message_types(self):
        return []
    
    def start(self, context):
        self.context = context
    
    def execute(self, message):
        raise Exception('Unimplemented')
    
    def stop(self):
        pass


class BergamotAgentContext:
    def __init__(self, agent):
        self.agent = agent
