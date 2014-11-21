package com.intrbiz.bergamot.check.tcp;

import java.util.function.Consumer;

/**
 * Fluent interface to construct a HTTP check
 */
public abstract class TCPCheckBuilder
{
    private String address;
    
    private int port = -1;
    
    private int connectTimeout = -1;
    
    private int requestTimeout = -1;
    
    private Consumer<TCPCheckResponse> responseHandler;
    
    private Consumer<Throwable> errorHandler;
    
    public TCPCheckBuilder()
    {
        super();
    }
    
    public TCPCheckBuilder connect(String address)
    {
        this.address = address;
        return this;
    }
    
    public TCPCheckBuilder port(int port)
    {
        this.port = port;
        return this;
    }
    
    public TCPCheckBuilder connect(String address, int port)
    {
        this.address = address;
        this.port = port;
        return this;
    }
    
    public TCPCheckBuilder connectTimeout(int connectTimeout)
    {
        this.connectTimeout = connectTimeout;
        return this;
    }
    
    public TCPCheckBuilder requestTimeout(int requestTimeout)
    {
        this.requestTimeout = requestTimeout;
        return this;
    }
    
    public TCPCheckBuilder timeout(int connectTimeout, int requestTimeout)
    {
        this.connectTimeout = connectTimeout;
        this.requestTimeout = requestTimeout;
        return this;
    }
    
    public TCPCheckBuilder onResponse(Consumer<TCPCheckResponse> responseHandler)
    {
        this.responseHandler = responseHandler;
        return this;
    }
    
    public TCPCheckBuilder onError(Consumer<Throwable> errorHandler)
    {
        this.errorHandler = errorHandler;
        return this;
    }
    
    /* Executors */
    
    public void execute(Consumer<TCPCheckResponse> responseHandler, Consumer<Throwable> errorHandler)
    {
        this.responseHandler = responseHandler;
        this.errorHandler = errorHandler;
        this.execute();
    }
    
    public void execute()
    {
        // submit the check
        this.submit(
                this.address, 
                this.port, 
                this.connectTimeout, 
                this.requestTimeout,  
                this.responseHandler, 
                this.errorHandler
        );
    }
    
    protected abstract void submit(
            final String address, 
            final int port, 
            final int connectTimeout, 
            final int requestTimeout, 
            final Consumer<TCPCheckResponse> responseHandler, 
            final Consumer<Throwable> errorHandler
    );
}
