package com.intrbiz.bergamot.model;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.intrbiz.Util;
import com.intrbiz.bergamot.compat.macro.MacroFrame;
import com.intrbiz.bergamot.model.message.ActiveCheckMO;
import com.intrbiz.bergamot.model.message.task.check.ExecuteCheck;
import com.intrbiz.bergamot.model.stats.ActiveCheckStats;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.converter.Converter;
import com.intrbiz.express.DefaultContext;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressEntityResolver;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.dynamic.DynamicEntity;
import com.intrbiz.express.value.ValueExpression;
import com.intrbiz.validator.Validator;

/**
 * A check which is actively polled
 */
public abstract class ActiveCheck<T extends ActiveCheckMO> extends RealCheck<T>
{
    /**
     * How often should checks be executed (in milliseconds)
     */
    protected long checkInterval = TimeUnit.MINUTES.toMillis(5);

    /**
     * How often should checks be executed when not in an ok state (in milliseconds)
     */
    protected long retryInterval = TimeUnit.MINUTES.toMillis(1);

    /**
     * When should we check, a calendar
     */
    protected TimePeriod timePeriod;

    /**
     * The check command to execute
     */
    protected Command command;

    /**
     * The stats for this active check
     */
    protected ActiveCheckStats stats = new ActiveCheckStats();

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

    public Command getCommand()
    {
        return command;
    }

    public void setCommand(Command command)
    {
        this.command = command;
    }

    public long getCurrentInterval()
    {
        return (this.getState().isOk() && this.getState().isHard()) ? this.getCheckInterval() : this.getRetryInterval();
    }

    public TimePeriod getTimePeriod()
    {
        return timePeriod;
    }

    public void setTimePeriod(TimePeriod timePeriod)
    {
        this.timePeriod = timePeriod;
    }

    public final ExecuteCheck executeCheck()
    {
        if (this.command == null) return null;
        ExecuteCheck executeCheck = new ExecuteCheck();
        executeCheck.setId(UUID.randomUUID());
        executeCheck.setCheckType(this.getType());
        executeCheck.setCheckId(this.getId());
        executeCheck.setEngine(this.command.getEngine());
        executeCheck.setName(this.command.getName());
        // TODO: eval the parameters
        ExpressContext context = this.createExpressContext();
        // configured parameters
        for (Parameter parameter : this.command.getParameters())
        {
            ValueExpression vexp = new ValueExpression(context, parameter.getValue());
            String value = (String) vexp.get(context, this);
            executeCheck.setParameter(parameter.getName(), value);
        }
        executeCheck.setTimeout(30_000L);
        executeCheck.setScheduled(System.currentTimeMillis());
        return executeCheck;
    }
    
    // TODO
    
    protected ExpressContext createExpressContext()
    {
        return new DefaultContext(new ExpressEntityResolver() {
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
                    else if (source instanceof Service)
                    {
                        return ((Service) source).getHost();
                    }
                }
                else if ("service".equals(name))
                {
                    if (source instanceof Service)
                    {
                        return source;
                    }
                }
                else if ("nagios".equals(name))
                {
                    return new DynamicEntity() {
                        @Override
                        public Object get(String name, ExpressContext context, Object source) throws ExpressException
                        {
                            if ("path".equals(name)) return MacroFrame.GLOBAL_MACROS.get("USER1");
                            return MacroFrame.GLOBAL_MACROS.get(name.toUpperCase());
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
                    ActiveCheck<?> check = (ActiveCheck<?>) source;
                    if (check.getCommand() != null)
                    {
                        String param = check.command.getParameter(name);
                        if (param != null)
                        {
                            return param;
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
        if (! stub)
        {
            mo.setCommand(Util.nullable(this.getCommand(), Command::toStubMO));
            mo.setTimePeriod(Util.nullable(this.getTimePeriod(), TimePeriod::toStubMO));
        }
    }

    public ActiveCheckStats getStats()
    {
        return this.stats;
    }
}
