package com.intrbiz.bergamot.check.http;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.intrbiz.Util;
import com.intrbiz.bergamot.crypto.util.TLSConstants;
import com.intrbiz.bergamot.crypto.util.TLSConstants.CipherInfo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

/**
 * Fluent interface to construct a HTTP check
 */
public abstract class HTTPCheckBuilder
{
    private String address;
    
    private int port = -1;
    
    private int connectTimeout = -1;
    
    private int requestTimeout = -1;
    
    private boolean ssl = false;
    
    private boolean permitInvalidCerts = false;
    
    private HttpVersion version = HttpVersion.HTTP_1_1;
    
    private HttpMethod method = HttpMethod.GET;
    
    private String virtualHost;
    
    private String path = "/";
    
    private HttpHeaders headers = new DefaultHttpHeaders();
    
    private Consumer<HTTPCheckResponse> responseHandler;
    
    private Consumer<Throwable> errorHandler;
    
    private List<String> ciphers = new LinkedList<String>();
    
    private List<String> protocols = new LinkedList<String>();
    
    private String username;
    
    private String password;
    
    private String contentType;
    
    private String content;
    
    public HTTPCheckBuilder()
    {
        super();
    }
    
    public HTTPCheckBuilder connect(String address)
    {
        this.address = address;
        return this;
    }
    
    public HTTPCheckBuilder port(int port)
    {
        this.port = port;
        return this;
    }
    
    public HTTPCheckBuilder connect(String address, int port)
    {
        this.address = address;
        this.port = port;
        return this;
    }
    
    public HTTPCheckBuilder connectTimeout(int connectTimeout)
    {
        this.connectTimeout = connectTimeout;
        return this;
    }
    
    public HTTPCheckBuilder requestTimeout(int requestTimeout)
    {
        this.requestTimeout = requestTimeout;
        return this;
    }
    
    public HTTPCheckBuilder timeout(int connectTimeout, int requestTimeout)
    {
        this.connectTimeout = connectTimeout;
        this.requestTimeout = requestTimeout;
        return this;
    }
    
    public HTTPCheckBuilder http()
    {
        this.ssl = false;
        return this;
    }
    
    public HTTPCheckBuilder https()
    {
        this.ssl = true;
        return this;
    }
    
    public HTTPCheckBuilder ssl()
    {
        this.ssl = true;
        return this;
    }
    
    public HTTPCheckBuilder ssl(boolean ssl)
    {
        this.ssl = ssl;
        return this;
    }
    
    public HTTPCheckBuilder permitInvalidCerts()
    {
        this.permitInvalidCerts = true;
        return this;
    }
    
    public HTTPCheckBuilder permitInvalidCerts(boolean permitInvalidCerts)
    {
        this.permitInvalidCerts = permitInvalidCerts;
        return this;
    }
    
    public HTTPCheckBuilder enabledSSLCiphers(Collection<String> ciphers)
    {
        this.ciphers.addAll(ciphers);
        return this;
    }
    
    public HTTPCheckBuilder enabledSSLCiphers(String... ciphers)
    {
        for (String cipher : ciphers)
        {
            this.ciphers.add(cipher);
        }
        return this;
    }
    
    public HTTPCheckBuilder enableSSLCipher(String cipher)
    {
        this.ciphers.add(cipher);
        return this;
    }
    
    public HTTPCheckBuilder enabledSSLCiphers(CipherInfo... ciphers)
    {
        for (CipherInfo cipher : ciphers)
        {
            this.ciphers.add(cipher.getName());
        }
        return this;
    }
    
    public HTTPCheckBuilder enabledSSLProtocols(Collection<String> protocols)
    {
        this.protocols.addAll(protocols);
        return this;
    }
    
    public HTTPCheckBuilder enabledSSLProtocols(String... protocols)
    {
        for (String protocol : protocols)
        {
            this.protocols.add(protocol);
        }
        return this;
    }
    
    public HTTPCheckBuilder enableSSLProtocol(String protocol)
    {
        this.protocols.add(protocol);
        return this;
    }
    
    public HTTPCheckBuilder enableSSLv3()
    {
        this.protocols.add(TLSConstants.PROTOCOLS.SSLv3);
        return this;
    }
    
    public HTTPCheckBuilder enableTLSv1()
    {
        this.protocols.add(TLSConstants.PROTOCOLS.TLSv1);
        return this;
    }
    
    public HTTPCheckBuilder enableTLSv1_1()
    {
        this.protocols.add(TLSConstants.PROTOCOLS.TLSv1_1);
        return this;
    }
    
    public HTTPCheckBuilder enableTLSv1_2()
    {
        this.protocols.add(TLSConstants.PROTOCOLS.TLSv1_2);
        return this;
    }
    
    public HTTPCheckBuilder version(HttpVersion version)
    {
        this.version = version;
        return this;
    }
    
    public HTTPCheckBuilder http1_0()
    {
        this.version = HttpVersion.HTTP_1_0;
        return this;
    }
    
    public HTTPCheckBuilder http1_1()
    {
        this.version = HttpVersion.HTTP_1_1;
        return this;
    }
    
    public HTTPCheckBuilder method(HttpMethod method)
    {
        this.method = method;
        return this;
    }
    
    public HTTPCheckBuilder path(String path)
    {
        this.path = path;
        return this;
    }
    
    public HTTPCheckBuilder get(String path)
    {
        this.method = HttpMethod.GET;
        this.path = path;
        return this;
    }
    
    public HTTPCheckBuilder get(URL url)
    {
        this.method = HttpMethod.GET;
        this.ssl = "https".equalsIgnoreCase(url.getProtocol());
        this.address = url.getHost();
        this.port = url.getPort();
        this.path = url.getPath();
        return this;
    }
    
    public HTTPCheckBuilder post(String path)
    {
        this.method = HttpMethod.POST;
        this.path = path;
        return this;
    }
    
    public HTTPCheckBuilder post(URL url)
    {
        this.method = HttpMethod.POST;
        this.ssl = "https".equalsIgnoreCase(url.getProtocol());
        this.address = url.getHost();
        this.port = url.getPort();
        this.path = url.getPath();
        return this;
    }
    
    public HTTPCheckBuilder post()
    {
        this.method = HttpMethod.POST;
        this.path = "/";
        return this;
    }
    
    public HTTPCheckBuilder head(String path)
    {
        this.method = HttpMethod.HEAD;
        this.path = path;
        return this;
    }
    
    public HTTPCheckBuilder head(URL url)
    {
        this.method = HttpMethod.GET;
        this.ssl = "https".equalsIgnoreCase(url.getProtocol());
        this.address = url.getHost();
        this.port = url.getPort();
        this.path = url.getPath();
        return this;
    }
    
    public HTTPCheckBuilder head()
    {
        this.method = HttpMethod.HEAD;
        this.path = "/";
        return this;
    }
    
    public HTTPCheckBuilder host(String virtualHost)
    {
        this.virtualHost = virtualHost;
        return this;
    }
    
    public HTTPCheckBuilder header(String name, Object value)
    {
        this.headers.add(name, value);
        return this;
    }
    
    public HTTPCheckBuilder basicAuth(String username, String password)
    {
        this.username = username;
        this.password = password;
        return this;
    }
    
    public HTTPCheckBuilder content(String contentType, String content)
    {
        this.contentType = contentType;
        this.content = content;
        return this;
    }
    
    public HTTPCheckBuilder contentJSON(String content)
    {
        this.contentType = HttpHeaders.Values.APPLICATION_JSON;
        this.content = content;
        return this;
    }
    
    public HTTPCheckBuilder contentFormData(String content)
    {
        this.contentType = HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED;
        this.content = content;
        return this;
    }
    
    public HTTPCheckBuilder contentFormData(Map<String, String> content)
    {
        this.contentType = HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED;
        StringBuilder contentStr = new StringBuilder();
        boolean ns = false;
        for (Entry<String, String> entry : content.entrySet())
        {
            if (ns) contentStr.append('&');
            try
            {
                contentStr.append(URLEncoder.encode(entry.getKey(), "UTF8")).append('=').append(URLEncoder.encode(entry.getValue(), "UTF8"));
            }
            catch (UnsupportedEncodingException e)
            {
            }
            ns = true;
        }
        this.content = contentStr.toString();
        return this;
    }
    
    public HTTPCheckBuilder onResponse(Consumer<HTTPCheckResponse> responseHandler)
    {
        this.responseHandler = responseHandler;
        return this;
    }
    
    public HTTPCheckBuilder onError(Consumer<Throwable> errorHandler)
    {
        this.errorHandler = errorHandler;
        return this;
    }
    
    /* Executors */
    
    public void execute(Consumer<HTTPCheckResponse> responseHandler, Consumer<Throwable> errorHandler)
    {
        this.responseHandler = responseHandler;
        this.errorHandler = errorHandler;
        this.execute();
    }
    
    public void execute()
    {
        // default port
        if (this.port == -1) this.port = this.ssl ? 443 : 80;
        // default virtual host
        if (this.virtualHost == null) this.virtualHost = this.address;
        // content
        ByteBuf content = this.content == null ? Unpooled.buffer(0) : Unpooled.copiedBuffer(this.content, CharsetUtil.UTF_8);
        // build the request
        FullHttpRequest request = new DefaultFullHttpRequest(this.version, this.method, this.path, content);
        // default to connection close
        request.headers().add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        // virtual host
        if (this.port == 80 || this.port == 443) request.headers().add(HttpHeaders.Names.HOST, this.virtualHost);
        else request.headers().add(HttpHeaders.Names.HOST, this.virtualHost + ":" + this.port);
        // user agent
        request.headers().add(HttpHeaders.Names.USER_AGENT, "Bergamot Monitoring Check HTTP 1.0.0");
        // Content Type
        if (this.contentType != null)
        {
            request.headers().add(HttpHeaders.Names.CONTENT_TYPE, this.contentType);
        }
        // Content Length
        if (this.content != null)
        {
            request.headers().add(HttpHeaders.Names.CONTENT_TYPE, request.content().readableBytes());
        }
        // HTTP auth
        if (! Util.isEmpty(this.username))
        {
            String encodedCredentials = new String(Base64.getEncoder().encode((this.username + ":" + Util.coalesce(this.password, "")).getBytes(Util.UTF8)), Util.UTF8);
            request.headers().add(HttpHeaders.Names.AUTHORIZATION, "Basic " + encodedCredentials);
        }
        // add headers
        for (Entry<String, String> e : this.headers)
        {
            request.headers().add(e.getKey(), e.getValue());
        }
        // submit the check
        this.submit(
                this.address, 
                this.port, 
                this.connectTimeout, 
                this.requestTimeout, 
                this.ssl, 
                this.permitInvalidCerts, 
                this.virtualHost, 
                this.protocols, 
                this.ciphers,
                request, 
                this.responseHandler, 
                this.errorHandler
        );
    }
    
    protected abstract void submit(
            final String address, 
            final int port, 
            final int connectTimeout, 
            final int requestTimeout, 
            final boolean ssl, 
            final boolean permitInvalidCerts, 
            final String SNIHost,
            final List<String> protocols,
            final List<String> ciphers, 
            final FullHttpRequest request, 
            final Consumer<HTTPCheckResponse> responseHandler, 
            final Consumer<Throwable> errorHandler
    );
}
