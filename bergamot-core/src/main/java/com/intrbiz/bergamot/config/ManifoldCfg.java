package com.intrbiz.bergamot.config;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.manifold.router.DefaultRouter;
import com.intrbiz.configuration.Configuration;

@XmlType(name = "manifold")
@XmlRootElement(name = "manifold")
public class ManifoldCfg extends Configuration
{
    private List<ExchangeCfg> exchanges = new LinkedList<ExchangeCfg>();

    private List<RouterCfg> routers = new LinkedList<RouterCfg>();

    public ManifoldCfg()
    {
        super();
    }

    @XmlElementRef(type = ExchangeCfg.class)
    public List<ExchangeCfg> getExchanges()
    {
        return exchanges;
    }

    public void setExchanges(List<ExchangeCfg> exchanges)
    {
        this.exchanges = exchanges;
    }

    @XmlElementRef(type = RouterCfg.class)
    public List<RouterCfg> getRouters()
    {
        return routers;
    }

    public void setRouters(List<RouterCfg> routers)
    {
        this.routers = routers;
    }
    
    public void applyDefaults()
    {
        if (this.routers.isEmpty())
        {
            // default router
            this.routers.add(new RouterCfg(DefaultRouter.class));
        }
        if (this.exchanges.isEmpty())
        {
            // default exchanges
            this.exchanges.add(new ExchangeCfg("bergamot.check.nagios", "topic", true, "all"));
            this.exchanges.add(new ExchangeCfg("bergamot.check.nrpe",   "topic", true, "all"));
            this.exchanges.add(new ExchangeCfg("bergamot.result",       "topic", true, "all"));
            this.exchanges.add(new ExchangeCfg("bergamot.notification", "topic", true, "all"));
        }
    }
}
