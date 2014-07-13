package com.intrbiz.bergamot.worker.engine.snmp.script;

import java.util.function.Consumer;

import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.Result;

public class BergamotScriptContext
{
    private final ExecuteCheck executeCheck;
    
    private final Consumer<Result> publishResult;
    
    public BergamotScriptContext(ExecuteCheck executeCheck, Consumer<Result> publishResult)
    {
        this.executeCheck = executeCheck;
        this.publishResult = publishResult;
    }

    public ExecuteCheck getExecuteCheck()
    {
        return executeCheck;
    }
    
    public ExecuteCheck getCheck()
    {
        return executeCheck;
    }
    
    public void ok(String message)
    {
        this.publish(new Result().fromCheck(this.executeCheck).ok(message));
    }
    
    public void warning(String message)
    {
        this.publish(new Result().fromCheck(this.executeCheck).warning(message));
    }
    
    public void critical(String message)
    {
        this.publish(new Result().fromCheck(this.executeCheck).critical(message));
    }
    
    public void unknown(String message)
    {
        this.publish(new Result().fromCheck(this.executeCheck).unknown(message));
    }
    
    public void error(String message)
    {
        this.publish(new Result().fromCheck(this.executeCheck).error(message));
    }
    
    public void error(Throwable error)
    {
        this.publish(new Result().fromCheck(this.executeCheck).error(error));
    }
    
    public void timeout(String message)
    {
        this.publish(new Result().fromCheck(this.executeCheck).timeout(message));
    }
    
    public void publish(Result result)
    {
        this.publishResult.accept(result);
    }
    
    public Result createResult()
    {
        return new Result().fromCheck(this.getCheck());
    }
}
