package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;


@Prefix("/api/contact")
@RequireValidPrincipal()
public class ContactAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    public List<ContactMO> getContacts(BergamotDB db, @Var("site") Site site)
    {
        return db.listContacts(site.getId()).stream().map(Contact::toMO).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ContactMO getContact(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getContactByName(site.getId(), name), Contact::toMO);
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ContactMO getContact(BergamotDB db, @AsUUID UUID id)
    {
        return Util.nullable(db.getContact(id), Contact::toMO);
    }
    
    @Get("/email/:email")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ContactMO getContactByEmail(BergamotDB db, @Var("site") Site site, String email)
    {
        return Util.nullable(db.getContactByEmail(site.getId(), email), Contact::toMO);
    }
    
    @Get("/name-or-email/:nameOrEmail")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ContactMO getContactByNameOrEmail(BergamotDB db, @Var("site") Site site, String nameOrEmail)
    {
        return Util.nullable(db.getContactByNameOrEmail(site.getId(), nameOrEmail), Contact::toMO);
    }
}
