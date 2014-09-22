package com.intrbiz.bergamot.ui.router.admin;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.ContactCfg;
import com.intrbiz.bergamot.config.model.NotificationEngineCfg;
import com.intrbiz.bergamot.config.model.NotificationsCfg;
import com.intrbiz.bergamot.config.model.TeamCfg;
import com.intrbiz.bergamot.config.model.TimePeriodCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.bergamot.ui.util.Validation;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsBoolean;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.IsaUUID;
import com.intrbiz.metadata.ListParam;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/admin/contact")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
@RequirePermission("ui.admin.contact")
public class ContactAdminRouter extends Router<BergamotApp>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @SessionVar("site") Site site)
    {
        model("contacts", db.listContacts(site.getId()));
        model("contact_templates", db.listConfigTemplates(site.getId(), Configuration.getRootElement(ContactCfg.class)));
        encode("admin/contact/index");
    }
    
    @Any("/lock")
    @WithDataAdapter(BergamotDB.class)
    public void lock(BergamotDB db, @Param("id") @IsaUUID(mandatory = true) UUID contactId) throws IOException
    {
        action("lock-password", db.getContact(contactId));
        redirect(path("/admin/contact/"));
    }
    
    @Any("/unlock")
    @WithDataAdapter(BergamotDB.class)
    public void unlock(BergamotDB db, @Param("id") @IsaUUID(mandatory = true) UUID contactId) throws IOException
    {
        action("unlock-password", db.getContact(contactId));
        redirect(path("/admin/contact/"));
    }
    
    @Any("/reset")
    @WithDataAdapter(BergamotDB.class)
    public void reset(BergamotDB db, @Param("id") @IsaUUID(mandatory = true) UUID contactId) throws IOException
    {
        action("reset-password", db.getContact(contactId));
        redirect(path("/admin/contact/"));
    }
    
    @Get("/configure/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void showConfigure(BergamotDB db, @AsUUID UUID id, @SessionVar("site") Site site)
    {
        model("contact", Util.nullable(db.getConfig(id), Config::getConfiguration));
        model("templates", db.listConfigTemplates(site.getId(), Configuration.getRootElement(ContactCfg.class)));
        model("teams", db.listConfig(site.getId(), Configuration.getRootElement(TeamCfg.class)));
        model("timeperiods", db.listConfig(site.getId(), Configuration.getRootElement(TimePeriodCfg.class)));
        encode("admin/contact/configure");
    }
    
    @Post("/configure/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void doConfigure(
            BergamotDB db, 
            @AsUUID UUID id,
            @SessionVar("site") Site site,
            @Param("name")        @CheckStringLength(min = 1, max = 80, mandatory = true)           String name,
            @Param("summary")     @CheckStringLength(min = 1, max = 80, mandatory = true)           String summary,
            @Param("email")       @CheckStringLength(min = 1, max = 80, mandatory = true)           String email,
            @Param("pager")       @CheckStringLength(min = 0, max = 80)                             String pager,
            @Param("mobile")      @CheckStringLength(min = 0, max = 80)                             String mobile,
            @Param("phone")       @CheckStringLength(min = 0, max = 80)                             String phone,
            @Param("description") @CheckStringLength(min = 0, max = 1000)                           String description,
            @ListParam("extends") @CheckStringLength(min = 0, max = 80, mandatory = true)           List<String> inherits,
            @Param("template")    @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean template,
            @ListParam("teams")   @CheckStringLength(min = 1, max = 80, mandatory = true)           List<String> teams,
            @ListParam("grants")  @CheckStringLength(min = 1, max = 255, mandatory = true)          List<String> grants, 
            @ListParam("revokes") @CheckStringLength(min = 1, max = 255, mandatory = true)          List<String> revokes,
            /* Notifications */
            @Param("notifications")                 @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notifications,
            @Param("notifications_enabled")         @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsEnabled,
            @Param("notification_period")           @CheckStringLength(min = 0, max = 80)                             String notificationPeriod,
            @Param("notifications_on_alert")        @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsOnAlert,
            @Param("notifications_on_recovery")     @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsOnRecovery,
            @Param("notifications_ignore_pending")  @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsIgnorePending,
            @Param("notifications_ignore_ok")       @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsIgnoreOk,
            @Param("notifications_ignore_warning")  @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsIgnoreWarning,
            @Param("notifications_ignore_critical") @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsIgnoreCritical,
            @Param("notifications_ignore_unknown")  @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsIgnoreUnknown,
            @Param("notifications_ignore_timeout")  @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsIgnoreTimeout,
            @Param("notifications_ignore_error")    @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsIgnoreError,
            @Param("notifications_all_engines")     @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsAllEngines,
            /* Notification Engines */
            @ListParam("notification_engine_engine")          @CheckStringLength(min = 0, max = 80, mandatory = true)           List<String> notificationEngineEngine,
            @ListParam("notification_engine_enabled")         @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineEnabled,
            @ListParam("notification_engine_period")          @CheckStringLength(min = 0, max = 80)                             List<String> notificationEnginePeriod,
            @ListParam("notification_engine_on_alert")        @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineOnAlert,
            @ListParam("notification_engine_on_recovery")     @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineOnRecovery,
            @ListParam("notification_engine_ignore_pending")  @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineIgnorePending,
            @ListParam("notification_engine_ignore_ok")       @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineIgnoreOk,
            @ListParam("notification_engine_ignore_warning")  @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineIgnoreWarning,
            @ListParam("notification_engine_ignore_critical") @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineIgnoreCritical,
            @ListParam("notification_engine_ignore_unknown")  @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineIgnoreUnknown,
            @ListParam("notification_engine_ignore_timeout")  @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineIgnoreTimeout,
            @ListParam("notification_engine_ignore_error")    @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineIgnoreError
    ) throws IOException
    {
        // create the config
        ContactCfg config = (ContactCfg) db.getConfig(id).getConfiguration();
        // basic
        config.setName(name);
        config.setSummary(summary);
        config.setEmail(email);
        config.setPager(pager);
        config.setMobile(mobile);
        config.setPhone(phone);
        config.setDescription(description);
        // extends
        config.setInheritedTemplates(inherits.stream().collect(Collectors.toSet()));
        config.setTemplate(template);
        // teams
        config.setTeams(teams.stream().collect(Collectors.toSet()));
        // permissions
        config.setGrantedPermissions(grants.stream().collect(Collectors.toSet()));
        config.setRevokedPermissions(revokes.stream().collect(Collectors.toSet()));
        // notifications
        if (notifications)
        {
            NotificationsCfg ncfg = new NotificationsCfg();
            config.setNotifications(ncfg);
            ncfg.setEnabled(notificationsEnabled);
            ncfg.setNotificationPeriod(notificationPeriod);
            ncfg.setAlerts(notificationsOnAlert);
            ncfg.setRecovery(notificationsOnRecovery);
            if (notificationsIgnorePending)  ncfg.getIgnore().add("pending");
            if (notificationsIgnoreOk)       ncfg.getIgnore().add("ok");
            if (notificationsIgnoreWarning)  ncfg.getIgnore().add("warning");
            if (notificationsIgnoreCritical) ncfg.getIgnore().add("critical");
            if (notificationsIgnoreUnknown)  ncfg.getIgnore().add("unknown");
            if (notificationsIgnoreTimeout)  ncfg.getIgnore().add("timeout");
            if (notificationsIgnoreError)    ncfg.getIgnore().add("error");
            ncfg.setAllEnginesEnabled(notificationsAllEngines);
            // notification engines
            for (int i = 0; i < notificationEngineEngine.size(); i++)
            {
                NotificationEngineCfg necfg = new NotificationEngineCfg();
                ncfg.getNotificationEngines().add(necfg);
                necfg.setEngine(notificationEngineEngine.get(i));
                necfg.setEnabled(notificationEngineEnabled.get(i));
                necfg.setNotificationPeriod(notificationEnginePeriod.get(i));
                necfg.setAlerts(notificationEngineOnAlert.get(i));
                necfg.setRecovery(notificationEngineOnRecovery.get(i));
                if (notificationEngineIgnorePending.get(i))  necfg.getIgnore().add("pending");
                if (notificationEngineIgnoreOk.get(i))       necfg.getIgnore().add("ok");
                if (notificationEngineIgnoreWarning.get(i))  necfg.getIgnore().add("warning");
                if (notificationEngineIgnoreCritical.get(i)) necfg.getIgnore().add("critical");
                if (notificationEngineIgnoreUnknown.get(i))  necfg.getIgnore().add("unknown");
                if (notificationEngineIgnoreTimeout.get(i))  necfg.getIgnore().add("timeout");
                if (notificationEngineIgnoreError.get(i))    necfg.getIgnore().add("error");
            }
        }
        // create the contact
        if (config.getTemplateBooleanValue()) 
            action("create-contact-template", config);
        else
            action("create-contact", config);
        // redirect
        redirect(path("/admin/contact/"));
    }
    
    @Get("/configure")
    @WithDataAdapter(BergamotDB.class)
    public void showConfigureNew(BergamotDB db, @SessionVar("site") Site site)
    {
        model("contact", new ContactCfg());
        model("templates", db.listConfigTemplates(site.getId(), Configuration.getRootElement(ContactCfg.class)));
        model("teams", db.listConfig(site.getId(), Configuration.getRootElement(TeamCfg.class)));
        model("timeperiods", db.listConfig(site.getId(), Configuration.getRootElement(TimePeriodCfg.class)));
        encode("admin/contact/configure");
    }
    
    @Post("/configure")
    @WithDataAdapter(BergamotDB.class)
    public void doConfigureNew(
            @SessionVar("site") Site site,
            @Param("name")        @CheckStringLength(min = 1, max = 80, mandatory = true)           String name,
            @Param("summary")     @CheckStringLength(min = 1, max = 80, mandatory = true)           String summary,
            @Param("email")       @CheckStringLength(min = 1, max = 80, mandatory = true)           String email,
            @Param("pager")       @CheckStringLength(min = 0, max = 80)                             String pager,
            @Param("mobile")      @CheckStringLength(min = 0, max = 80)                             String mobile,
            @Param("phone")       @CheckStringLength(min = 0, max = 80)                             String phone,
            @Param("description") @CheckStringLength(min = 0, max = 1000)                           String description,
            @ListParam("extends") @CheckStringLength(min = 0, max = 80, mandatory = true)           List<String> inherits,
            @Param("template")    @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean template,
            @ListParam("teams")   @CheckStringLength(min = 1, max = 80, mandatory = true)           List<String> teams,
            @ListParam("grants")  @CheckStringLength(min = 1, max = 255, mandatory = true)          List<String> grants, 
            @ListParam("revokes") @CheckStringLength(min = 1, max = 255, mandatory = true)          List<String> revokes,
            /* Notifications */
            @Param("notifications")                 @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notifications,
            @Param("notifications_enabled")         @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsEnabled,
            @Param("notification_period")           @CheckStringLength(min = 0, max = 80)                             String notificationPeriod,
            @Param("notifications_on_alert")        @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsOnAlert,
            @Param("notifications_on_recovery")     @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsOnRecovery,
            @Param("notifications_ignore_pending")  @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsIgnorePending,
            @Param("notifications_ignore_ok")       @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsIgnoreOk,
            @Param("notifications_ignore_warning")  @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsIgnoreWarning,
            @Param("notifications_ignore_critical") @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsIgnoreCritical,
            @Param("notifications_ignore_unknown")  @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsIgnoreUnknown,
            @Param("notifications_ignore_timeout")  @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsIgnoreTimeout,
            @Param("notifications_ignore_error")    @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsIgnoreError,
            @Param("notifications_all_engines")     @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) Boolean notificationsAllEngines,
            /* Notification Engines */
            @ListParam("notification_engine_engine")          @CheckStringLength(min = 0, max = 80, mandatory = true)           List<String> notificationEngineEngine,
            @ListParam("notification_engine_enabled")         @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineEnabled,
            @ListParam("notification_engine_period")          @CheckStringLength(min = 0, max = 80)                             List<String> notificationEnginePeriod,
            @ListParam("notification_engine_on_alert")        @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineOnAlert,
            @ListParam("notification_engine_on_recovery")     @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineOnRecovery,
            @ListParam("notification_engine_ignore_pending")  @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineIgnorePending,
            @ListParam("notification_engine_ignore_ok")       @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineIgnoreOk,
            @ListParam("notification_engine_ignore_warning")  @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineIgnoreWarning,
            @ListParam("notification_engine_ignore_critical") @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineIgnoreCritical,
            @ListParam("notification_engine_ignore_unknown")  @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineIgnoreUnknown,
            @ListParam("notification_engine_ignore_timeout")  @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineIgnoreTimeout,
            @ListParam("notification_engine_ignore_error")    @AsBoolean(coalesce = CoalesceMode.ON_NULL, defaultValue = false) List<Boolean> notificationEngineIgnoreError            
    ) throws IOException
    {
        // create the config
        ContactCfg config = new ContactCfg();
        config.setId(site.randomObjectId());
        // basic
        config.setName(name);
        config.setSummary(summary);
        config.setEmail(email);
        config.setPager(pager);
        config.setMobile(mobile);
        config.setPhone(phone);
        config.setDescription(description);
        // extends
        config.setInheritedTemplates(inherits.stream().collect(Collectors.toSet()));
        config.setTemplate(template);
        // teams
        config.setTeams(teams.stream().collect(Collectors.toSet()));
        // permissions
        config.setGrantedPermissions(grants.stream().collect(Collectors.toSet()));
        config.setRevokedPermissions(revokes.stream().collect(Collectors.toSet()));
        // notifications
        if (notifications)
        {
            NotificationsCfg ncfg = new NotificationsCfg();
            config.setNotifications(ncfg);
            ncfg.setEnabled(notificationsEnabled);
            ncfg.setNotificationPeriod(notificationPeriod);
            ncfg.setAlerts(notificationsOnAlert);
            ncfg.setRecovery(notificationsOnRecovery);
            if (notificationsIgnorePending)  ncfg.getIgnore().add("pending");
            if (notificationsIgnoreOk)       ncfg.getIgnore().add("ok");
            if (notificationsIgnoreWarning)  ncfg.getIgnore().add("warning");
            if (notificationsIgnoreCritical) ncfg.getIgnore().add("critical");
            if (notificationsIgnoreUnknown)  ncfg.getIgnore().add("unknown");
            if (notificationsIgnoreTimeout)  ncfg.getIgnore().add("timeout");
            if (notificationsIgnoreError)    ncfg.getIgnore().add("error");
            ncfg.setAllEnginesEnabled(notificationsAllEngines);
            // notification engines
            for (int i = 0; i < notificationEngineEngine.size(); i++)
            {
                NotificationEngineCfg necfg = new NotificationEngineCfg();
                ncfg.getNotificationEngines().add(necfg);
                necfg.setEngine(notificationEngineEngine.get(i));
                necfg.setEnabled(notificationEngineEnabled.get(i));
                necfg.setNotificationPeriod(notificationEnginePeriod.get(i));
                necfg.setAlerts(notificationEngineOnAlert.get(i));
                necfg.setRecovery(notificationEngineOnRecovery.get(i));
                if (notificationEngineIgnorePending.get(i))  necfg.getIgnore().add("pending");
                if (notificationEngineIgnoreOk.get(i))       necfg.getIgnore().add("ok");
                if (notificationEngineIgnoreWarning.get(i))  necfg.getIgnore().add("warning");
                if (notificationEngineIgnoreCritical.get(i)) necfg.getIgnore().add("critical");
                if (notificationEngineIgnoreUnknown.get(i))  necfg.getIgnore().add("unknown");
                if (notificationEngineIgnoreTimeout.get(i))  necfg.getIgnore().add("timeout");
                if (notificationEngineIgnoreError.get(i))    necfg.getIgnore().add("error");
            }
        }
        // create the contact
        if (config.getTemplateBooleanValue()) 
            action("create-contact-template", config);
        else
            action("create-contact", config);
        // redirect
        redirect(path("/admin/contact/"));
    }
}
