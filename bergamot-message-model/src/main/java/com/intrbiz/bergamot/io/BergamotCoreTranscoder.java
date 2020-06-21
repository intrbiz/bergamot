package com.intrbiz.bergamot.io;

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
import com.intrbiz.bergamot.model.message.cluster.ProxyRegistration;
import com.intrbiz.bergamot.model.message.cluster.WorkerRegistration;
import com.intrbiz.bergamot.model.message.config.BergamotValidationReportMO;
import com.intrbiz.bergamot.model.message.event.global.FlushGlobalCaches;
import com.intrbiz.bergamot.model.message.event.site.DeinitSite;
import com.intrbiz.bergamot.model.message.event.site.InitSite;
import com.intrbiz.bergamot.model.message.event.update.AlertUpdate;
import com.intrbiz.bergamot.model.message.event.update.CheckUpdate;
import com.intrbiz.bergamot.model.message.event.update.GroupUpdate;
import com.intrbiz.bergamot.model.message.event.update.LocationUpdate;
import com.intrbiz.bergamot.model.message.importer.BergamotImportReportMO;
import com.intrbiz.bergamot.model.message.notification.BackupCodeUsed;
import com.intrbiz.bergamot.model.message.notification.PasswordResetNotification;
import com.intrbiz.bergamot.model.message.notification.RegisterContactNotification;
import com.intrbiz.bergamot.model.message.notification.SendAcknowledge;
import com.intrbiz.bergamot.model.message.notification.SendAlert;
import com.intrbiz.bergamot.model.message.notification.SendRecovery;
import com.intrbiz.bergamot.model.message.notification.SendUpdate;
import com.intrbiz.bergamot.model.message.notification.U2FADeviceRegistered;
import com.intrbiz.bergamot.model.message.processor.agent.AgentRegister;
import com.intrbiz.bergamot.model.message.processor.agent.LookupAgentKey;
import com.intrbiz.bergamot.model.message.processor.proxy.LookupProxyKey;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.processor.result.PassiveResult;
import com.intrbiz.bergamot.model.message.processor.result.match.MatchOnAgentId;
import com.intrbiz.bergamot.model.message.processor.result.match.MatchOnCheckId;
import com.intrbiz.bergamot.model.message.processor.result.match.MatchOnHostAddress;
import com.intrbiz.bergamot.model.message.processor.result.match.MatchOnHostExternalRef;
import com.intrbiz.bergamot.model.message.processor.result.match.MatchOnHostName;
import com.intrbiz.bergamot.model.message.processor.result.match.MatchOnServiceExternalRef;
import com.intrbiz.bergamot.model.message.processor.result.match.MatchOnServiceName;
import com.intrbiz.bergamot.model.message.processor.result.match.MatchOnTrapExternalRef;
import com.intrbiz.bergamot.model.message.processor.result.match.MatchOnTrapName;
import com.intrbiz.bergamot.model.message.proxy.AgentState;
import com.intrbiz.bergamot.model.message.report.SLAReportMO;
import com.intrbiz.bergamot.model.message.scheduler.ScheduleCheck;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.model.message.state.CheckStatsMO;
import com.intrbiz.bergamot.model.message.state.CheckTransitionMO;
import com.intrbiz.bergamot.model.message.state.GroupStateMO;
import com.intrbiz.bergamot.model.message.worker.agent.FoundAgentKey;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.worker.check.RegisterCheck;
import com.intrbiz.bergamot.model.message.worker.check.UnregisterCheck;
import com.intrbiz.gerald.polyakov.io.PolyakovTranscoder;

/**
 * Encode and decode messages
 */
public class BergamotCoreTranscoder extends BergamotTranscoder
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
        CheckReadingMO.class,
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
        // model
        ParameterMO.class,
        AuthTokenMO.class,
        CheckStatsMO.class,
        CheckTransitionMO.class,
        SLAReportMO.class,
        BergamotImportReportMO.class,
        AppliedConfigChange.class,
        BergamotValidationReportMO.class,
        VerifiedCommand.class,
        // cluster
        WorkerRegistration.class,
        NotifierRegistration.class,
        ProcessorRegistration.class,
        AgentRegistration.class,
        NodeRegistration.class,
        PoolRegistration.class,
        ProxyRegistration.class,
        // events
        InitSite.class,
        FlushGlobalCaches.class,
        DeinitSite.class,
        // scheduler
        ScheduleCheck.class,
        // worker
        ExecuteCheck.class,
        RegisterCheck.class,
        UnregisterCheck.class,
        FoundAgentKey.class,
        // processor
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
        ReadingParcelMessage.class,
        AgentRegister.class,
        LookupAgentKey.class,
        LookupProxyKey.class,
        // proxy
        AgentState.class,
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
        AlertUpdate.class
    };
    
    private static final BergamotCoreTranscoder DEFAULT = new BergamotCoreTranscoder();
    
    public static final BergamotCoreTranscoder getDefault()
    {
        return DEFAULT;
    }
    
    public BergamotCoreTranscoder()
    {
        super();
        // add Bergamot main types
        this.addEventType(BergamotCoreTranscoder.CLASSES);
        // include the metric reading models from Polyakov
        this.addEventType(PolyakovTranscoder.CLASSES);
    }
}
