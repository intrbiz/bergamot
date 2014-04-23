package com.intrbiz.bergamot.worker;

import com.intrbiz.bergamot.config.RunnerCfg;
import com.intrbiz.bergamot.model.message.task.Task;
import com.intrbiz.configuration.Configurable;

/**
 * Runners execute a particular task.
 * 
 * Note: runners must be thread safe!
 *
 * @param <V> the result type of the task
 * @param <T> the task type
 */
public interface Runner extends Configurable<RunnerCfg>
{
    Worker getWorker();
    
    void setWorker(Worker worker);
    
    boolean accept(Task task);
    
    void execute(Task task);
}
