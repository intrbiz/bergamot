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
import com.intrbiz.bergamot.model.message.AlertMO;
import com.intrbiz.bergamot.model.message.AuthTokenMO;
import com.intrbiz.bergamot.model.message.ClusterMO;
import com.intrbiz.bergamot.model.message.CommandMO;
import com.intrbiz.bergamot.model.message.CommentMO;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.DowntimeMO;
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
import com.intrbiz.bergamot.model.message.agent.manager.request.CreateSiteCA;
import com.intrbiz.bergamot.model.message.agent.manager.request.GetAgent;
import com.intrbiz.bergamot.model.message.agent.manager.request.GetRootCA;
import com.intrbiz.bergamot.model.message.agent.manager.request.GetServer;
import com.intrbiz.bergamot.model.message.agent.manager.request.GetSiteCA;
import com.intrbiz.bergamot.model.message.agent.manager.request.SignAgent;
import com.intrbiz.bergamot.model.message.agent.manager.request.SignServer;
import com.intrbiz.bergamot.model.message.agent.manager.response.AgentManagerError;
import com.intrbiz.bergamot.model.message.agent.manager.response.CreatedSiteCA;
import com.intrbiz.bergamot.model.message.agent.manager.response.GotAgent;
import com.intrbiz.bergamot.model.message.agent.manager.response.GotRootCA;
import com.intrbiz.bergamot.model.message.agent.manager.response.GotServer;
import com.intrbiz.bergamot.model.message.agent.manager.response.GotSiteCA;
import com.intrbiz.bergamot.model.message.agent.manager.response.SignedAgent;
import com.intrbiz.bergamot.model.message.agent.manager.response.SignedServer;
import com.intrbiz.bergamot.model.message.api.error.APIError;
import com.intrbiz.bergamot.model.message.api.notification.NotificationEvent;
import com.intrbiz.bergamot.model.message.api.notification.RegisterForNotifications;
import com.intrbiz.bergamot.model.message.api.notification.RegisteredForNotifications;
import com.intrbiz.bergamot.model.message.api.update.RegisterForUpdates;
import com.intrbiz.bergamot.model.message.api.update.RegisteredForUpdates;
import com.intrbiz.bergamot.model.message.api.update.UpdateEvent;
import com.intrbiz.bergamot.model.message.api.util.APIPing;
import com.intrbiz.bergamot.model.message.api.util.APIPong;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.cluster.manager.request.FlushGlobalCaches;
import com.intrbiz.bergamot.model.message.cluster.manager.request.InitSite;
import com.intrbiz.bergamot.model.message.cluster.manager.response.ClusterManagerError;
import com.intrbiz.bergamot.model.message.cluster.manager.response.FlushedGlobalCaches;
import com.intrbiz.bergamot.model.message.cluster.manager.response.InitedSite;
import com.intrbiz.bergamot.model.message.event.control.RegisterWatcher;
import com.intrbiz.bergamot.model.message.event.watcher.RegisterCheck;
import com.intrbiz.bergamot.model.message.event.watcher.UnregisterCheck;
import com.intrbiz.bergamot.model.message.notification.PasswordResetNotification;
import com.intrbiz.bergamot.model.message.notification.RegisterContactNotification;
import com.intrbiz.bergamot.model.message.notification.SendAcknowledge;
import com.intrbiz.bergamot.model.message.notification.SendAlert;
import com.intrbiz.bergamot.model.message.notification.SendRecovery;
import com.intrbiz.bergamot.model.message.reading.CheckReadingMO;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.MatchOnAgentId;
import com.intrbiz.bergamot.model.message.result.MatchOnCheckId;
import com.intrbiz.bergamot.model.message.result.MatchOnHostAddress;
import com.intrbiz.bergamot.model.message.result.MatchOnHostExternalRef;
import com.intrbiz.bergamot.model.message.result.MatchOnHostName;
import com.intrbiz.bergamot.model.message.result.MatchOnServiceExternalRef;
import com.intrbiz.bergamot.model.message.result.MatchOnServiceName;
import com.intrbiz.bergamot.model.message.result.MatchOnTrapExternalRef;
import com.intrbiz.bergamot.model.message.result.MatchOnTrapName;
import com.intrbiz.bergamot.model.message.result.PassiveResultMO;
import com.intrbiz.bergamot.model.message.scheduler.DisableCheck;
import com.intrbiz.bergamot.model.message.scheduler.EnableCheck;
import com.intrbiz.bergamot.model.message.scheduler.PauseScheduler;
import com.intrbiz.bergamot.model.message.scheduler.RescheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.ResumeScheduler;
import com.intrbiz.bergamot.model.message.scheduler.ScheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.UnscheduleCheck;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.model.message.state.CheckStatsMO;
import com.intrbiz.bergamot.model.message.state.CheckTransitionMO;
import com.intrbiz.bergamot.model.message.state.GroupStateMO;
import com.intrbiz.bergamot.model.message.update.AlertUpdate;
import com.intrbiz.bergamot.model.message.update.CheckUpdate;
import com.intrbiz.bergamot.model.message.update.GroupUpdate;
import com.intrbiz.bergamot.model.message.update.LocationUpdate;
import com.intrbiz.gerald.polyakov.io.PolyakovTranscoder;
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
        AlertMO.class,
        CommentMO.class,
        DowntimeMO.class,
        // model
        ParameterMO.class,
        // messages
        // check
        ExecuteCheck.class,
        ActiveResultMO.class,
        PassiveResultMO.class,
        MatchOnAgentId.class,
        MatchOnCheckId.class,
        MatchOnHostAddress.class,
        MatchOnHostName.class,
        MatchOnServiceName.class,
        MatchOnTrapName.class,
        MatchOnHostExternalRef.class,
        MatchOnServiceExternalRef.class,
        MatchOnTrapExternalRef.class,
        ReadingParcelMO.class,
        CheckReadingMO.class,
        // notifications
        SendAlert.class,
        SendRecovery.class,
        PasswordResetNotification.class,
        RegisterContactNotification.class,
        SendAcknowledge.class,
        // updates
        CheckUpdate.class,
        GroupUpdate.class,
        LocationUpdate.class,
        AlertUpdate.class,
        // scheduler
        EnableCheck.class,
        DisableCheck.class,
        ScheduleCheck.class,
        RescheduleCheck.class,
        UnscheduleCheck.class,
        PauseScheduler.class,
        ResumeScheduler.class,
        // API
        APIError.class,
        APIPing.class,
        APIPong.class,
        UpdateEvent.class,
        RegisterForUpdates.class,
        RegisteredForUpdates.class,
        NotificationEvent.class,
        RegisterForNotifications.class,
        RegisteredForNotifications.class,
        // control
        RegisterWatcher.class,
        // watcher
        RegisterCheck.class,
        UnregisterCheck.class,
        // generic
        ErrorMO.class,
        // auth
        AuthTokenMO.class,
        // stats
        CheckStatsMO.class,
        CheckTransitionMO.class,
        // bergamot agent manager
        CreateSiteCA.class,
        CreatedSiteCA.class,
        GetSiteCA.class,
        GotSiteCA.class,
        GetRootCA.class,
        GotRootCA.class,
        GetAgent.class,
        GotAgent.class,
        GetServer.class,
        GotServer.class,
        SignAgent.class,
        SignedAgent.class,
        SignServer.class,
        SignedServer.class,
        AgentManagerError.class,
        // cluster manager
        ClusterManagerError.class,
        InitSite.class,
        InitedSite.class,
        FlushGlobalCaches.class,
        FlushedGlobalCaches.class
    };
    
    private final ObjectMapper factory = new ObjectMapper();
    
    public BergamotTranscoder()
    {
        super();
        this.factory.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        this.factory.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        this.factory.configure(SerializationFeature.INDENT_OUTPUT, true);
        this.factory.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // add types
        this.addEventType(BergamotTranscoder.CLASSES);
        // include the metric reading models from Polyakov
        this.addEventType(PolyakovTranscoder.CLASSES);
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
