package com.intrbiz.bergamot.worker.engine.snmp;

import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
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
        return "snmp".equals(task.getEngine());
    }
    
    protected abstract void executeSNMP(ExecuteCheck executeCheck, CheckExecutionContext context, SNMPContext<?> agent) throws Exception;

    @Override
    public void execute(ExecuteCheck executeCheck, final CheckExecutionContext context)
    {
        if (logger.isDebugEnabled()) logger.debug("Executing check : " + executeCheck.getEngine() + "::" + executeCheck.getName() + " for " + executeCheck.getCheckType() + " " + executeCheck.getCheckId());
        try
        {
            // validate parameters
            this.validate(executeCheck);
            // open the context
            SNMPContext<?> agent = this.openContext(executeCheck);
            // invoke the specifics of the SNMP check
            this.executeSNMP(executeCheck, context, agent);
        }
        catch (Exception e)
        {
            logger.error("Error executing check", e);
            context.publishActiveResult(new ActiveResult().fromCheck(executeCheck).error(e));
        }
    }
    
    protected void validate(ExecuteCheck executeCheck)
    {
        if (Util.isEmpty(executeCheck.getParameter("host"))) throw new RuntimeException("The host must be defined!");
        if (Util.isEmpty(executeCheck.getParameter("snmp_version"))) throw new RuntimeException("The snmp_version must be defined!");
        SNMPVersion version = SNMPVersion.parse(executeCheck.getParameter("snmp_version"));
        if (version == null) throw new RuntimeException("The snmp_version must be one of: '1', '2c', '3'");
        if (SNMPVersion.V1 == version || SNMPVersion.V2C == version)
        {
            if (Util.isEmpty(executeCheck.getParameter("snmp_community"))) throw new RuntimeException("The snmp_community must be defined!");
        }
        else if (SNMPVersion.V3 == version)
        {
            if (Util.isEmpty(executeCheck.getParameter("snmp_auth"))) throw new RuntimeException("The snmp_auth must be defined!");
            if (SNMPAuthMode.parse(executeCheck.getParameter("snmp_auth")) == null) throw new RuntimeException("The snmp_auth must be one of: 'none', 'sha1', 'md5'");
            if (Util.isEmpty(executeCheck.getParameter("snmp_priv"))) throw new RuntimeException("The snmp_auth must be defined!");
            if (SNMPPrivMode.parse(executeCheck.getParameter("snmp_priv")) == null) throw new RuntimeException("The snmp_priv must be one of: 'none', 'aes128', 'des'");
            if (Util.isEmpty(executeCheck.getParameter("snmp_user"))) throw new RuntimeException("The snmp_user must be defined!");
            if (Util.isEmpty(executeCheck.getParameter("snmp_password"))) throw new RuntimeException("The snmp_password must be defined!");
            // for now require the SNMP engine id for V3
            if (Util.isEmpty(executeCheck.getParameter("snmp_engine_id"))) throw new RuntimeException("The snmp_engine_id must be defined!");
        }
        else
        {
            throw new RuntimeException("Unexpected SNMP version");
        }
    }
    
    protected synchronized SNMPContext<?> openContext(ExecuteCheck executeCheck) throws UnknownHostException
    {
        SNMPVersion version = SNMPVersion.parse(executeCheck.getParameter("snmp_version"));
        String host = executeCheck.getParameter("host");
        if (SNMPVersion.V1 == version)
        {
            return this.getEngine().getTransport().openV1Context(host, executeCheck.getParameter("snmp_community"));
        }
        else if (SNMPVersion.V2C == version)
        {
            return this.getEngine().getTransport().openV2Context(host, executeCheck.getParameter("snmp_community"));
        }
        else if (SNMPVersion.V3 == version)
        {
            String engineId = executeCheck.getParameter("snmp_engine_id");
            SNMPAuthMode auth = SNMPAuthMode.parse(executeCheck.getParameter("snmp_auth"));
            SNMPPrivMode priv = SNMPPrivMode.parse(executeCheck.getParameter("snmp_priv"));
            String user = executeCheck.getParameter("snmp_user");
            String pass = executeCheck.getParameter("snmp_password");
            // open the context
            return this.getEngine().getTransport().openV3Context(host, engineId).setUser(user, auth, priv, pass, pass);
        }
        else
        {
            throw new RuntimeException("Unexpected SNMP version");
        }
    }
}
