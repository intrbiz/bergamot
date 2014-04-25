package com.intrbiz.bergamot.compat.config.model;

import java.util.LinkedList;
import java.util.List;

import com.intrbiz.bergamot.compat.config.builder.metadata.ParameterName;
import com.intrbiz.bergamot.compat.config.builder.metadata.TypeName;
import com.intrbiz.bergamot.compat.config.parser.model.ObjectParameter;
import com.intrbiz.bergamot.model.util.Parameter;

@TypeName("service")
public class ServiceCfg extends ConfigObject<ServiceCfg>
{
    private List<String> hostName = new LinkedList<String>();

    private List<String> hostgroupName = new LinkedList<String>();

    private String serviceDescription;

    private String displayName;

    private List<String> servicegroups = new LinkedList<String>();

    private Boolean isVolatile;

    private String checkCommand;

    private List<String> initialState = new LinkedList<String>();

    private Integer maxCheckAttempts;

    private Long checkInterval;

    private Long retryInterval;

    private Boolean activeChecksEnabled;

    private Boolean passiveChecksEnabled;

    private String checkPeriod;

    private Boolean obsessOverService;

    private Boolean checkFreshness;

    private Integer freshnessThreshold;

    private String eventHandler;

    private Boolean eventHandlerEnabled;

    private Float lowFlapThreshold;

    private Float highFlapThreshold;

    private Boolean flapDetectionEnabled;

    private List<String> flapDetectionOptions = new LinkedList<String>();

    private Boolean processPerfData;

    private String retainStatusInformation;

    private String retainNonstatusInformation;

    private Long notificationInterval;

    private Long firstNotificationDelay;

    private String notificationPeriod;

    private List<String> notificationOptions = new LinkedList<String>();

    private Boolean notificationsEnabled;

    private List<String> contacts;

    private List<String> contactGroups;

    private List<String> stalkingOptions = new LinkedList<String>();

    private String notes;

    private String notesUrl;

    private String actionUrl;

    private String iconImage;

    private String iconImageAlt;

    // extended

    private List<Parameter> checkParameters;

    public ServiceCfg()
    {
        super();
    }

    public List<String> getHostName()
    {
        return hostName;
    }

    @ParameterName("host_name")
    public void setHostName(List<String> hostName)
    {
        this.hostName = hostName;
    }

    public List<String> getHostgroupName()
    {
        return hostgroupName;
    }

    @ParameterName("hostgroup_name")
    public void setHostgroupName(List<String> hostgroupName)
    {
        this.hostgroupName = hostgroupName;
    }

    public String getServiceDescription()
    {
        return serviceDescription;
    }

    @ParameterName("service_description")
    public void setServiceDescription(String serviceDescription)
    {
        this.serviceDescription = serviceDescription;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    @ParameterName("display_name")
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public List<String> getServicegroups()
    {
        return servicegroups;
    }

    @ParameterName("servicegroups")
    public void setServicegroups(List<String> servicegroups)
    {
        this.servicegroups = servicegroups;
    }

    public Boolean isVolatile()
    {
        return isVolatile;
    }

    @ParameterName("is_volatile")
    public void setVolatile(Boolean isVolatile)
    {
        this.isVolatile = isVolatile;
    }

    public String getCheckCommand()
    {
        return checkCommand;
    }

    @ParameterName("check_command")
    public void setCheckCommand(String checkCommand)
    {
        this.checkCommand = checkCommand;
    }

    public List<String> getInitialState()
    {
        return initialState;
    }

    @ParameterName("initial_state")
    public void setInitialState(List<String> initialState)
    {
        this.initialState = initialState;
    }

    public Integer getMaxCheckAttempts()
    {
        return maxCheckAttempts;
    }

    public Integer resolveMaxCheckAttempts()
    {
        return this.resolveProperty((p) -> {
            return p.getMaxCheckAttempts();
        });
    }

    @ParameterName("max_check_attempts")
    public void setMaxCheckAttempts(Integer maxCheckAttempts)
    {
        this.maxCheckAttempts = maxCheckAttempts;
    }

    public Long getCheckInterval()
    {
        return checkInterval;
    }

    public Long resolveCheckInterval()
    {
        return this.resolveProperty((p) -> {
            return p.getCheckInterval();
        });
    }

    @ParameterName("check_interval")
    public void setCheckInterval(Long checkInterval)
    {
        this.checkInterval = checkInterval;
    }

    @ParameterName("normal_check_interval")
    public void setNormalCheckInterval(Long checkInterval)
    {
        this.checkInterval = checkInterval;
    }

    public Long getRetryInterval()
    {
        return retryInterval;
    }

    public Long resolveRetryInterval()
    {
        return this.resolveProperty((p) -> {
            return p.getRetryInterval();
        });
    }

    @ParameterName("retry_interval")
    public void setRetryInterval(Long retryInterval)
    {
        this.retryInterval = retryInterval;
    }

    @ParameterName("retry_check_interval")
    public void setRetryCheckInterval(Long retryInterval)
    {
        this.retryInterval = retryInterval;
    }

    public Boolean isActiveChecksEnabled()
    {
        return activeChecksEnabled;
    }

    @ParameterName("active_checks_enabled")
    public void setActiveChecksEnabled(Boolean activeChecksEnabled)
    {
        this.activeChecksEnabled = activeChecksEnabled;
    }

    public Boolean isPassiveChecksEnabled()
    {
        return passiveChecksEnabled;
    }

    @ParameterName("passive_checks_enabled")
    public void setPassiveChecksEnabled(Boolean passiveChecksEnabled)
    {
        this.passiveChecksEnabled = passiveChecksEnabled;
    }

    public String getCheckPeriod()
    {
        return checkPeriod;
    }

    @ParameterName("check_period")
    public void setCheckPeriod(String checkPeriod)
    {
        this.checkPeriod = checkPeriod;
    }

    public Boolean isObsessOverService()
    {
        return obsessOverService;
    }

    @ParameterName("obsess_over_service")
    public void setObsessOverService(Boolean obsessOverService)
    {
        this.obsessOverService = obsessOverService;
    }

    public Boolean isCheckFreshness()
    {
        return checkFreshness;
    }

    @ParameterName("check_freshness")
    public void setCheckFreshness(Boolean checkFreshness)
    {
        this.checkFreshness = checkFreshness;
    }

    public Integer getFreshnessThreshold()
    {
        return freshnessThreshold;
    }

    @ParameterName("freshness_threshold")
    public void setFreshnessThreshold(Integer freshnessThreshold)
    {
        this.freshnessThreshold = freshnessThreshold;
    }

    public String getEventHandler()
    {
        return eventHandler;
    }

    @ParameterName("event_handler")
    public void setEventHandler(String eventHandler)
    {
        this.eventHandler = eventHandler;
    }

    public Boolean isEventHandlerEnabled()
    {
        return eventHandlerEnabled;
    }

    @ParameterName("event_handler_enabled")
    public void setEventHandlerEnabled(Boolean eventHandlerEnabled)
    {
        this.eventHandlerEnabled = eventHandlerEnabled;
    }

    public Float getLowFlapThreshold()
    {
        return lowFlapThreshold;
    }

    @ParameterName("low_flap_threshold")
    public void setLowFlapThreshold(Float lowFlapThreshold)
    {
        this.lowFlapThreshold = lowFlapThreshold;
    }

    public Float getHighFlapThreshold()
    {
        return highFlapThreshold;
    }

    @ParameterName("high_flap_threshold")
    public void setHighFlapThreshold(Float highFlapThreshold)
    {
        this.highFlapThreshold = highFlapThreshold;
    }

    public Boolean isFlapDetectionEnabled()
    {
        return flapDetectionEnabled;
    }

    @ParameterName("flap_detection_enabled")
    public void setFlapDetectionEnabled(Boolean flapDetectionEnabled)
    {
        this.flapDetectionEnabled = flapDetectionEnabled;
    }

    public List<String> getFlapDetectionOptions()
    {
        return flapDetectionOptions;
    }

    @ParameterName("flap_detection_options")
    public void setFlapDetectionOptions(List<String> flapDetectionOptions)
    {
        this.flapDetectionOptions = flapDetectionOptions;
    }

    public Boolean isProcessPerfData()
    {
        return processPerfData;
    }

    @ParameterName("process_perf_data")
    public void setProcessPerfData(Boolean processPerfData)
    {
        this.processPerfData = processPerfData;
    }

    public String getRetainStatusInformation()
    {
        return retainStatusInformation;
    }

    @ParameterName("retain_status_information")
    public void setRetainStatusInformation(String retainStatusInformation)
    {
        this.retainStatusInformation = retainStatusInformation;
    }

    public String getRetainNonstatusInformation()
    {
        return retainNonstatusInformation;
    }

    @ParameterName("retain_nonstatus_information")
    public void setRetainNonstatusInformation(String retainNonstatusInformation)
    {
        this.retainNonstatusInformation = retainNonstatusInformation;
    }

    public Long getNotificationInterval()
    {
        return notificationInterval;
    }

    @ParameterName("notification_interval")
    public void setNotificationInterval(Long notificationInterval)
    {
        this.notificationInterval = notificationInterval;
    }

    public Long getFirstNotificationDelay()
    {
        return firstNotificationDelay;
    }

    @ParameterName("first_notification_delay")
    public void setFirstNotificationDelay(Long firstNotificationDelay)
    {
        this.firstNotificationDelay = firstNotificationDelay;
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

    public List<String> getNotificationOptions()
    {
        return notificationOptions;
    }

    @ParameterName("notification_options")
    public void setNotificationOptions(List<String> notificationOptions)
    {
        this.notificationOptions = notificationOptions;
    }

    public Boolean isNotificationsEnabled()
    {
        return notificationsEnabled;
    }

    @ParameterName("notifications_enabled")
    public void setNotificationsEnabled(Boolean notifications_Enabled)
    {
        this.notificationsEnabled = notifications_Enabled;
    }

    public List<String> getContacts()
    {
        return contacts;
    }

    @ParameterName("contacts")
    public void setContacts(List<String> contacts)
    {
        this.contacts = contacts;
    }

    public List<String> getContactGroups()
    {
        return contactGroups;
    }

    @ParameterName("contact_groups")
    public void setContactGroups(List<String> contactGroups)
    {
        this.contactGroups = contactGroups;
    }

    public List<String> getStalkingOptions()
    {
        return stalkingOptions;
    }

    @ParameterName("stalking_options")
    public void setStalkingOptions(List<String> stalkingOptions)
    {
        this.stalkingOptions = stalkingOptions;
    }

    public String getNotes()
    {
        return notes;
    }

    @ParameterName("notes")
    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public String getNotesUrl()
    {
        return notesUrl;
    }

    @ParameterName("notes_url")
    public void setNotesUrl(String notesUrl)
    {
        this.notesUrl = notesUrl;
    }

    public String getActionUrl()
    {
        return actionUrl;
    }

    @ParameterName("action_url")
    public void setActionUrl(String actionUrl)
    {
        this.actionUrl = actionUrl;
    }

    public String getIconImage()
    {
        return iconImage;
    }

    @ParameterName("icon_image")
    public void setIconImage(String iconImage)
    {
        this.iconImage = iconImage;
    }

    public String getIconImageAlt()
    {
        return iconImageAlt;
    }

    @ParameterName("icon_image_alt")
    public void setIconImageAlt(String iconImageAlt)
    {
        this.iconImageAlt = iconImageAlt;
    }

    public List<String> resolveHostName()
    {
        return this.resolveProperty((p) -> {
            return p.getHostName();
        });
    }

    public String resolveDisplayName()
    {
        return this.resolveProperty((p) -> {
            return p.getDisplayName();
        });
    }

    public String resolveServiceDescription()
    {
        return this.resolveProperty((p) -> {
            return p.getServiceDescription();
        });
    }

    public List<String> resolveHostgroupName()
    {
        return this.resolveProperty((p) -> {
            return p.getHostgroupName();
        });
    }

    public List<String> resolveServicegroups()
    {
        return this.resolveProperty((p) -> {
            return p.getServicegroups();
        });
    }

    public String resolveCheckCommand()
    {
        return this.resolveProperty((p) -> {
            return p.getCheckCommand();
        });
    }

    public List<String> resolveInitialState()
    {
        return this.resolveProperty((p) -> {
            return p.getInitialState();
        });
    }

    public String resolveCheckPeriod()
    {
        return this.resolveProperty((p) -> {
            return p.getCheckPeriod();
        });
    }

    public Integer resolveFreshnessThreshold()
    {
        return this.resolveProperty((p) -> {
            return p.getFreshnessThreshold();
        });
    }

    public String resolveEventHandler()
    {
        return this.resolveProperty((p) -> {
            return p.getEventHandler();
        });
    }

    public Float resolveLowFlapThreshold()
    {
        return this.resolveProperty((p) -> {
            return p.getLowFlapThreshold();
        });
    }

    public Float resolveHighFlapThreshold()
    {
        return this.resolveProperty((p) -> {
            return p.getHighFlapThreshold();
        });
    }

    public List<String> resolveFlapDetectionOptions()
    {
        return this.resolveProperty((p) -> {
            return p.getFlapDetectionOptions();
        });
    }

    public String resolveRetainStatusInformation()
    {
        return this.resolveProperty((p) -> {
            return p.getRetainStatusInformation();
        });
    }

    public String resolveRetainNonstatusInformation()
    {
        return this.resolveProperty((p) -> {
            return p.getRetainNonstatusInformation();
        });
    }

    public Long resolveNotificationInterval()
    {
        return this.resolveProperty((p) -> {
            return p.getNotificationInterval();
        });
    }

    public Long resolveFirstNotificationDelay()
    {
        return this.resolveProperty((p) -> {
            return p.getFirstNotificationDelay();
        });
    }

    public String resolveNotificationPeriod()
    {
        return this.resolveProperty((p) -> {
            return p.getNotificationPeriod();
        });
    }

    public List<String> resolveNotificationOptions()
    {
        return this.resolveProperty((p) -> {
            return p.getNotificationOptions();
        });
    }

    public List<String> resolveContacts()
    {
        return this.resolveProperty((p) -> {
            return p.getContacts();
        });
    }

    public List<String> resolveContactGroups()
    {
        return this.resolveProperty((p) -> {
            return p.getContactGroups();
        });
    }

    public List<String> resolveStalkingOptions()
    {
        return this.resolveProperty((p) -> {
            return p.getStalkingOptions();
        });
    }

    public String resolveNotes()
    {
        return this.resolveProperty((p) -> {
            return p.getNotes();
        });
    }

    public String resolveNotesUrl()
    {
        return this.resolveProperty((p) -> {
            return p.getNotesUrl();
        });
    }

    public String resolveActionUrl()
    {
        return this.resolveProperty((p) -> {
            return p.getActionUrl();
        });
    }

    public String resolveIconImage()
    {
        return this.resolveProperty((p) -> {
            return p.getIconImage();
        });
    }

    public String resolveIconImageAlt()
    {
        return this.resolveProperty((p) -> {
            return p.getIconImageAlt();
        });
    }

    // extended
    
    public List<Parameter> getCheckParameters()
    {
        return checkParameters;
    }

    public void setCheckParameters(List<Parameter> checkParameters)
    {
        this.checkParameters = checkParameters;
    }
    
    public List<Parameter> resolveCheckParameters()
    {
        return this.resolveProperty((p) -> { return p.getCheckParameters(); });
    }

    @Override
    public boolean unhandledObjectParameter(ObjectParameter parameter)
    {
        if ("check_parameter".equals(parameter.getName()))
        {
            if (this.checkParameters == null) this.checkParameters = new LinkedList<Parameter>();
            System.out.println("Got check_parameter: " + parameter.getValue());
            this.checkParameters.add(Parameter.parse(parameter.getValue()));
            return true;
        }
        return super.unhandledObjectParameter(parameter);
    }

    public String toString()
    {
        return "service { " + this.serviceDescription + " }";
    }
}
