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
import com.intrbiz.bergamot.model.message.agent.check.CheckCPU;
import com.intrbiz.bergamot.model.message.agent.check.CheckDisk;
import com.intrbiz.bergamot.model.message.agent.check.CheckMem;
import com.intrbiz.bergamot.model.message.agent.check.CheckOS;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.hello.AgentHello;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPing;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPong;
import com.intrbiz.bergamot.model.message.agent.stat.CPUStat;
import com.intrbiz.bergamot.model.message.agent.stat.DiskStat;
import com.intrbiz.bergamot.model.message.agent.stat.MemStat;
import com.intrbiz.bergamot.model.message.agent.stat.OSStat;
import com.intrbiz.bergamot.model.message.agent.stat.cpu.CPUInfo;
import com.intrbiz.bergamot.model.message.agent.stat.cpu.CPUTime;
import com.intrbiz.bergamot.model.message.agent.stat.cpu.CPUUsage;
import com.intrbiz.bergamot.model.message.agent.stat.disk.DiskInfo;

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
        OSStat.class
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
        try (JsonGenerator g = this.factory.getFactory().createGenerator(to))
        {
            this.factory.writeValue(g, event);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to encode event", e);
        }
    }
    
    public void encode(Object event, Writer to)
    {
        try (JsonGenerator g = this.factory.getFactory().createGenerator(to))
        {
            this.factory.writeValue(g, event);
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
        try (FileWriter fw = new FileWriter(file))
        {
            this.encode(event, fw);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to write file", e);
        }
    }
    
    public <T> T decode(InputStream from, JavaType type)
    {
        try (JsonParser p = this.factory.getFactory().createParser(from))
        {
            return this.factory.readValue(p, type);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to decode event", e);
        }
    }
    
    public <T> T decode(InputStream from, Class<T> type)
    {
        try (JsonParser p = this.factory.getFactory().createParser(from))
        {
            return (T) this.factory.readValue(p, type);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to decode event", e);
        }
    }
    
    public <T> List<T> decodeList(InputStream from, Class<T> elementType)
    {
        try (JsonParser p = this.factory.getFactory().createParser(from))
        {
            return this.factory.readValue(p, this.factory.getTypeFactory().constructCollectionType(List.class, elementType));
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to decode event", e);
        }
    }
    
    public <T> Set<T> decodeSet(InputStream from, Class<T> elementType)
    {
        try (JsonParser p = this.factory.getFactory().createParser(from))
        {
            return this.factory.readValue(p, this.factory.getTypeFactory().constructCollectionType(Set.class, elementType));
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to decode event", e);
        }
    }
    
    public <T> T decode(Reader from, Class<T> type)
    {
        try (JsonParser p = this.factory.getFactory().createParser(from))
        {
            return (T) this.factory.readValue(p, type);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to decode event", e);
        }
    }
    
    public <T> T decode(Reader from, JavaType type)
    {
        try (JsonParser p = this.factory.getFactory().createParser(from))
        {
            return this.factory.readValue(p, type);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to decode event", e);
        }
    }
    
    public <T> List<T> decodeList(Reader from, Class<T> elementType)
    {
        try (JsonParser p = this.factory.getFactory().createParser(from))
        {
            return this.factory.readValue(p, this.factory.getTypeFactory().constructCollectionType(List.class, elementType));
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to decode event", e);
        }
    }
    
    public <T> Set<T> decodeSet(Reader from, Class<T> elementType)
    {
        try (JsonParser p = this.factory.getFactory().createParser(from))
        {
            return this.factory.readValue(p, this.factory.getTypeFactory().constructCollectionType(Set.class, elementType));
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
        try (FileReader fr = new FileReader(event))
        {
            return this.decode(fr, type);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read file", e);
        }
    }
    
    public <T> T decode(File event, JavaType type)
    {
        try (FileReader fr = new FileReader(event))
        {
            return this.decode(fr, type);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read file", e);
        }
    }
    
    public <T> List<T> decodeList(File event, Class<T> elementType)
    {
        try (FileReader fr = new FileReader(event))
        {
            return this.decodeList(fr, elementType);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read file", e);
        }
    }
    
    public <T> Set<T> decodeSet(File event, Class<T> elementType)
    {
        try (FileReader fr = new FileReader(event))
        {
            return this.decodeSet(fr, elementType);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read file", e);
        }
    }
}
