package com.intrbiz.bergamot.watcher.engine.snmp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.event.check.CheckEvent;
import com.intrbiz.bergamot.model.message.event.watcher.RegisterCheck;
import com.intrbiz.bergamot.model.message.event.watcher.UnregisterCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.snmp.SNMPContext;
import com.intrbiz.snmp.SNMPContextId;
import com.intrbiz.snmp.handler.trap.OnLinkChange;

public class LinkStateExecutor extends AbstractSNMPExecutor
{
    private Logger logger = Logger.getLogger(LinkStateExecutor.class);
    
    private ConcurrentMap<SNMPContextId, ConcurrentMap<String, LinkStateTrap>> traps = new ConcurrentHashMap<SNMPContextId, ConcurrentMap<String, LinkStateTrap>>();
    
    @Override
    public boolean accept(CheckEvent check)
    {
        return super.accept(check) && "link-state".equalsIgnoreCase(check.getExecutor());
    }
    
    private ConcurrentMap<String, LinkStateTrap> getContextTraps(SNMPContextId id)
    {
        ConcurrentMap<String, LinkStateTrap> current = new ConcurrentHashMap<String, LinkStateTrap>();
        ConcurrentMap<String, LinkStateTrap> previous = this.traps.putIfAbsent(id, current);
        return previous == null ? current : previous;
    }

    @Override
    public void register(RegisterCheck check, Consumer<Result> resultConsumer)
    {
        try
        {
            // validate the check
            this.validate(check);
            // the interface name
            String interfaceName = check.getParameter("interface-name");
            if (Util.isEmpty(interfaceName)) throw new RuntimeException("The interface-name must be defined!");
            // open the context
            SNMPContext<?> context = this.openContext(check);
            final ConcurrentMap<String, LinkStateTrap> contextTraps = this.getContextTraps(context.getContextId());
            // store the check
            contextTraps.put(interfaceName, new LinkStateTrap(check, resultConsumer));
            // setup the trap
            context.registerTrapHandler("link-state", new OnLinkChange.LinkTrapAdapter((linkEvent) -> {
                logger.info("Got link state change event: " + linkEvent.getEventType() + " " + linkEvent.getDescription() + " " + linkEvent.getAdminState() + " == " + linkEvent.getOperationalState());
                // lookup the trap
                LinkStateTrap trap = contextTraps.get(linkEvent.getDescription());
                if (trap != null)
                {
                    logger.debug("Mapped to trap: " + linkEvent.getDescription() + " => " + trap.check.getCheckType() + " " + trap.check.getCheckId());
                    Result result = new ActiveResultMO().fromCheck(trap.check);
                    // is the link ok or not?
                    if (linkEvent.getAdminState() == linkEvent.getOperationalState())
                    {
                       result.ok("Link on " + linkEvent.getDescription() + " is " + linkEvent.getOperationalState());
                    }
                    else
                    {
                       result.critical("Link on " + linkEvent.getDescription() + " is " + linkEvent.getOperationalState());
                    }
                    // publish
                    trap.resultConsumer.accept(result);
                }
                else
                {
                    logger.warn("Failed to map SNMP trap to Bergamot trap: " + context.getAgent() + " " + linkEvent.getDescription() + " " + linkEvent.getOperationalState());
                }
            }));
            // TODO query the initial state?
        }
        catch (Exception e)
        {
            logger.error("Failed to register check for " + check.getCheckType() + " " + check.getCheckId(), e);
            resultConsumer.accept(new ActiveResultMO().fromCheck(check).error(e));
        }
    }

    @Override
    public void unregister(UnregisterCheck check)
    {
    }
    
    private class LinkStateTrap
    {   
        public final RegisterCheck check;
        
        public final Consumer<Result> resultConsumer;

        public LinkStateTrap(RegisterCheck check, Consumer<Result> resultConsumer)
        {
            super();
            this.check = check;
            this.resultConsumer = resultConsumer;
        }
    }
}
