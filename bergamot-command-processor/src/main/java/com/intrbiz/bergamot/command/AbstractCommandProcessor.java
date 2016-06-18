package com.intrbiz.bergamot.command;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.command.handler.BergamotCommandHandler;
import com.intrbiz.bergamot.model.message.command.CommandRequest;
import com.intrbiz.bergamot.model.message.command.CommandResponse;
import com.intrbiz.bergamot.queue.BergamotCommandQueue;
import com.intrbiz.queue.RPCServer;

public abstract class AbstractCommandProcessor implements CommandProcessor
{
    private Logger logger = Logger.getLogger(AbstractCommandProcessor.class);
    
    private int threads = Runtime.getRuntime().availableProcessors();
    
    private BergamotCommandQueue commandQueue;
    
    private List<RPCServer<CommandRequest, CommandResponse>> commandServers;
    
    private ConcurrentMap<Class<? extends CommandRequest>, BergamotCommandHandler<?>> handlers = new ConcurrentHashMap<Class<? extends CommandRequest>, BergamotCommandHandler<?>>();
    
    public AbstractCommandProcessor()
    {
        super();
    }

    @Override
    public int getThreads()
    {
        return this.threads;
    }

    @Override
    public void setThreads(int threads)
    {
        this.threads = threads;
    }
    
    public void registerHandler(BergamotCommandHandler<?> handler)
    {
        for (Class<? extends CommandRequest> handles : handler.handles())
        {
            this.handlers.put(handles, handler);
            handler.init(this);
        }
    }
    
    public BergamotCommandHandler<?> getHandler(Class<? extends CommandRequest> forRequestType)
    {
        return this.handlers.get(forRequestType);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void start()
    {
        // open the command queue
        this.commandQueue = BergamotCommandQueue.open();
        // setup the servers
        for (int i = 0; i < this.threads; i++)
        {
            this.commandServers.add(
                this.commandQueue.createBergamotCommandRPCServer((request) -> {
                    // lookup the command handler and process the request
                    if (request != null)
                    {
                        BergamotCommandHandler handler = this.getHandler(request.getClass());
                        if (handler != null)
                        {
                            return handler.process(request);
                        }
                        else
                        {
                            logger.warn("Failed to find command handler for request: " + request.getClass());
                        }
                    }
                    return null;
                })
            );
        }
    }
}
