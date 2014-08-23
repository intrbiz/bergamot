package com.intrbiz.bergamot.worker.engine.snmp;

import java.net.UnknownHostException;
import java.util.function.Consumer;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.bergamot.worker.engine.snmp.script.BergamotScriptContext;
import com.intrbiz.scripting.RestrictedScriptEngineManager;
import com.intrbiz.snmp.SNMPContext;
import com.intrbiz.snmp.SNMPVersion;
import com.intrbiz.snmp.security.SNMPAuthMode;
import com.intrbiz.snmp.security.SNMPPrivMode;

/**
 * Execute SNMP checks
 */
public class SNMPExecutor extends AbstractExecutor<SNMPEngine>
{
    private Logger logger = Logger.getLogger(SNMPExecutor.class);
    
    private ScriptEngineManager factory = new RestrictedScriptEngineManager();

    public SNMPExecutor()
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

    @Override
    public void execute(ExecuteCheck executeCheck, Consumer<Result> resultSubmitter)
    {
        logger.debug("Executing check : " + executeCheck.getEngine() + "::" + executeCheck.getName() + " for " + executeCheck.getCheckType() + " " + executeCheck.getCheckId());
        try
        {
            // validate parameters
            this.validate(executeCheck);
            // open the context
            SNMPContext<?> agent = this.openContext(executeCheck);
            // setup wrapped context
            SNMPContext<?> wrapped = agent.with((error) -> { resultSubmitter.accept(new Result().error(error)); });
            // setup the script engine
            ScriptEngine script = factory.getEngineByName("nashorn");
            SimpleBindings bindings = new SimpleBindings();
            bindings.put("check", executeCheck);
            bindings.put("agent", wrapped);
            bindings.put("bergamot", new BergamotScriptContext(executeCheck, resultSubmitter));
            script.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
            // execute
            script.eval(executeCheck.getParameter("script"));
        }
        catch (Exception e)
        {
            logger.error("Error executing check", e);
            resultSubmitter.accept(new Result().fromCheck(executeCheck).error(e));
        }
    }
    
    private void validate(ExecuteCheck executeCheck)
    {
        if (Util.isEmpty(executeCheck.getParameter("host"))) throw new RuntimeException("The host must be defined!");
        if (Util.isEmpty(executeCheck.getParameter("script"))) throw new RuntimeException("The script must be defined!");
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
    }
    
    private synchronized SNMPContext<?> openContext(ExecuteCheck executeCheck) throws UnknownHostException
    {
        SNMPVersion version = SNMPVersion.parse(executeCheck.getParameter("snmp-version"));
        String host = executeCheck.getParameter("host");
        if (SNMPVersion.V1 == version)
        {
            return this.getEngine().getTransport().openV2Context(host, executeCheck.getParameter("snmp-community"));
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
            throw new RuntimeException("Invalid SNMP version");
        }
    }
}
