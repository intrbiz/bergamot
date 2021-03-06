package com.intrbiz.bergamot.model.message.scheduler;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.SiteMO;

/**
 * Schedule an active check
 */
@JsonTypeName("bergamot.scheduler.check")
public class ScheduleCheck extends SchedulerMessage
{
    private static final long serialVersionUID = 1L;
    
    public enum Command { SCHEDULE, RESCHEDULE, UNSCHEDULE, ENABLE, DISABLE }
    
    @JsonProperty("check_id")
    private UUID checkId;
    
    @JsonProperty("command")
    private Command command;
    
    @JsonProperty("interval")
    private long interval;
    
    public ScheduleCheck()
    {
        super();
    }
    
    public ScheduleCheck(int pool, UUID checkId, Command command)
    {
        super(pool);
        this.checkId = checkId;
        this.command = command;
    }
    
    public ScheduleCheck(int pool, UUID checkId, Command command, long interval)
    {
        this(pool, checkId, command);
        this.interval = interval;
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
    }
    
    public Command getCommand()
    {
        return this.command;
    }

    public void setCommand(Command command)
    {
        this.command = command;
    }

    public long getInterval()
    {
        return this.interval;
    }

    public void setInterval(long interval)
    {
        this.interval = interval;
    }
    
    public UUID getSiteId()
    {
        return SiteMO.getSiteId(this.checkId);
    }
}
