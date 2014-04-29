package com.intrbiz.bergamot.compat.config.model;

import java.util.List;

import com.intrbiz.bergamot.compat.config.builder.metadata.ParameterName;
import com.intrbiz.bergamot.compat.config.builder.metadata.TypeName;

@TypeName("host")
public class HostCfg extends ConfigObject<HostCfg>
{
    private String hostName;

    private String alias;

    private String displayName;

    private String address;

    private List<String> parents;

    private List<String> hostgroups;

    private String checkCommand;

    private List<String> initialState;

    private Integer maxCheckAttempts;

    private Long checkInterval;

    private Long retryInterval;

    private Boolean activeChecksEnabled;

    private Boolean passiveChecksEnabled;

    private String checkPeriod;

    private Boolean obsessOverHost;

    private Boolean checkFreshness;

    private Integer freshnessThreshold;

    private String eventHandler;

    private Boolean eventHandlerEnabled;

    private Float lowFlapThreshold;

    private Float highFlapThreshold;

    private Boolean flapDetectionEnabled;

    private List<String> flapDetectionOptions;

    private Boolean processPerfData;

    private Boolean retainStatusInformation;

    private Boolean retainNonstatusInformation;

    private List<String> contacts;

    private List<String> contactGroups;

    private Long notificationInterval;

    private Long firstNotificationDelay;

    private String notificationPeriod;

    private List<String> notificationOptions;

    private Boolean notificationsEnabled;

    private List<String> stalkingOptions;

    private String notes;

    private String notesUrl;

    private String actionUrl;

    private String iconImage;

    private String iconImageAlt;

    private String vrmlImage;

    private String statusmapImage;

    private String coords2D;

    private String coords3D;
    
    // extended
    private String location;

    public HostCfg()
    {
        super();
    }

    public String getHostName()
    {
        return hostName;
    }
    
    public String resolveHostName()
    {
        return this.resolveProperty((p) -> { return p.getHostName(); });
    }

    @ParameterName("host_name")
    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    public String getAlias()
    {
        return alias;
    }
    
    public String resolveAlias()
    {
        return this.resolveProperty((p) -> { return p.getAlias(); });
    }

    @ParameterName("alias")
    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    public String getDisplayName()
    {
        return displayName;
    }
    
    public String resolveDisplayName()
    {
        return this.resolveProperty((p) -> { return p.getDisplayName(); });
    }

    @ParameterName("display_name")
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getAddress()
    {
        return address;
    }
    
    public String resolveAddress()
    {
        return this.resolveProperty((p) -> { return p.getAddress(); });
    }

    @ParameterName("address")
    public void setAddress(String address)
    {
        this.address = address;
    }

    
    public List<String> getParents()
    {
        return parents;
    }
    
    public List<String> resolveParents()
    {
        return this.resolveProperty((p) -> { return p.getParents(); });
    }

    @ParameterName("parents")
    public void setParents(List<String> parents)
    {
        this.parents = parents;
    }

    public List<String> getHostgroups()
    {
        return hostgroups;
    }
    
    public List<String> resolveHostgroups()
    {
        return this.resolveProperty((p) -> { return p.getHostgroups(); });
    }

    @ParameterName("hostgroups")
    public void setHostgroups(List<String> hostgroups)
    {
        this.hostgroups = hostgroups;
    }

    public String getCheckCommand()
    {
        return checkCommand;
    }
    
    public String resolveCheckCommand()
    {
        return this.resolveProperty((p) -> { return p.getCheckCommand(); });
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
    
    public List<String> resolveInitialState()
    {
        return this.resolveProperty((p) -> { return p.getInitialState(); });
    }

    @ParameterName("initial_state")
    public void setInitialState(List<String> initialState)
    {
        this.initialState = initialState;
    }
    
    public Integer resolveMaxCheckAttempts()
    {
        return this.resolveProperty((p) -> { return p.getMaxCheckAttempts(); }); 
    }

    public Integer getMaxCheckAttempts()
    {
        return maxCheckAttempts;
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
        return this.resolveProperty((p) -> { return p.getCheckInterval(); });
    }

    @ParameterName("check_interval")
    public void setCheckInterval(Long checkInterval)
    {
        this.checkInterval = checkInterval;
    }

    public Long getRetryInterval()
    {
        return retryInterval;
    }
    
    public Long resolveRetryInterval()
    {
        return this.resolveProperty((p) -> { return p.getRetryInterval(); });
    }

    @ParameterName("retry_interval")
    public void setRetryInterval(Long retryInterval)
    {
        this.retryInterval = retryInterval;
    }

    public Boolean isActiveChecksEnabled()
    {
        return activeChecksEnabled;
    }
    
    public Boolean resolveActiveChecksEnabled()
    {
        return this.resolveProperty((p) -> { return p.isActiveChecksEnabled(); });
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
    
    public Boolean resolvePassiveChecksEnabled()
    {
        return this.resolveProperty((p) -> { return p.isPassiveChecksEnabled(); });
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
    
    public String resolveCheckPeriod()
    {
        return this.resolveProperty((p) -> { return p.getCheckPeriod(); });
    }

    @ParameterName("check_period")
    public void setCheckPeriod(String checkPeriod)
    {
        this.checkPeriod = checkPeriod;
    }

    public Boolean isObsessOverHost()
    {
        return obsessOverHost;
    }
    
    public Boolean resolveObsessOverHost()
    {
        return this.resolveProperty((p) -> { return p.isObsessOverHost(); });
    }

    @ParameterName("obsess_over_host")
    public void setObsessOverHost(Boolean obsessOverHost)
    {
        this.obsessOverHost = obsessOverHost;
    }

    public Boolean isCheckFreshness()
    {
        return checkFreshness;
    }
    
    public Boolean resolveCheckFreshness()
    {
        return this.resolveProperty((p) -> { return p.isCheckFreshness(); });
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
    
    public Integer resolveFreshnessThreshold()
    {
        return this.resolveProperty((p) -> { return p.getFreshnessThreshold(); });
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
    
    public String resolveEventHandler()
    {
        return this.resolveProperty((p) -> { return p.getEventHandler(); });
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
    
    public Boolean resolveEventHandlerEnabled()
    {
        return this.resolveProperty((p) -> { return p.isEventHandlerEnabled(); });
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
    
    public Float resolveLowFlapThreshold()
    {
        return this.resolveProperty((p) -> { return p.getLowFlapThreshold(); });
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
    
    public Float resolveHighFlapThreshold()
    {
        return this.resolveProperty((p) -> { return p.getHighFlapThreshold(); });
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
    
    public Boolean resolveFlapDetectionEnabled()
    {
        return this.resolveProperty((p) -> { return p.isFlapDetectionEnabled(); });
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
    
    public List<String> resolveFlapDetectionOptions()
    {
        return this.resolveProperty((p) -> { return p.getFlapDetectionOptions(); });
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
    
    public Boolean resolveProcessPerfData()
    {
        return this.resolveProperty((p) -> { return p.isProcessPerfData(); });
    }

    @ParameterName("process_perf_data")
    public void setProcessPerfData(Boolean processPerfData)
    {
        this.processPerfData = processPerfData;
    }

    public Boolean isRetainStatusInformation()
    {
        return retainStatusInformation;
    }
    
    public Boolean resolveRetainStatusInformation()
    {
        return this.resolveProperty((p) -> { return p.isRetainStatusInformation(); });
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
    
    public Boolean resolveRetainNonstatusInformation()
    {
        return this.resolveProperty((p) -> { return p.isRetainNonstatusInformation(); });
    }

    @ParameterName("retain_nonstatus_information")
    public void setRetainNonstatusInformation(Boolean retainNonstatusInformation)
    {
        this.retainNonstatusInformation = retainNonstatusInformation;
    }

    public List<String> getContacts()
    {
        return contacts;
    }
    
    public List<String> resolveContacts()
    {
        return this.resolveProperty((p) -> { return p.getContacts(); });
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
    
    public List<String> resolveContactGroups()
    {
        return this.resolveProperty((p) -> { return p.getContactGroups(); });
    }

    @ParameterName("contact_groups")
    public void setContactGroups(List<String> contactGroups)
    {
        this.contactGroups = contactGroups;
    }

    public Long getNotificationInterval()
    {
        return notificationInterval;
    }
    
    public Long resolveNotificationInterval()
    {
        return this.resolveProperty((p) -> { return p.getNotificationInterval(); });
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
    
    public Long resolveFirstNotificationDelay()
    {
        return this.resolveProperty((p) -> { return p.getFirstNotificationDelay(); });
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
    
    public String resolveNotificationPeriod()
    {
        return this.resolveProperty((p) -> { return p.getNotificationPeriod(); });
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
    
    public List<String> resolveNotificationOptions()
    {
        return this.resolveProperty((p) -> { return p.getNotificationOptions(); });
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
    
    public Boolean resolveNotificationsEnabled()
    {
        return this.resolveProperty((p) -> { return p.isNotificationsEnabled(); });
    }

    @ParameterName("notifications_enabled")
    public void setNotificationsEnabled(Boolean notificationsEnabled)
    {
        this.notificationsEnabled = notificationsEnabled;
    }

    public List<String> getStalkingOptions()
    {
        return stalkingOptions;
    }
    
    public List<String> resolveStalkingOptions()
    {
        return this.resolveProperty((p) -> { return p.getStalkingOptions(); });
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
    
    public String resolveNotes()
    {
        return this.resolveProperty((p) -> { return p.getNotes(); });
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
    
    public String resolveNotesUrl()
    {
        return this.resolveProperty((p) -> { return p.getNotesUrl(); });
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
    
    public String resolveActionUrl()
    {
        return this.resolveProperty((p) -> { return p.getActionUrl(); });
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
    
    public String resolveIconImage()
    {
        return this.resolveProperty((p) -> { return p.getIconImage(); });
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
    
    public String resolveIconImageAlt()
    {
        return this.resolveProperty((p) -> { return p.getIconImageAlt(); });
    }

    @ParameterName("icon_image_alt")
    public void setIconImageAlt(String iconImageAlt)
    {
        this.iconImageAlt = iconImageAlt;
    }

    public String getVrmlImage()
    {
        return vrmlImage;
    }
    
    public String resolveVrmlImage()
    {
        return this.resolveProperty((p) -> { return p.getVrmlImage(); });
    }

    @ParameterName("vrml_image")
    public void setVrmlImage(String vrmlImage)
    {
        this.vrmlImage = vrmlImage;
    }

    public String getStatusmapImage()
    {
        return statusmapImage;
    }
    
    public String resolveStatusmapImage()
    {
        return this.resolveProperty((p) -> { return p.getStatusmapImage(); });
    }

    @ParameterName("status_map_image")
    public void setStatusmapImage(String statusmapImage)
    {
        this.statusmapImage = statusmapImage;
    }

    public String getCoords2D()
    {
        return coords2D;
    }
    
    public String resolveCoords2D()
    {
        return this.resolveProperty((p) -> { return p.getCoords2D(); });
    }

    @ParameterName("2d_coords")
    public void setCoords2D(String coords2d)
    {
        coords2D = coords2d;
    }

    public String getCoords3D()
    {
        return coords3D;
    }
    
    public String resolveCoords3D()
    {
        return this.resolveProperty((p) -> { return p.getCoords3D(); });
    }

    @ParameterName("3d_coords")
    public void setCoords3D(String coords3d)
    {
        coords3D = coords3d;
    }
    
    // extended
    
    public String getLocation()
    {
        return this.location;
    }
    
    public String resolveLocation()
    {
        return this.resolveProperty((p) -> { return p.getLocation(); });
    }

    @ParameterName("location")
    public void setLocation(String location)
    {
        this.location = location;
    }
    
    public String toString()
    {
        return "host { " + this.hostName + " }";
    }
}
