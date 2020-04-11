package com.intrbiz.bergamot.express.functions;

import com.intrbiz.Util;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.NamedObject;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.operator.Function;

public class ResolveCredentials extends Function
{
    public ResolveCredentials()
    {
        super("resolve_credentials");
    }

    @Override
    public Object get(ExpressContext context, Object source) throws ExpressException
    {
        String credentialsName = this.getCredentialsName(context, source);
        if (Util.isEmpty(credentialsName)) throw new ExpressException("No credential name provided");
        // resolve the credentials name
        credentialsName = this.resolveCredentialsName(credentialsName, source);
        // lookup the credentials
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getCredentialByName(((NamedObject<?,?>) source).getSiteId(), credentialsName);
        }
    }
    
    private String resolveCredentialsName(String credentialsName, Object source)
    {
        Host host = this.getHost(source);
        if (host != null)
        {
            String newCredentialsName = host.getParameter("credentials." + credentialsName);
            if (! Util.isEmpty(newCredentialsName))
            {
                return newCredentialsName;
            }

            newCredentialsName = host.getLocation().getParameter("credentials." + credentialsName);
            if (! Util.isEmpty(newCredentialsName))
            {
                return newCredentialsName;
            }
        }
        return "default." + credentialsName;
    }
    
    private Host getHost(Object source)
    {
        if (source instanceof Host)
        {
            return (Host) source;
        }
        else if (source instanceof Service)
        {
            return ((Service) source).getHost();
        }
        return null;
    }
    
    private String getCredentialsName(ExpressContext context, Object source) throws ExpressException
    {
        try
        {
            return (String) this.getParameter(0).get(context, source);
        }
        catch (Exception e)
        {
            throw new ExpressException("Failed to get parameter", e);
        }
    }

}
