package com.intrbiz.bergamot.model;

import java.util.LinkedList;
import java.util.List;

import com.intrbiz.bergamot.compat.config.model.ContactCfg;
import com.intrbiz.bergamot.model.message.ContactMO;

public class Contact extends NamedObject
{
    private List<ContactGroup> contactGroups = new LinkedList<ContactGroup>();
    
    private String password;
    
    private String email;
    
    private String pager;
    
    private String mobile;
    
    private String phone;

    public Contact()
    {
        super();
    }

    public List<ContactGroup> getContactGroups()
    {
        return contactGroups;
    }

    public void setContactGroups(List<ContactGroup> contactGroups)
    {
        this.contactGroups = contactGroups;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
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
    
    public ContactMO toMO()
    {
        ContactMO mo = new ContactMO();
        super.toMO(mo);
        mo.setEmail(this.getEmail());
        mo.setMobile(this.getMobile());
        mo.setPager(this.getPager());
        mo.setPhone(this.getPhone());
        return mo;
    }
    
    public void configure(ContactCfg cfg)
    {
        this.name = cfg.resolveContactName();
        this.displayName = cfg.resolveAlias();
        this.email = cfg.resolveEmail();
        this.pager = cfg.resolvePager();
        this.mobile = cfg.resolvePager();
    }
}
