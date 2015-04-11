package com.intrbiz.bergamot.worker.engine.dummy;

import java.util.function.Consumer;

import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;

/**
 * Execute static dummy checks
 */
public class StaticExecutor extends AbstractExecutor<DummyEngine>
{
    public static final String NAME = "static";

    public StaticExecutor()
    {
        super();
    }
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return super.accept(task) && DummyEngine.NAME.equals(task.getEngine()) && NAME.equals(task.getExecutor());
    }

    @Override
    public void execute(ExecuteCheck executeCheck, Consumer<ResultMO> resultSubmitter)
    {
        long start = System.nanoTime();
        ActiveResultMO result = new ActiveResultMO().fromCheck(executeCheck);
        result.setOk(executeCheck.getBooleanParameter("ok", true));
        result.setStatus(executeCheck.getParameter("status", "OK"));
        result.setOutput(executeCheck.getParameter("output", ""));
        result.setRuntime(((double)(System.nanoTime() - start)) / 1_000_000D);
        resultSubmitter.accept(result);
    }
}
