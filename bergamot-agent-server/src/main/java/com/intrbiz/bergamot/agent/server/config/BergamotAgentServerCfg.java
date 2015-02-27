package com.intrbiz.bergamot.agent.server.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.Util;
import com.intrbiz.configuration.Configuration;

@XmlRootElement(name = "bergamot-agent-server")
@XmlType(name = "bergamot-agent-server")
public class BergamotAgentServerCfg extends Configuration
{
    private static final long serialVersionUID = 1L;

    private String caCertificate;
    
    private String key;
    
    private String certificate;
    
    private boolean requireMatchingCertificate = true;
    
    private int port = 8080;
    
    public BergamotAgentServerCfg()
    {
        super();
    }

    @XmlElement(name = "ca-certificate")
    public String getCaCertificate()
    {
        return caCertificate;
    }
    
    public String getCaCertificateTrimmed()
    {
        return trim(this.getCaCertificate());
    }

    public void setCaCertificate(String caCertificate)
    {
        this.caCertificate = caCertificate;
    }

    @XmlElement(name = "key")
    public String getKey()
    {
        return key;
    }
    
    public String getKeyTrimmed()
    {
        return trim(this.getKey());
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    @XmlElement(name = "certificate")
    public String getCertificate()
    {
        return certificate;
    }
    
    public String getCertificateTrimmed()
    {
        return trim(this.getCertificate());
    }

    public void setCertificate(String certificate)
    {
        this.certificate = certificate;
    }

    @XmlAttribute(name = "require-matching-certificate")
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
    
    private static String trim(String in)
    {
        if (Util.isEmpty(in)) return null;
        StringBuilder out = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new StringReader(in)))
        {
            String l;
            while ((l = r.readLine()) != null)
            {
                if (! Util.isEmpty(l)) out.append(l.trim()).append("\r\n");
            }
        }
        catch (IOException e)
        {
        }
        String trimmed = out.toString();
        return Util.isEmpty(trimmed) ? null : trimmed;
    }
}
