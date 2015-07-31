package com.intrbiz.bergamot.check.http;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.intrbiz.Util;
import com.intrbiz.bergamot.crypto.util.TLSInfo;

import io.netty.handler.codec.http.FullHttpResponse;

public class HTTPCheckResponse
{
    private final String url;
    
    private final long runtime;
    
    private final FullHttpResponse response;
    
    private final TLSInfo tlsInfo;
    
    public HTTPCheckResponse(String url, long runtime, FullHttpResponse response, TLSInfo tlsInfo)
    {
        this.url = url;
        this.runtime = runtime;
        this.response = response;
        this.tlsInfo = tlsInfo;
    }
    
    public String getUrl()
    {
        return this.url;
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
    
    /**
     * Assuming the response is a HTML document, parse it using jsoup
     * @return a jsoup Document
     */
    public Document parseHTML()
    {
        return Jsoup.parse(this.content(), this.url);
    }

    public String toString()
    {
        return "http-check-response { runtime: " + this.runtime + "ms, status: " + this.response.getStatus() + " }" + (this.tlsInfo == null ? "" : "\n" + this.tlsInfo.toString());
    }
}
