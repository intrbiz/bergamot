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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intrbiz.bergamot.model.message.AuthTokenMO;
import com.intrbiz.bergamot.model.message.ClusterMO;
import com.intrbiz.bergamot.model.message.CommandMO;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.ErrorMO;
import com.intrbiz.bergamot.model.message.GroupMO;
import com.intrbiz.bergamot.model.message.HostMO;
import com.intrbiz.bergamot.model.message.LocationMO;
import com.intrbiz.bergamot.model.message.NotificationEngineMO;
import com.intrbiz.bergamot.model.message.NotificationsMO;
import com.intrbiz.bergamot.model.message.ParameterMO;
import com.intrbiz.bergamot.model.message.ResourceMO;
import com.intrbiz.bergamot.model.message.ServiceMO;
import com.intrbiz.bergamot.model.message.TeamMO;
import com.intrbiz.bergamot.model.message.TimePeriodMO;
import com.intrbiz.bergamot.model.message.TrapMO;
import com.intrbiz.bergamot.model.message.api.error.APIError;
import com.intrbiz.bergamot.model.message.api.update.RegisterForUpdates;
import com.intrbiz.bergamot.model.message.api.update.RegisteredForUpdates;
import com.intrbiz.bergamot.model.message.api.update.UpdateEvent;
import com.intrbiz.bergamot.model.message.api.util.APIPing;
import com.intrbiz.bergamot.model.message.api.util.APIPong;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.event.control.RegisterWatcher;
import com.intrbiz.bergamot.model.message.event.watcher.RegisterCheck;
import com.intrbiz.bergamot.model.message.event.watcher.UnregisterCheck;
import com.intrbiz.bergamot.model.message.notification.SendAlert;
import com.intrbiz.bergamot.model.message.notification.SendRecovery;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.model.message.scheduler.DisableCheck;
import com.intrbiz.bergamot.model.message.scheduler.EnableCheck;
import com.intrbiz.bergamot.model.message.scheduler.PauseScheduler;
import com.intrbiz.bergamot.model.message.scheduler.RescheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.ResumeScheduler;
import com.intrbiz.bergamot.model.message.scheduler.ScheduleCheck;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.model.message.state.GroupStateMO;
import com.intrbiz.bergamot.model.message.update.Update;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.QueueException;

/**
 * Encode and decode messages
 */
public class BergamotTranscoder
{   
    public static final Class<?>[] CLASSES = {
        // message objects
        NotificationEngineMO.class,
        NotificationsMO.class,
        CheckStateMO.class,
        GroupStateMO.class,
        CommandMO.class,
        HostMO.class,
        ClusterMO.class,
        ServiceMO.class,
        TrapMO.class,
        ResourceMO.class,
        ContactMO.class,
        GroupMO.class,
        LocationMO.class,
        TeamMO.class,
        TimePeriodMO.class,
        // model
        ParameterMO.class,
        // messages
        // check
        ExecuteCheck.class,
        Result.class,
        // notifications
        SendAlert.class,
        SendRecovery.class,
        // updates
        Update.class,
        // scheduler
        EnableCheck.class,
        DisableCheck.class,
        ScheduleCheck.class,
        RescheduleCheck.class,
        PauseScheduler.class,
        ResumeScheduler.class,
        // API
        APIError.class,
        APIPing.class,
        APIPong.class,
        UpdateEvent.class,
        RegisterForUpdates.class,
        RegisteredForUpdates.class,
        // control
        RegisterWatcher.class,
        // watcher
        RegisterCheck.class,
        UnregisterCheck.class,
        // generic
        ErrorMO.class,
        // auth
        AuthTokenMO.class
    };
    
    private final ObjectMapper factory = new ObjectMapper();
    
    public BergamotTranscoder()
    {
        super();
        this.factory.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        this.factory.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        this.factory.configure(SerializationFeature.INDENT_OUTPUT, true);
        // add types
        this.addEventType(BergamotTranscoder.CLASSES);
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
    
    public <T> T decodeFromString(String event, Class<T> type)
    {
        return this.decode(new StringReader(event), type);
    }
    
    public <T> T decodeFromBytes(byte[] event, Class<T> type)
    {
        return this.decode(new ByteArrayInputStream(event), type);
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
    
    /**
     * Adapt this transcoder for use as a QueueTranscoder
     */
    public <T> QueueEventTranscoder<T> asQueueEventTranscoder(final Class<T> type)
    {
        return new QueueEventTranscoder<T>()
        {
            @Override
            public byte[] encodeAsBytes(T event) throws QueueException
            {
                return BergamotTranscoder.this.encodeAsBytes(event);
            }

            @Override
            public T decodeFromBytes(byte[] data) throws QueueException
            {
                return (T) BergamotTranscoder.this.decodeFromBytes(data, type);
            }
        };
    }
}
