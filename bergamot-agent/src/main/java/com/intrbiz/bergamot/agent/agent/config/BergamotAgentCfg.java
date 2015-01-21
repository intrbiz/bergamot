package com.intrbiz.bergamot.agent.agent.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.configuration.Configuration;

@XmlRootElement(name = "bergamot-agent")
@XmlType(name = "bergamot-agent")
public class BergamotAgentCfg extends Configuration
{
    private static final long serialVersionUID = 1L;

    private String server;
    
    private String caCertificateFile;
    
    private String keyFile;
    
    private String certificateFile;
    
    public BergamotAgentCfg()
    {
        super();
    }

    @XmlElement(name = "ca-certificate-file")
    public String getCaCertificateFile()
    {
        return caCertificateFile;
    }

    public void setCaCertificateFile(String caCertificateFile)
    {
        this.caCertificateFile = caCertificateFile;
    }

    @XmlElement(name = "key-file")
    public String getKeyFile()
    {
        return keyFile;
    }

    public void setKeyFile(String keyFile)
    {
        this.keyFile = keyFile;
    }

    @XmlElement(name = "certificate-file")
    public String getCertificateFile()
    {
        return certificateFile;
    }

    public void setCertificateFile(String certificateFile)
    {
        this.certificateFile = certificateFile;
    }

    @XmlAttribute(name = "server")
    public String getServer()
    {
        return server;
    }

    public void setServer(String server)
    {
        this.server = server;
    }
}
