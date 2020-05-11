package com.intrbiz.bergamot.cluster.util;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public class ZKPaths
{
    private static final int UUID_STR_LEN = 36;
    
    private static final int ZK_SEQ_LEN = 10;
    
    public static final String BERGAMOT = "/bergamot";
    
    public static final String WORKERS = "workers";
    
    public static final String NOTIFIERS = "notifiers";
    
    public static final String PROCESSORS = "processors";
    
    public static final String PROXIES = "proxies";
    
    public static final String AGENTS = "agents";
    
    public static final String POOLS = "pools";
    
    public static final String GLOBAL = "global";
    
    public static final String LEADER = "leader";
    
    public static final String NODES = "nodes";
    
    /**
     * Build a ZooKeeper path
     * @param names the names of the path to be separated by /
     * @return the built path
     */
    public static final String zkPath(Object... names)
    {
        return Arrays.stream(names).map(Object::toString).collect(Collectors.joining("/"));
    }
    
    /**
     * Extract the UUID from  a ZK path when used as the prefix for a ephemeral sequential node
     * @param name the ZK Node name
     * @return the UUID
     */
    public static final UUID uuidPrefixFromName(String name)
    {
        return UUID.fromString(name.substring(0, UUID_STR_LEN));
    }
    
    /**
     * Extract the ephemeral sequence from a ZK path
     * @param name the ZK node name
     * @return the ephemeral sequence
     */
    public static final int sequenceFromPath(String name)
    {
        return Integer.parseInt(name.substring(name.length() - ZK_SEQ_LEN));
    }
}
