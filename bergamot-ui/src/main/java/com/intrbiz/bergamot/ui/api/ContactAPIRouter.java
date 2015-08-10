package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.ContactCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
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
    public List<ContactMO> getContacts(BergamotDB db, @Var("site") Site site)
    {
        return db.listContacts(site.getId()).stream().map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ContactMO getContact(BergamotDB db, @Var("site") Site site, String name)
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
    public ContactCfg getContactConfig(BergamotDB db, @Var("site") Site site, String name)
    {
        Contact contact = notNull(db.getContactByName(site.getId(), name));
        require(permission("read.config", contact));
        return contact.getConfiguration();
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ContactCfg getContactConfig(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Contact contact = notNull(db.getContact(id));
        require(permission("read.config", contact));
        return contact.getConfiguration();
    }
    
    @Get("/id/:id/set-password")
    @JSON()
    @RequirePermission("api.write.contact.set-password")
    @WithDataAdapter(BergamotDB.class)
    public Boolean setPassword(BergamotDB db, @IsaObjectId(session = false) UUID id, @Param("password") @CheckStringLength(min = 1, max = 80, mandatory = true) String password)
    {
        Contact contact = notNull(db.getContact(id));
        boolean res = action("set-password", contact, password); 
        return res;
    }
}
