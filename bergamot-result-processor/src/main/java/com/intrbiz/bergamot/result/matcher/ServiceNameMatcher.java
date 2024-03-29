package com.intrbiz.bergamot.result.matcher;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.message.processor.result.match.MatchOnServiceName;
import com.intrbiz.bergamot.model.message.processor.result.match.MatchableMO;

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
    public Check<?, ?> match(BergamotDB db, MatchOnServiceName matchOn, MatchableMO matchable)
    {
        Check<?, ?> hostCheck = ((Matcher) this.hostMatcher).match(db, matchOn.getHostMatch(), matchable);
        if (hostCheck instanceof Host)
        {
            Host host = (Host) hostCheck;
            return host.getService(matchOn.getServiceName());
        }
        return null;
    }   
}
