package com.intrbiz.bergamot.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "broker")
@XmlRootElement(name = "broker")
public class BrokerCfg
{
    private String type;
    
    private String url;

    private String username;

    private String password;
    
    private List<String> urls = new LinkedList<String>();
    
    private String role;

    public BrokerCfg()
    {
        super();
    }

    public BrokerCfg(String type, String[] urls)
    {
        super();
        this.type = type;
        this.url = urls[0];
        for (int i = 1; i < urls.length; i++)
        {
            this.urls.add(urls[i]);
        }
    }
    
    public BrokerCfg(String role, String type, String[] urls)
    {
        this(type, urls);
        this.role = role;
    }

    public BrokerCfg(String type, String[] urls, String username, String password)
    {
        this(type, urls);
        this.username = username;
        this.password = password;
    }
    
    public BrokerCfg(String role, String type, String[] urls, String username, String password)
    {
        this(type, urls, username, password);
        this.role = role;
    }
    
    public BrokerCfg(String type, String url)
    {
        super();
        this.type = type;
        this.url = url;
    }
    
    public BrokerCfg(String type, String url, String username, String password)
    {
        this(type, url);
        this.username = username;
        this.password = password;
    }
    
    public BrokerCfg(String role, String type, String url, String username, String password)
    {
        this(type, url, username, password);
        this.role = role;
    }
    
    @XmlAttribute(name = "type")
    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
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
    
    @XmlElement(name = "url")
    public List<String> getUrls()
    {
        return urls;
    }

    public void setUrls(List<String> urls)
    {
        this.urls = urls;
    }

    @XmlAttribute(name = "role")
    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public String[] getAllUrls()
    {
        Set<String> urlSet = new HashSet<String>();
        if (this.url != null) urlSet.add(this.url);
        if (this.urls != null) urlSet.addAll(this.urls);
        List<String> urls = new LinkedList<String>(urlSet);
        Collections.shuffle(urls);
        return urls.toArray(new String[0]);
    }
}
