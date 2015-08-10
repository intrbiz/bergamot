package com.intrbiz.bergamot.model;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import com.intrbiz.bergamot.config.model.SecuredObjectCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.SecuredObjectMO;
import com.intrbiz.bergamot.model.util.Parameterised;

/**
 * An object which (optionally) exists within a security domain
 */
public abstract class SecuredObject<T extends SecuredObjectMO, C extends SecuredObjectCfg<C>> extends NamedObject<T, C> implements Parameterised
{
    private static final long serialVersionUID = 1L;

    public SecuredObject()
    {
        super();
    }
    
    /**
     * Get the security domains that this object exists within
     */
    public final List<SecurityDomain> getSecurityDomains()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getSecurityDomainsForObject(this.getId());
        }
    }
    
    protected void toMO(SecuredObjectMO mo, Contact contact, EnumSet<MOFlag> options)
    {
        super.toMO(mo, contact, options);
        mo.setSecurityDomains(this.getSecurityDomains().stream().map((sd) -> sd.toMO(contact, options)).collect(Collectors.toList()));
    }
}
