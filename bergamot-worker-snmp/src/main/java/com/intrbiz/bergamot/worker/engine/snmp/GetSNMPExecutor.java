package com.intrbiz.bergamot.worker.engine.snmp;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.snmp.SNMPContext;
import com.intrbiz.snmp.error.SNMPTimeout;

/**
 * Execute simple SNMP gets
 */
public class GetSNMPExecutor extends AbstractSNMPExecutor
{
    public GetSNMPExecutor()
    {
        super();
    }

    /**
     * Where executor == 'get'
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return super.accept(task) && "get".equalsIgnoreCase(task.getExecutor()); 
    }

    @Override
    protected void executeSNMP(ExecuteCheck executeCheck, SNMPContext<?> agent) throws Exception
    {
        // we need an OID to get
        if (Util.isEmpty(executeCheck.getParameter("oid"))) throw new RuntimeException("The OID to get must be defined!");
        // get the OID
        final long start = System.currentTimeMillis();
        agent.getValue(
                executeCheck.getParameter("oid"), 
                (vb) -> {
                    long runtime = System.currentTimeMillis() - start;
                    this.publishActiveResult(executeCheck, 
                            new ActiveResultMO()
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
                        this.publishActiveResult(executeCheck, 
                                new ActiveResultMO()
                                .fromCheck(executeCheck)
                                .timeout(ex.getMessage())
                                .runtime(runtime)
                        );
                    }
                    else
                    {
                        // generic error
                        this.publishActiveResult(executeCheck, 
                                new ActiveResultMO()
                                .fromCheck(executeCheck)
                                .error(ex)
                                .runtime(runtime)
                        );
                    }
                }
        );
    }
}
