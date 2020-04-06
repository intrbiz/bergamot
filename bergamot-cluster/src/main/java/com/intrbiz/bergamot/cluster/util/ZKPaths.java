package com.intrbiz.bergamot.cluster.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ZKPaths
{
    public static final String BERGAMOT = "/bergamot";
    
    public static final String WORKERS = "workers";
    
    public static final String NOTIFIERS = "notifiers";
    
    public static final String PROCESSORS = "processors";
    
    public static final String AGENTS = "agents";
    
    public static final String POOLS = "pools";
    
    public static final String LEADER = "leader";
    
    public static final String NODES = "nodes";
    
    public static final String zkPath(Object... names)
    {
        return Arrays.stream(names).map(Object::toString).collect(Collectors.joining("/"));
    }
}
