package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;


@Prefix("/api/contact")
public class ContactAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    public List<ContactMO> getContacts()
    {
        return null; //return this.app().getBergamot().getObjectStore().getContacts().stream().map(Contact::toMO).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    public ContactMO getContact(String name)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupContact(name), Contact::toMO);
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    public ContactMO getContact(@AsUUID UUID id)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupContact(id), Contact::toMO);
    }
    
    @Get("/email/:email")
    @JSON(notFoundIfNull = true)
    public ContactMO getContactByEmail(String email)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupContactByEmail(email), Contact::toMO);
    }
}
