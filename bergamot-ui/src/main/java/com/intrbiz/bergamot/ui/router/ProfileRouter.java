package com.intrbiz.bergamot.ui.router;

import java.io.IOException;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.APIToken;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsString;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/profile")
@Template("layout/main")
@RequireValidPrincipal()
public class ProfileRouter extends Router<BergamotApp>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db)
    {
        encode("profile/index");
    }
    
    @Any("/revoke-api-token")
    @WithDataAdapter(BergamotDB.class)
    public void revokeAPIToken(BergamotDB db, @Param("token") @CheckStringLength(mandatory = true) String token) throws IOException
    {
        APIToken apiToken = db.getAPIToken(token);
        if (apiToken != null)
        {
            db.setAPIToken(apiToken.revoke());
        }
        redirect(path("/profile/"));
    }
    
    @Any("/remove-api-token")
    @WithDataAdapter(BergamotDB.class)
    public void removeAPIToken(BergamotDB db, @Param("token") @CheckStringLength(mandatory = true) String token) throws IOException
    {
        db.removeAPIToken(token);
        redirect(path("/profile/"));
    }
        
    @Any("/generate-api-token")
    @WithDataAdapter(BergamotDB.class)
    public void generateAPIToken(BergamotDB db, @Param("summary") @AsString() String summary) throws IOException
    {
        String token = app().getSecurityEngine().generatePerpetualAuthenticationTokenForPrincipal(currentPrincipal());
        db.setAPIToken(new APIToken(token, currentPrincipal(), Util.coalesceEmpty(summary, "API Access")));
        redirect(path("/profile/"));
    }
}
