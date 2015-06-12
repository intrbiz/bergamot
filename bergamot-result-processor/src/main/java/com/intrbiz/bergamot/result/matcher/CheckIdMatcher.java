package com.intrbiz.bergamot.result.matcher;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.message.result.MatchOnCheckId;
import com.intrbiz.bergamot.model.message.result.MatchableMO;

public class CheckIdMatcher implements Matcher<MatchOnCheckId>
{
    @Override
    public boolean build(Matchers matchers, MatchOnCheckId matchOn)
    {
        return true;
    }

    @Override
    public Check<?, ?> match(BergamotDB db, MatchOnCheckId matchOn, MatchableMO matchable)
    {
        if ("host".equalsIgnoreCase(matchOn.getCheckType()))
        {
            return db.getHost(matchOn.getCheckId());
        }
        else if ("service".equalsIgnoreCase(matchOn.getCheckType()))
        {
            return db.getService(matchOn.getCheckId());
        }
        else if ("trap".equalsIgnoreCase(matchOn.getCheckType()))
        {
            return db.getTrap(matchOn.getCheckId());
        }
        else
        {
            return db.getCheck(matchOn.getCheckId());    
        }
    }   
}
