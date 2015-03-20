package com.intrbiz.bergamot.result.matcher;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.message.result.MatchOnHostName;
import com.intrbiz.bergamot.model.message.result.PassiveResultMO;

public class HostNameMatcher implements Matcher<MatchOnHostName>
{
    @Override
    public boolean build(Matchers matchers, MatchOnHostName matchOn)
    {
        return true;
    }

    @Override
    public Check<?, ?> match(BergamotDB db, MatchOnHostName matchOn, PassiveResultMO passiveResult)
    {
        return db.getHostByName(passiveResult.getSiteId(), matchOn.getHostName());
    }   
}
