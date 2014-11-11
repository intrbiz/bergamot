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
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLVersion;
import com.intrbiz.express.DefaultContext;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.value.ValueExpression;
import com.intrbiz.queue.name.GenericKey;

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
    @SQLForeignKey(references = TimePeriod.class, on = "id", onDelete = Action.RESTRICT, onUpdate = Action.RESTRICT)
    protected UUID timePeriodId;

    @SQLColumn(index = 4, name = "worker_pool", since = @SQLVersion({ 1, 0, 0 }))
    protected String workerPool;

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

    public long getCurrentInterval()
    {
        return (this.getState().isOk() && this.getState().isHard()) ? this.getCheckInterval() : this.getRetryInterval();
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

    /**
     * Construct the routing key which should be used to route this execute check message
     * @return
     */
    public GenericKey getRoutingKey()
    {
        Command command = Util.nullable(this.getCheckCommand(), CheckCommand::getCommand);
        if (command == null) return null;
        // th key
        return new GenericKey(this.getSiteId() +  "." + Util.coalesceEmpty(this.resolveWorkerPool(), "any") +  "." +  command.getEngine());
    }
    
    /**
     * Get the TTL in ms for the execute check message
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
        if (logger.isTraceEnabled()) logger.trace("Executing check of " + this.getId() + " " + this.getName() + " with command " + command);
        ExecuteCheck executeCheck = new ExecuteCheck();
        executeCheck.setId(UUID.randomUUID());
        executeCheck.setSiteId(this.getSiteId());
        executeCheck.setCheckType(this.getType());
        executeCheck.setCheckId(this.getId());
        executeCheck.setProcessingPool(this.getPool());
        executeCheck.setEngine(command.getEngine());
        executeCheck.setExecutor(command.getExecutor());
        executeCheck.setName(command.getName());
        ExpressContext context = new DefaultContext(new BergamotEntityResolver());
        // configured parameters
        for (Parameter parameter : checkCommand.resolveCheckParameters())
        {
            ValueExpression vexp = new ValueExpression(context, parameter.getValue());
            String value = (String) vexp.get(context, this);
            if (Util.isEmpty(value))
            {
                if (logger.isTraceEnabled()) logger.trace("Adding parameter: " + parameter.getName() + " => " + value + " (" + parameter.getValue() + ")");
                executeCheck.setParameter(parameter.getName(), value);
            }
            else
            {
                if (logger.isTraceEnabled()) logger.trace("Skipping null parameter: " + parameter.getName() + " => " + value + " (" + parameter.getValue() + ")");
            }
        }
        executeCheck.setTimeout(30_000L);
        executeCheck.setScheduled(System.currentTimeMillis());
        if (logger.isTraceEnabled()) logger.trace("Executing check: " + new BergamotTranscoder().encodeAsString(executeCheck));
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
}
