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
        final long start = System.currentTimeMillis();
        agent.getValue(
                executeCheck.getParameter("oid"), 
                (vb) -> {
                    long runtime = System.currentTimeMillis() - start;
                    context.publishActiveResult( 
                            new ActiveResult()
                            .fromCheck(executeCheck)
                            .info(Util.coalesce(executeCheck.getParameter("prefix"), "") + vb.valueToString() + Util.coalesce(executeCheck.getParameter("suffix"), ""))
                            .runtime(runtime)
                    );
                }, 
                (ex) -> {
                    long runtime = System.currentTimeMillis() - start;
                    if (ex instanceof SNMPTimeout)
                    {
                        // timeout
                        context.publishActiveResult( 
                                new ActiveResult()
                                .fromCheck(executeCheck)
                                .timeout(ex.getMessage())
                                .runtime(runtime)
                        );
                    }
                    else
                    {
                        // generic error
                        context.publishActiveResult( 
                                new ActiveResult()
                                .fromCheck(executeCheck)
                                .error(ex)
                                .runtime(runtime)
                        );
                    }
                }
        );
    }
}
