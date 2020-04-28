package com.intrbiz.bergamot.cluster.consumer;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public interface BaseConsumer<T>
{
    UUID getId();
    
    long getSequence();
    
    long getTailSequence();
    
    long getHeadSequence();
    
    boolean start(Executor executor, Consumer<T> consumer);
    
    void stop();
    
    void drainTo(Consumer<T> consumer);
    
    void destroy();
}
