package com.intrbiz.bergamot.result.matcher;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.message.result.MatchOnHostAddress;
import com.intrbiz.bergamot.model.message.result.PassiveResultMO;

public class HostAddressMatcher implements Matcher<MatchOnHostAddress>
{
    @Override
    public boolean build(Matchers matchers, MatchOnHostAddress matchOn)
    {
        return true;
    }

    @Override
    public Check<?, ?> match(BergamotDB db, MatchOnHostAddress matchOn, PassiveResultMO passiveResult)
    {
        return db.getHostByAddress(passiveResult.getSiteId(), matchOn.getHostAddress());
    }   
}
