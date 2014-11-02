package com.intrbiz.bergamot.pinger;

public interface OnPingUpdate
{
    void onUpdate(PingTarget target, PingSnapshot currentSnapshot);
}
