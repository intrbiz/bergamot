package com.intrbiz.bergamot.io;

import com.intrbiz.bergamot.model.message.agent.check.CheckAgent;
import com.intrbiz.bergamot.model.message.agent.check.CheckCPU;
import com.intrbiz.bergamot.model.message.agent.check.CheckDisk;
import com.intrbiz.bergamot.model.message.agent.check.CheckDiskIO;
import com.intrbiz.bergamot.model.message.agent.check.CheckMem;
import com.intrbiz.bergamot.model.message.agent.check.CheckMetrics;
import com.intrbiz.bergamot.model.message.agent.check.CheckNetCon;
import com.intrbiz.bergamot.model.message.agent.check.CheckNetIO;
import com.intrbiz.bergamot.model.message.agent.check.CheckNetIf;
import com.intrbiz.bergamot.model.message.agent.check.CheckOS;
import com.intrbiz.bergamot.model.message.agent.check.CheckProcess;
import com.intrbiz.bergamot.model.message.agent.check.CheckUptime;
import com.intrbiz.bergamot.model.message.agent.check.CheckWho;
import com.intrbiz.bergamot.model.message.agent.check.ExecCheck;
import com.intrbiz.bergamot.model.message.agent.check.ShellCheck;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPing;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPong;
import com.intrbiz.bergamot.model.message.agent.stat.AgentStat;
import com.intrbiz.bergamot.model.message.agent.stat.CPUStat;
import com.intrbiz.bergamot.model.message.agent.stat.DiskIOStat;
import com.intrbiz.bergamot.model.message.agent.stat.DiskStat;
import com.intrbiz.bergamot.model.message.agent.stat.ExecStat;
import com.intrbiz.bergamot.model.message.agent.stat.MemStat;
import com.intrbiz.bergamot.model.message.agent.stat.MetricsStat;
import com.intrbiz.bergamot.model.message.agent.stat.NetConStat;
import com.intrbiz.bergamot.model.message.agent.stat.NetIOStat;
import com.intrbiz.bergamot.model.message.agent.stat.NetIfStat;
import com.intrbiz.bergamot.model.message.agent.stat.OSStat;
import com.intrbiz.bergamot.model.message.agent.stat.ProcessStat;
import com.intrbiz.bergamot.model.message.agent.stat.ShellStat;
import com.intrbiz.bergamot.model.message.agent.stat.UptimeStat;
import com.intrbiz.bergamot.model.message.agent.stat.WhoStat;
import com.intrbiz.bergamot.model.message.agent.stat.cpu.CPUInfo;
import com.intrbiz.bergamot.model.message.agent.stat.cpu.CPUTime;
import com.intrbiz.bergamot.model.message.agent.stat.cpu.CPUUsage;
import com.intrbiz.bergamot.model.message.agent.stat.disk.DiskInfo;
import com.intrbiz.bergamot.model.message.agent.stat.diskio.DiskIOInfo;
import com.intrbiz.bergamot.model.message.agent.stat.diskio.DiskIORateInfo;
import com.intrbiz.bergamot.model.message.agent.stat.netcon.NetConInfo;
import com.intrbiz.bergamot.model.message.agent.stat.netif.NetIfInfo;
import com.intrbiz.bergamot.model.message.agent.stat.netif.NetRouteInfo;
import com.intrbiz.bergamot.model.message.agent.stat.netio.NetIOInfo;
import com.intrbiz.bergamot.model.message.agent.stat.netio.NetIORateInfo;
import com.intrbiz.bergamot.model.message.agent.stat.process.ProcessInfo;
import com.intrbiz.bergamot.model.message.agent.stat.who.WhoInfo;
import com.intrbiz.gerald.polyakov.io.PolyakovTranscoder;

/**
 * Encode and decode messages
 */
public class BergamotAgentTranscoder extends BergamotTranscoder
{   
    public static final Class<?>[] CLASSES = {
        // error
        GeneralError.class,
        // ping pong
        AgentPing.class,
        AgentPong.class,
        // cpu
        CheckCPU.class,
        CPUInfo.class,
        CPUTime.class,
        CPUUsage.class,
        CPUStat.class,
        // mem
        CheckMem.class,
        MemStat.class,
        // disk
        CheckDisk.class,
        DiskInfo.class,
        DiskStat.class,
        // os
        CheckOS.class,
        OSStat.class,
        // uptime
        CheckUptime.class,
        UptimeStat.class,
        // netif
        CheckNetIf.class,
        NetIfInfo.class,
        NetRouteInfo.class,
        NetIfStat.class,
        // exec
        ExecCheck.class,
        ExecStat.class,
        // process
        ProcessInfo.class,
        CheckProcess.class,
        ProcessStat.class,
        // who
        WhoInfo.class,
        CheckWho.class,
        WhoStat.class,
        // net con
        NetConInfo.class,
        CheckNetCon.class,
        NetConStat.class,
        // agent
        CheckAgent.class,
        AgentStat.class,
        // net io
        CheckNetIO.class,
        NetIOStat.class,
        NetIOInfo.class,
        NetIORateInfo.class,
        // disk io
        CheckDiskIO.class,
        DiskIOStat.class,
        DiskIOInfo.class,
        DiskIORateInfo.class,
        // metrics
        CheckMetrics.class,
        MetricsStat.class,
        // shell
        ShellCheck.class,
        ShellStat.class
    };
    
    private static final BergamotAgentTranscoder DEFAULT = new BergamotAgentTranscoder();
    
    public static BergamotAgentTranscoder getDefault()
    {
        return DEFAULT;
    }
    
    public BergamotAgentTranscoder()
    {
        super();
        this.addEventType(BergamotAgentTranscoder.CLASSES);
        // include the metric reading models from Polyakov
        this.addEventType(PolyakovTranscoder.CLASSES);
    }
}
