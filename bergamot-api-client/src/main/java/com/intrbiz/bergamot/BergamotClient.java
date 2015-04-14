package com.intrbiz.bergamot;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.intrbiz.Util;
import com.intrbiz.bergamot.call.agent.SignAgentKey;
import com.intrbiz.bergamot.call.alert.GetAlertsCall;
import com.intrbiz.bergamot.call.auth.AppAuthTokenCall;
import com.intrbiz.bergamot.call.auth.AuthTokenCall;
import com.intrbiz.bergamot.call.auth.ExtendAuthTokenCall;
import com.intrbiz.bergamot.call.config.BuildSiteConfigCall;
import com.intrbiz.bergamot.call.contact.GetContactByEmailCall;
import com.intrbiz.bergamot.call.contact.GetContactByNameCall;
import com.intrbiz.bergamot.call.contact.GetContactByNameOrEmailCall;
import com.intrbiz.bergamot.call.contact.GetContactCall;
import com.intrbiz.bergamot.call.contact.GetContactConfigByNameCall;
import com.intrbiz.bergamot.call.contact.GetContactConfigCall;
import com.intrbiz.bergamot.call.contact.GetContactsCall;
import com.intrbiz.bergamot.call.test.GoodbyeCruelWorldCall;
import com.intrbiz.bergamot.call.test.HelloWorldCall;
import com.intrbiz.bergamot.call.test.HelloYouCall;
import com.intrbiz.bergamot.call.test.LookingForSomethingCall;
import com.intrbiz.bergamot.credentials.BasicCredentials;
import com.intrbiz.bergamot.credentials.ClientCredentials;
import com.intrbiz.bergamot.credentials.TokenCredentials;
import com.intrbiz.bergamot.crypto.util.BergamotTrustManager;
import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.AuthTokenMO;

public class BergamotClient
{
    private final BergamotTranscoder transcoder = new BergamotTranscoder();
    
    // the API base url
    private final String baseURL;
    
    // the credentials to authenticate with
    private ClientCredentials credentials;
    
    // the current auth token
    private AuthTokenMO authToken;
    
    // customised HTTPClient
    
    private Registry<ConnectionSocketFactory> schemeRegistry;
    
    private PoolingHttpClientConnectionManager connectionManager;
    
    private HttpClient client;
    
    private Executor executor;
    
    public BergamotClient(String baseURL)
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
    
    public BergamotClient(String baseURL, ClientCredentials credentials)
    {
        this(baseURL);
        this.credentials = credentials;
    }
    
    public BergamotClient(String baseURL, String token)
    {
        this(baseURL);
        this.credentials = new TokenCredentials(token);
    }
    
    public BergamotClient(String baseURL, String username, String password)
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
    
    // calls
    
    // auth
    
    public AuthTokenCall authToken()
    {
        return new AuthTokenCall(this);
    }
    
    public ExtendAuthTokenCall extendAuthToken()
    {
        return new ExtendAuthTokenCall(this);
    }
    
    public AppAuthTokenCall appAuthToken()
    {
        return new AppAuthTokenCall(this);
    }
    
    // test calls
    
    public HelloWorldCall helloWorld()
    {
        return new HelloWorldCall(this);
    }
    
    public HelloYouCall helloYou()
    {
        return new HelloYouCall(this);
    }
    
    public GoodbyeCruelWorldCall goodbyeCruelWorld()
    {
        return new GoodbyeCruelWorldCall(this);
    }
    
    public LookingForSomethingCall lookingForSomething()
    {
        return new LookingForSomethingCall(this);
    }
    
    // config
    
    public BuildSiteConfigCall buildSiteConfig()
    {
        return new BuildSiteConfigCall(this);
    }
    
    // contacts
    
    public GetContactsCall getContacts()
    {
        return new GetContactsCall(this);
    }
    
    public GetContactCall getContact()
    {
        return new GetContactCall(this);
    }
    
    public GetContactByNameCall getContactByName()
    {
        return new GetContactByNameCall(this);
    }
    
    public GetContactByEmailCall getContactByEmail()
    {
        return new GetContactByEmailCall(this);
    }
    
    public GetContactByNameOrEmailCall getContactByNameOrEmail()
    {
        return new GetContactByNameOrEmailCall(this);
    }
    
    public GetContactConfigCall getContactConfig()
    {
        return new GetContactConfigCall(this);
    }
    
    public GetContactConfigByNameCall getContactConfigByName()
    {
        return new GetContactConfigByNameCall(this);
    }
    
    // alerts
    
    public GetAlertsCall getAlerts()
    {
        return new GetAlertsCall(this);
    }
    
    // agent
    
    public SignAgentKey signAgentKey()
    {
        return new SignAgentKey(this);
    }
}
