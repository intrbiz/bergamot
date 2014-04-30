package com.intrbiz.bergamot.model.message;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contact message object
 */
@JsonTypeName("bergamot.contact")
public class ContactMO extends NamedObjectMO
{
    @JsonProperty("email")
    private String email;

    @JsonProperty("pager")
    private String pager;

    @JsonProperty("mobile")
    private String mobile;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("engines")
    private List<String> engines = new LinkedList<String>();

    public ContactMO()
    {
        super();
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPager()
    {
        return pager;
    }

    public void setPager(String pager)
    {
        this.pager = pager;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public List<String> getEngines()
    {
        return engines;
    }

    public void setEngines(List<String> engines)
    {
        this.engines = engines;
    }
    
    public boolean hasEngine(String engine)
    {
        return this.engines.contains(engine);
    }

    public String toString()
    {
        return "contact { name: " + this.name + ", email: " + this.email + "}";
    }
}
