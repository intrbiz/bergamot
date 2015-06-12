package com.intrbiz.bergamot.result.matcher;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.message.result.MatchOnHostAddress;
import com.intrbiz.bergamot.model.message.result.MatchableMO;

public class HostAddressMatcher implements Matcher<MatchOnHostAddress>
{
    @Override
    public boolean build(Matchers matchers, MatchOnHostAddress matchOn)
    {
        return true;
    }

    @Override
    public Check<?, ?> match(BergamotDB db, MatchOnHostAddress matchOn, MatchableMO matchable)
    {
        return db.getHostByAddress(matchable.getSiteId(), matchOn.getHostAddress());
    }   
}
