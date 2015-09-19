package com.intrbiz.bergamot;

import com.intrbiz.bergamot.call.agent.SignAgentKey;
import com.intrbiz.bergamot.call.alert.AcknowledgeAlertCall;
import com.intrbiz.bergamot.call.alert.GetAlertsCall;
import com.intrbiz.bergamot.call.auth.AppAuthTokenCall;
import com.intrbiz.bergamot.call.auth.AuthTokenCall;
import com.intrbiz.bergamot.call.auth.ExtendAuthTokenCall;
import com.intrbiz.bergamot.call.config.ApplyConfigChangeCall;
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
import com.intrbiz.bergamot.credentials.ClientCredentials;

public class BergamotClient extends BaseBergamotClient
{
    
    public BergamotClient(String baseURL, ClientCredentials credentials)
    {
        super(baseURL, credentials);
    }

    public BergamotClient(String baseURL, String username, String password)
    {
        super(baseURL, username, password);
    }

    public BergamotClient(String baseURL, String token)
    {
        super(baseURL, token);
    }

    public BergamotClient(String baseURL)
    {
        super(baseURL);
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
    
    public ApplyConfigChangeCall applyConfigChange()
    {
        return new ApplyConfigChangeCall(this);
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
    
    public AcknowledgeAlertCall acknowledgeAlert()
    {
        return new AcknowledgeAlertCall(this);
    }
    
    // agent
    
    public SignAgentKey signAgentKey()
    {
        return new SignAgentKey(this);
    }
}
