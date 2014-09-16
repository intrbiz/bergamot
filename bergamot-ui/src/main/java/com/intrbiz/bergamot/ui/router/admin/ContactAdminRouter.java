package com.intrbiz.bergamot.ui.router.admin;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.ContactCfg;
import com.intrbiz.bergamot.config.model.TeamCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Contact.LockOutReason;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.IsaUUID;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/admin/contact")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
@RequirePermission("ui.admin.contact")
public class ContactAdminRouter extends Router<BergamotApp>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @SessionVar("site") Site site)
    {
        model("contacts", db.listContacts(site.getId()));
        model("contact_templates", db.listConfigTemplates(site.getId(), Configuration.getRootElement(ContactCfg.class)));
        encode("admin/contact/index");
    }
    
    @Any("/lock")
    @WithDataAdapter(BergamotDB.class)
    public void lock(BergamotDB db, @Param("id") @IsaUUID(mandatory = true) UUID contactId) throws IOException
    {
        Contact contact = db.getContact(contactId);
        if (contact != null)
        {
            db.setContact(contact.lock(LockOutReason.ADMINISTRATIVE));
        }
        redirect(path("/admin/contact/"));
    }
    
    @Any("/unlock")
    @WithDataAdapter(BergamotDB.class)
    public void unlock(BergamotDB db, @Param("id") @IsaUUID(mandatory = true) UUID contactId) throws IOException
    {
        Contact contact = db.getContact(contactId);
        if (contact != null)
        {
            db.setContact(contact.unlock());
        }
        redirect(path("/admin/contact/"));
    }
    
    @Any("/reset")
    @WithDataAdapter(BergamotDB.class)
    public void reset(BergamotDB db, @Param("id") @IsaUUID(mandatory = true) UUID contactId) throws IOException
    {
        Contact contact = db.getContact(contactId);
        if (contact != null)
        {
            db.setContact(contact.forcePasswordChange());
        }
        redirect(path("/admin/contact/"));
    }
    
    @Get("/configure/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void showConfigure(BergamotDB db, @AsUUID UUID timeperiodId, @SessionVar("site") Site site)
    {
        model("contact", Util.nullable(db.getConfig(timeperiodId), Config::getResolvedConfiguration));
        model("templates", db.listConfigTemplates(site.getId(), Configuration.getRootElement(ContactCfg.class)));
        model("teams", db.listConfig(site.getId(), Configuration.getRootElement(TeamCfg.class)));
        encode("admin/contact/configure");
    }
    
    @Get("/configure")
    @WithDataAdapter(BergamotDB.class)
    public void showConfigureNew(BergamotDB db, @SessionVar("site") Site site)
    {
        model("contact", new ContactCfg());
        model("templates", db.listConfigTemplates(site.getId(), Configuration.getRootElement(ContactCfg.class)));
        model("teams", db.listConfig(site.getId(), Configuration.getRootElement(TeamCfg.class)));
        encode("admin/contact/configure");
    }
}
