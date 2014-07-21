package com.intrbiz.bergamot.express;

import com.intrbiz.bergamot.model.CheckCommand;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.RealCheck;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.converter.Converter;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressEntityResolver;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.dynamic.DynamicEntity;
import com.intrbiz.validator.Validator;

public class BergamotEntityResolver extends ExpressEntityResolver
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
                return source;
            else if (source instanceof Service)
                return ((Service) source).getHost();
            else if (source instanceof Trap)
                return ((Trap) source).getHost();
        }
        else if ("service".equals(name))
        {
            if (source instanceof Service)
                return source;
        }
        else if ("trap".equals(name))
        {
            if (source instanceof Trap)
                return source;
        }
        else if ("nagios".equals(name))
        {
            return new DynamicEntity()
            {
                @Override
                public Object get(String name, ExpressContext context, Object source) throws ExpressException
                {
                    if ("path".equals(name))
                        return "/usr/lib/nagios/plugins";
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
        else if (source instanceof RealCheck)
        {
            RealCheck<?, ?> check = (RealCheck<?, ?>) source;
            CheckCommand checkCommand = check.getCheckCommand();
            if (check.getCheckCommand() != null)
            {
                for (Parameter param : checkCommand.resolveCheckParameters())
                {
                    if (name.equals(param.getName())) 
                        return param.getValue();
                }
            }
        }
        return null;
    }
}
