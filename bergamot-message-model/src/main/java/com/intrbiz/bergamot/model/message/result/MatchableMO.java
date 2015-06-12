package com.intrbiz.bergamot.model.message.result;

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
