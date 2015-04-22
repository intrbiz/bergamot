package com.intrbiz.bergamot.worker.engine.snmp;

import java.net.UnknownHostException;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.snmp.SNMPContext;
import com.intrbiz.snmp.SNMPVersion;
import com.intrbiz.snmp.security.SNMPAuthMode;
import com.intrbiz.snmp.security.SNMPPrivMode;

/**
 * Generic SNMP check handling
 */
public abstract class AbstractSNMPExecutor extends AbstractExecutor<SNMPEngine>
{
    private Logger logger = Logger.getLogger(AbstractSNMPExecutor.class);

    public AbstractSNMPExecutor()
    {
        super();
    }

    /**
     * Only execute Checks where the engine == "snmp"
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return super.accept(task) && "snmp".equals(task.getEngine());
    }
    
    protected abstract void executeSNMP(ExecuteCheck executeCheck, Consumer<ResultMO> resultSubmitter, SNMPContext<?> agent) throws Exception;

    @Override
    public void execute(ExecuteCheck executeCheck, Consumer<ResultMO> resultSubmitter)
    {
        if (logger.isDebugEnabled()) logger.debug("Executing check : " + executeCheck.getEngine() + "::" + executeCheck.getName() + " for " + executeCheck.getCheckType() + " " + executeCheck.getCheckId());
        try
        {
            // validate parameters
            this.validate(executeCheck);
            // open the context
            SNMPContext<?> agent = this.openContext(executeCheck);
            // invoke the specifics of the SNMP check
            this.executeSNMP(executeCheck, resultSubmitter, agent);
        }
        catch (Exception e)
        {
            logger.error("Error executing check", e);
            resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).error(e));
        }
    }
    
    protected void validate(ExecuteCheck executeCheck)
    {
        if (Util.isEmpty(executeCheck.getParameter("host"))) throw new RuntimeException("The host must be defined!");
        if (Util.isEmpty(executeCheck.getParameter("snmp-version"))) throw new RuntimeException("The snmp-version must be defined!");
        SNMPVersion version = SNMPVersion.parse(executeCheck.getParameter("snmp-version"));
        if (version == null) throw new RuntimeException("The snmp-version must be one of: '1', '2c', '3'");
        if (SNMPVersion.V1 == version || SNMPVersion.V2C == version)
        {
            if (Util.isEmpty(executeCheck.getParameter("snmp-community"))) throw new RuntimeException("The snmp-community must be defined!");
        }
        else if (SNMPVersion.V3 == version)
        {
            if (Util.isEmpty(executeCheck.getParameter("snmp-auth"))) throw new RuntimeException("The snmp-auth must be defined!");
            if (SNMPAuthMode.parse(executeCheck.getParameter("snmp-auth")) == null) throw new RuntimeException("The snmp-auth must be one of: 'none', 'sha1', 'md5'");
            if (Util.isEmpty(executeCheck.getParameter("snmp-priv"))) throw new RuntimeException("The snmp-auth must be defined!");
            if (SNMPPrivMode.parse(executeCheck.getParameter("snmp-priv")) == null) throw new RuntimeException("The snmp-priv must be one of: 'none', 'aes128', 'des'");
            if (Util.isEmpty(executeCheck.getParameter("snmp-user"))) throw new RuntimeException("The snmp-user must be defined!");
            if (Util.isEmpty(executeCheck.getParameter("snmp-password"))) throw new RuntimeException("The snmp-password must be defined!");
            // for now require the SNMP engine id for V3
            if (Util.isEmpty(executeCheck.getParameter("snmp-engine-id"))) throw new RuntimeException("The snmp-engine-id must be defined!");
        }
        else
        {
            throw new RuntimeException("Unexpected SNMP version");
        }
    }
    
    protected synchronized SNMPContext<?> openContext(ExecuteCheck executeCheck) throws UnknownHostException
    {
        SNMPVersion version = SNMPVersion.parse(executeCheck.getParameter("snmp-version"));
        String host = executeCheck.getParameter("host");
        if (SNMPVersion.V1 == version)
        {
            return this.getEngine().getTransport().openV1Context(host, executeCheck.getParameter("snmp-community"));
        }
        else if (SNMPVersion.V2C == version)
        {
            return this.getEngine().getTransport().openV2Context(host, executeCheck.getParameter("snmp-community"));
        }
        else if (SNMPVersion.V3 == version)
        {
            String engineId = executeCheck.getParameter("snmp-engine-id");
            SNMPAuthMode auth = SNMPAuthMode.parse(executeCheck.getParameter("snmp-auth"));
            SNMPPrivMode priv = SNMPPrivMode.parse(executeCheck.getParameter("snmp-priv"));
            String user = executeCheck.getParameter("snmp-user");
            String pass = executeCheck.getParameter("snmp-password");
            // open the context
            return this.getEngine().getTransport().openV3Context(host, engineId).setUser(user, auth, priv, pass, pass);
        }
        else
        {
            throw new RuntimeException("Unexpected SNMP version");
        }
    }
}
