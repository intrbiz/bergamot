package com.intrbiz.bergamot.model;

import java.util.LinkedList;
import java.util.List;

import com.intrbiz.bergamot.compat.config.model.ContactgroupCfg;

public class ContactGroup extends NamedObject
{
    private List<Contact> contacts = new LinkedList<Contact>();

    public ContactGroup()
    {
        super();
    }

    public List<Contact> getContacts()
    {
        return contacts;
    }

    public void setContacts(List<Contact> contacts)
    {
        this.contacts = contacts;
    }
    
    public void addContact(Contact contact)
    {
        this.contacts.add(contact);
        contact.getContactGroups().add(this);
    }
    
    public void configure(ContactgroupCfg cfg)
    {
        this.name = cfg.resolveContactgroupName();
        this.displayName = cfg.resolveAlias();
    }
}
