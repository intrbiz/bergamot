package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Resource;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/resource")
@Template("layout/main")
@RequireValidPrincipal()
public class ResourceRouter extends Router<BergamotApp>
{   
    @Any("/name/:host/:resource")
    @WithDataAdapter(BergamotDB.class)
    public void showResourceByName(BergamotDB db, String clusterName, String resourceName, @SessionVar("site") Site site)
    {
        Resource resource = model("resource", db.getResourceOnClusterByName(site.getId(), clusterName, resourceName));
        model("alerts", db.getAllAlertsForCheck(resource.getId()));
        encode("resource/detail");
    }
    
    @Any("/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void showResourceById(BergamotDB db, @IsaObjectId UUID id)
    {
        model("resource", db.getResource(id));
        model("alerts", db.getAllAlertsForCheck(id));
        encode("resource/detail");
    }
    
    @Any("/enable/:id")
    @WithDataAdapter(BergamotDB.class)
    public void enableResource(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Resource resource = db.getResource(id);
        if (resource != null)
        {
            resource.setEnabled(true);
            db.setResource(resource);
        }
        redirect("/resource/id/" + id);
    }
    
    @Any("/disable/:id")
    @WithDataAdapter(BergamotDB.class)
    public void disableResource(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Resource resource = db.getResource(id);
        if (resource != null)
        {
            resource.setEnabled(false);
            db.setResource(resource);
        }
        redirect("/resource/id/" + id);
    }
    
    @Any("/suppress/:id")
    @WithDataAdapter(BergamotDB.class)
    public void suppressResource(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Resource resource = db.getResource(id);
        if (resource != null)
        {
            action("suppress-check", resource);
        }
        redirect("/resource/id/" + id);
    }
    
    @Any("/unsuppress/:id")
    @WithDataAdapter(BergamotDB.class)
    public void unsuppressResource(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Resource resource = db.getResource(id);
        if (resource != null)
        {
            action("unsuppress-check", resource);
        }
        redirect("/resource/id/" + id);
    }
}
