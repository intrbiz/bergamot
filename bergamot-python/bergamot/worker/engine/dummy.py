from bergamot.agent.api import *


class BergamotDummyWorkerEngine(BaseBergamotWorkerEngine):
    def get_name(self):
        return 'dummy'
    
    def start(self, context):
        self.context = context
        print("Starting dummy engine")
    
    def execute(self, check, context):
        print("Processing check: %s" % (check));
        if check.executor() == 'static':
            self.execute_static_check(check, context)
        elif check.executor() == 'random':
            self.execute_random_check(check, context)
    
    def execute_static_check(self, check, context):
        context.publish_result(ActiveResult().ok("All good, nothing to see here"))
    
    def execute_random_check(self, check, context):
        context.publish_result(ActiveResult().ok("All good, nothing to see here"))
    
    def stop(self):
        print("Stopping dummy engine")
