package com.intrbiz.bergamot.result.matcher;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.message.result.MatchOnTrapExternalRef;
import com.intrbiz.bergamot.model.message.result.MatchableMO;

public class TrapExternalRefMatcher implements Matcher<MatchOnTrapExternalRef>
{
    private Matcher<?> hostMatcher;
    
    @Override
    public boolean build(Matchers matchers, MatchOnTrapExternalRef matchOn)
    {
        this.hostMatcher = matchers.buildMatcher(matchOn.getHostMatch());
        return this.hostMatcher != null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Check<?, ?> match(BergamotDB db, MatchOnTrapExternalRef matchOn, MatchableMO matchable)
    {
        Check<?, ?> hostCheck = ((Matcher) this.hostMatcher).match(db, matchOn.getHostMatch(), matchable);
        if (hostCheck instanceof Host)
        {
            Host host = (Host) hostCheck;
            return host.getTrapByExternalRef(matchOn.getExternalRef());
        }
        return null;
    }   
}
