package com.intrbiz.bergamot.model;

import java.util.EnumSet;
import java.util.List;

import com.intrbiz.bergamot.config.model.SecurityDomainCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.SecurityDomainMO;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLUnique;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * A security domain, grouping checks for access controls
 */
@SQLTable(schema = BergamotDB.class, name = "security_domain", since = @SQLVersion({ 3, 8, 0 }))
@SQLUnique(name = "name_unq", columns = { "site_id", "name" })
public class SecurityDomain extends NamedObject<SecurityDomainMO, SecurityDomainCfg>
{
    private static final long serialVersionUID = 1L;

    public SecurityDomain()
    {
        super();
    }

    @Override
    public void configure(SecurityDomainCfg configuration, SecurityDomainCfg resolvedConfiguration)
    {
        super.configure(configuration, resolvedConfiguration);
    }
    
    public List<AccessControl> getAccessControls()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getAccessControlsForSecurityDomain(this.getId());
        }
    }

    public String toString()
    {
        return "SecurityDomain " + this.getName();
    }

    @Override
    public SecurityDomainMO toMO(Contact contact, EnumSet<MOFlag> options)
    {
        SecurityDomainMO mo = new SecurityDomainMO();
        super.toMO(mo, contact, options);
        return null;
    }
}
