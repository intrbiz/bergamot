package com.intrbiz.bergamot.agent.manager.config;

import java.io.File;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.agent.manager.store.impl.FileKeyStore;

@XmlType(name = "file-key-store")
@XmlRootElement(name = "file-key-store")
public class FileKeyStoreCfg extends BergamotKeyStoreCfg
{
    private static final long serialVersionUID = 1L;
    
    private String base;
    
    public FileKeyStoreCfg()
    {
        super();
    }

    @XmlAttribute(name = "base")
    public String getBase()
    {
        return base;
    }

    public void setBase(String base)
    {
        this.base = base;
    }

    @Override
    public Object create() throws Exception
    {
        return new FileKeyStore(new File(this.base));
    }
}
