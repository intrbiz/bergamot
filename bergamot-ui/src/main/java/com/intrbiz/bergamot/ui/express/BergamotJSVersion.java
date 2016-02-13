package com.intrbiz.bergamot.ui.express;

import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.operator.Function;

public class BergamotJSVersion extends Function
{
    public BergamotJSVersion()
    {
        super("bergamot_js_version");
    }

    @Override
    public Object get(ExpressContext context, Object source) throws ExpressException
    {
        return BergamotApp.VERSION.COMPONENTS.JS;
    }
}
