package com.intrbiz.bergamot.express;

import com.intrbiz.bergamot.express.functions.ResolveCredentials;
import com.intrbiz.express.ExpressExtensionRegistry;

public class BergamotExpressExtensionRegistry extends ExpressExtensionRegistry
{
    private static final BergamotExpressExtensionRegistry DEFAULT_INSTANCE = new BergamotExpressExtensionRegistry();
    
    public static BergamotExpressExtensionRegistry getDefaultInstance()
    {
        return DEFAULT_INSTANCE;
    }
    
    public BergamotExpressExtensionRegistry()
    {
        super("bergamot");
        this.addFunction("resolve_credentials", ResolveCredentials::new);
    }
}
