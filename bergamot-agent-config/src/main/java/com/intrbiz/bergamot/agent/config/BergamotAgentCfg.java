package com.intrbiz.bergamot.agent.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
        if (in == null || in.length() == 0) return null;
        StringBuilder out = new StringBuilder();
        try
        {
            BufferedReader r = new BufferedReader(new StringReader(in));
            try
            {
                String l;
                while ((l = r.readLine()) != null)
                {
                    if (!(l == null || l.length() == 0)) out.append(l.trim()).append("\r\n");
                }
            }
            finally
            {
                r.close();
            }
        }
        catch (IOException e)
        {
        }
        String trimmed = out.toString();
        return(trimmed == null || trimmed.length() == 0) ? null : trimmed;
    }
}
