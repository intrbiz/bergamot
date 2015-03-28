package com.intrbiz.bergamot.result.matcher;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.message.result.MatchOnServiceExternalRef;
import com.intrbiz.bergamot.model.message.result.PassiveResultMO;

public class ServiceExternalRefMatcher implements Matcher<MatchOnServiceExternalRef>
{
    private Matcher<?> hostMatcher;
    
    @Override
    public boolean build(Matchers matchers, MatchOnServiceExternalRef matchOn)
    {
        this.hostMatcher = matchers.buildMatcher(matchOn.getHostMatch());
        return this.hostMatcher != null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Check<?, ?> match(BergamotDB db, MatchOnServiceExternalRef matchOn, PassiveResultMO passiveResult)
    {
        Check<?, ?> hostCheck = ((Matcher) this.hostMatcher).match(db, matchOn.getHostMatch(), passiveResult);
        if (hostCheck instanceof Host)
        {
            Host host = (Host) hostCheck;
            return host.getServiceByExternalRef(matchOn.getExternalRef());
        }
        return null;
    }   
}
