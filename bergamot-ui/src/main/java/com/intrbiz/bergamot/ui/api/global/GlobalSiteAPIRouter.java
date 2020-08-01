package com.intrbiz.bergamot.ui.api.global;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.GroupMO;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.bergamot.ui.router.global.CreateSiteRequest;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Before;
import com.intrbiz.metadata.CurrentPrincipal;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.ListOf;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.doc.Desc;
import com.intrbiz.metadata.doc.Title;


@Prefix("/api/global/site")
@RequireValidPrincipal()
public class GlobalSiteAPIRouter extends Router<BergamotUI>
{
    private static final Logger logger = Logger.getLogger(GlobalSiteAPIRouter.class);
    
    @Before()
    @Any("**")
    @WithDataAdapter(BergamotDB.class)
    public void requireGlobalAdmin(BergamotDB db, @CurrentPrincipal Contact principal)
    {
        require(var("globalAdmin", principal.isGlobalAdmin()));
    }
    
    @Title("Create a site")
    @Desc({
        "Create a new site, initialize it with the default configuration and setup the initial administrator with the given user details."
    })
    @Post("/")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    @ListOf(GroupMO.class)
    public Site createSite(
        BergamotDB db,
        @Param("site_name") String siteName,    
        @Param("site_summary") String siteSummary,
        @Param("admin_first_name") String userFirstName,
        @Param("admin_last_name") String userLastName,
        @Param("admin_email") String userEmail,
        @Param("admin_mobile") String userMobile,
        @Param("admin_username") String username,
        @Param("admin_email") String password
    ) {
        Site site = action("site-create", new CreateSiteRequest() {{
            this.setSiteName(siteName);
            this.setSiteSummary(siteSummary);
            this.setUserFirstName(userFirstName);
            this.setUserLastName(userLastName);
            this.setUserEmail(userEmail);
            this.setUserMobile(userMobile);
            this.setUsername(username);
            this.setPassword(password);
            this.setConfirmPassword(password);
        }});
        logger.info("Created site " + site.getId() + " " + site.getName());
        return site;
    }    
}
