package com.intrbiz.bergamot.agent.server.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.configuration.Configuration;

@XmlRootElement(name = "bergamot-agent-server")
@XmlType(name = "bergamot-agent-server")
public class BergamotAgentServerCfg extends Configuration
{
    private static final long serialVersionUID = 1L;

    private String caCertificateFile;
    
    private String keyFile;
    
    private String certificateFile;
    
    private boolean requireMatchingCertificate = true;
    
    private int port = 8080;
    
    public BergamotAgentServerCfg()
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

    @XmlElement(name = "require-matching-certificate")
    public boolean isRequireMatchingCertificate()
    {
        return requireMatchingCertificate;
    }

    public void setRequireMatchingCertificate(boolean requireMatchingCertificate)
    {
        this.requireMatchingCertificate = requireMatchingCertificate;
    }

    @XmlAttribute(name = "port")
    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }
}
