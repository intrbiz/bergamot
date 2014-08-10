package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.ContactCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsBoolean;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.ListParam;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermissions;
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
    
    @Get("/name/:name/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ContactCfg getContactConfig(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getContactByName(site.getId(), name), Contact::getConfiguration);
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ContactCfg getContactConfig(BergamotDB db, @AsUUID UUID id)
    {
        return Util.nullable(db.getContact(id), Contact::getConfiguration);
    }
    
    @Any("/configure")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public ContactMO configureContact(
            BergamotDB db, 
            @Var("site") Site site, 
            @Param("configuration") @CheckStringLength(min = 1, max = 128 * 1024, mandatory = true) String configurationXML
    )
    {
        // parse the config and allocate the id
        ContactCfg config = ContactCfg.fromString(ContactCfg.class, configurationXML);
        config.setId(site.randomObjectId());
        // create the team
        Contact contact = action("create-contact", config);
        return contact.toMO();
    }
    
    @Any("/create")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public ContactMO createContact(
            BergamotDB db, 
            @Var("site") Site site, 
            @Param("name") @CheckStringLength(min = 1, max = 80, mandatory = true) String name, 
            @Param("summary") @CheckStringLength(min = 1, max = 80, mandatory = true) String summary, 
            @Param("description") @CheckStringLength(min = 1, max = 1000) String description, 
            @Param("template") @AsBoolean(coalesce = CoalesceMode.ON_NULL) Boolean template, 
            @ListParam("extends") @CheckStringLength(min = 1, max = 80, mandatory = true) List<String> inherits,
            @ListParam("team") @CheckStringLength(min = 1, max = 80, mandatory = true) List<String> teams,
            @Param("email") @CheckStringLength(min = 1, max = 255, mandatory = true) String email, 
            @Param("pager") @CheckStringLength(min = 1, max = 20, mandatory = true) String pager, 
            @Param("mobile") @CheckStringLength(min = 1, max = 20, mandatory = true) String mobile,
            @Param("phone") @CheckStringLength(min = 1, max = 20, mandatory = true) String phone, 
            @Param("im") @CheckStringLength(min = 1, max = 255, mandatory = true) String im, 
            @ListParam("grants") @CheckStringLength(min = 1, max = 255, mandatory = true) List<String> grants, 
            @ListParam("revokes") @CheckStringLength(min = 1, max = 255, mandatory = true) List<String> revokes
    )
    {
        // create the team config
        ContactCfg config = new ContactCfg();
        config.setId(site.randomObjectId());
        config.setName(name);
        config.setSummary(summary);
        config.setEmail(email);
        config.setPager(pager);
        config.setMobile(mobile);
        config.setPhone(phone);
        config.setIm(im);
        config.setDescription(description);
        config.setTemplate(template);
        for (String inherit : inherits)
        {
            config.getInheritedTemplates().add(inherit);
        }
        for (String team : teams)
        {
            config.getTeams().add(team);
        }
        for (String grant : grants)
        {
            config.getGrantedPermissions().add(grant);
        }
        for (String revoke : revokes)
        {
            config.getRevokedPermissions().add(revoke);
        }
        // create the contact
        Contact contact = action("create-contact", config);
        return contact.toMO();
    }
    
    @Get("/id/:id/set-password")
    @JSON()
    @RequirePermissions("api.write.contact.set-password")
    @WithDataAdapter(BergamotDB.class)
    public Boolean setPassword(BergamotDB db, @AsUUID UUID id, @Param("password") @CheckStringLength(min = 1, max = 80, mandatory = true) String password)
    {
        Contact contact = db.getContact(id);
        if (contact == null) throw new BalsaNotFound("No contact with the id: " + id);
        boolean res = action("set-password", contact, password); 
        return res;
    }
}
