package com.intrbiz.bergamot;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;

import com.intrbiz.bergamot.io.BergamotTranscoder;

public abstract class BergamotAPICall<T>
{
    private BergamotClient client;
    
    public BergamotAPICall(BergamotClient client)
    {
        this.client = client;
    }
    
    protected BergamotClient client()
    {
        return this.client;
    }
    
    protected BergamotTranscoder transcoder()
    {
        return this.client.transcoder();
    }
    
    protected String url(String... urlElements)
    {
        return this.client.url(urlElements);
    }
    
    protected String appendQuery(String url, NameValuePair... parameters)
    {
        return this.client.appendQuery(url, parameters);
    }
    
    protected String appendQuery(String url, Iterable<NameValuePair> parameters)
    {
        return this.client.appendQuery(url, parameters);
    }
    
    protected String authToken()
    {
        return this.client.getAuthToken().getToken();
    }
    
    protected Header authHeader()
    {
        return new BasicHeader("X-Bergamot-Auth", this.authToken());
    }
    
    /**
     * Execute this call
     */
    public abstract T execute();
}
