package com.intrbiz.bergamot.worker.runner;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.model.message.task.Check;
import com.intrbiz.bergamot.model.message.task.Task;
import com.intrbiz.bergamot.worker.CheckRunner;

public abstract class AbstractCheckRunner extends AbstractRunner implements CheckRunner
{
    private Logger logger = Logger.getLogger(AbstractCheckRunner.class);
    
    public AbstractCheckRunner()
    {
        super();
    }

    @Override
    public boolean accept(Task task)
    {
        return task instanceof Check;
    }

    @Override
    public void execute(Task task)
    {
        Check check = (Check) task;
        logger.debug("Executing check: " + check.getId() + " " + check.getEngine() + "::" + check.getName() + " from " + check.getSender());
        Result result = this.run(check);
        logger.debug("Publishing result: " + result.getId() + " " + result.isOk() + " " + result.getStatus() + " " + result.getOutput());
        this.getWorker().getBergamot().getManifold().publish(result);
    }
}
