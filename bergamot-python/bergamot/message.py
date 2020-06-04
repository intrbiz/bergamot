#!/usr/bin/python3


#{
#  "type" : "bergamot.worker.check.execute",
#  "id" : "d2dd562d-05ee-4086-889e-2b8d4ea447a0",
#  "reply_to" : null,
#  "worker_id" : "932ecf03-a571-4a1b-abbb-da613003787b",
#  "agent-id" : null,
#  "engine" : "dummy",
#  "executor" : "random",
#  "name" : "dummy-random-check",
#  "check_type" : "service",
#  "check_id" : "59bec13b-8a3f-4d4e-a439-3c7fc3e85417",
#  "site_id" : "59bec13b-8a3f-4000-8000-000000000000",
#  "worker_pool" : null,
#  "parameters" : [ {
#    "type" : "bergamot.parameter",
#    "name" : "warning",
#    "description" : null,
#    "value" : "0.97"
#  }, {
#    "type" : "bergamot.parameter",
#    "name" : "critical",
#    "description" : null,
#    "value" : "0.99"
#  }, {
#    "type" : "bergamot.parameter",
#    "name" : "output",
#    "description" : null,
#    "value" : "Look at me, I'm random"
#  } ],
#  "adhoc_id" : null,
#  "processor_id" : "1aee6b8b-006c-4deb-9a75-219af87cf623",
#  "timeout" : 30000,
#  "scheduled" : 1591124122615,
#  "script" : null,
#  "saved_state" : null
#}


class ExecuteCheck:
    def __init__(self, message):
        self.message = message
        self.parameters = {}
        params = self.message.get('parameters')
        if params != None:
            for parameter in params:
                self.parameters[parameter['name']] = parameter.get('value')
    
    def received(self, timestamp):
        self.message['received'] = timestamp
        return self
    
    def id(self):
        return self.message.get('id')
    
    def agent_id(self):
        return self.message.get('agent_id')
    
    def engine(self):
        return self.message.get('engine')

    def executor(self):
        return self.message.get('executor')

    def name(self):
        return self.message.get('name')

    def check_type(self):
        return self.message.get('check_type')

    def check_id(self):
        return self.message.get('check_id')

    def site_id(self):
        return self.message.get('site_id')

    def timeout(self):
        return self.message.get('timeout')

    def scheduled(self):
        return self.message.get('scheduled')

    def script(self):
        return self.message.get('script')

    def saved_state(self):
        return self.message.get('saved_state')

    def parameters(self):
        return self.parameters

    def get_parameter(self, name, default=None):
        value = self.parameters.get(name)
        if value == None:
            return default
        return value
    
    def __str__(self):
        return "ExecutCheck::%s::%s::%s" %  (self.engine(), self.executor(), self.name())


class ResultStatus:    
    def __init__(self, value, ok):
        self.value = value
        self.ok = ok
    
    def is_ok(self):
        return self.ok
    
    def get_value(self):
        return self.value
    
    def __str__(self):
        return self.value


class Status:
    PENDING      = ResultStatus('PENDING', True)
    INFO         = ResultStatus('INFO', True)
    OK           = ResultStatus('OK', True)
    WARNING      = ResultStatus('WARNING', False) 
    CRITICAL     = ResultStatus('CRITICAL', False)
    UNKNOWN      = ResultStatus('UNKNOWN', False)
    TIMEOUT      = ResultStatus('TIMEOUT', False)
    ERROR        = ResultStatus('ERROR', False)
    DISCONNECTED = ResultStatus('DISCONNECTED', False)
    ACTION       = ResultStatus('ACTION', False)


class ActiveResult:
    def __init__(self):
        self.message = {'type': 'bergamot.processor.result.active'}

    def from_check(self, check):
        self.message['processor_id'] = check.message.get('processor_id')
        self.message['site_id'] = check.message.get('site_id')
        self.message['check_type'] = check.message.get('check_type')
        self.message['check_id'] = check.message.get('check_id')
        self.message['scheduled'] = check.message.get('scheduled')
        self.message['received'] = check.message.get('received')
        self.message['adhoc_id'] = check.message.get('adhoc_id')
        return self
    
    def sent(self, timestamp):
        self.message['sent'] = timestamp
        return self

    def set_state(self, status, output):
        self.message['status'] = status.get_value()
        self.message['ok'] = status.is_ok()
        self.message['output'] = output
        return self

    def ok(self, output):
        self.set_state(Status.OK, output)
        return self
    
    def warning(self, output):
        self.set_state(Status.WARNING, output)
        return self
        
    def critical(self, output):
        self.set_state(Status.CRITICAL, output)
        return self
        
    def error(self, output):
        self.set_state(Status.ERROR, output)
        return self

    def with_saved_state(self, saved_state):
        self.message['saved_state'] = saved_state
        return self

    def __str__(self):
        return "ActiveResult::%s::%s" % (self.message.get('status'), self.message.get('output'))
