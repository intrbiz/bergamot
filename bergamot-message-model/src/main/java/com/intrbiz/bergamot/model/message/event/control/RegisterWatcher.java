package com.intrbiz.bergamot.model.message.event.control;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Register a watcher with Bergamot
 */
@JsonTypeName("bergamot.register_watcher")
public class RegisterWatcher extends ControlEvent
{
    /**
     * The transient id of the watcher
     */
    @JsonProperty("watcher")
    private UUID watcher;

    /**
     * The name of the engine inside the watcher (eg: SNMP, LibVirt, etc)
     */
    @JsonProperty("engine")
    private String engine;

    /**
     * The site the watcher is registering for
     */
    @JsonProperty("site")
    private UUID site;

    /**
     * The location the watcher is registering for
     */
    @JsonProperty("location")
    private UUID location;

    public RegisterWatcher()
    {
        super();
    }

    public RegisterWatcher(UUID watcher, String engine, UUID site, UUID location)
    {
        super();
        this.watcher = watcher;
        this.engine = engine;
        this.site = site;
        this.location = location;
    }

    public UUID getWatcher()
    {
        return watcher;
    }

    public void setWatcher(UUID watcher)
    {
        this.watcher = watcher;
    }

    public String getEngine()
    {
        return engine;
    }

    public void setEngine(String engine)
    {
        this.engine = engine;
    }

    public UUID getSite()
    {
        return site;
    }

    public void setSite(UUID site)
    {
        this.site = site;
    }

    public UUID getLocation()
    {
        return location;
    }

    public void setLocation(UUID location)
    {
        this.location = location;
    }
}
