package com.intrbiz.bergamot.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.ActiveCheckCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.ActiveCheckMO;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.converter.Converter;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLVersion;
import com.intrbiz.express.DefaultContext;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressEntityResolver;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.dynamic.DynamicEntity;
import com.intrbiz.express.value.ValueExpression;
import com.intrbiz.queue.name.GenericKey;
import com.intrbiz.validator.Validator;

/**
 * A check which is actively polled
 */
public abstract class ActiveCheck<T extends ActiveCheckMO, C extends ActiveCheckCfg<C>> extends RealCheck<T, C>
{
    private static final long serialVersionUID = 1L;
    
    private transient Logger logger = Logger.getLogger(ActiveCheck.class);

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

    public CheckCommand getCheckCommand()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getCheckCommand(this.getId());
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
     * @return
     */
    public final ExecuteCheck executeCheck()
    {
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
        executeCheck.setEngine(command.getEngine());
        executeCheck.setName(command.getName());
        // TODO: eval the parameters
        ExpressContext context = this.createExpressContext();
        // configured parameters
        for (Parameter parameter : checkCommand.resolveCheckParameters())
        {
            ValueExpression vexp = new ValueExpression(context, parameter.getValue());
            String value = (String) vexp.get(context, this);
            if (logger.isTraceEnabled()) logger.trace("Adding parameter: " + parameter.getName() + " => " + value + " (" + parameter.getValue() + ")");
            executeCheck.setParameter(parameter.getName(), value);
        }
        executeCheck.setTimeout(30_000L);
        executeCheck.setScheduled(System.currentTimeMillis());
        if (logger.isTraceEnabled()) logger.trace("Executing check: " + new BergamotTranscoder().encodeAsString(executeCheck));
        return executeCheck;
    }

    // TODO

    protected ExpressContext createExpressContext()
    {
        return new DefaultContext(new ExpressEntityResolver()
        {
            @Override
            public Object getEntity(String name, Object source)
            {
                if ("this".equals(name))
                {
                    return source;
                }
                else if ("host".equals(name))
                {
                    if (source instanceof Host)
                    {
                        return source;
                    }
                    else if (source instanceof Service) { return ((Service) source).getHost(); }
                }
                else if ("service".equals(name))
                {
                    if (source instanceof Service) { return source; }
                }
                else if ("nagios".equals(name))
                {
                    return new DynamicEntity()
                    {
                        @Override
                        public Object get(String name, ExpressContext context, Object source) throws ExpressException
                        {
                            if ("path".equals(name))
                            {
                                return "/usr/lib/nagios/plugins";
                            }
                            return null;
                        }

                        @Override
                        public void set(String name, Object value, ExpressContext context, Object source) throws ExpressException
                        {
                        }

                        @Override
                        public Converter<?> getConverter(String name, ExpressContext context, Object source) throws ExpressException
                        {
                            return null;
                        }

                        @Override
                        public Validator<?> getValidator(String name, ExpressContext context, Object source) throws ExpressException
                        {
                            return null;
                        }
                    };
                }
                else if (source instanceof ActiveCheck)
                {
                    ActiveCheck<?, ?> check = (ActiveCheck<?, ?>) source;
                    CheckCommand checkCommand = check.getCheckCommand();
                    if (check.getCheckCommand() != null)
                    {
                        for (Parameter param : checkCommand.resolveCheckParameters())
                        {
                            if (name.equals(param.getName())) return param.getValue();
                        }
                    }
                }
                return null;
            }
        });
    }

    protected void toMO(ActiveCheckMO mo, boolean stub)
    {
        super.toMO(mo, stub);
        mo.setCheckInterval(this.getCheckInterval());
        mo.setRetryInterval(this.getRetryInterval());
        mo.setCurrentInterval(this.getCurrentInterval());
        if (!stub)
        {
            mo.setCheckCommand(Util.nullable(this.getCheckCommand(), CheckCommand::toStubMO));
            mo.setTimePeriod(Util.nullable(this.getTimePeriod(), TimePeriod::toStubMO));
        }
    }
    
    /*
     * Serialisation shit
     */    
    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException  
    {  
        is.defaultReadObject();
        this.logger = Logger.getLogger(ActiveCheck.class);
    }
}
