package com.intrbiz.bergamot.agent.agent.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.Util;
import com.intrbiz.configuration.Configuration;

@XmlRootElement(name = "bergamot-agent")
@XmlType(name = "bergamot-agent")
public class BergamotAgentCfg extends Configuration
{
    private static final long serialVersionUID = 1L;

    private String server;

    private String caCertificate;
    
    private String siteCaCertificate;

    private String key;

    private String certificate;

    public BergamotAgentCfg()
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
    
    @XmlElement(name = "site-ca-certificate")
    public String getSiteCaCertificate()
    {
        return siteCaCertificate;
    }

    public String getSiteCaCertificateTrimmed()
    {
        return trim(this.getSiteCaCertificate());
    }

    public void setSiteCaCertificate(String siteCaCertificate)
    {
        this.siteCaCertificate = siteCaCertificate;
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

    @XmlAttribute(name = "server")
    public String getServer()
    {
        return server;
    }

    public void setServer(String server)
    {
        this.server = server;
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
                if (!Util.isEmpty(l)) out.append(l.trim()).append("\r\n");
            }
        }
        catch (IOException e)
        {
        }
        String trimmed = out.toString();
        return Util.isEmpty(trimmed) ? null : trimmed;
    }
}
