package com.intrbiz.bergamot.agent.handler;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hyperic.sigar.Humidor;
import org.hyperic.sigar.ProcCredName;
import org.hyperic.sigar.ProcExe;
import org.hyperic.sigar.ProcMem;
import org.hyperic.sigar.ProcStat;
import org.hyperic.sigar.ProcState;
import org.hyperic.sigar.ProcTime;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarPermissionDeniedException;
import org.hyperic.sigar.SigarProxy;

import com.intrbiz.bergamot.agent.AgentHandler;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.check.CheckProcess;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.stat.ProcessStat;
import com.intrbiz.bergamot.model.message.agent.stat.process.ProcessInfo;
import com.intrbiz.bergamot.util.AgentUtil;

public class ProcessInfoHandler implements AgentHandler
{
    private Logger logger = Logger.getLogger(ProcessInfoHandler.class);
    
    private SigarProxy sigar = Humidor.getInstance().getSigar();

    public ProcessInfoHandler()
    {
        super();
    }
    
    @Override
    public Class<?>[] getMessages()
    {
        return new Class[] {
                CheckProcess.class
        };
    }

    @Override
    public AgentMessage handle(AgentMessage request)
    {
        CheckProcess checkProc = (CheckProcess) request;
        try
        {
            ProcessStat stat = new ProcessStat(request);
            // stats
            ProcStat ps = this.sigar.getProcStat();
            stat.setTotal(ps.getTotal());
            stat.setSleeping(ps.getSleeping());
            stat.setIdle(ps.getIdle());
            stat.setRunning(ps.getRunning());
            stat.setStopped(ps.getStopped());
            stat.setThreads(ps.getThreads());
            stat.setZombie(ps.getZombie());
            // enumerate the processes
            if (checkProc.isListProcesses())
            {
                
                long[] pids = this.sigar.getProcList();
                for (long pid : pids)
                {
                    try
                    {
                        // get basic information about the process
                        List<String> commandLine = Arrays.asList(this.sigar.getProcArgs(pid));
                        ProcCredName creds       = this.sigar.getProcCredName(pid);
                        ProcState    state       = this.sigar.getProcState(pid);
                        // filtered?
                        if (matchesFilter(checkProc, state, creds, commandLine))
                        {
                            // detailed info
                            ProcExe  exe  = this.sigar.getProcExe(pid);
                            ProcMem  mem  = this.sigar.getProcMem(pid);
                            ProcTime time = this.sigar.getProcTime(pid);
                            // build the info
                            ProcessInfo info = new ProcessInfo();
                            info.setPid(pid);
                            info.setTitle(state.getName());
                            info.setState(new String(new char[] { state.getState() }));
                            info.setParentPid(state.getPpid());
                            info.setThreads(state.getThreads());
                            info.setCommandLine(commandLine);
                            info.setUser(creds.getUser());
                            info.setGroup(creds.getGroup());
                            info.setExecutable(exe.getName());
                            info.setCurrentWorkingDirectory(exe.getCwd());
                            info.setSize(mem.getSize());
                            info.setResident(mem.getResident());
                            info.setShare(mem.getShare());
                            info.setStartedAt(time.getStartTime());
                            info.setTotalTime(time.getTotal());
                            info.setUserTime(time.getUser());
                            info.setSysTime(time.getSys());
                            // add
                            stat.getProcesses().add(info);
                        }
                    }
                    catch (SigarPermissionDeniedException e)
                    {
                    }
                    catch (SigarException e)
                    {
                        logger.warn("Cannot get process information", e);
                    }
                }
            }
            return stat;
        }
        catch (SigarException e)
        {
            return new GeneralError(e.getMessage());
        }
    }
    
    private static boolean matchesFilter(CheckProcess check, ProcState state, ProcCredName creds, List<String> commandLine)
    {
        // user
        if (! AgentUtil.isEmpty(check.getUser()))
        {
            if (creds.getUser() != null && (! creds.getUser().equalsIgnoreCase(check.getUser())))
                return false;
        }
        // group
        if (! AgentUtil.isEmpty(check.getGroup()))
        {
            if (creds.getGroup() != null && (! creds.getGroup().equalsIgnoreCase(check.getGroup())))
                return false;
        }
        // state
        if (check.getState() != null && check.getState().size() > 0)
        {
            if (! isStateInList(check.getState(), new String(new char[] { state.getState() })))
                return false;
        }
        // process title
        if (! AgentUtil.isEmpty(check.getTitle()))
        {
            String title = state.getName();
            // regex?
            if (check.isRegex())
            {
                Pattern pattern = Pattern.compile(check.getTitle());
                Matcher matcher = pattern.matcher(title);
                if (! matcher.find())
                    return false;
            }
            else
            {
                if (! title.contains(check.getTitle()))
                    return false;
            }
        }
        // command name
        if (! AgentUtil.isEmpty(check.getCommand()))
        {
            if (commandLine.size() == 0)
                return false;
            // flatten command line
            String command = check.isFlattenCommand() ? flattenCommandLine(commandLine) : commandLine.get(0);
            // regex?
            if (check.isRegex())
            {
                Pattern pattern = Pattern.compile(check.getCommand());
                Matcher matcher = pattern.matcher(command);
                if (! matcher.find())
                    return false;
            }
            else
            {
                if (! command.contains(check.getCommand()))
                    return false;
            }
        }
        // arguments
        if (check.getArguments() != null && check.getArguments().size() > 0)
        {
            // match each argument against the command line arguments
            int matched = 0;
            for (String argument : check.getArguments())
            {
                for (int i = 0; i < commandLine.size(); i++)
                {
                    String processArgument = commandLine.get(i);
                    if (check.isRegex())
                    {
                        Pattern pattern = Pattern.compile(argument);
                        Matcher matcher = pattern.matcher(processArgument);
                        if (matcher.find())
                        {
                            matched++;
                            break;
                        }
                    }
                    else
                    {
                        if (processArgument.contains(argument))
                        {
                            matched ++;
                            break;
                        }
                    }
                }
            }
            // did we match each required argument
            if (matched != check.getArguments().size())
                return false;
        }
        return true;
    }
    
    private static String flattenCommandLine(List<String> commandLine)
    {
        StringBuilder sb = new StringBuilder();
        boolean ns = false;
        for (String part : commandLine)
        {
            if (ns) sb.append(" ");
            sb.append(part);
            ns = true;
        }
        return sb.toString().trim();
    }
    
    private static boolean isStateInList(List<String> states, String state)
    {
        for (String x : states)
        {
            if (state.equals(x))
                return true;
        }
        return false;
    }
}
