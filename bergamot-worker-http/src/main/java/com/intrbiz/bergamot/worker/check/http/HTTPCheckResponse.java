package com.intrbiz.bergamot.worker.check.http;

import io.netty.handler.codec.http.FullHttpResponse;

public class HTTPCheckResponse
{
    private final long runtime;
    
    private final FullHttpResponse response;
    
    public HTTPCheckResponse(long runtime, FullHttpResponse response)
    {
        this.runtime = runtime;
        this.response = response;
    }

    public long getRuntime()
    {
        return runtime;
    }

    public FullHttpResponse getResponse()
    {
        return response;
    }
    
    public String toString()
    {
        return "http-check-response { runtime: " + this.runtime + "ms, status: " + this.response.getStatus() + " }";
    }
}
