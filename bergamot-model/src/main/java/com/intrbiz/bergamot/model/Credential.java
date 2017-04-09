package com.intrbiz.bergamot.model;

import java.util.EnumSet;

import com.intrbiz.bergamot.config.model.CredentialCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.CredentialMO;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLUnique;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * A credential what will most likely be used by a check
 */
@SQLTable(schema = BergamotDB.class, name = "credential", since = @SQLVersion({ 3, 46, 0 }))
@SQLUnique(name = "name_unq", columns = { "site_id", "name" })
public class Credential extends SecuredObject<CredentialMO, CredentialCfg>
{
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 1, name = "username", since = @SQLVersion({ 3, 46, 0 }))
    protected String username;

    @SQLColumn(index = 2, name = "password", since = @SQLVersion({ 3, 46, 0 }))
    protected String password;

    @SQLColumn(index = 3, name = "key_id", since = @SQLVersion({ 3, 46, 0 }))
    protected String keyId;

    @SQLColumn(index = 4, name = "key_secret", since = @SQLVersion({ 3, 46, 0 }))
    protected String keySecret;

    public Credential()
    {
        super();
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getKeyId()
    {
        return keyId;
    }

    public void setKeyId(String keyId)
    {
        this.keyId = keyId;
    }

    public String getKeySecret()
    {
        return keySecret;
    }

    public void setKeySecret(String keySecret)
    {
        this.keySecret = keySecret;
    }

    @Override
    public void configure(CredentialCfg configuration, CredentialCfg resolvedConfiguration)
    {
        super.configure(configuration, resolvedConfiguration);
        this.username = resolvedConfiguration.getUsername();
        this.password = resolvedConfiguration.getPassword();
        this.keyId = resolvedConfiguration.getKeyId();
        this.keySecret = resolvedConfiguration.getKeySecret();
    }

    @Override
    public CredentialMO toMO(Contact contact, EnumSet<MOFlag> options)
    {
        CredentialMO mo = new CredentialMO();
        super.toMO(mo, contact, options);
        mo.setUsername(this.username);
        mo.setPassword(this.password);
        mo.setKeyId(this.keyId);
        mo.setKeySecret(this.keySecret);
        return mo;
    }
}
