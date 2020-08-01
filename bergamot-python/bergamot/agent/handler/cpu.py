from bergamot.agent.api import *
from bergamot.agent.message import *

import psutil
import platform


class BergamotAgentCheckCPUHandler(BaseBergamotAgentHandler):
    def get_message_types(self):
        return [ CheckCPU.TYPE_NAME ]
    
    def execute(self, message):
        stat = CPUStat()
        # load average
        load = psutil.getloadavg();
        stat.with_load(load[0]).with_load(load[1]).with_load(load[2])
        # cpu count
        count = psutil.cpu_count()
        stat.with_cpu_count(count)
        # cpu times
        for t in psutil.cpu_times(percpu=True):
            stat.with_time(CPUTime().with_total(sum(x for x in t) - t.idle).with_system(t.system).with_user(t.user).with_wait(t.iowait))
        tt = psutil.cpu_times()
        stat.with_total_time(CPUTime().with_total(sum(x for x in tt) - tt.idle).with_system(tt.system).with_user(tt.user).with_wait(tt.iowait))
        # cpu usage
        for u in psutil.cpu_times_percent(percpu=True):
            stat.with_usage(CPUUsage().with_total((sum(x for x in u) - u.idle) / 100).with_system(u.system / 100).with_user(u.user / 100).with_wait(u.iowait / 100))
        tu = psutil.cpu_times_percent()
        stat.with_total_usage(CPUUsage().with_total((sum(x for x in tu) - tu.idle) / 100).with_system(tu.system / 100).with_user(tu.user / 100).with_wait(tu.iowait / 100))
        # info
        proc = platform.processor()
        for s in  psutil.cpu_freq(percpu=True):
            stat.with_info(CPUInfo().with_speed(int(s.current)).with_vendor('').with_model(proc))
        return stat

