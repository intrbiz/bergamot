package com.intrbiz.bergamot.watcher.engine.snmp;

import java.net.UnknownHostException;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.event.check.CheckEvent;
import com.intrbiz.bergamot.watcher.engine.AbstractExecutors;
import com.intrbiz.snmp.SNMPContext;
import com.intrbiz.snmp.SNMPVersion;
import com.intrbiz.snmp.security.SNMPAuthMode;
import com.intrbiz.snmp.security.SNMPPrivMode;

/**
 * Listen for SNMP traps
 */
public abstract class AbstractSNMPExecutor extends AbstractExecutors<SNMPEngine>
{
    public AbstractSNMPExecutor()
    {
        super();
    }

    /**
     * Only execute Checks where the engine == "snmp"
     */
    @Override
    public boolean accept(CheckEvent check)
    {
        return "snmp".equalsIgnoreCase(check.getEngine());
    }
    
    protected void validate(CheckEvent watchedCheck)
    {
        if (Util.isEmpty(watchedCheck.getParameter("host"))) throw new RuntimeException("The host must be defined!");
        if (Util.isEmpty(watchedCheck.getParameter("snmp-version"))) throw new RuntimeException("The snmp-version must be defined!");
        SNMPVersion version = SNMPVersion.parse(watchedCheck.getParameter("snmp-version"));
        if (version == null) throw new RuntimeException("The snmp-version must be one of: '1', '2c', '3'");
        if (SNMPVersion.V1 == version || SNMPVersion.V2C == version)
        {
            if (Util.isEmpty(watchedCheck.getParameter("snmp-community"))) throw new RuntimeException("The snmp-community must be defined!");
        }
        else if (SNMPVersion.V3 == version)
        {
            if (Util.isEmpty(watchedCheck.getParameter("snmp-auth"))) throw new RuntimeException("The snmp-auth must be defined!");
            if (SNMPAuthMode.parse(watchedCheck.getParameter("snmp-auth")) == null) throw new RuntimeException("The snmp-auth must be one of: 'none', 'sha1', 'md5'");
            if (Util.isEmpty(watchedCheck.getParameter("snmp-priv"))) throw new RuntimeException("The snmp-auth must be defined!");
            if (SNMPPrivMode.parse(watchedCheck.getParameter("snmp-priv")) == null) throw new RuntimeException("The snmp-priv must be one of: 'none', 'aes128', 'des'");
            if (Util.isEmpty(watchedCheck.getParameter("snmp-user"))) throw new RuntimeException("The snmp-user must be defined!");
            if (Util.isEmpty(watchedCheck.getParameter("snmp-password"))) throw new RuntimeException("The snmp-password must be defined!");
            // for now require the SNMP engine id for V3
            if (Util.isEmpty(watchedCheck.getParameter("snmp-engine-id"))) throw new RuntimeException("The snmp-engine-id must be defined!");
        }
    }
    
    protected synchronized SNMPContext<?> openContext(CheckEvent watchedCheck) throws UnknownHostException
    {
        SNMPVersion version = SNMPVersion.parse(watchedCheck.getParameter("snmp-version"));
        String host = watchedCheck.getParameter("host");
        if (SNMPVersion.V1 == version)
        {
            return this.getEngine().getTransport().openV2Context(host, watchedCheck.getParameter("snmp-community"));
        }
        else if (SNMPVersion.V2C == version)
        {
            return this.getEngine().getTransport().openV2Context(host, watchedCheck.getParameter("snmp-community"));
        }
        else if (SNMPVersion.V3 == version)
        {
            String engineId = watchedCheck.getParameter("snmp-engine-id");
            SNMPAuthMode auth = SNMPAuthMode.parse(watchedCheck.getParameter("snmp-auth"));
            SNMPPrivMode priv = SNMPPrivMode.parse(watchedCheck.getParameter("snmp-priv"));
            String user = watchedCheck.getParameter("snmp-user");
            String pass = watchedCheck.getParameter("snmp-password");
            // open the context
            return this.getEngine().getTransport().openV3Context(host, engineId).setUser(user, auth, priv, pass, pass);
        }
        else
        {
            throw new RuntimeException("Invalid SNMP version");
        }
    }
}
