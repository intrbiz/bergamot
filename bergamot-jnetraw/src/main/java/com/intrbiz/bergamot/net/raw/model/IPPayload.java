package com.intrbiz.bergamot.net.raw.model;

import java.nio.ByteBuffer;

public interface IPPayload
{
    int computeLength();
    
    void pack(ByteBuffer to);
}
