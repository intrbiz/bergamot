package com.intrbiz.bergamot.agent.manager.config;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.BrokerCfg;
import com.intrbiz.configuration.Configuration;

@XmlType(name = "bergamot-agent-manager")
@XmlRootElement(name = "bergamot-agent-manager")
public class BergamotAgentManagerCfg extends Configuration
{
    private static final long serialVersionUID = 1L;
    
    private CertDNCfg certName;
    
    private BergamotKeyStoreCfg keyStore;
    
    private BrokerCfg broker;
    
    public BergamotAgentManagerCfg()
    {
        super();
    }

    @XmlElementRef(type = CertDNCfg.class)
    public CertDNCfg getCertName()
    {
        return certName;
    }

    public void setCertName(CertDNCfg certName)
    {
        this.certName = certName;
    }

    @XmlElementRefs({
        @XmlElementRef(type = FileKeyStoreCfg.class)
    })
    public BergamotKeyStoreCfg getKeyStore()
    {
        return keyStore;
    }

    public void setKeyStore(BergamotKeyStoreCfg keyStore)
    {
        this.keyStore = keyStore;
    }

    @XmlElementRef(type = BrokerCfg.class)
    public BrokerCfg getBroker()
    {
        return broker;
    }

    public void setBroker(BrokerCfg broker)
    {
        this.broker = broker;
    }
}
