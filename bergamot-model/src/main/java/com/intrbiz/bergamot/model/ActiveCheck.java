package com.intrbiz.bergamot.model;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
public abstract class ActiveCheck extends RealCheck
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
    protected TimePeriod checkPeriod;

    /**
     * The check command to execute
     */
    protected Command checkCommand;

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

    public Command getCheckCommand()
    {
        return checkCommand;
    }

    public void setCheckCommand(Command checkCommand)
    {
        this.checkCommand = checkCommand;
    }

    public long getCurrentInterval()
    {
        return (this.getState().isOk() && this.getState().isHard()) ? this.getCheckInterval() : this.getRetryInterval();
    }

    public TimePeriod getCheckPeriod()
    {
        return checkPeriod;
    }

    public void setCheckPeriod(TimePeriod timePeriod)
    {
        this.checkPeriod = timePeriod;
    }

    public final ExecuteCheck executeCheck()
    {
        if (this.checkCommand == null) return null;
        ExecuteCheck executeCheck = new ExecuteCheck();
        executeCheck.setId(UUID.randomUUID());
        executeCheck.setCheckType(this.getType());
        executeCheck.setCheckId(this.getId());
        executeCheck.setEngine(this.checkCommand.getEngine());
        executeCheck.setName(this.checkCommand.getName());
        // TODO: eval the parameters
        ExpressContext context = this.createExpressContext();
        // configured parameters
        for (Parameter parameter : this.checkCommand.getParameters())
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
                    ActiveCheck check = (ActiveCheck) source;
                    if (check.getCheckCommand() != null)
                    {
                        String param = check.checkCommand.getParameter(name);
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

    protected void toMO(ActiveCheckMO mo)
    {
        super.toMO(mo);
        mo.setCheckInterval(this.getCheckInterval());
        mo.setRetryInterval(this.getRetryInterval());
    }

    public ActiveCheckStats getStats()
    {
        return this.stats;
    }
}
