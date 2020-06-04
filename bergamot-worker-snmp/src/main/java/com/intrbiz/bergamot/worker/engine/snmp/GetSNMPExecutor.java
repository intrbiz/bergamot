package com.intrbiz.bergamot.worker.engine.snmp;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.snmp.SNMPContext;
import com.intrbiz.snmp.error.SNMPTimeout;

/**
 * Execute simple SNMP gets
 */
public class GetSNMPExecutor extends AbstractSNMPExecutor
{
    public GetSNMPExecutor()
    {
        super("get");
    }

    @Override
    protected void executeSNMP(ExecuteCheck executeCheck, CheckExecutionContext context, SNMPContext<?> agent) throws Exception
    {
        // we need an OID to get
        if (Util.isEmpty(executeCheck.getParameter("oid"))) throw new RuntimeException("The OID to get must be defined!");
        // get the OID
        agent.getValue(
                executeCheck.getParameter("oid"), 
                (vb) -> {
                    context.publishActiveResult( 
                            new ActiveResult()
                            .info(Util.coalesce(executeCheck.getParameter("prefix"), "") + vb.valueToString() + Util.coalesce(executeCheck.getParameter("suffix"), ""))
                    );
                }, 
                (ex) -> {
                    if (ex instanceof SNMPTimeout)
                    {
                        // timeout
                        context.publishActiveResult( 
                                new ActiveResult()
                                .timeout(ex.getMessage())
                        );
                    }
                    else
                    {
                        // generic error
                        context.publishActiveResult( 
                                new ActiveResult()
                                .error(ex)
                        );
                    }
                }
        );
    }
}
