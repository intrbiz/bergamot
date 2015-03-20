package com.intrbiz.bergamot.result.matcher;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.intrbiz.bergamot.model.message.result.MatchOn;
import com.intrbiz.bergamot.model.message.result.MatchOnAgentId;
import com.intrbiz.bergamot.model.message.result.MatchOnCheckId;
import com.intrbiz.bergamot.model.message.result.MatchOnHostAddress;
import com.intrbiz.bergamot.model.message.result.MatchOnHostName;
import com.intrbiz.bergamot.model.message.result.MatchOnServiceName;

public class Matchers
{
    private Map<Class<? extends MatchOn>, Supplier<Matcher<?>>> matchers = new HashMap<Class<? extends MatchOn>, Supplier<Matcher<?>>>();
    
    public Matchers()
    {
        // register default matchers
        this.registerMatcher(MatchOnAgentId.class,     AgentIdMatcher::new);
        this.registerMatcher(MatchOnCheckId.class,     CheckIdMatcher::new);
        this.registerMatcher(MatchOnHostName.class,    HostNameMatcher::new);
        this.registerMatcher(MatchOnHostAddress.class, HostAddressMatcher::new);
        this.registerMatcher(MatchOnServiceName.class, ServiceNameMatcher::new);
    }
    
    public void registerMatcher(Class<? extends MatchOn> matchOn, Supplier<Matcher<?>> matcherFactory)
    {
        this.matchers.put(matchOn, matcherFactory);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Matcher<?> buildMatcher(MatchOn match)
    {
        if (match == null) return null;
        // find a factory
        Supplier<Matcher<?>> matcherFactory = this.matchers.get(match.getClass());
        if (matcherFactory == null) return null;
        // create the matcher
        Matcher<?> matcher = matcherFactory.get();
        if (! ((Matcher) matcher).build(this, match)) return null;
        return matcher;
    }
}
