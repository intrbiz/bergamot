package com.intrbiz.bergamot;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Response;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.intrbiz.Util;
import com.intrbiz.bergamot.call.ApplyConfigChangeCall;
import com.intrbiz.bergamot.call.BuildSiteConfigCall;
import com.intrbiz.bergamot.call.GoodbyeCruelWorldCall;
import com.intrbiz.bergamot.call.HelloWorldCall;
import com.intrbiz.bergamot.call.HelloYouCall;
import com.intrbiz.bergamot.call.LookingForSomethingCall;
import com.intrbiz.bergamot.call.SignAgentKey;
import com.intrbiz.bergamot.credentials.BasicCredentials;
import com.intrbiz.bergamot.credentials.ClientCredentials;
import com.intrbiz.bergamot.credentials.TokenCredentials;
import com.intrbiz.bergamot.crypto.util.BergamotTrustManager;
import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.AuthTokenMO;

public abstract class BaseBergamotClient
{
    protected final BergamotTranscoder transcoder = new BergamotTranscoder();
    
    // the API base url
    protected final String baseURL;
    
    // the credentials to authenticate with
    protected ClientCredentials credentials;
    
    // the current auth token
    protected AuthTokenMO authToken;
    
    // customised HTTPClient
    
    protected Registry<ConnectionSocketFactory> schemeRegistry;
    
    protected PoolingHttpClientConnectionManager connectionManager;
    
    protected HttpClient client;
    
    protected Executor executor;
    
    public BaseBergamotClient(String baseURL)
    {
        super();
        // remove trailing /
        if (baseURL.endsWith("/"))
        {
            baseURL = baseURL.substring(0, baseURL.length() - 1);
        }
        this.baseURL = baseURL;
        // setup our executor
        try
        {
            // custom TLS handling
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[] { new BergamotTrustManager(Boolean.getBoolean("bergamot.api.allow-invalid-certs")) }, new SecureRandom());
            // scheme registry
            this.schemeRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
             .register("http",  PlainConnectionSocketFactory.getSocketFactory())
             .register("https", new SSLConnectionSocketFactory(ctx))
             .build();
            // create out connection manager
            this.connectionManager = new PoolingHttpClientConnectionManager(this.schemeRegistry);
            this.connectionManager.setDefaultMaxPerRoute(5);
            this.connectionManager.setMaxTotal(10);
            // create our client
            this.client = HttpClientBuilder.create().setConnectionManager(this.connectionManager).build();
            // create our executor
            this.executor = Executor.newInstance(this.client);
        }
        catch (KeyManagementException | NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Failed to setup HTTP client", e);
        }
    }
    
    public BaseBergamotClient(String baseURL, ClientCredentials credentials)
    {
        this(baseURL);
        this.credentials = credentials;
    }
    
    public BaseBergamotClient(String baseURL, String token)
    {
        this(baseURL);
        this.credentials = new TokenCredentials(token);
    }
    
    public BaseBergamotClient(String baseURL, String username, String password)
    {
        this(baseURL);
        this.credentials = new BasicCredentials(username, password);
    }
    
    public BergamotTranscoder transcoder()
    {
        return this.transcoder;
    }
    
    // HTTP Client Executor
    
    public Executor executor()
    {
        return this.executor;
    }
    
    // the base url
    
    public String getBaseURL()
    {
        return this.baseURL;
    }
    
    // current auth details
    
    public ClientCredentials getCredentials()
    {
        return this.credentials;
    }

    public void setAuthToken(AuthTokenMO authToken)
    {
        this.authToken = authToken;
    }    

    public AuthTokenMO getAuthToken()
    {
        if (this.authTokenExpired())
        {
            this.authToken = this.credentials.auth(this);
        }
        return authToken;
    }
    
    public boolean authTokenExpired()
    {
        return this.authToken == null || (this.authToken.getExpiresAt() > 0 && this.authToken.getExpiresAt() < System.currentTimeMillis());
    }
    
    // create a full URL from the following path element
    
    public String url(String... urlElements)
    {
        StringBuilder sb = new StringBuilder(this.baseURL);
        for (String urlElement : urlElements)
        {
            sb.append(urlElement);
        }
        return sb.toString();
    }
    
    public String appendQuery(String url, NameValuePair... parameters)
    {
        StringBuilder sb = new StringBuilder(url);
        sb.append("?");
        boolean ns = false;
        for (NameValuePair parameter : parameters)
        {
            if (ns) sb.append("&");
            sb.append(Util.urlEncode(parameter.getName(), Util.UTF8)).append("=").append(Util.urlEncode(parameter.getValue(), Util.UTF8));
            ns = true;
        }
        return sb.toString();
    }
    
    public String appendQuery(String url, Iterable<NameValuePair> parameters)
    {
        StringBuilder sb = new StringBuilder(url);
        sb.append("?");
        boolean ns = false;
        for (NameValuePair parameter : parameters)
        {
            if (ns) sb.append("&");
            sb.append(Util.urlEncode(parameter.getName(), Util.UTF8)).append("=").append(Util.urlEncode(parameter.getValue(), Util.UTF8));
            ns = true;
        }
        return sb.toString();
    }
    
    // auth calls
    
    public static class GetAuthTokenCall extends BergamotAPICall<AuthTokenMO>
    {
        private String username;

        private String password;

        public GetAuthTokenCall(BaseBergamotClient client)
        {
            super(client);
        }

        public GetAuthTokenCall username(String username)
        {
            this.username = username;
            return this;
        }

        public GetAuthTokenCall password(String password)
        {
            this.password = password;
            return this;
        }

        public AuthTokenMO execute()
        {
            try
            {
                Response response = execute(
                    post(url("/api/auth-token"))
                    .addHeader(authHeader())
                    .bodyForm(
                        param("username", this.username),
                        param("password", this.password)
                    )
                );
                return transcoder().decodeFromString(response.returnContent().asString(), AuthTokenMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }
    }

    public GetAuthTokenCall callGetAuthToken()
    {
        return new GetAuthTokenCall(this);
    }
    
    // test calls
    
    public HelloWorldCall callHelloWorld()
    {
        return new HelloWorldCall(this);
    }
    
    public HelloYouCall callHelloYou()
    {
        return new HelloYouCall(this);
    }
    
    public GoodbyeCruelWorldCall callGoodbyeCruelWorld()
    {
        return new GoodbyeCruelWorldCall(this);
    }
    
    public LookingForSomethingCall callLookingForSomething()
    {
        return new LookingForSomethingCall(this);
    }
    
    // config
    
    public BuildSiteConfigCall callBuildSiteConfig()
    {
        return new BuildSiteConfigCall(this);
    }
    
    public ApplyConfigChangeCall callApplyConfigChange()
    {
        return new ApplyConfigChangeCall(this);
    }
    
    // agent
    
    public SignAgentKey callSignAgentKey()
    {
        return new SignAgentKey(this);
    }
}
