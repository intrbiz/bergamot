package com.intrbiz.bergamot.agent.client;

import java.net.URI;

import com.intrbiz.bergamot.io.BergamotAgentTranscoder;
import com.intrbiz.bergamot.proxy.BaseBergamotClient;

public class BergamotAgentClient extends BaseBergamotClient
{
    public BergamotAgentClient(URI server)
    {
        super(server, BergamotAgentTranscoder.getDefault());
    }
}
