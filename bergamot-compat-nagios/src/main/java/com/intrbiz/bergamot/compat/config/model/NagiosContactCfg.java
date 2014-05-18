package com.intrbiz.bergamot.compat.config.model;

import java.util.List;

import com.intrbiz.bergamot.compat.config.builder.metadata.ParameterName;
import com.intrbiz.bergamot.compat.config.builder.metadata.TypeName;

@TypeName("contact")
public class NagiosContactCfg extends ConfigObject<NagiosContactCfg>
{

    private String contactName;

    private String alias;

    private List<String> contactgroups;

    private Boolean hostNotificationsEnabled;

    private Boolean serviceNotificationsEnabled;

    private String hostNotificationPeriod;

    private String serviceNotificationPeriod;

    private List<String> hostNotificationOptions;

    private List<String> serviceNotificationOptions;

    private String hostNotificationCommands;

    private String serviceNotificationCommands;

    private String email;

    private String pager;

    private List<String> addressx;

    private Boolean canSubmitCommands;

    private Boolean retainStatusInformation;

    private Boolean retainNonstatusInformation;
    
    // extended
    
    private Boolean notificationsEnabled;
    
    private String notificationPeriod;

    public NagiosContactCfg()
    {
    }

    public String getContactName()
    {
        return contactName;
    }

    @ParameterName("contact_name")
    public void setContactName(String contactName)
    {
        this.contactName = contactName;
    }

    public String getAlias()
    {
        return alias;
    }

    @ParameterName("alias")
    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    public List<String> getContactgroups()
    {
        return contactgroups;
    }

    @ParameterName("contactgroups")
    public void setContactgroups(List<String> contactgroups)
    {
        this.contactgroups = contactgroups;
    }

    public Boolean isHostNotificationsEnabled()
    {
        return hostNotificationsEnabled;
    }

    @ParameterName("host_notifications_enabled")
    public void setHostNotificationsEnabled(Boolean hostNotificationsEnabled)
    {
        this.hostNotificationsEnabled = hostNotificationsEnabled;
    }

    public Boolean isServiceNotificationsEnabled()
    {
        return serviceNotificationsEnabled;
    }

    @ParameterName("service_notifications_enabled")
    public void setServiceNotificationsEnabled(Boolean serviceNotificationsEnabled)
    {
        this.serviceNotificationsEnabled = serviceNotificationsEnabled;
    }

    public String getHostNotificationPeriod()
    {
        return hostNotificationPeriod;
    }

    @ParameterName("host_notification_period")
    public void setHostNotificationPeriod(String hostNotificationPeriod)
    {
        this.hostNotificationPeriod = hostNotificationPeriod;
    }

    public String getServiceNotificationPeriod()
    {
        return serviceNotificationPeriod;
    }

    @ParameterName("service_notification_period")
    public void setServiceNotificationPeriod(String serviceNotificationPeriod)
    {
        this.serviceNotificationPeriod = serviceNotificationPeriod;
    }

    public List<String> getHostNotificationOptions()
    {
        return hostNotificationOptions;
    }

    @ParameterName("host_notification_options")
    public void setHostNotificationOptions(List<String> hostNotificationOptions)
    {
        this.hostNotificationOptions = hostNotificationOptions;
    }

    public List<String> getServiceNotificationOptions()
    {
        return serviceNotificationOptions;
    }

    @ParameterName("service_notification_options")
    public void setServiceNotificationOptions(List<String> serviceNotificationOptions)
    {
        this.serviceNotificationOptions = serviceNotificationOptions;
    }

    public String getHostNotificationCommands()
    {
        return hostNotificationCommands;
    }

    @ParameterName("host_notification_commands")
    public void setHostNotificationCommands(String hostNotificationCommands)
    {
        this.hostNotificationCommands = hostNotificationCommands;
    }

    public String getServiceNotificationCommands()
    {
        return serviceNotificationCommands;
    }

    @ParameterName("service_notification_commands")
    public void setServiceNotificationCommands(String serviceNotificationCommands)
    {
        this.serviceNotificationCommands = serviceNotificationCommands;
    }

    public String getEmail()
    {
        return email;
    }

    @ParameterName("email")
    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPager()
    {
        return pager;
    }

    @ParameterName("pager")
    public void setPager(String pager)
    {
        this.pager = pager;
    }

    public List<String> getAddressx()
    {
        return addressx;
    }

    @ParameterName("addressx")
    public void setAddressx(List<String> addressx)
    {
        this.addressx = addressx;
    }

    public Boolean isCanSubmitCommands()
    {
        return canSubmitCommands;
    }

    @ParameterName("can_submit_commands")
    public void setCanSubmitCommands(Boolean canSubmitCommands)
    {
        this.canSubmitCommands = canSubmitCommands;
    }

    public Boolean isRetainStatusInformation()
    {
        return retainStatusInformation;
    }

    @ParameterName("retain_status_information")
    public void setRetainStatusInformation(Boolean retainStatusInformation)
    {
        this.retainStatusInformation = retainStatusInformation;
    }

    public Boolean isRetainNonstatusInformation()
    {
        return retainNonstatusInformation;
    }

    @ParameterName("retain_nonstatus_information")
    public void setRetainNonstatusInformation(Boolean retainNonstatusInformation)
    {
        this.retainNonstatusInformation = retainNonstatusInformation;
    }
    

    public String resolveContactName()
    {
        return this.resolveProperty((p) -> { return p.getContactName(); });
    }

    public String resolveAlias()
    {
        return this.resolveProperty((p) -> { return p.getAlias(); });
    }

    public List<String> resolveContactgroups()
    {
        return this.resolveProperty((p) -> { return p.getContactgroups(); });
    }

    public String resolveHostNotificationPeriod()
    {
        return this.resolveProperty((p) -> { return p.getHostNotificationPeriod(); });
    }

    public String resolveServiceNotificationPeriod()
    {
        return this.resolveProperty((p) -> { return p.getServiceNotificationPeriod(); });
    }

    public List<String> resolveHostNotificationOptions()
    {
        return this.resolveProperty((p) -> { return p.getHostNotificationOptions(); });
    }

    public List<String> resolveServiceNotificationOptions()
    {
        return this.resolveProperty((p) -> { return p.getServiceNotificationOptions(); });
    }

    public String resolveHostNotificationCommands()
    {
        return this.resolveProperty((p) -> { return p.getHostNotificationCommands(); });
    }

    public String resolveServiceNotificationCommands()
    {
        return this.resolveProperty((p) -> { return p.getServiceNotificationCommands(); });
    }

    public String resolveEmail()
    {
        return this.resolveProperty((p) -> { return p.getEmail(); });
    }

    public String resolvePager()
    {
        return this.resolveProperty((p) -> { return p.getPager(); });
    }

    public List<String> resolveAddressx()
    {
        return this.resolveProperty((p) -> { return p.getAddressx(); });
    }
    
    public Boolean resolveHostNotificationsEnabled()
    {
        return this.resolveProperty((p) -> { return p.isHostNotificationsEnabled(); });
    }
    
    public Boolean resolveServiceNotificationsEnabled()
    {
        return this.resolveProperty((p) -> { return p.isServiceNotificationsEnabled(); });
    }
    
    public Boolean resolveCanSubmitCommands()
    {
        return this.resolveProperty((p) -> { return p.isCanSubmitCommands(); });
    }

    public Boolean resolveRetainStatusInformation()
    {
        return this.resolveProperty((p) -> { return p.isRetainStatusInformation(); });
    }

    public Boolean resolveRetainNonstatusInformation()
    {
        return this.resolveProperty((p) -> { return p.isRetainNonstatusInformation(); });
    }

    public Boolean isNotificationsEnabled()
    {
        return notificationsEnabled;
    }

    @ParameterName("notifications_enabled")
    public void setNotificationsEnabled(Boolean notificationsEnabled)
    {
        this.notificationsEnabled = notificationsEnabled;
    }
    
    public Boolean resolveNotificationsEnabled()
    {
        return this.resolveProperty((p) -> { return p.isNotificationsEnabled(); });
    }

    public String getNotificationPeriod()
    {
        return notificationPeriod;
    }

    @ParameterName("notification_period")
    public void setNotificationPeriod(String notificationPeriod)
    {
        this.notificationPeriod = notificationPeriod;
    }
    
    public String resolveNotificationPeriod()
    {
        return this.resolveProperty((p) -> { return p.getNotificationPeriod(); });
    }

    public String toString()
    {
        return "contact { " + this.contactName + " }";
    }
}
