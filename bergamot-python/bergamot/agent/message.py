
class ParameterMO:

    TYPE_NAME = 'bergamot.parameter'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return ParameterMO.TYPE_NAME

    def to_message(self):
        self.message['type'] = ParameterMO.TYPE_NAME
        return self.message

    # name String

    def name(self):
        return self.message.get('name')

    def with_name(self, val):
        self.message['name'] = val
        return self

    # description String

    def description(self):
        return self.message.get('description')

    def with_description(self, val):
        self.message['description'] = val
        return self

    # value String

    def value(self):
        return self.message.get('value')

    def with_value(self, val):
        self.message['value'] = val
        return self



class GeneralError:

    TYPE_NAME = 'bergamot.agent.error.general'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return GeneralError.TYPE_NAME

    def to_message(self):
        self.message['type'] = GeneralError.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # message String

    def message(self):
        return self.message.get('message')

    def with_message(self, val):
        self.message['message'] = val
        return self



class AgentPing:

    TYPE_NAME = 'bergamot.agent.ping'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return AgentPing.TYPE_NAME

    def to_message(self):
        self.message['type'] = AgentPing.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # timestamp long

    def timestamp(self):
        return self.message.get('timestamp')

    def with_timestamp(self, val):
        self.message['timestamp'] = val
        return self



class AgentPong:

    TYPE_NAME = 'bergamot.agent.pong'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return AgentPong.TYPE_NAME

    def to_message(self):
        self.message['type'] = AgentPong.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # timestamp long

    def timestamp(self):
        return self.message.get('timestamp')

    def with_timestamp(self, val):
        self.message['timestamp'] = val
        return self



class CheckCPU:

    TYPE_NAME = 'bergamot.agent.check.cpu'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return CheckCPU.TYPE_NAME

    def to_message(self):
        self.message['type'] = CheckCPU.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self



class CPUInfo:

    TYPE_NAME = 'bergamot.agent.model.cpu-info'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return CPUInfo.TYPE_NAME

    def to_message(self):
        self.message['type'] = CPUInfo.TYPE_NAME
        return self.message

    # vendor String

    def vendor(self):
        return self.message.get('vendor')

    def with_vendor(self, val):
        self.message['vendor'] = val
        return self

    # model String

    def model(self):
        return self.message.get('model')

    def with_model(self, val):
        self.message['model'] = val
        return self

    # speed int

    def speed(self):
        return self.message.get('speed')

    def with_speed(self, val):
        self.message['speed'] = val
        return self



class CPUTime:

    TYPE_NAME = 'bergamot.agent.model.cpu-time'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return CPUTime.TYPE_NAME

    def to_message(self):
        self.message['type'] = CPUTime.TYPE_NAME
        return self.message

    # total long

    def total(self):
        return self.message.get('total')

    def with_total(self, val):
        self.message['total'] = val
        return self

    # system long

    def system(self):
        return self.message.get('system')

    def with_system(self, val):
        self.message['system'] = val
        return self

    # user long

    def user(self):
        return self.message.get('user')

    def with_user(self, val):
        self.message['user'] = val
        return self

    # wait long

    def wait(self):
        return self.message.get('wait')

    def with_wait(self, val):
        self.message['wait'] = val
        return self



class CPUUsage:

    TYPE_NAME = 'bergamot.agent.model.cpu-usage'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return CPUUsage.TYPE_NAME

    def to_message(self):
        self.message['type'] = CPUUsage.TYPE_NAME
        return self.message

    # total double

    def total(self):
        return self.message.get('total')

    def with_total(self, val):
        self.message['total'] = val
        return self

    # system double

    def system(self):
        return self.message.get('system')

    def with_system(self, val):
        self.message['system'] = val
        return self

    # user double

    def user(self):
        return self.message.get('user')

    def with_user(self, val):
        self.message['user'] = val
        return self

    # wait double

    def wait(self):
        return self.message.get('wait')

    def with_wait(self, val):
        self.message['wait'] = val
        return self



class CPUStat:

    TYPE_NAME = 'bergamot.agent.stat.cpu'

    def __init__(self, message = None):
        if message:
            self.message = message
            v = self.message.get('total-usage')
            if v:
                self.message['total-usage'] = decode_agent_message(v)
            l = []
            for x in self.message.get('usage', []):
                l.append(decode_agent_message(x))
            self.message['usage'] = l
            l = []
            for x in self.message.get('info', []):
                l.append(decode_agent_message(x))
            self.message['info'] = l
            v = self.message.get('total-time')
            if v:
                self.message['total-time'] = decode_agent_message(v)
            l = []
            for x in self.message.get('time', []):
                l.append(decode_agent_message(x))
            self.message['time'] = l
        else:
            self.message = {}

    def get_type_name(self):
        return CPUStat.TYPE_NAME

    def to_message(self):
        self.message['type'] = CPUStat.TYPE_NAME
        v = self.message.get('total-usage')
        if v:
            self.message['total-usage'] = v.to_message()
        l = []
        for x in self.message.get('usage', []):
            l.append(x.to_message())
        self.message['usage'] = l
        l = []
        for x in self.message.get('info', []):
            l.append(x.to_message())
        self.message['info'] = l
        v = self.message.get('total-time')
        if v:
            self.message['total-time'] = v.to_message()
        l = []
        for x in self.message.get('time', []):
            l.append(x.to_message())
        self.message['time'] = l
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # cpu-count int

    def cpu_count(self):
        return self.message.get('cpu-count')

    def with_cpu_count(self, val):
        self.message['cpu-count'] = val
        return self

    # load List<Double>

    def load(self):
        return self.message.get('load', [])

    def with_load(self, value):
        if not self.message.get('load'):
            self.message['load'] = []
        self.message['load'].append(value)
        return self

    # total-usage CPUUsage

    def total_usage(self):
        return self.message.get('total-usage')

    def with_total_usage(self, val):
        self.message['total-usage'] = val
        return self

    # usage List<CPUUsage>

    def usage(self):
        return self.message.get('usage', [])

    def with_usage(self, value):
        if not self.message.get('usage'):
            self.message['usage'] = []
        self.message['usage'].append(value)
        return self

    # info List<CPUInfo>

    def info(self):
        return self.message.get('info', [])

    def with_info(self, value):
        if not self.message.get('info'):
            self.message['info'] = []
        self.message['info'].append(value)
        return self

    # total-time CPUTime

    def total_time(self):
        return self.message.get('total-time')

    def with_total_time(self, val):
        self.message['total-time'] = val
        return self

    # time List<CPUTime>

    def time(self):
        return self.message.get('time', [])

    def with_time(self, value):
        if not self.message.get('time'):
            self.message['time'] = []
        self.message['time'].append(value)
        return self



class CheckMem:

    TYPE_NAME = 'bergamot.agent.check.mem'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return CheckMem.TYPE_NAME

    def to_message(self):
        self.message['type'] = CheckMem.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self



class MemStat:

    TYPE_NAME = 'bergamot.agent.stat.mem'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return MemStat.TYPE_NAME

    def to_message(self):
        self.message['type'] = MemStat.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # ram long

    def ram(self):
        return self.message.get('ram')

    def with_ram(self, val):
        self.message['ram'] = val
        return self

    # total-memory long

    def total_memory(self):
        return self.message.get('total-memory')

    def with_total_memory(self, val):
        self.message['total-memory'] = val
        return self

    # used-memory long

    def used_memory(self):
        return self.message.get('used-memory')

    def with_used_memory(self, val):
        self.message['used-memory'] = val
        return self

    # free-memory long

    def free_memory(self):
        return self.message.get('free-memory')

    def with_free_memory(self, val):
        self.message['free-memory'] = val
        return self

    # actual-used-memory long

    def actual_used_memory(self):
        return self.message.get('actual-used-memory')

    def with_actual_used_memory(self, val):
        self.message['actual-used-memory'] = val
        return self

    # actual-free-memory long

    def actual_free_memory(self):
        return self.message.get('actual-free-memory')

    def with_actual_free_memory(self, val):
        self.message['actual-free-memory'] = val
        return self

    # used-memory-percentage double

    def used_memory_percentage(self):
        return self.message.get('used-memory-percentage')

    def with_used_memory_percentage(self, val):
        self.message['used-memory-percentage'] = val
        return self

    # free-memory-percentage double

    def free_memory_percentage(self):
        return self.message.get('free-memory-percentage')

    def with_free_memory_percentage(self, val):
        self.message['free-memory-percentage'] = val
        return self



class CheckDisk:

    TYPE_NAME = 'bergamot.agent.check.disk'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return CheckDisk.TYPE_NAME

    def to_message(self):
        self.message['type'] = CheckDisk.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self



class DiskInfo:

    TYPE_NAME = 'bergamot.agent.model.disk-info'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return DiskInfo.TYPE_NAME

    def to_message(self):
        self.message['type'] = DiskInfo.TYPE_NAME
        return self.message

    # mount String

    def mount(self):
        return self.message.get('mount')

    def with_mount(self, val):
        self.message['mount'] = val
        return self

    # device String

    def device(self):
        return self.message.get('device')

    def with_device(self, val):
        self.message['device'] = val
        return self

    # type String

    def type(self):
        return self.message.get('type')

    def with_type(self, val):
        self.message['type'] = val
        return self

    # size long

    def size(self):
        return self.message.get('size')

    def with_size(self, val):
        self.message['size'] = val
        return self

    # available long

    def available(self):
        return self.message.get('available')

    def with_available(self, val):
        self.message['available'] = val
        return self

    # used long

    def used(self):
        return self.message.get('used')

    def with_used(self, val):
        self.message['used'] = val
        return self

    # used-percent double

    def used_percent(self):
        return self.message.get('used-percent')

    def with_used_percent(self, val):
        self.message['used-percent'] = val
        return self



class DiskStat:

    TYPE_NAME = 'bergamot.agent.stat.disk'

    def __init__(self, message = None):
        if message:
            self.message = message
            l = []
            for x in self.message.get('disks', []):
                l.append(decode_agent_message(x))
            self.message['disks'] = l
        else:
            self.message = {}

    def get_type_name(self):
        return DiskStat.TYPE_NAME

    def to_message(self):
        self.message['type'] = DiskStat.TYPE_NAME
        l = []
        for x in self.message.get('disks', []):
            l.append(x.to_message())
        self.message['disks'] = l
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # disks List<DiskInfo>

    def disks(self):
        return self.message.get('disks', [])

    def with_disks(self, value):
        if not self.message.get('disks'):
            self.message['disks'] = []
        self.message['disks'].append(value)
        return self



class CheckOS:

    TYPE_NAME = 'bergamot.agent.check.os'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return CheckOS.TYPE_NAME

    def to_message(self):
        self.message['type'] = CheckOS.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self



class OSStat:

    TYPE_NAME = 'bergamot.agent.stat.os'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return OSStat.TYPE_NAME

    def to_message(self):
        self.message['type'] = OSStat.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # arch String

    def arch(self):
        return self.message.get('arch')

    def with_arch(self, val):
        self.message['arch'] = val
        return self

    # name String

    def name(self):
        return self.message.get('name')

    def with_name(self, val):
        self.message['name'] = val
        return self

    # description String

    def description(self):
        return self.message.get('description')

    def with_description(self, val):
        self.message['description'] = val
        return self

    # machine String

    def machine(self):
        return self.message.get('machine')

    def with_machine(self, val):
        self.message['machine'] = val
        return self

    # version String

    def version(self):
        return self.message.get('version')

    def with_version(self, val):
        self.message['version'] = val
        return self

    # patch-level String

    def patch_level(self):
        return self.message.get('patch-level')

    def with_patch_level(self, val):
        self.message['patch-level'] = val
        return self

    # vendor String

    def vendor(self):
        return self.message.get('vendor')

    def with_vendor(self, val):
        self.message['vendor'] = val
        return self

    # vendor-name String

    def vendor_name(self):
        return self.message.get('vendor-name')

    def with_vendor_name(self, val):
        self.message['vendor-name'] = val
        return self

    # vendor-code-name String

    def vendor_code_name(self):
        return self.message.get('vendor-code-name')

    def with_vendor_code_name(self, val):
        self.message['vendor-code-name'] = val
        return self

    # vendor-version String

    def vendor_version(self):
        return self.message.get('vendor-version')

    def with_vendor_version(self, val):
        self.message['vendor-version'] = val
        return self



class CheckUptime:

    TYPE_NAME = 'bergamot.agent.check.uptime'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return CheckUptime.TYPE_NAME

    def to_message(self):
        self.message['type'] = CheckUptime.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self



class UptimeStat:

    TYPE_NAME = 'bergamot.agent.stat.uptime'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return UptimeStat.TYPE_NAME

    def to_message(self):
        self.message['type'] = UptimeStat.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # uptime double

    def uptime(self):
        return self.message.get('uptime')

    def with_uptime(self, val):
        self.message['uptime'] = val
        return self



class CheckNetIf:

    TYPE_NAME = 'bergamot.agent.check.netif'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return CheckNetIf.TYPE_NAME

    def to_message(self):
        self.message['type'] = CheckNetIf.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self



class NetIfInfo:

    TYPE_NAME = 'bergamot.agent.model.netif-info'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return NetIfInfo.TYPE_NAME

    def to_message(self):
        self.message['type'] = NetIfInfo.TYPE_NAME
        return self.message

    # name String

    def name(self):
        return self.message.get('name')

    def with_name(self, val):
        self.message['name'] = val
        return self

    # hw-address String

    def hw_address(self):
        return self.message.get('hw-address')

    def with_hw_address(self, val):
        self.message['hw-address'] = val
        return self

    # address String

    def address(self):
        return self.message.get('address')

    def with_address(self, val):
        self.message['address'] = val
        return self

    # netmask String

    def netmask(self):
        return self.message.get('netmask')

    def with_netmask(self, val):
        self.message['netmask'] = val
        return self

    # tx-bytes long

    def tx_bytes(self):
        return self.message.get('tx-bytes')

    def with_tx_bytes(self, val):
        self.message['tx-bytes'] = val
        return self

    # rx-bytes long

    def rx_bytes(self):
        return self.message.get('rx-bytes')

    def with_rx_bytes(self, val):
        self.message['rx-bytes'] = val
        return self



class NetRouteInfo:

    TYPE_NAME = 'bergamot.agent.model.netroute-info'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return NetRouteInfo.TYPE_NAME

    def to_message(self):
        self.message['type'] = NetRouteInfo.TYPE_NAME
        return self.message

    # destination String

    def destination(self):
        return self.message.get('destination')

    def with_destination(self, val):
        self.message['destination'] = val
        return self

    # mask String

    def mask(self):
        return self.message.get('mask')

    def with_mask(self, val):
        self.message['mask'] = val
        return self

    # gateway String

    def gateway(self):
        return self.message.get('gateway')

    def with_gateway(self, val):
        self.message['gateway'] = val
        return self

    # metric long

    def metric(self):
        return self.message.get('metric')

    def with_metric(self, val):
        self.message['metric'] = val
        return self

    # mtu long

    def mtu(self):
        return self.message.get('mtu')

    def with_mtu(self, val):
        self.message['mtu'] = val
        return self

    # interface String

    def interface(self):
        return self.message.get('interface')

    def with_interface(self, val):
        self.message['interface'] = val
        return self



class NetIfStat:

    TYPE_NAME = 'bergamot.agent.stat.netif'

    def __init__(self, message = None):
        if message:
            self.message = message
            l = []
            for x in self.message.get('routes', []):
                l.append(decode_agent_message(x))
            self.message['routes'] = l
            l = []
            for x in self.message.get('interfaces', []):
                l.append(decode_agent_message(x))
            self.message['interfaces'] = l
        else:
            self.message = {}

    def get_type_name(self):
        return NetIfStat.TYPE_NAME

    def to_message(self):
        self.message['type'] = NetIfStat.TYPE_NAME
        l = []
        for x in self.message.get('routes', []):
            l.append(x.to_message())
        self.message['routes'] = l
        l = []
        for x in self.message.get('interfaces', []):
            l.append(x.to_message())
        self.message['interfaces'] = l
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # hostname String

    def hostname(self):
        return self.message.get('hostname')

    def with_hostname(self, val):
        self.message['hostname'] = val
        return self

    # routes List<NetRouteInfo>

    def routes(self):
        return self.message.get('routes', [])

    def with_routes(self, value):
        if not self.message.get('routes'):
            self.message['routes'] = []
        self.message['routes'].append(value)
        return self

    # interfaces List<NetIfInfo>

    def interfaces(self):
        return self.message.get('interfaces', [])

    def with_interfaces(self, value):
        if not self.message.get('interfaces'):
            self.message['interfaces'] = []
        self.message['interfaces'].append(value)
        return self



class ExecCheck:

    TYPE_NAME = 'bergamot.agent.check.exec'

    def __init__(self, message = None):
        if message:
            self.message = message
            l = []
            for x in self.message.get('parameters', []):
                l.append(decode_agent_message(x))
            self.message['parameters'] = l
        else:
            self.message = {}

    def get_type_name(self):
        return ExecCheck.TYPE_NAME

    def to_message(self):
        self.message['type'] = ExecCheck.TYPE_NAME
        l = []
        for x in self.message.get('parameters', []):
            l.append(x.to_message())
        self.message['parameters'] = l
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # engine String

    def engine(self):
        return self.message.get('engine')

    def with_engine(self, val):
        self.message['engine'] = val
        return self

    # executor String

    def executor(self):
        return self.message.get('executor')

    def with_executor(self, val):
        self.message['executor'] = val
        return self

    # name String

    def name(self):
        return self.message.get('name')

    def with_name(self, val):
        self.message['name'] = val
        return self

    # parameters List<ParameterMO>

    def parameters(self):
        return self.message.get('parameters', [])

    def with_parameters(self, value):
        if not self.message.get('parameters'):
            self.message['parameters'] = []
        self.message['parameters'].append(value)
        return self

    def parameters_value(self, name):
        vals = self.message.get('parameters', {})
        for val in vals:
            if val.name() == name:
                return val
        return None

    def with_parameters_value(self, name, value):
        if not self.message.get('parameters'):
            self.message['parameters'] = []
        self.message['parameters'].append(ParameterMO().with_name(name).with_value(value))
        return self



class ExecStat:

    TYPE_NAME = 'bergamot.agent.stat.exec'

    def __init__(self, message = None):
        if message:
            self.message = message
            l = []
            for x in self.message.get('readings', []):
                l.append(decode_agent_message(x))
            self.message['readings'] = l
            l = []
            for x in self.message.get('parameters', []):
                l.append(decode_agent_message(x))
            self.message['parameters'] = l
        else:
            self.message = {}

    def get_type_name(self):
        return ExecStat.TYPE_NAME

    def to_message(self):
        self.message['type'] = ExecStat.TYPE_NAME
        l = []
        for x in self.message.get('readings', []):
            l.append(x.to_message())
        self.message['readings'] = l
        l = []
        for x in self.message.get('parameters', []):
            l.append(x.to_message())
        self.message['parameters'] = l
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # ok boolean

    def ok(self):
        return self.message.get('ok')

    def with_ok(self, val):
        self.message['ok'] = val
        return self

    # status String

    def status(self):
        return self.message.get('status')

    def with_status(self, val):
        self.message['status'] = val
        return self

    # output String

    def output(self):
        return self.message.get('output')

    def with_output(self, val):
        self.message['output'] = val
        return self

    # runtime double

    def runtime(self):
        return self.message.get('runtime')

    def with_runtime(self, val):
        self.message['runtime'] = val
        return self

    # captured long

    def captured(self):
        return self.message.get('captured')

    def with_captured(self, val):
        self.message['captured'] = val
        return self

    # readings List<Reading>

    def readings(self):
        return self.message.get('readings', [])

    def with_readings(self, value):
        if not self.message.get('readings'):
            self.message['readings'] = []
        self.message['readings'].append(value)
        return self

    # parameters List<ParameterMO>

    def parameters(self):
        return self.message.get('parameters', [])

    def with_parameters(self, value):
        if not self.message.get('parameters'):
            self.message['parameters'] = []
        self.message['parameters'].append(value)
        return self

    def parameters_value(self, name):
        vals = self.message.get('parameters', {})
        for val in vals:
            if val.name() == name:
                return val
        return None

    def with_parameters_value(self, name, value):
        if not self.message.get('parameters'):
            self.message['parameters'] = []
        self.message['parameters'].append(ParameterMO().with_name(name).with_value(value))
        return self



class ProcessInfo:

    TYPE_NAME = 'bergamot.agent.model.process-info'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return ProcessInfo.TYPE_NAME

    def to_message(self):
        self.message['type'] = ProcessInfo.TYPE_NAME
        return self.message

    # pid long

    def pid(self):
        return self.message.get('pid')

    def with_pid(self, val):
        self.message['pid'] = val
        return self

    # parent_pid long

    def parent_pid(self):
        return self.message.get('parent_pid')

    def with_parent_pid(self, val):
        self.message['parent_pid'] = val
        return self

    # state String

    def state(self):
        return self.message.get('state')

    def with_state(self, val):
        self.message['state'] = val
        return self

    # title String

    def title(self):
        return self.message.get('title')

    def with_title(self, val):
        self.message['title'] = val
        return self

    # executable String

    def executable(self):
        return self.message.get('executable')

    def with_executable(self, val):
        self.message['executable'] = val
        return self

    # current_working_directory String

    def current_working_directory(self):
        return self.message.get('current_working_directory')

    def with_current_working_directory(self, val):
        self.message['current_working_directory'] = val
        return self

    # command_line List<String>

    def command_line(self):
        return self.message.get('command_line', [])

    def with_command_line(self, value):
        if not self.message.get('command_line'):
            self.message['command_line'] = []
        self.message['command_line'].append(value)
        return self

    # user String

    def user(self):
        return self.message.get('user')

    def with_user(self, val):
        self.message['user'] = val
        return self

    # group String

    def group(self):
        return self.message.get('group')

    def with_group(self, val):
        self.message['group'] = val
        return self

    # threads long

    def threads(self):
        return self.message.get('threads')

    def with_threads(self, val):
        self.message['threads'] = val
        return self

    # started_at long

    def started_at(self):
        return self.message.get('started_at')

    def with_started_at(self, val):
        self.message['started_at'] = val
        return self

    # size long

    def size(self):
        return self.message.get('size')

    def with_size(self, val):
        self.message['size'] = val
        return self

    # resident long

    def resident(self):
        return self.message.get('resident')

    def with_resident(self, val):
        self.message['resident'] = val
        return self

    # share long

    def share(self):
        return self.message.get('share')

    def with_share(self, val):
        self.message['share'] = val
        return self

    # total_time long

    def total_time(self):
        return self.message.get('total_time')

    def with_total_time(self, val):
        self.message['total_time'] = val
        return self

    # user_time long

    def user_time(self):
        return self.message.get('user_time')

    def with_user_time(self, val):
        self.message['user_time'] = val
        return self

    # sys_time long

    def sys_time(self):
        return self.message.get('sys_time')

    def with_sys_time(self, val):
        self.message['sys_time'] = val
        return self



class CheckProcess:

    TYPE_NAME = 'bergamot.agent.check.process'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return CheckProcess.TYPE_NAME

    def to_message(self):
        self.message['type'] = CheckProcess.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # list_processes boolean

    def list_processes(self):
        return self.message.get('list_processes')

    def with_list_processes(self, val):
        self.message['list_processes'] = val
        return self

    # command String

    def command(self):
        return self.message.get('command')

    def with_command(self, val):
        self.message['command'] = val
        return self

    # flatten_command boolean

    def flatten_command(self):
        return self.message.get('flatten_command')

    def with_flatten_command(self, val):
        self.message['flatten_command'] = val
        return self

    # arguments List<String>

    def arguments(self):
        return self.message.get('arguments', [])

    def with_arguments(self, value):
        if not self.message.get('arguments'):
            self.message['arguments'] = []
        self.message['arguments'].append(value)
        return self

    # regex boolean

    def regex(self):
        return self.message.get('regex')

    def with_regex(self, val):
        self.message['regex'] = val
        return self

    # state List<String>

    def state(self):
        return self.message.get('state', [])

    def with_state(self, value):
        if not self.message.get('state'):
            self.message['state'] = []
        self.message['state'].append(value)
        return self

    # user String

    def user(self):
        return self.message.get('user')

    def with_user(self, val):
        self.message['user'] = val
        return self

    # group String

    def group(self):
        return self.message.get('group')

    def with_group(self, val):
        self.message['group'] = val
        return self

    # title String

    def title(self):
        return self.message.get('title')

    def with_title(self, val):
        self.message['title'] = val
        return self



class ProcessStat:

    TYPE_NAME = 'bergamot.agent.stat.process'

    def __init__(self, message = None):
        if message:
            self.message = message
            l = []
            for x in self.message.get('processes', []):
                l.append(decode_agent_message(x))
            self.message['processes'] = l
        else:
            self.message = {}

    def get_type_name(self):
        return ProcessStat.TYPE_NAME

    def to_message(self):
        self.message['type'] = ProcessStat.TYPE_NAME
        l = []
        for x in self.message.get('processes', []):
            l.append(x.to_message())
        self.message['processes'] = l
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # total long

    def total(self):
        return self.message.get('total')

    def with_total(self, val):
        self.message['total'] = val
        return self

    # threads long

    def threads(self):
        return self.message.get('threads')

    def with_threads(self, val):
        self.message['threads'] = val
        return self

    # running long

    def running(self):
        return self.message.get('running')

    def with_running(self, val):
        self.message['running'] = val
        return self

    # sleeping long

    def sleeping(self):
        return self.message.get('sleeping')

    def with_sleeping(self, val):
        self.message['sleeping'] = val
        return self

    # idle long

    def idle(self):
        return self.message.get('idle')

    def with_idle(self, val):
        self.message['idle'] = val
        return self

    # stopped long

    def stopped(self):
        return self.message.get('stopped')

    def with_stopped(self, val):
        self.message['stopped'] = val
        return self

    # zombie long

    def zombie(self):
        return self.message.get('zombie')

    def with_zombie(self, val):
        self.message['zombie'] = val
        return self

    # processes List<ProcessInfo>

    def processes(self):
        return self.message.get('processes', [])

    def with_processes(self, value):
        if not self.message.get('processes'):
            self.message['processes'] = []
        self.message['processes'].append(value)
        return self



class WhoInfo:

    TYPE_NAME = 'bergamot.agent.model.who-info'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return WhoInfo.TYPE_NAME

    def to_message(self):
        self.message['type'] = WhoInfo.TYPE_NAME
        return self.message

    # user String

    def user(self):
        return self.message.get('user')

    def with_user(self, val):
        self.message['user'] = val
        return self

    # device String

    def device(self):
        return self.message.get('device')

    def with_device(self, val):
        self.message['device'] = val
        return self

    # host String

    def host(self):
        return self.message.get('host')

    def with_host(self, val):
        self.message['host'] = val
        return self

    # time long

    def time(self):
        return self.message.get('time')

    def with_time(self, val):
        self.message['time'] = val
        return self



class CheckWho:

    TYPE_NAME = 'bergamot.agent.check.who'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return CheckWho.TYPE_NAME

    def to_message(self):
        self.message['type'] = CheckWho.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self



class WhoStat:

    TYPE_NAME = 'bergamot.agent.stat.who'

    def __init__(self, message = None):
        if message:
            self.message = message
            l = []
            for x in self.message.get('users', []):
                l.append(decode_agent_message(x))
            self.message['users'] = l
        else:
            self.message = {}

    def get_type_name(self):
        return WhoStat.TYPE_NAME

    def to_message(self):
        self.message['type'] = WhoStat.TYPE_NAME
        l = []
        for x in self.message.get('users', []):
            l.append(x.to_message())
        self.message['users'] = l
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # users List<WhoInfo>

    def users(self):
        return self.message.get('users', [])

    def with_users(self, value):
        if not self.message.get('users'):
            self.message['users'] = []
        self.message['users'].append(value)
        return self



class NetConInfo:

    TYPE_NAME = 'bergamot.agent.model.netcon-info'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return NetConInfo.TYPE_NAME

    def to_message(self):
        self.message['type'] = NetConInfo.TYPE_NAME
        return self.message

    # protocol String

    def protocol(self):
        return self.message.get('protocol')

    def with_protocol(self, val):
        self.message['protocol'] = val
        return self

    # state String

    def state(self):
        return self.message.get('state')

    def with_state(self, val):
        self.message['state'] = val
        return self

    # local_address String

    def local_address(self):
        return self.message.get('local_address')

    def with_local_address(self, val):
        self.message['local_address'] = val
        return self

    # local_port long

    def local_port(self):
        return self.message.get('local_port')

    def with_local_port(self, val):
        self.message['local_port'] = val
        return self

    # remote_address String

    def remote_address(self):
        return self.message.get('remote_address')

    def with_remote_address(self, val):
        self.message['remote_address'] = val
        return self

    # remote_port long

    def remote_port(self):
        return self.message.get('remote_port')

    def with_remote_port(self, val):
        self.message['remote_port'] = val
        return self



class CheckNetCon:

    TYPE_NAME = 'bergamot.agent.check.netcon'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return CheckNetCon.TYPE_NAME

    def to_message(self):
        self.message['type'] = CheckNetCon.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # client boolean

    def client(self):
        return self.message.get('client')

    def with_client(self, val):
        self.message['client'] = val
        return self

    # server boolean

    def server(self):
        return self.message.get('server')

    def with_server(self, val):
        self.message['server'] = val
        return self

    # tcp boolean

    def tcp(self):
        return self.message.get('tcp')

    def with_tcp(self, val):
        self.message['tcp'] = val
        return self

    # udp boolean

    def udp(self):
        return self.message.get('udp')

    def with_udp(self, val):
        self.message['udp'] = val
        return self

    # unix boolean

    def unix(self):
        return self.message.get('unix')

    def with_unix(self, val):
        self.message['unix'] = val
        return self

    # raw boolean

    def raw(self):
        return self.message.get('raw')

    def with_raw(self, val):
        self.message['raw'] = val
        return self

    # local-port int

    def local_port(self):
        return self.message.get('local-port')

    def with_local_port(self, val):
        self.message['local-port'] = val
        return self

    # remote-port int

    def remote_port(self):
        return self.message.get('remote-port')

    def with_remote_port(self, val):
        self.message['remote-port'] = val
        return self

    # local-address String

    def local_address(self):
        return self.message.get('local-address')

    def with_local_address(self, val):
        self.message['local-address'] = val
        return self

    # remote-sddress String

    def remote_sddress(self):
        return self.message.get('remote-sddress')

    def with_remote_sddress(self, val):
        self.message['remote-sddress'] = val
        return self



class NetConStat:

    TYPE_NAME = 'bergamot.agent.stat.netcon'

    def __init__(self, message = None):
        if message:
            self.message = message
            l = []
            for x in self.message.get('connections', []):
                l.append(decode_agent_message(x))
            self.message['connections'] = l
        else:
            self.message = {}

    def get_type_name(self):
        return NetConStat.TYPE_NAME

    def to_message(self):
        self.message['type'] = NetConStat.TYPE_NAME
        l = []
        for x in self.message.get('connections', []):
            l.append(x.to_message())
        self.message['connections'] = l
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # connections List<NetConInfo>

    def connections(self):
        return self.message.get('connections', [])

    def with_connections(self, value):
        if not self.message.get('connections'):
            self.message['connections'] = []
        self.message['connections'].append(value)
        return self



class CheckAgent:

    TYPE_NAME = 'bergamot.agent.check.agent'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return CheckAgent.TYPE_NAME

    def to_message(self):
        self.message['type'] = CheckAgent.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self



class AgentStat:

    TYPE_NAME = 'bergamot.agent.stat.agent'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return AgentStat.TYPE_NAME

    def to_message(self):
        self.message['type'] = AgentStat.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # processors int

    def processors(self):
        return self.message.get('processors')

    def with_processors(self, val):
        self.message['processors'] = val
        return self

    # free_memory long

    def free_memory(self):
        return self.message.get('free_memory')

    def with_free_memory(self, val):
        self.message['free_memory'] = val
        return self

    # total_memory long

    def total_memory(self):
        return self.message.get('total_memory')

    def with_total_memory(self, val):
        self.message['total_memory'] = val
        return self

    # max_memory long

    def max_memory(self):
        return self.message.get('max_memory')

    def with_max_memory(self, val):
        self.message['max_memory'] = val
        return self

    # runtime String

    def runtime(self):
        return self.message.get('runtime')

    def with_runtime(self, val):
        self.message['runtime'] = val
        return self

    # runtime_vendor String

    def runtime_vendor(self):
        return self.message.get('runtime_vendor')

    def with_runtime_vendor(self, val):
        self.message['runtime_vendor'] = val
        return self

    # runtime_version String

    def runtime_version(self):
        return self.message.get('runtime_version')

    def with_runtime_version(self, val):
        self.message['runtime_version'] = val
        return self

    # agent_vendor String

    def agent_vendor(self):
        return self.message.get('agent_vendor')

    def with_agent_vendor(self, val):
        self.message['agent_vendor'] = val
        return self

    # agent_product String

    def agent_product(self):
        return self.message.get('agent_product')

    def with_agent_product(self, val):
        self.message['agent_product'] = val
        return self

    # agent_version String

    def agent_version(self):
        return self.message.get('agent_version')

    def with_agent_version(self, val):
        self.message['agent_version'] = val
        return self

    # user_name String

    def user_name(self):
        return self.message.get('user_name')

    def with_user_name(self, val):
        self.message['user_name'] = val
        return self

    # os_name String

    def os_name(self):
        return self.message.get('os_name')

    def with_os_name(self, val):
        self.message['os_name'] = val
        return self

    # os_version String

    def os_version(self):
        return self.message.get('os_version')

    def with_os_version(self, val):
        self.message['os_version'] = val
        return self

    # os_arch String

    def os_arch(self):
        return self.message.get('os_arch')

    def with_os_arch(self, val):
        self.message['os_arch'] = val
        return self



class CheckNetIO:

    TYPE_NAME = 'bergamot.agent.check.netio'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return CheckNetIO.TYPE_NAME

    def to_message(self):
        self.message['type'] = CheckNetIO.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # interfaces List<String>

    def interfaces(self):
        return self.message.get('interfaces', [])

    def with_interfaces(self, value):
        if not self.message.get('interfaces'):
            self.message['interfaces'] = []
        self.message['interfaces'].append(value)
        return self



class NetIOStat:

    TYPE_NAME = 'bergamot.agent.stat.netio'

    def __init__(self, message = None):
        if message:
            self.message = message
            l = []
            for x in self.message.get('interfaces', []):
                l.append(decode_agent_message(x))
            self.message['interfaces'] = l
        else:
            self.message = {}

    def get_type_name(self):
        return NetIOStat.TYPE_NAME

    def to_message(self):
        self.message['type'] = NetIOStat.TYPE_NAME
        l = []
        for x in self.message.get('interfaces', []):
            l.append(x.to_message())
        self.message['interfaces'] = l
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # interfaces List<NetIOInfo>

    def interfaces(self):
        return self.message.get('interfaces', [])

    def with_interfaces(self, value):
        if not self.message.get('interfaces'):
            self.message['interfaces'] = []
        self.message['interfaces'].append(value)
        return self



class NetIOInfo:

    TYPE_NAME = 'bergamot.agent.model.netio-info'

    def __init__(self, message = None):
        if message:
            self.message = message
            v = self.message.get('instant-rate')
            if v:
                self.message['instant-rate'] = decode_agent_message(v)
            v = self.message.get('five-minute-rate')
            if v:
                self.message['five-minute-rate'] = decode_agent_message(v)
        else:
            self.message = {}

    def get_type_name(self):
        return NetIOInfo.TYPE_NAME

    def to_message(self):
        self.message['type'] = NetIOInfo.TYPE_NAME
        v = self.message.get('instant-rate')
        if v:
            self.message['instant-rate'] = v.to_message()
        v = self.message.get('five-minute-rate')
        if v:
            self.message['five-minute-rate'] = v.to_message()
        return self.message

    # name String

    def name(self):
        return self.message.get('name')

    def with_name(self, val):
        self.message['name'] = val
        return self

    # instant-rate NetIORateInfo

    def instant_rate(self):
        return self.message.get('instant-rate')

    def with_instant_rate(self, val):
        self.message['instant-rate'] = val
        return self

    # five-minute-rate NetIORateInfo

    def five_minute_rate(self):
        return self.message.get('five-minute-rate')

    def with_five_minute_rate(self, val):
        self.message['five-minute-rate'] = val
        return self



class NetIORateInfo:

    TYPE_NAME = 'bergamot.agent.model.netio-rate-info'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return NetIORateInfo.TYPE_NAME

    def to_message(self):
        self.message['type'] = NetIORateInfo.TYPE_NAME
        return self.message

    # tx-rate double

    def tx_rate(self):
        return self.message.get('tx-rate')

    def with_tx_rate(self, val):
        self.message['tx-rate'] = val
        return self

    # rx-rate double

    def rx_rate(self):
        return self.message.get('rx-rate')

    def with_rx_rate(self, val):
        self.message['rx-rate'] = val
        return self

    # tx-peak-rate double

    def tx_peak_rate(self):
        return self.message.get('tx-peak-rate')

    def with_tx_peak_rate(self, val):
        self.message['tx-peak-rate'] = val
        return self

    # rx-peak-rate double

    def rx_peak_rate(self):
        return self.message.get('rx-peak-rate')

    def with_rx_peak_rate(self, val):
        self.message['rx-peak-rate'] = val
        return self



class CheckDiskIO:

    TYPE_NAME = 'bergamot.agent.check.diskio'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return CheckDiskIO.TYPE_NAME

    def to_message(self):
        self.message['type'] = CheckDiskIO.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # devices List<String>

    def devices(self):
        return self.message.get('devices', [])

    def with_devices(self, value):
        if not self.message.get('devices'):
            self.message['devices'] = []
        self.message['devices'].append(value)
        return self



class DiskIOStat:

    TYPE_NAME = 'bergamot.agent.stat.diskio'

    def __init__(self, message = None):
        if message:
            self.message = message
            l = []
            for x in self.message.get('disks', []):
                l.append(decode_agent_message(x))
            self.message['disks'] = l
        else:
            self.message = {}

    def get_type_name(self):
        return DiskIOStat.TYPE_NAME

    def to_message(self):
        self.message['type'] = DiskIOStat.TYPE_NAME
        l = []
        for x in self.message.get('disks', []):
            l.append(x.to_message())
        self.message['disks'] = l
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # disks List<DiskIOInfo>

    def disks(self):
        return self.message.get('disks', [])

    def with_disks(self, value):
        if not self.message.get('disks'):
            self.message['disks'] = []
        self.message['disks'].append(value)
        return self



class DiskIOInfo:

    TYPE_NAME = 'bergamot.agent.model.diskio-info'

    def __init__(self, message = None):
        if message:
            self.message = message
            v = self.message.get('instant-rate')
            if v:
                self.message['instant-rate'] = decode_agent_message(v)
            v = self.message.get('five-minute-rate')
            if v:
                self.message['five-minute-rate'] = decode_agent_message(v)
        else:
            self.message = {}

    def get_type_name(self):
        return DiskIOInfo.TYPE_NAME

    def to_message(self):
        self.message['type'] = DiskIOInfo.TYPE_NAME
        v = self.message.get('instant-rate')
        if v:
            self.message['instant-rate'] = v.to_message()
        v = self.message.get('five-minute-rate')
        if v:
            self.message['five-minute-rate'] = v.to_message()
        return self.message

    # name String

    def name(self):
        return self.message.get('name')

    def with_name(self, val):
        self.message['name'] = val
        return self

    # instant-rate DiskIORateInfo

    def instant_rate(self):
        return self.message.get('instant-rate')

    def with_instant_rate(self, val):
        self.message['instant-rate'] = val
        return self

    # five-minute-rate DiskIORateInfo

    def five_minute_rate(self):
        return self.message.get('five-minute-rate')

    def with_five_minute_rate(self, val):
        self.message['five-minute-rate'] = val
        return self



class DiskIORateInfo:

    TYPE_NAME = 'bergamot.agent.model.diskio-rate-info'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return DiskIORateInfo.TYPE_NAME

    def to_message(self):
        self.message['type'] = DiskIORateInfo.TYPE_NAME
        return self.message

    # read-rate double

    def read_rate(self):
        return self.message.get('read-rate')

    def with_read_rate(self, val):
        self.message['read-rate'] = val
        return self

    # write-rate double

    def write_rate(self):
        return self.message.get('write-rate')

    def with_write_rate(self, val):
        self.message['write-rate'] = val
        return self

    # reads double

    def reads(self):
        return self.message.get('reads')

    def with_reads(self, val):
        self.message['reads'] = val
        return self

    # writes double

    def writes(self):
        return self.message.get('writes')

    def with_writes(self, val):
        self.message['writes'] = val
        return self

    # read-peak-rate double

    def read_peak_rate(self):
        return self.message.get('read-peak-rate')

    def with_read_peak_rate(self, val):
        self.message['read-peak-rate'] = val
        return self

    # write-peak-rate double

    def write_peak_rate(self):
        return self.message.get('write-peak-rate')

    def with_write_peak_rate(self, val):
        self.message['write-peak-rate'] = val
        return self

    # peak-reads double

    def peak_reads(self):
        return self.message.get('peak-reads')

    def with_peak_reads(self, val):
        self.message['peak-reads'] = val
        return self

    # peak-writes double

    def peak_writes(self):
        return self.message.get('peak-writes')

    def with_peak_writes(self, val):
        self.message['peak-writes'] = val
        return self



class CheckMetrics:

    TYPE_NAME = 'bergamot.agent.check.metrics'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return CheckMetrics.TYPE_NAME

    def to_message(self):
        self.message['type'] = CheckMetrics.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # metric_name_filter String

    def metric_name_filter(self):
        return self.message.get('metric_name_filter')

    def with_metric_name_filter(self, val):
        self.message['metric_name_filter'] = val
        return self

    # strip_source_from_metric_name boolean

    def strip_source_from_metric_name(self):
        return self.message.get('strip_source_from_metric_name')

    def with_strip_source_from_metric_name(self, val):
        self.message['strip_source_from_metric_name'] = val
        return self



class MetricsStat:

    TYPE_NAME = 'bergamot.agent.stat.metrics'

    def __init__(self, message = None):
        if message:
            self.message = message
            l = []
            for x in self.message.get('readings', []):
                l.append(decode_agent_message(x))
            self.message['readings'] = l
        else:
            self.message = {}

    def get_type_name(self):
        return MetricsStat.TYPE_NAME

    def to_message(self):
        self.message['type'] = MetricsStat.TYPE_NAME
        l = []
        for x in self.message.get('readings', []):
            l.append(x.to_message())
        self.message['readings'] = l
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # readings List<Reading>

    def readings(self):
        return self.message.get('readings', [])

    def with_readings(self, value):
        if not self.message.get('readings'):
            self.message['readings'] = []
        self.message['readings'].append(value)
        return self



class ShellCheck:

    TYPE_NAME = 'bergamot.agent.check.shell'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return ShellCheck.TYPE_NAME

    def to_message(self):
        self.message['type'] = ShellCheck.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # command-line String

    def command_line(self):
        return self.message.get('command-line')

    def with_command_line(self, val):
        self.message['command-line'] = val
        return self

    # environment Map<>

    def environment(self):
        return self.message.get('environment', {})

    def environment_value(self, name):
        return self.message.get('environment', {}).get(name)

    def with_environment(self, name, value):
        if not self.message.get('environment'):
            self.message['environment'] = {}
        self.message['environment'][name] = value
        return self



class ShellStat:

    TYPE_NAME = 'bergamot.agent.stat.shell'

    def __init__(self, message = None):
        if message:
            self.message = message
        else:
            self.message = {}

    def get_type_name(self):
        return ShellStat.TYPE_NAME

    def to_message(self):
        self.message['type'] = ShellStat.TYPE_NAME
        return self.message

    # id UUID

    def id(self):
        return self.message.get('id')

    def with_id(self, val):
        self.message['id'] = val
        return self

    # reply_to UUID

    def reply_to(self):
        return self.message.get('reply_to')

    def with_reply_to(self, val):
        self.message['reply_to'] = val
        return self

    # exit int

    def exit(self):
        return self.message.get('exit')

    def with_exit(self, val):
        self.message['exit'] = val
        return self

    # output String

    def output(self):
        return self.message.get('output')

    def with_output(self, val):
        self.message['output'] = val
        return self

    # runtime double

    def runtime(self):
        return self.message.get('runtime')

    def with_runtime(self, val):
        self.message['runtime'] = val
        return self


def decode_agent_message(message):
    type_name = message.get('type')
    if type_name == 'bergamot.parameter':
        return ParameterMO(message)
    elif type_name == 'bergamot.agent.error.general':
        return GeneralError(message)
    elif type_name == 'bergamot.agent.ping':
        return AgentPing(message)
    elif type_name == 'bergamot.agent.pong':
        return AgentPong(message)
    elif type_name == 'bergamot.agent.check.cpu':
        return CheckCPU(message)
    elif type_name == 'bergamot.agent.model.cpu-info':
        return CPUInfo(message)
    elif type_name == 'bergamot.agent.model.cpu-time':
        return CPUTime(message)
    elif type_name == 'bergamot.agent.model.cpu-usage':
        return CPUUsage(message)
    elif type_name == 'bergamot.agent.stat.cpu':
        return CPUStat(message)
    elif type_name == 'bergamot.agent.check.mem':
        return CheckMem(message)
    elif type_name == 'bergamot.agent.stat.mem':
        return MemStat(message)
    elif type_name == 'bergamot.agent.check.disk':
        return CheckDisk(message)
    elif type_name == 'bergamot.agent.model.disk-info':
        return DiskInfo(message)
    elif type_name == 'bergamot.agent.stat.disk':
        return DiskStat(message)
    elif type_name == 'bergamot.agent.check.os':
        return CheckOS(message)
    elif type_name == 'bergamot.agent.stat.os':
        return OSStat(message)
    elif type_name == 'bergamot.agent.check.uptime':
        return CheckUptime(message)
    elif type_name == 'bergamot.agent.stat.uptime':
        return UptimeStat(message)
    elif type_name == 'bergamot.agent.check.netif':
        return CheckNetIf(message)
    elif type_name == 'bergamot.agent.model.netif-info':
        return NetIfInfo(message)
    elif type_name == 'bergamot.agent.model.netroute-info':
        return NetRouteInfo(message)
    elif type_name == 'bergamot.agent.stat.netif':
        return NetIfStat(message)
    elif type_name == 'bergamot.agent.check.exec':
        return ExecCheck(message)
    elif type_name == 'bergamot.agent.stat.exec':
        return ExecStat(message)
    elif type_name == 'bergamot.agent.model.process-info':
        return ProcessInfo(message)
    elif type_name == 'bergamot.agent.check.process':
        return CheckProcess(message)
    elif type_name == 'bergamot.agent.stat.process':
        return ProcessStat(message)
    elif type_name == 'bergamot.agent.model.who-info':
        return WhoInfo(message)
    elif type_name == 'bergamot.agent.check.who':
        return CheckWho(message)
    elif type_name == 'bergamot.agent.stat.who':
        return WhoStat(message)
    elif type_name == 'bergamot.agent.model.netcon-info':
        return NetConInfo(message)
    elif type_name == 'bergamot.agent.check.netcon':
        return CheckNetCon(message)
    elif type_name == 'bergamot.agent.stat.netcon':
        return NetConStat(message)
    elif type_name == 'bergamot.agent.check.agent':
        return CheckAgent(message)
    elif type_name == 'bergamot.agent.stat.agent':
        return AgentStat(message)
    elif type_name == 'bergamot.agent.check.netio':
        return CheckNetIO(message)
    elif type_name == 'bergamot.agent.stat.netio':
        return NetIOStat(message)
    elif type_name == 'bergamot.agent.model.netio-info':
        return NetIOInfo(message)
    elif type_name == 'bergamot.agent.model.netio-rate-info':
        return NetIORateInfo(message)
    elif type_name == 'bergamot.agent.check.diskio':
        return CheckDiskIO(message)
    elif type_name == 'bergamot.agent.stat.diskio':
        return DiskIOStat(message)
    elif type_name == 'bergamot.agent.model.diskio-info':
        return DiskIOInfo(message)
    elif type_name == 'bergamot.agent.model.diskio-rate-info':
        return DiskIORateInfo(message)
    elif type_name == 'bergamot.agent.check.metrics':
        return CheckMetrics(message)
    elif type_name == 'bergamot.agent.stat.metrics':
        return MetricsStat(message)
    elif type_name == 'bergamot.agent.check.shell':
        return ShellCheck(message)
    elif type_name == 'bergamot.agent.stat.shell':
        return ShellStat(message)
    return None

