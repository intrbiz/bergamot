package com.intrbiz.bergamot.result.matcher;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.message.result.MatchOn;
import com.intrbiz.bergamot.model.message.result.MatchableMO;

/**
 * Match the given passive result to a check
 */
public interface Matcher<T extends MatchOn>
{
    /**
     * Build any sub matchers this matcher needs
     * @param matchers
     * @return true if successfully built
     */
    boolean build(Matchers matchers, T matchOn);
    
    /**
     * Match the given criteria to a check
     * @param db
     * @param passiveResult
     * @return the check - tada!
     */
    Check<?, ?> match(BergamotDB db, T matchOn, MatchableMO matchable);
}
