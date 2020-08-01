from bergamot.agent.api import *
from bergamot.agent.message import *

import psutil

class BergamotAgentCheckDiskHandler(BaseBergamotAgentHandler):
    
    IGNORED_FS_TYPES = set(['proc','devtmpfs','securityfs','devpts','cgroup2','pstore','efivarfs','bpf','cgroup','mqueue','debugfs','hugetlbfs','tracefs','configfs','nsfs','fuse.gvfsd-fuse','fusectl','fuse','tracefs','binfmt_misc', 'sysfs', 'autofs'])
    
    IGNORED_PATH_PREFIXES = ['/run', '/proc', '/sys', '/dev']
    
    def get_message_types(self):
        return [ CheckDisk.TYPE_NAME ]
    
    def execute(self, message):
        parts = psutil.disk_partitions(all=True)
        stat = DiskStat()
        for part in parts:
            try:
                if BergamotAgentCheckDiskHandler.filter_disk(part):
                    usage = psutil.disk_usage(part.mountpoint)
                    info = DiskInfo() \
                        .with_mount(part.mountpoint) \
                        .with_device(part.device) \
                        .with_type(part.fstype) \
                        .with_size(usage.total) \
                        .with_available(usage.free) \
                        .with_used(usage.used) \
                        .with_used_percent(usage.percent / 100)
                    stat.with_disks(info)
            except:
                pass
        return stat
    
    def filter_disk(part):
        if part.fstype in BergamotAgentCheckDiskHandler.IGNORED_FS_TYPES:
            return False
        for prefix in BergamotAgentCheckDiskHandler.IGNORED_PATH_PREFIXES:
            if part.mountpoint.find(prefix) == 0 and part.mountpoint != prefix:
                return False
        return True


