package com.intrbiz.bergamot.ui.api;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;


/**
 * Simple test API calls to prove conectivity, etc.
 */
@Prefix("/api/test")
public class TestAPIRouter extends Router<BergamotApp>
{    
    @Get("/hello/world")
    @JSON()
    @IgnoreBinding
    public String helloWorld()
    {    
        return "Hello World";
    }
    
    @Get("/hello/you")
    @JSON()
    @RequireValidPrincipal()
    @IgnoreBinding
    public String helloYou()
    {    
        return "Hello " + ((Contact) currentPrincipal()).getName();
    }
    
    @Get("/goodbye/cruel/world")
    @RequireValidPrincipal()
    @IgnoreBinding
    public void goodbyeCruelWorld()
    {    
        throw new RuntimeException("Goodby Cruel World");
    }
    
    @Get("/looking/for/something")
    @RequireValidPrincipal()
    @IgnoreBinding
    public void lookingForSomething()
    {    
        throw new BalsaNotFound();
    }
}
