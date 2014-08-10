package com.intrbiz.bergamot.ui.action;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.model.ContactCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.metadata.Action;

public class ContactActions
{
    private Logger logger = Logger.getLogger(ContactActions.class);
    
    @Action("create-contact")
    public Contact createContact(ContactCfg config)
    {
        if (config.getId() == null) throw new IllegalArgumentException("Config must have a valid Id");
        try (BergamotDB db = BergamotDB.connect())
        {
            // resolve the config
            db.getConfigResolver(Site.getSiteId(config.getId())).resolveInherit(config);
            // store the config
            db.setConfig(new Config(config.getId(), Site.getSiteId(config.getId()), config));
            // create the team
            Contact contact = new Contact();
            contact.configure(config);
            // store it?
            if (! config.getTemplateBooleanValue())
            {
                logger.info("Storing contact: " + contact.toJSON());
                db.setContact(contact);
            }
            return contact;
        }
    }
    
    @Action("set-password")
    public boolean setPassword(Contact contact, String newPassword)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            // change it
            contact.hashPassword(newPassword);
            // store it
            logger.info("Setting password for contact: " + contact.getId() + " " + contact.getName());
            db.setContact(contact);
            return true;
        }
    }
}
