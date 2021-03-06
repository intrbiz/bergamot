package com.intrbiz.bergamot.ui.router.admin;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.ContactCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.GetBergamotSite;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/admin/contact")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
public class ContactAdminRouter extends Router<BergamotUI>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @GetBergamotSite() Site site)
    {
        model("contacts", db.listContacts(site.getId()));
        model("contact_templates", db.listConfigTemplates(site.getId(), Configuration.getRootElement(ContactCfg.class)));
        encode("admin/contact/index");
    }
    
    @Any("/lock")
    @WithDataAdapter(BergamotDB.class)
    public void lock(BergamotDB db, @Param("id") @IsaObjectId UUID contactId) throws IOException
    {
        action("lock-contact", db.getContact(contactId));
        redirect(path("/admin/contact/"));
    }
    
    @Any("/unlock")
    @WithDataAdapter(BergamotDB.class)
    public void unlock(BergamotDB db, @Param("id") @IsaObjectId UUID contactId) throws IOException
    {
        action("unlock-contact", db.getContact(contactId));
        redirect(path("/admin/contact/"));
    }
    
    @Any("/reset")
    @WithDataAdapter(BergamotDB.class)
    public void reset(BergamotDB db, @Param("id") @IsaObjectId UUID contactId) throws IOException
    {
        action("reset-password", db.getContact(contactId));
        redirect(path("/admin/contact/"));
    }
}
