package com.intrbiz.bergamot;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import com.intrbiz.bergamot.io.BergamotTranscoder;

public abstract class BergamotAPICall<T>
{
    private BaseBergamotClient client;
    
    public BergamotAPICall(BaseBergamotClient client)
    {
        this.client = client;
    }
    
    protected BaseBergamotClient client()
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
    
    protected NameValuePair param(String name, String value)
    {
        return new BasicNameValuePair(name, value);
    }
    
    protected NameValuePair param(String name, Object value)
    {
        return new BasicNameValuePair(name, String.valueOf(value));
    }
    
    protected Response execute(Request request) throws ClientProtocolException, IOException
    {
        return this.client.executor().execute(request);
    }
    
    protected Request get(String url)
    {
        return Request.Get(url);
    }
    
    protected Request post(String url)
    {
        return Request.Post(url);
    }
    
    protected Request put(String url)
    {
        return Request.Put(url);
    }
    
    protected Request delte(String url)
    {
        return Request.Delete(url);
    }
    
    /**
     * Execute this call
     */
    public abstract T execute() throws BergamotAPIException;
}
