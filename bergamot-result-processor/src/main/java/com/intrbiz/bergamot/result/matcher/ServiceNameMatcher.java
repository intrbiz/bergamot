package com.intrbiz.bergamot.result.matcher;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.message.result.MatchOnServiceName;
import com.intrbiz.bergamot.model.message.result.PassiveResultMO;

public class ServiceNameMatcher implements Matcher<MatchOnServiceName>
{
    private Matcher<?> hostMatcher;
    
    @Override
    public boolean build(Matchers matchers, MatchOnServiceName matchOn)
    {
        this.hostMatcher = matchers.buildMatcher(matchOn.getHostMatch());
        return this.hostMatcher != null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Check<?, ?> match(BergamotDB db, MatchOnServiceName matchOn, PassiveResultMO passiveResult)
    {
        Check<?, ?> hostCheck = ((Matcher) this.hostMatcher).match(db, matchOn.getHostMatch(), passiveResult);
        if (hostCheck instanceof Host)
        {
            Host host = (Host) hostCheck;
            return host.getService(matchOn.getServiceName());
        }
        return null;
    }   
}
