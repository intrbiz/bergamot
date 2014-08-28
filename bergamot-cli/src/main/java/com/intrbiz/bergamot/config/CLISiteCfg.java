package com.intrbiz.bergamot.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.configuration.Configuration;

@XmlType(name = "site")
@XmlRootElement(name = "site")
public class CLISiteCfg extends Configuration
{
    private static final long serialVersionUID = 1L;

    private String url;

    private String username;

    private String password;

    public CLISiteCfg()
    {
        super();
    }

    public CLISiteCfg(String name, String url, String username, String password)
    {
        super();
        this.setName(name);
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public void applyDefaults()
    {
    }

    @XmlAttribute(name = "url")
    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    @XmlAttribute(name = "username")
    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    @XmlAttribute(name = "password")
    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
