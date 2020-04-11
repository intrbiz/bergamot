package com.intrbiz.bergamot.express;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.CheckCommand;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.NamedObject;
import com.intrbiz.bergamot.model.RealCheck;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.converter.Converter;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressEntityResolver;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.dynamic.DynamicEntity;
import com.intrbiz.validator.Validator;

public class BergamotEntityResolver extends ExpressEntityResolver
{
    private static final BergamotEntityResolver DEFAULT_INSTANCE = new BergamotEntityResolver();
    
    public static BergamotEntityResolver getDefaultInstance()
    {
        return DEFAULT_INSTANCE;
    }
    
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
        else if ("site".equals(name))
        {
            if (source instanceof NamedObject)
                return ((NamedObject<?,?>) source).getSite();
        }
        else if ("global".equals(name))
        {
            return new DynamicEntity()
            {
                @Override
                public Object get(String name, ExpressContext context, Object source) throws ExpressException
                {
                    // lookup the value as a site parameter
                    Site site = ((NamedObject<?,?>) source).getSite();
                    if (site == null) return null;
                    return site.getParameter(name);
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
        else if ("credential".equals(name) || "credentials".equals(name))
        {
            return new DynamicEntity()
            {
                @Override
                public Object get(String name, ExpressContext context, Object source) throws ExpressException
                {
                    // lookup the credential by name
                    try (BergamotDB db = BergamotDB.connect())
                    {
                        return db.getCredentialByName(((NamedObject<?,?>) source).getSiteId(), name);
                    }
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
            return checkCommand.resolveCheckParameter(name);
        }
        return null;
    }
}
