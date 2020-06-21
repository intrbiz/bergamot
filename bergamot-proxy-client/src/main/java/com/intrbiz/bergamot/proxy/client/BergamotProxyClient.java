package com.intrbiz.bergamot.proxy.client;

import java.net.URI;

import com.intrbiz.bergamot.io.BergamotCoreTranscoder;
import com.intrbiz.bergamot.proxy.BaseBergamotClient;

public class BergamotProxyClient extends BaseBergamotClient
{ 
    public BergamotProxyClient(URI server)
    {
        super(server, BergamotCoreTranscoder.getDefault());
    }
}
