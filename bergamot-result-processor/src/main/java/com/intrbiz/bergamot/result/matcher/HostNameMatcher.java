package com.intrbiz.bergamot.result.matcher;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.message.result.MatchOnHostName;
import com.intrbiz.bergamot.model.message.result.MatchableMO;

public class HostNameMatcher implements Matcher<MatchOnHostName>
{
    @Override
    public boolean build(Matchers matchers, MatchOnHostName matchOn)
    {
        return true;
    }

    @Override
    public Check<?, ?> match(BergamotDB db, MatchOnHostName matchOn, MatchableMO matchable)
    {
        return db.getHostByName(matchable.getSiteId(), matchOn.getHostName());
    }   
}
