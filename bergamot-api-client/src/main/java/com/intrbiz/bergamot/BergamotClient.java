package com.intrbiz.bergamot;

import org.apache.http.NameValuePair;

import com.intrbiz.Util;
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
import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.AuthTokenMO;

public class BergamotClient
{
    private final BergamotTranscoder transcoder = new BergamotTranscoder();
    
    // the API base url
    private final String baseURL;
    
    // the username to authenticate with
    private String username;
    
    // the password to authenticate with
    private String password;
    
    // the current auth token
    private AuthTokenMO authToken;
    
    public BergamotClient(String baseURL)
    {
        super();
        // remove trailing /
        if (baseURL.endsWith("/"))
        {
            baseURL = baseURL.substring(0, baseURL.length() - 1);
        }
        this.baseURL = baseURL;
    }
    
    public BergamotClient(String baseURL, String username, String password)
    {
        this(baseURL);
        this.username = username;
        this.password = password;
    }
    
    public BergamotTranscoder transcoder()
    {
        return this.transcoder;
    }
    
    // the base url
    
    public String getBaseURL()
    {
        return this.baseURL;
    }
    
    // current auth details

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setAuthToken(AuthTokenMO authToken)
    {
        this.authToken = authToken;
    }    

    public AuthTokenMO getAuthToken()
    {
        if (this.authTokenExpired() && !(Util.isEmpty(this.username) || Util.isEmpty(this.password)))
        {
            this.authToken = this.authToken().username(this.getUsername()).password(this.getPassword()).execute();
        }
        return authToken;
    }
    
    public boolean authTokenExpired()
    {
        return this.authToken == null || this.authToken.getExpiresAt() < System.currentTimeMillis();
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
    
}
