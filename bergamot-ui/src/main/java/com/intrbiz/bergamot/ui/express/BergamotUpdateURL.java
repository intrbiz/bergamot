package com.intrbiz.bergamot.ui.express;

import static com.intrbiz.balsa.BalsaContext.*;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.operator.Function;

public class BergamotUpdateURL extends Function
{
    public BergamotUpdateURL()
    {
        super("bergamot_update_url");
    }

    @Override
    public Object get(ExpressContext context, Object source) throws ExpressException
    {
        BalsaContext ctx = Balsa();
        StringBuilder url = new StringBuilder();
        url.append(ctx.request().isSecure() ? "wss" : "ws");
        url.append("://");
        url.append(ctx.request().getServerName());
        url.append(ctx.path("/websocket"));
        return url.toString();
    }
}
