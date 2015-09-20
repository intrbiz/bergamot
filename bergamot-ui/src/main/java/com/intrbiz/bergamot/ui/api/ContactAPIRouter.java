package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.ContactCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.ListOf;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.XML;


@Prefix("/api/contact")
@RequireValidPrincipal()
public class ContactAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    @ListOf(ContactMO.class)
    public List<ContactMO> getContacts(BergamotDB db, @Var("site") Site site)
    {
        return db.listContacts(site.getId()).stream().map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ContactMO getContactByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Contact contact = notNull(db.getContactByName(site.getId(), name));
        require(permission("read", contact));
        return contact.toMO(currentPrincipal());
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ContactMO getContact(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Contact contact = notNull(db.getContact(id));
        require(permission("read", contact));
        return contact.toMO(currentPrincipal());
    }
    
    @Get("/email/:email")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ContactMO getContactByEmail(BergamotDB db, @Var("site") Site site, String email)
    {
        Contact contact = notNull(db.getContactByEmail(site.getId(), email));
        require(permission("read", contact));
        return contact.toMO(currentPrincipal());
    }
    
    @Get("/name-or-email/:nameOrEmail")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ContactMO getContactByNameOrEmail(BergamotDB db, @Var("site") Site site, String nameOrEmail)
    {
        Contact contact = notNull(db.getContactByNameOrEmail(site.getId(), nameOrEmail));
        require(permission("read", contact));
        return contact.toMO(currentPrincipal());
    }
    
    @Get("/name/:name/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public ContactCfg getContactConfigByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Contact contact = notNull(db.getContactByName(site.getId(), name));
        require(permission("read.config", contact));
        return contact.getConfiguration();
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public ContactCfg getContactConfig(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Contact contact = notNull(db.getContact(id));
        require(permission("read.config", contact));
        return contact.getConfiguration();
    }
}
