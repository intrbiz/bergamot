package com.intrbiz.bergamot.model.message.processor.result.match;

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
