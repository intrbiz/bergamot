package com.intrbiz.bergamot.result.matcher;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.message.result.MatchOnHostExternalRef;
import com.intrbiz.bergamot.model.message.result.MatchableMO;

public class HostExternalRefMatcher implements Matcher<MatchOnHostExternalRef>
{
    @Override
    public boolean build(Matchers matchers, MatchOnHostExternalRef matchOn)
    {
        return true;
    }

    @Override
    public Check<?, ?> match(BergamotDB db, MatchOnHostExternalRef matchOn, MatchableMO matchable)
    {
        return db.getHostByExternalRef(matchable.getSiteId(), matchOn.getExternalRef());
    }   
}
