package com.intrbiz.bergamot.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intrbiz.bergamot.model.message.agent.check.CheckAgent;
import com.intrbiz.bergamot.model.message.agent.check.CheckCPU;
import com.intrbiz.bergamot.model.message.agent.check.CheckDisk;
import com.intrbiz.bergamot.model.message.agent.check.CheckDiskIO;
import com.intrbiz.bergamot.model.message.agent.check.CheckMem;
import com.intrbiz.bergamot.model.message.agent.check.CheckNetCon;
import com.intrbiz.bergamot.model.message.agent.check.CheckNetIO;
import com.intrbiz.bergamot.model.message.agent.check.CheckNetIf;
import com.intrbiz.bergamot.model.message.agent.check.CheckOS;
import com.intrbiz.bergamot.model.message.agent.check.CheckProcess;
import com.intrbiz.bergamot.model.message.agent.check.CheckUptime;
import com.intrbiz.bergamot.model.message.agent.check.CheckWho;
import com.intrbiz.bergamot.model.message.agent.check.ExecCheck;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.hello.AgentHello;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPing;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPong;
import com.intrbiz.bergamot.model.message.agent.stat.AgentStat;
import com.intrbiz.bergamot.model.message.agent.stat.CPUStat;
import com.intrbiz.bergamot.model.message.agent.stat.DiskIOStat;
import com.intrbiz.bergamot.model.message.agent.stat.DiskStat;
import com.intrbiz.bergamot.model.message.agent.stat.ExecStat;
import com.intrbiz.bergamot.model.message.agent.stat.MemStat;
import com.intrbiz.bergamot.model.message.agent.stat.NetConStat;
import com.intrbiz.bergamot.model.message.agent.stat.NetIOStat;
import com.intrbiz.bergamot.model.message.agent.stat.NetIfStat;
import com.intrbiz.bergamot.model.message.agent.stat.OSStat;
import com.intrbiz.bergamot.model.message.agent.stat.ProcessStat;
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
import com.intrbiz.bergamot.model.message.agent.util.Parameter;

/**
 * Encode and decode messages
 */
public class BergamotAgentTranscoder
{   
    public static final Class<?>[] CLASSES = {
        // error
        GeneralError.class,
        // hello
        AgentHello.class,
        // ping
        AgentPing.class,
        AgentPong.class,
        // util
        Parameter.class,
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
        DiskIORateInfo.class
    };
    
    private static final BergamotAgentTranscoder US = new BergamotAgentTranscoder();
    
    public static BergamotAgentTranscoder getDefaultInstance()
    {
        return US;
    }
    
    private final ObjectMapper factory = new ObjectMapper();
    
    private final boolean sealed;
    
    public BergamotAgentTranscoder(boolean sealed)
    {
        super();
        this.factory.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        this.factory.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        this.factory.configure(SerializationFeature.INDENT_OUTPUT, true);
        this.sealed = sealed;
        this.factory.registerSubtypes(BergamotAgentTranscoder.CLASSES);
    }
    
    public BergamotAgentTranscoder()
    {
        this(false);
    }
    
    public boolean isSealed()
    {
        return this.sealed;
    }
    
    public void addEventType(Class<?>... classes)
    {
        if (! this.sealed)
        {
            this.factory.registerSubtypes(classes);
        }
    }
    
    public void encode(Object event, OutputStream to)
    {
        try
        {
            JsonGenerator g = this.factory.getFactory().createGenerator(to);
            try
            {
                this.factory.writeValue(g, event);
            }
            finally
            {
                g.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to encode event", e);
        }
    }
    
    public void encode(Object event, Writer to)
    {
        try
        {
            JsonGenerator g = this.factory.getFactory().createGenerator(to);
            try
            {
                this.factory.writeValue(g, event);
            }
            finally
            {
                g.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to encode event", e);
        }
    }
    
    public byte[] encodeAsBytes(Object event)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.encode(event, baos);
        return baos.toByteArray();
    }
    
    public String encodeAsString(Object event)
    {
        StringWriter sw = new StringWriter();
        this.encode(event, sw);
        return sw.toString();
    }
    
    public void encode(Object event, File file)
    {
        file.getParentFile().mkdirs();
        try
        {
            FileWriter fw = new FileWriter(file);
            try
            {
                this.encode(event, fw);
            }
            finally
            {
                fw.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to write file", e);
        }
    }
    
    public <T> T decode(InputStream from, JavaType type)
    {
        try
        {
            JsonParser p = this.factory.getFactory().createParser(from);
            try
            {
                return this.factory.readValue(p, type);
            }
            finally
            {
                p.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to decode event", e);
        }
    }
    
    public <T> T decode(InputStream from, Class<T> type)
    {
        try
        {
            JsonParser p = this.factory.getFactory().createParser(from);
            try
            {
                return this.factory.readValue(p, type);
            }
            finally
            {
                p.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to decode event", e);
        }
    }
    
    public <T> List<T> decodeList(InputStream from, Class<T> elementType)
    {
        try
        {
            JsonParser p = this.factory.getFactory().createParser(from);
            try
            {
                return this.factory.readValue(p, this.factory.getTypeFactory().constructCollectionType(List.class, elementType));
            }
            finally
            {
                p.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to decode event", e);
        }
    }
    
    public <T> Set<T> decodeSet(InputStream from, Class<T> elementType)
    {
        try
        {
            JsonParser p = this.factory.getFactory().createParser(from);
            try
            {
                return this.factory.readValue(p, this.factory.getTypeFactory().constructCollectionType(Set.class, elementType));
            }
            finally
            {
                p.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to decode event", e);
        }
        
    }
    
    public <T> T decode(Reader from, Class<T> type)
    {
        try
        {
            JsonParser p = this.factory.getFactory().createParser(from);
            try
            {
                return (T) this.factory.readValue(p, type);
            }
            finally
            {
                p.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to decode event", e);
        }
    }
    
    public <T> T decode(Reader from, JavaType type)
    {
        try
        {
            JsonParser p = this.factory.getFactory().createParser(from);
            try
            {
                return this.factory.readValue(p, type);
            }
            finally
            {
                p.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to decode event", e);
        }
    }
    
    public <T> List<T> decodeList(Reader from, Class<T> elementType)
    {
        try
        {
            JsonParser p = this.factory.getFactory().createParser(from);
            try
            {
                return this.factory.readValue(p, this.factory.getTypeFactory().constructCollectionType(List.class, elementType));
            }
            finally
            {
                p.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to decode event", e);
        }
    }
    
    public <T> Set<T> decodeSet(Reader from, Class<T> elementType)
    {
        try
        {
            JsonParser p = this.factory.getFactory().createParser(from);
            try
            {
                return this.factory.readValue(p, this.factory.getTypeFactory().constructCollectionType(Set.class, elementType));
            }
            finally
            {
                p.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to decode event", e);
        }
    }
    
    public <T> T decodeFromString(String event, Class<T> type)
    {
        return this.decode(new StringReader(event), type);
    }
    
    public <T> T decodeFromString(String event, JavaType type)
    {
        return this.decode(new StringReader(event), type);
    }
    
    public <T> List<T> decodeListFromString(String event, Class<T> elementType)
    {
        return this.decodeList(new StringReader(event), elementType);
    }
    
    public <T> Set<T> decodeSetFromString(String event, Class<T> elementType)
    {
        return this.decodeSet(new StringReader(event), elementType);
    }
    
    public <T> T decodeFromBytes(byte[] event, Class<T> type)
    {
        return this.decode(new ByteArrayInputStream(event), type);
    }
    
    public <T> T decodeFromBytes(byte[] event, JavaType type)
    {
        return this.decode(new ByteArrayInputStream(event), type);
    }
    
    public <T> List<T> decodeListFromBytes(byte[] event, Class<T> elementType)
    {
        return this.decodeList(new ByteArrayInputStream(event), elementType);
    }
    
    public <T> Set<T> decodeSetFromBytes(byte[] event, Class<T> elementType)
    {
        return this.decodeSet(new ByteArrayInputStream(event), elementType);
    }
    
    public <T> T decode(File event, Class<T> type)
    {
        try
        {
            FileReader fr = new FileReader(event);
            try
            {
                return this.decode(fr, type);
            }
            finally
            {
                fr.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read file", e);
        }
    }
    
    public <T> T decode(File event, JavaType type)
    {
        try
        {
            FileReader fr = new FileReader(event);
            try
            {
                return this.decode(fr, type);
            }
            finally
            {
                fr.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read file", e);
        }
    }
    
    public <T> List<T> decodeList(File event, Class<T> elementType)
    {
        try
        {
            FileReader fr = new FileReader(event);
            try
            {
                return this.decodeList(fr, elementType);
            }
            finally
            {
                fr.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read file", e);
        }
    }
    
    public <T> Set<T> decodeSet(File event, Class<T> elementType)
    {
        try
        {
            FileReader fr = new FileReader(event);
            try
            {
                return this.decodeSet(fr, elementType);
            }
            finally
            {
                fr.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read file", e);
        }
    }
}
