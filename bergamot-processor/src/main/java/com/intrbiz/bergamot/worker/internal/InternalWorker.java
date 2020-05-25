package com.intrbiz.bergamot.worker.internal;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

import com.intrbiz.Util;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.WorkerMessage;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.processor.Locator;
import com.intrbiz.bergamot.worker.internal.executor.NotifierExecutor;
import com.intrbiz.bergamot.worker.internal.executor.ProxiesExecutor;
import com.intrbiz.bergamot.worker.internal.executor.WorkerExecutor;

public class InternalWorker
{
    public static final String NAME = "internal";
    
    private final ConcurrentMap<String, InternalExecutor> executors = new ConcurrentHashMap<>();
    
    private final Executor executorService;
    
    private final ProcessorDispatcher dispatcher;
    
    private final Locator locator;
    
    public InternalWorker(Executor executorService, ProcessorDispatcher dispatcher, Locator locator)
    {
        super();
        this.executorService = Objects.requireNonNull(executorService);
        this.dispatcher = Objects.requireNonNull(dispatcher);
        this.locator = (Objects.requireNonNull(locator));
        // register default executors
        this.registerExecutor(new WorkerExecutor());
        this.registerExecutor(new NotifierExecutor());
        this.registerExecutor(new ProxiesExecutor());
    }
    
    public void registerExecutor(InternalExecutor executor)
    {
        this.executors.put(executor.getName(), executor);
    }
    
    public void processMessage(WorkerMessage message)
    {
        if (message instanceof ExecuteCheck)
        {
            this.executorService.execute(() -> this.executeCheck((ExecuteCheck) message));
        }
    }
    
    protected void executeCheck(ExecuteCheck check)
    {
        InternalExecutor executor = this.executors.get(Util.coalesce(check.getExecutor(), ""));
        if (executor != null)
        {
            try
            {
                executor.execute(check, this.createContext(check));
            }
            catch (Exception e)
            {
                this.dispatcher.dispatch(new ActiveResult().fromCheck(check).error(e.getMessage()).withProcessorId(check.getProcessorId()));
            }
        }
        else
        {
            this.dispatcher.dispatch(new ActiveResult().fromCheck(check).error("No such executor: " + check.getExecutor()).withProcessorId(check.getProcessorId()));
        }
    }
    
    protected InternalExecutorContext createContext(final ExecuteCheck check)
    {
        return new InternalExecutorContext()
        {
            @Override
            public Locator getLocator()
            {
                return locator;
            }

            @Override
            public void publishResult(ActiveResult result)
            {
                dispatcher.dispatch(result.fromCheck(check).withProcessorId(check.getProcessorId()));
            }

            @Override
            public void publishReading(ReadingParcelMessage reading)
            {
                reading.fromCheck(check);
                reading.setProcessorId(check.getProcessorId());
                dispatcher.dispatch(reading);
            }
        };
    }
    
}
