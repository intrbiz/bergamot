package com.intrbiz.bergamot.result.matcher;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.message.result.MatchOnTrapName;
import com.intrbiz.bergamot.model.message.result.MatchableMO;

public class TrapNameMatcher implements Matcher<MatchOnTrapName>
{
    private Matcher<?> hostMatcher;
    
    @Override
    public boolean build(Matchers matchers, MatchOnTrapName matchOn)
    {
        this.hostMatcher = matchers.buildMatcher(matchOn.getHostMatch());
        return this.hostMatcher != null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Check<?, ?> match(BergamotDB db, MatchOnTrapName matchOn, MatchableMO matchable)
    {
        Check<?, ?> hostCheck = ((Matcher) this.hostMatcher).match(db, matchOn.getHostMatch(), matchable);
        if (hostCheck instanceof Host)
        {
            Host host = (Host) hostCheck;
            return host.getTrap(matchOn.getTrapName());
        }
        return null;
    }   
}
