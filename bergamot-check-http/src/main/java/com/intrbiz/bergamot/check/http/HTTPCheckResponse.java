package com.intrbiz.bergamot.check.http;

import io.netty.handler.codec.http.FullHttpResponse;

import com.intrbiz.Util;
import com.intrbiz.bergamot.crypto.util.TLSInfo;

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
    
    public long runtime()
    {
        return runtime;
    }

    public FullHttpResponse getResponse()
    {
        return response;
    }
    
    public FullHttpResponse response()
    {
        return response;
    }
    
    public int status()
    {
        return this.getResponse().getStatus().code();
    }
    
    public String content()
    {
        return this.getResponse().content().toString(Util.UTF8);
    }
    
    public TLSInfo getTlsInfo()
    {
        return tlsInfo;
    }
    
    public TLSInfo tlsInfo()
    {
        return tlsInfo;
    }

    public String toString()
    {
        return "http-check-response { runtime: " + this.runtime + "ms, status: " + this.response.getStatus() + " }" + (this.tlsInfo == null ? "" : "\n" + this.tlsInfo.toString());
    }
}
