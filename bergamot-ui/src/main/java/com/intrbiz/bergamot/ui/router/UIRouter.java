package com.intrbiz.bergamot.ui.router;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Before;
import com.intrbiz.metadata.Order;
import com.intrbiz.metadata.Prefix;

@Prefix("/")
public class UIRouter extends Router<BergamotApp>
{
    @Before
    @Any("**")
    @Order(Order.FIRST)
    public void addDefaultHeaders()
    {
        // Default security headers
        response()
        .header("X-Frame-Options", "DENY")
        .header("X-Content-Type-Options", "nosniff")
        .header("X-Xss-Protection", "1; mode=block")
        .header("Referrer-Policy", "no-referrer");
        // Default security headers for when over HTTPS
        if (request().isSecure())
        {
            response()
            .header("Strict-Transport-Security", "max-age=31536000");
        }
    }
}
