from bergamot.agent.api import *
from bergamot.agent.message import *

import psutil
import re

class BergamotAgentCheckProcessHandler(BaseBergamotAgentHandler):
    def get_message_types(self):
        return [ CheckProcess.TYPE_NAME ]
    
    def execute(self, message):
        total = 0;
        threads = 0;
        running = 0;
        sleeping = 0;
        idle = 0;
        stopped = 0;
        zombie = 0;
        processes = [];
        for proc in psutil.process_iter(['pid', 'ppid', 'name', 'exe', 'cmdline', 'cwd', 'status', 'username', 'num_threads', 'cpu_times', 'memory_info', 'create_time']):
            if BergamotAgentCheckProcessHandler.matchesFilter(message, proc):
                total += 1
                threads += proc.info.get('num_threads', 0)
                processes.append(proc)
                status = proc.info.get('status')
                if status == psutil.STATUS_RUNNING:
                    running += 1
                elif status == psutil.STATUS_SLEEPING:
                    sleeping += 1
                elif status == psutil.STATUS_IDLE:
                    idle += 1
                elif status == psutil.STATUS_STOPPED:
                    stopped += 1
                elif psutil.STATUS_ZOMBIE:
                    zombie += 1

        stat = ProcessStat() \
            .with_total(total) \
            .with_threads(threads) \
            .with_running(running) \
            .with_sleeping(sleeping) \
            .with_idle(idle) \
            .with_stopped(stopped) \
            .with_zombie(zombie)
        
        if message.list_processes():
            for proc in processes:
                info = ProcessInfo() \
                    .with_pid(proc.pid) \
                    .with_parent_pid(proc.info.get('ppid')) \
                    .with_state(proc.info.get('status')) \
                    .with_title(proc.info.get('name')) \
                    .with_executable(proc.info.get('exe')) \
                    .with_current_working_directory(proc.info.get('cwd')) \
                    .with_user(proc.info.get('username')) \
                    .with_group('') \
                    .with_threads(proc.info.get('num_threads', 0)) \
                    .with_started_at(int(proc.info.get('create_time', 0) * 1000)) \
                    .with_size(proc.info['memory_info'].vms) \
                    .with_resident(proc.info['memory_info'].rss) \
                    .with_share(proc.info['memory_info'].shared) \
                    .with_total_time(proc.info['cpu_times'].user + proc.info['cpu_times'].system + proc.info['cpu_times'].iowait) \
                    .with_user_time(proc.info['cpu_times'].user) \
                    .with_sys_time(proc.info['cpu_times'].system)
                for part in proc.info.get('cmdline', []):
                    info.with_command_line(part)
                stat.with_processes(info);
        
        return stat

    def matchesFilter(message, proc):
        # User
        if message.user() and proc.info.get('username') != message.user():
            return False;
        
        # State
        if message.state():
            matched = 0
            for state in message.state():
                if state == proc.info.get('status'):
                    matched += 1
            if matched == 0:
                return False
            
        # Title
        if message.title():
            if message.regex():
                pattern = re.compile(message.title())
                if not pattern.match(proc.info.get('name')):
                    return False
            else:
                if proc.info.get('name') != message.title():
                    return False
                
        # Command name
        if message.command():
            cmd = proc.info.get('exe')
            if message.flatten_command():
                cmd = ' '.join(proc.info.get('cmdline', []))
            if cmd:
                if message.regex():
                    pattern = re.compile(message.command())
                    if not pattern.match(cmd):
                        return False
                else:
                    if cmd.find(message.command()) == -1:
                        return False
            else:
                return False
        
        # Arguments
        if message.arguments():
            matched = 0
            for arg in message.arguments():
                if message.regex():
                    pattern = re.compile(message.command())
                    for part in proc.info.get('cmdline', []):
                        if pattern.match(part):
                            matched += 1
                else:
                    for part in proc.info.get('cmdline', []):
                        if part.find(arg) != -1:
                            matched += 1
            if matched == 0:
                return False
        
        return True;
        
