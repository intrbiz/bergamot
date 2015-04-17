package com.intrbiz.bergamot.model;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.ActiveCheckCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.express.BergamotEntityResolver;
import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.ActiveCheckMO;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.bergamot.queue.key.WorkerKey;
import com.intrbiz.bergamot.util.TimeInterval;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLVersion;
import com.intrbiz.express.DefaultContext;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.value.ValueExpression;

/**
 * A check which is actively polled
 */
public abstract class ActiveCheck<T extends ActiveCheckMO, C extends ActiveCheckCfg<C>> extends RealCheck<T, C>
{
    private static final long serialVersionUID = 1L;

    /**
     * How often should checks be executed (in milliseconds)
     */
    @SQLColumn(index = 1, name = "check_interval", since = @SQLVersion({ 1, 0, 0 }))
    protected long checkInterval = TimeUnit.MINUTES.toMillis(5);

    /**
     * How often should checks be executed when not in an ok state (in milliseconds)
     */
    @SQLColumn(index = 2, name = "retry_interval", since = @SQLVersion({ 1, 0, 0 }))
    protected long retryInterval = TimeUnit.MINUTES.toMillis(1);

    /**
     * When should we check, a calendar
     */
    @SQLColumn(index = 3, name = "timeperiod_id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLForeignKey(references = TimePeriod.class, on = "id", onDelete = Action.RESTRICT, onUpdate = Action.RESTRICT, since = @SQLVersion({ 1, 0, 0 }))
    protected UUID timePeriodId;

    @SQLColumn(index = 4, name = "worker_pool", since = @SQLVersion({ 1, 0, 0 }))
    protected String workerPool;

    /**
     * How often should checks be executed when transitioning between hard states (in milliseconds)
     */
    @SQLColumn(index = 5, name = "changing_interval", since = @SQLVersion({ 1, 4, 0 }))
    protected long changingInterval = 0;

    public ActiveCheck()
    {
        super();
    }

    public long getCheckInterval()
    {
        return checkInterval;
    }

    public void setCheckInterval(long checkInterval)
    {
        this.checkInterval = checkInterval;
    }

    public long getRetryInterval()
    {
        return retryInterval;
    }

    public void setRetryInterval(long retryInterval)
    {
        this.retryInterval = retryInterval;
    }

    public long getChangingInterval()
    {
        return changingInterval;
    }

    public void setChangingInterval(long changingInterval)
    {
        this.changingInterval = changingInterval;
    }

    
    /**
     * Get the current check interval for this check, given it's current state
     */
    public long getCurrentInterval()
    {
        return this.computeCurrentInterval(this.getState());
    }
    
    /**
     * Compute the current interval for this check using the 
     * check state and the configured intervals.
     */
    public long computeCurrentInterval(CheckState state)
    {
        if (state.isSoft() && this.getChangingInterval() > 0)
        {
            return this.getChangingInterval();
        }
        else if (state.isHardOk())
        {
            return this.getCheckInterval();
        }
        else
        {
            return this.getRetryInterval();
        }
    }

    public TimePeriod getTimePeriod()
    {
        if (this.getTimePeriodId() == null) return null;
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getTimePeriod(this.getTimePeriodId());
        }
    }

    public UUID getTimePeriodId()
    {
        return timePeriodId;
    }

    public void setTimePeriodId(UUID timePeriodId)
    {
        this.timePeriodId = timePeriodId;
    }

    public String getWorkerPool()
    {
        return workerPool;
    }

    public void setWorkerPool(String workerPool)
    {
        this.workerPool = workerPool;
    }

    public abstract String resolveWorkerPool();
    
    public abstract UUID resolveAgentId();

    /**
     * Construct the routing key which should be used to route this execute check message
     * 
     * @return
     */
    public WorkerKey getRoutingKey()
    {
        Command command = Util.nullable(this.getCheckCommand(), CheckCommand::getCommand);
        if (command == null) return null;
        // the key
        return new WorkerKey(this.getSiteId(), this.resolveWorkerPool(), command.getEngine(), this.resolveAgentId());
    }
    /**
     * Get the TTL in ms for the execute check message
     * 
     * @return
     */
    public long getMessageTTL()
    {
        return this.getCurrentInterval();
    }

    /**
     * Construct the execute check message for this check
     */
    public final ExecuteCheck executeCheck()
    {
        Logger logger = Logger.getLogger(ActiveCheck.class);
        CheckCommand checkCommand = this.getCheckCommand();
        if (checkCommand == null) return null;
        Command command = checkCommand.getCommand();
        if (command == null) return null;
        if (logger.isTraceEnabled()) logger.trace("Executing check of " + this.getType() + "::" + this.getId() + " " + this.getName() + " with command " + command);
        ExecuteCheck executeCheck = new ExecuteCheck();
        executeCheck.setId(UUID.randomUUID());
        executeCheck.setSiteId(this.getSiteId());
        executeCheck.setAgentId(this.resolveAgentId());
        executeCheck.setCheckType(this.getType());
        executeCheck.setCheckId(this.getId());
        executeCheck.setProcessingPool(this.getPool());
        executeCheck.setEngine(command.getEngine());
        executeCheck.setExecutor(command.getExecutor());
        executeCheck.setName(command.getName());
        // eval parameters
        ExpressContext context = new DefaultContext(new BergamotEntityResolver());
        // configured parameters
        for (Parameter parameter : checkCommand.resolveCheckParameters())
        {
            try
            {
                ValueExpression vexp = new ValueExpression(context, parameter.getValue());
                String value = Util.nullable(vexp.get(context, this), Object::toString);
                if (! Util.isEmpty(value)) executeCheck.setParameter(parameter.getName(), value);
            }
            catch (Exception e)
            {
                logger.error("Error computing parameter value, for check: " + this.getType() + "::" + this.getId() + " " + this.getName() , e);
            }
        }
        executeCheck.setTimeout(30_000L);
        executeCheck.setScheduled(System.currentTimeMillis());
        if (logger.isTraceEnabled())logger.trace("Executing check: " + new BergamotTranscoder().encodeAsString(executeCheck));
        return executeCheck;
    }

    protected void toMO(ActiveCheckMO mo, boolean stub)
    {
        super.toMO(mo, stub);
        mo.setCheckInterval(this.getCheckInterval());
        mo.setRetryInterval(this.getRetryInterval());
        mo.setCurrentInterval(this.getCurrentInterval());
        if (!stub)
        {
            mo.setTimePeriod(Util.nullable(this.getTimePeriod(), TimePeriod::toStubMO));
        }
    }
    
    public void configure(C configuration, C resolvedConfiguration)
    {
        super.configure(configuration, resolvedConfiguration);
        // scheduling
        if (resolvedConfiguration.getSchedule() != null)
        {
            this.checkInterval    = resolvedConfiguration.getSchedule().getEveryTimeInterval(TimeInterval.minutes(5)).toMillis();
            this.retryInterval    = resolvedConfiguration.getSchedule().getRetryEveryTimeInterval(TimeInterval.minutes(1)).toMillis();
            this.changingInterval = resolvedConfiguration.getSchedule().getChangingEveryTimeInterval(resolvedConfiguration.getSchedule().getRetryEveryTimeInterval(TimeInterval.minutes(1))).toMillis();
        }
    }
}
