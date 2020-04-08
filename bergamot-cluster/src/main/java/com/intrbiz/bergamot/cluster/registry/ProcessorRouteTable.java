package com.intrbiz.bergamot.cluster.registry;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class ProcessorRouteTable
{
    private final ConcurrentMap<UUID, Integer> processorsWithSequence = new ConcurrentHashMap<>();
    
    private final SecureRandom random = new SecureRandom();
    
    private volatile UUID[] processors = new UUID[0];
    
    public ProcessorRouteTable()
    {
        super();
    }
    
    void registerProcessor(UUID processor, int sequence)
    {
        this.processorsWithSequence.put(processor, sequence);
        this.updateProcessors();
    }
    
    void unregisterProcessor(UUID processor)
    {
        this.processorsWithSequence.remove(processor);
        this.updateProcessors();
    }
    
    private void updateProcessors()
    {
        this.processors = this.processorsWithSequence.entrySet().stream()
                            .sorted((a, b) -> Integer.compare(a.getValue(), b.getValue()))
                            .map(Entry::getKey)
                            .collect(Collectors.toList())
                            .toArray(new UUID[0]);
    }
    
    /**
     * Get all available processors
     * @return the processor ids
     */
    public UUID[] getProcessors()
    {
        return this.processors;
    }
    
    /**
     * Select a processor at random
     * @return a processor id or null
     */
    public UUID routeProcessor()
    {
        UUID[] procs = this.processors;
        return procs.length > 0 ? procs[Math.abs(this.random.nextInt() % procs.length)] : null;
    }
    
    /**
     * Select a processor based on the given hash value
     * @param hash the value to select a processor based on
     * @return a processor id or null
     */
    public UUID routeProcessor(long hash)
    {
        UUID[] procs = this.processors;
        return procs.length > 0 ? procs[Math.abs((int)(hash % procs.length))] : null;
    }
}
