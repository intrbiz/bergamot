package com.intrbiz.bergamot.worker.check.http;

import io.netty.handler.codec.http.FullHttpResponse;

public class HTTPCheckResponse
{
    private final long runtime;
    
    private final FullHttpResponse response;
    
    private final TLSInfo tlsInfo;
    
    public HTTPCheckResponse(long runtime, FullHttpResponse response, TLSInfo tlsInfo)
    {
        this.runtime = runtime;
        this.response = response;
        this.tlsInfo = tlsInfo;
    }

    public long getRuntime()
    {
        return runtime;
    }

    public FullHttpResponse getResponse()
    {
        return response;
    }
    
    public TLSInfo getTlsInfo()
    {
        return tlsInfo;
    }

    public String toString()
    {
        return "http-check-response { runtime: " + this.runtime + "ms, status: " + this.response.getStatus() + " }" + (this.tlsInfo == null ? "" : "\n" + this.tlsInfo.toString());
    }
}
