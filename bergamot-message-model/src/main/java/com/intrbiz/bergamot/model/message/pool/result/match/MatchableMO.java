package com.intrbiz.bergamot.model.message.pool.result.match;

import java.util.UUID;

/**
 * A message which can be matched to a check
 */
public interface MatchableMO
{
    UUID getSiteId();
    
    MatchOn getMatchOn();

    void setMatchOn(MatchOn matchOn);
}
