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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intrbiz.bergamot.model.message.ParameterMO;

/**
 * Encode and decode messages
 */
public class BergamotTranscoder
{
    public static final Class<?>[] BASE_CLASSES = {
        ParameterMO.class
    };
    
    private static final BergamotTranscoder DEFAULT = new BergamotTranscoder();
    
    public static BergamotTranscoder getDefault()
    {
        return DEFAULT;
    }
    
    private final ObjectMapper factory = new ObjectMapper();
    
    public BergamotTranscoder()
    {
        super();
        this.factory.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        this.factory.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        this.factory.configure(SerializationFeature.INDENT_OUTPUT, true);
        this.factory.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // add types
        this.addEventType(BergamotTranscoder.BASE_CLASSES);
    }
    
    public void addEventType(Class<?>... classes)
    {
        this.factory.registerSubtypes(classes);
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
