package com.intrbiz.bergamot.check.ssh;

import java.util.function.Consumer;

public class SSHCheckContext
{
    private final Consumer<Throwable> onError;
    
    public SSHCheckContext(Consumer<Throwable> onError)
    {
        this.onError = onError;
    }
}
