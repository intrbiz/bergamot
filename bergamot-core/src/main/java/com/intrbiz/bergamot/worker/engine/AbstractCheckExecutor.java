package com.intrbiz.bergamot.worker.engine;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.model.message.task.ExecuteCheck;
import com.intrbiz.bergamot.model.message.task.Task;
import com.intrbiz.bergamot.worker.CheckExecutor;
import com.intrbiz.bergamot.worker.Engine;

public abstract class AbstractCheckExecutor<T extends Engine> extends AbstractExecutor<T> implements CheckExecutor<T>
{
    private Logger logger = Logger.getLogger(AbstractCheckExecutor.class);
    
    public AbstractCheckExecutor()
    {
        super();
    }

    @Override
    public boolean accept(Task task)
    {
        return task instanceof ExecuteCheck;
    }

    @Override
    public void execute(Task task)
    {
        ExecuteCheck executeCheck = (ExecuteCheck) task;
        logger.debug("Executing check: " + executeCheck.getId() + " " + executeCheck.getEngine() + "::" + executeCheck.getName() + " from " + executeCheck.getSender());
        Result result = this.run(executeCheck);
        logger.debug("Publishing result: " + result.getId() + " " + result.isOk() + " " + result.getStatus() + " " + result.getOutput());
        this.getEngine().getWorker().getBergamot().getManifold().publish(result);
    }
}
