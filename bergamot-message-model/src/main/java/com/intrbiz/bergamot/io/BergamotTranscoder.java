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
import com.intrbiz.bergamot.model.message.AlertEncompassesMO;
import com.intrbiz.bergamot.model.message.AlertEscalationMO;
import com.intrbiz.bergamot.model.message.AlertMO;
import com.intrbiz.bergamot.model.message.AuthTokenMO;
import com.intrbiz.bergamot.model.message.CheckReadingMO;
import com.intrbiz.bergamot.model.message.ClusterMO;
import com.intrbiz.bergamot.model.message.CommandMO;
import com.intrbiz.bergamot.model.message.CommentMO;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.CredentialMO;
import com.intrbiz.bergamot.model.message.DowntimeMO;
import com.intrbiz.bergamot.model.message.EscalationMO;
import com.intrbiz.bergamot.model.message.GroupMO;
import com.intrbiz.bergamot.model.message.HostMO;
import com.intrbiz.bergamot.model.message.LocationMO;
import com.intrbiz.bergamot.model.message.NoteMO;
import com.intrbiz.bergamot.model.message.NotificationEngineMO;
import com.intrbiz.bergamot.model.message.NotificationsMO;
import com.intrbiz.bergamot.model.message.ParameterMO;
import com.intrbiz.bergamot.model.message.ResourceMO;
import com.intrbiz.bergamot.model.message.SLAFixedPeriodMO;
import com.intrbiz.bergamot.model.message.SLAMO;
import com.intrbiz.bergamot.model.message.SLARollingPeriodMO;
import com.intrbiz.bergamot.model.message.SecurityDomainMO;
import com.intrbiz.bergamot.model.message.ServiceMO;
import com.intrbiz.bergamot.model.message.TeamMO;
import com.intrbiz.bergamot.model.message.TimePeriodMO;
import com.intrbiz.bergamot.model.message.TrapMO;
import com.intrbiz.bergamot.model.message.api.call.AppliedConfigChange;
import com.intrbiz.bergamot.model.message.api.call.VerifiedCommand;
import com.intrbiz.bergamot.model.message.api.check.ExecuteAdhocCheck;
import com.intrbiz.bergamot.model.message.api.check.ExecutedAdhocCheck;
import com.intrbiz.bergamot.model.message.api.error.APIError;
import com.intrbiz.bergamot.model.message.api.notification.NotificationEvent;
import com.intrbiz.bergamot.model.message.api.notification.RegisterForNotifications;
import com.intrbiz.bergamot.model.message.api.notification.RegisteredForNotifications;
import com.intrbiz.bergamot.model.message.api.result.AdhocResultEvent;
import com.intrbiz.bergamot.model.message.api.result.RegisterForAdhocResults;
import com.intrbiz.bergamot.model.message.api.result.RegisteredForAdhocResults;
import com.intrbiz.bergamot.model.message.api.update.RegisterForUpdates;
import com.intrbiz.bergamot.model.message.api.update.RegisteredForUpdates;
import com.intrbiz.bergamot.model.message.api.update.UpdateEvent;
import com.intrbiz.bergamot.model.message.api.util.APIPing;
import com.intrbiz.bergamot.model.message.api.util.APIPong;
import com.intrbiz.bergamot.model.message.cluster.AgentRegistration;
import com.intrbiz.bergamot.model.message.cluster.NodeRegistration;
import com.intrbiz.bergamot.model.message.cluster.NotifierRegistration;
import com.intrbiz.bergamot.model.message.cluster.PoolRegistration;
import com.intrbiz.bergamot.model.message.cluster.ProcessorRegistration;
import com.intrbiz.bergamot.model.message.cluster.WorkerRegistration;
import com.intrbiz.bergamot.model.message.config.BergamotValidationReportMO;
import com.intrbiz.bergamot.model.message.event.global.FlushGlobalCaches;
import com.intrbiz.bergamot.model.message.event.site.DeinitSite;
import com.intrbiz.bergamot.model.message.event.site.InitSite;
import com.intrbiz.bergamot.model.message.event.update.AlertUpdate;
import com.intrbiz.bergamot.model.message.event.update.CheckUpdate;
import com.intrbiz.bergamot.model.message.event.update.GroupUpdate;
import com.intrbiz.bergamot.model.message.event.update.LocationUpdate;
import com.intrbiz.bergamot.model.message.event.watcher.RegisterCheck;
import com.intrbiz.bergamot.model.message.event.watcher.UnregisterCheck;
import com.intrbiz.bergamot.model.message.importer.BergamotImportReportMO;
import com.intrbiz.bergamot.model.message.notification.BackupCodeUsed;
import com.intrbiz.bergamot.model.message.notification.PasswordResetNotification;
import com.intrbiz.bergamot.model.message.notification.RegisterContactNotification;
import com.intrbiz.bergamot.model.message.notification.SendAcknowledge;
import com.intrbiz.bergamot.model.message.notification.SendAlert;
import com.intrbiz.bergamot.model.message.notification.SendRecovery;
import com.intrbiz.bergamot.model.message.notification.SendUpdate;
import com.intrbiz.bergamot.model.message.notification.U2FADeviceRegistered;
import com.intrbiz.bergamot.model.message.pool.agent.AgentRegister;
import com.intrbiz.bergamot.model.message.pool.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.pool.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.pool.result.ActiveResult;
import com.intrbiz.bergamot.model.message.pool.result.PassiveResult;
import com.intrbiz.bergamot.model.message.pool.result.match.MatchOnAgentId;
import com.intrbiz.bergamot.model.message.pool.result.match.MatchOnCheckId;
import com.intrbiz.bergamot.model.message.pool.result.match.MatchOnHostAddress;
import com.intrbiz.bergamot.model.message.pool.result.match.MatchOnHostExternalRef;
import com.intrbiz.bergamot.model.message.pool.result.match.MatchOnHostName;
import com.intrbiz.bergamot.model.message.pool.result.match.MatchOnServiceExternalRef;
import com.intrbiz.bergamot.model.message.pool.result.match.MatchOnServiceName;
import com.intrbiz.bergamot.model.message.pool.result.match.MatchOnTrapExternalRef;
import com.intrbiz.bergamot.model.message.pool.result.match.MatchOnTrapName;
import com.intrbiz.bergamot.model.message.pool.scheduler.ScheduleCheck;
import com.intrbiz.bergamot.model.message.report.SLAReportMO;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.model.message.state.CheckStatsMO;
import com.intrbiz.bergamot.model.message.state.CheckTransitionMO;
import com.intrbiz.bergamot.model.message.state.GroupStateMO;
import com.intrbiz.gerald.polyakov.io.PolyakovTranscoder;

/**
 * Encode and decode messages
 */
public class BergamotTranscoder
{
    public static final Class<?>[] CLASSES = {
        // message objects
        NotificationEngineMO.class,
        EscalationMO.class,
        NotificationsMO.class,
        NoteMO.class,
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
        AlertEscalationMO.class,
        AlertEncompassesMO.class,
        CommentMO.class,
        DowntimeMO.class,
        SecurityDomainMO.class,
        CredentialMO.class,
        SLAMO.class,
        SLARollingPeriodMO.class,
        SLAFixedPeriodMO.class,
        // model
        ParameterMO.class,
        // messages
        // check
        ExecuteCheck.class,
        ActiveResult.class,
        PassiveResult.class,
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
        U2FADeviceRegistered.class,
        BackupCodeUsed.class,
        SendUpdate.class,
        // updates
        CheckUpdate.class,
        GroupUpdate.class,
        LocationUpdate.class,
        AlertUpdate.class,
        // scheduler
        ScheduleCheck.class,
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
        RegisterForAdhocResults.class,
        RegisteredForAdhocResults.class,
        AdhocResultEvent.class,
        ExecuteAdhocCheck.class,
        ExecutedAdhocCheck.class,
        // watcher
        RegisterCheck.class,
        UnregisterCheck.class,
        // auth
        AuthTokenMO.class,
        // stats
        CheckStatsMO.class,
        CheckTransitionMO.class,
        // cluster manager
        InitSite.class,
        FlushGlobalCaches.class,
        DeinitSite.class,
        // util models
        BergamotImportReportMO.class,
        AppliedConfigChange.class,
        BergamotValidationReportMO.class,
        VerifiedCommand.class,
        // agent events
        AgentRegister.class,
        // reports
        SLAReportMO.class,
        // cluster registry
        WorkerRegistration.class,
        NotifierRegistration.class,
        ProcessorRegistration.class,
        AgentRegistration.class,
        NodeRegistration.class,
        PoolRegistration.class
    };
    
    private static final BergamotTranscoder DEFAULT = new BergamotTranscoder();
    
    public static final BergamotTranscoder getDefaultInstance()
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
}
