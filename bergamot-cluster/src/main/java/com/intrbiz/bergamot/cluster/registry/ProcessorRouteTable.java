package com.intrbiz.bergamot.cluster.registry;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class ProcessorRouteTable
{
    private final ConcurrentMap<UUID, UUID> processorsMap = new ConcurrentHashMap<>();
    
    private final SecureRandom random = new SecureRandom();
    
    private volatile UUID[] processorsTable = new UUID[0];
    
    public ProcessorRouteTable()
    {
        super();
    }
    
    void registerProcessor(UUID processor)
    {
        this.processorsMap.put(processor, processor);
        this.updateProcessors();
    }
    
    void unregisterProcessor(UUID processor)
    {
        this.processorsMap.remove(processor);
        this.updateProcessors();
    }
    
    private void updateProcessors()
    {
        this.processorsTable = this.processorsMap.keySet().stream()
                            .sorted()
                            .collect(Collectors.toList())
                            .toArray(new UUID[0]);
    }
    
    public boolean hasProcessor(UUID id)
    {
        return this.processorsMap.containsKey(id);
    }
    
    /**
     * Get all available processors
     * @return the processor ids
     */
    public UUID[] getProcessors()
    {
        return this.processorsTable;
    }
    
    /**
     * Select a processor at random
     * @return a processor id or null
     */
    public UUID routeProcessor()
    {
        UUID[] procs = this.processorsTable;
        return procs.length > 0 ? procs[Math.abs(this.random.nextInt() % procs.length)] : null;
    }
    
    /**
     * Select a processor based on the given hash value
     * @param hash the value to select a processor based on
     * @return a processor id or null
     */
    public UUID routeProcessor(long hash)
    {
        UUID[] procs = this.processorsTable;
        return procs.length > 0 ? procs[Math.abs((int)(hash % procs.length))] : null;
    }
    
    public String toString()
    {
        return Arrays.toString(this.processorsTable);
    }
}
