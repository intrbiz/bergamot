package com.intrbiz.bergamot.model;

import java.util.Map.Entry;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.model.PassiveCheckCfg;
import com.intrbiz.bergamot.express.BergamotEntityResolver;
import com.intrbiz.bergamot.io.BergamotCoreTranscoder;
import com.intrbiz.bergamot.model.message.PassiveCheckMO;
import com.intrbiz.bergamot.model.message.worker.check.RegisterCheck;
import com.intrbiz.bergamot.model.message.worker.check.UnregisterCheck;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.express.DefaultContext;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.value.ValueExpression;

/**
 * A check which is never polled
 */
public abstract class PassiveCheck<T extends PassiveCheckMO, C extends PassiveCheckCfg<C>> extends RealCheck<T,C>
{
    private static final long serialVersionUID = 1L;
    
    public PassiveCheck()
    {
        super();
    }
    
    /**
     * Construct the register check message for this check
     */
    public final RegisterCheck registerCheck()
    {
        Logger logger = Logger.getLogger(PassiveCheck.class);
        CheckCommand checkCommand = this.getCheckCommand();
        if (checkCommand == null) return null;
        Command command = checkCommand.getCommand();
        if (command == null) return null;
        if (logger.isTraceEnabled()) logger.trace("Registering check for " + this.getId() + " " + this.getName() + " with command " + command);
        RegisterCheck registerCheck = new RegisterCheck();
        registerCheck.setId(UUID.randomUUID());
        registerCheck.setSiteId(this.getSiteId());
        registerCheck.setCheckType(this.getType());
        registerCheck.setCheckId(this.getId());
        registerCheck.setEngine(command.getEngine());
        registerCheck.setExecutor(command.getExecutor());
        registerCheck.setName(command.getName());
        ExpressContext context = new DefaultContext(new BergamotEntityResolver());
        // configured parameters
        for (Entry<String, Parameter> parameter : checkCommand.resolveCheckParameters().entrySet())
        {
            try
            {
                ValueExpression vexp = new ValueExpression(context, parameter.getValue().getValue());
                String value = (String) vexp.get(context, this);
                if (logger.isTraceEnabled()) logger.trace("Adding parameter: " + parameter.getKey() + " => " + value + " (" + parameter.getValue().getValue() + ")");
                registerCheck.setParameter(parameter.getKey(), value);
            }
            catch (Exception e)
            {
                logger.error("Error computing parameter value, for check: " + this.getType() + "::" + this.getId() + " " + this.getName() , e);
            }
        }
        if (logger.isTraceEnabled()) logger.trace("Register check: " + new BergamotCoreTranscoder().encodeAsString(registerCheck));
        return registerCheck;
    }
    
    /**
     * Construct the unregister check message for this check
     */
    public final UnregisterCheck unregisterCheck()
    {
        Logger logger = Logger.getLogger(PassiveCheck.class);
        CheckCommand checkCommand = this.getCheckCommand();
        if (checkCommand == null) return null;
        Command command = checkCommand.getCommand();
        if (command == null) return null;
        if (logger.isTraceEnabled()) logger.trace("Unregistering check for " + this.getId() + " " + this.getName() + " with command " + command);
        UnregisterCheck unregisterCheck = new UnregisterCheck();
        unregisterCheck.setId(UUID.randomUUID());
        unregisterCheck.setSiteId(this.getSiteId());
        unregisterCheck.setCheckType(this.getType());
        unregisterCheck.setCheckId(this.getId());
        unregisterCheck.setEngine(command.getEngine());
        unregisterCheck.setExecutor(command.getExecutor());
        unregisterCheck.setName(command.getName());
        ExpressContext context = new DefaultContext(new BergamotEntityResolver());
        // configured parameters
        for (Entry<String, Parameter> parameter : checkCommand.resolveCheckParameters().entrySet())
        {
            try
            {
                ValueExpression vexp = new ValueExpression(context, parameter.getValue().getValue());
                String value = (String) vexp.get(context, this);
                if (logger.isTraceEnabled()) logger.trace("Adding parameter: " + parameter.getKey() + " => " + value + " (" + parameter.getValue().getValue() + ")");
                unregisterCheck.setParameter(parameter.getKey(), value);
            }
            catch (Exception e)
            {
                logger.error("Error computing parameter value, for check: " + this.getType() + "::" + this.getId() + " " + this.getName() , e);
            }
        }
        if (logger.isTraceEnabled()) logger.trace("Unregister check: " + new BergamotCoreTranscoder().encodeAsString(unregisterCheck));
        return unregisterCheck;
    }
}
