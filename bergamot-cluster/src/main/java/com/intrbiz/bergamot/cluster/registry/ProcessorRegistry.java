package com.intrbiz.bergamot.cluster.registry;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.intrbiz.bergamot.cluster.util.ZKPaths;
import com.intrbiz.bergamot.model.message.cluster.ProcessorRegistration;

/**
 * A registry of Bergamot Processors
 */
public class ProcessorRegistry extends GenericRegistry<UUID, ProcessorRegistration>
{
    public static final Logger logger = Logger.getLogger(ProcessorRegistry.class);
    
    private final ProcessorRouteTable routeTable = new ProcessorRouteTable();
    
    public ProcessorRegistry(ZooKeeper zooKeeper) throws KeeperException, InterruptedException
    {
        super(zooKeeper, ProcessorRegistration.class, UUID::fromString, ZKPaths.PROCESSORS);
        this.init();
    }
    
    private void init() throws KeeperException, InterruptedException
    {
        Set<UUID> processors = this.getProcessors().stream().map(ProcessorRegistration::getId).collect(Collectors.toSet());
        logger.info("Initialising with processors: " + processors);
        this.routeTable.registerProcessors(processors);
        logger.info("Processor routing table:\n" + this.routeTable);
    }
    
    @Override
    protected void onConnect()
    {
        try
        {
            this.init();
        }
        catch (KeeperException | InterruptedException e)
        {
            logger.error("");
        }
    }
    
    @Override
    protected void onItemUpdated(UUID id, ProcessorRegistration item)
    {
        this.routeTable.registerProcessor(id);
        logger.info("Processor routing table:\n" + this.routeTable);
    }

    @Override
    protected void onItemAdded(UUID id, ProcessorRegistration item)
    {
        this.routeTable.registerProcessor(id);
        logger.info("Processor routing table:\n" + this.routeTable);
    }

    @Override
    protected void onItemRemoved(UUID id)
    {
        this.routeTable.unregisterProcessor(id);
        logger.info("Processor routing table:\n" + this.routeTable);
    }

    /**
     * Get the registration data for a specific Processor from ZooKeeper
     * @param processorId the processor id
     * @return the Processor registration data or null
     * @throws KeeperException
     * @throws InterruptedException
     */
    public ProcessorRegistration getProcessor(UUID processorId) throws KeeperException, InterruptedException
    {
        return this.getItem(processorId);
    }
    
    /**
     * Get the current list of Processors from ZooKeeper 
     * @return The list of currently registered Processors
     * @throws KeeperException
     * @throws InterruptedException
     */
    public Set<ProcessorRegistration> getProcessors() throws KeeperException, InterruptedException
    {
        return this.getItems();
    }

    public ProcessorRouteTable getRouteTable()
    {
        return this.routeTable;
    }
}
