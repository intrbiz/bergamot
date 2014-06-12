package com.intrbiz.bergamot.model;

import java.io.Serializable;
import java.security.Principal;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.mindrot.jbcrypt.BCrypt;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.ContactCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.adapter.ContactCfgAdapter;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLUnique;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLTable(schema = BergamotDB.class, name = "contact", since = @SQLVersion({ 1, 0, 0 }))
@SQLUnique(name = "name_unq", columns = {"site_id", "name"})
public class Contact extends NamedObject<ContactMO, ContactCfg> implements Principal, Serializable
{
    private static final long serialVersionUID = 1L;
    
    public static final int BCRYPT_WORK_FACTOR = 12;
    
    @SQLColumn(index = 1, name = "configuration", type = "TEXT", adapter = ContactCfgAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    protected ContactCfg configuration;
    
    @SQLColumn(index = 2, name = "team_ids", type = "UUID[]", since = @SQLVersion({ 1, 0, 0 }))
    private List<UUID> teamIds = new LinkedList<UUID>();

    @SQLColumn(index = 3, name = "password_hash", since = @SQLVersion({ 1, 0, 0 }))
    private String passwordHash;

    @SQLColumn(index = 4, name = "email", since = @SQLVersion({ 1, 0, 0 }))
    @SQLUnique(name = "email_unq", columns = {"site_id", "email"})
    private String email;

    @SQLColumn(index = 5, name = "pager", since = @SQLVersion({ 1, 0, 0 }))
    private String pager;

    @SQLColumn(index = 6, name = "mobile", since = @SQLVersion({ 1, 0, 0 }))
    private String mobile;

    @SQLColumn(index = 7, name = "phone", since = @SQLVersion({ 1, 0, 0 }))
    private String phone;

    public Contact()
    {
        super();
    }
    
    @Override
    public ContactCfg getConfiguration()
    {
        return configuration;
    }

    @Override
    public void setConfiguration(ContactCfg configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void configure(ContactCfg cfg)
    {
        super.configure(cfg);
        ContactCfg rcfg = cfg.resolve();
        this.name = rcfg.getName();
        this.summary = Util.coalesceEmpty(rcfg.getSummary(), this.name);
        this.description = Util.coalesceEmpty(rcfg.getDescription(), "");
        // email
        this.email = rcfg.getEmail();
        // phones
        this.mobile = rcfg.getMobile();
        this.pager = rcfg.getPager();
        this.phone = rcfg.getPhone();
    }

    public List<UUID> getTeamIds()
    {
        return this.teamIds;
    }
    
    public void setTeamIds(List<UUID> teamIds)
    {
        this.teamIds = teamIds;
    }
    
    public List<Team> getTeams()
    {
        List<Team> r = new LinkedList<Team>();
        try (BergamotDB db = BergamotDB.connect())
        {
            for (UUID id : this.getTeamIds())
            {
                r.add(db.getTeam(id));
            }
        }
        return r;
    }

    public String getPasswordHash()
    {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash)
    {
        this.passwordHash = passwordHash;
    }
    
    public void hashPassword(String plainPassword)
    {
        this.passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_WORK_FACTOR));
    }
    
    public boolean verifyPassword(String plainPassword)
    {
        return BCrypt.checkpw(plainPassword, this.passwordHash);
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

    public Notifications getNotifications()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getNotifications(this.getId());
        }
    }

    public void setNotifications(Notifications notifications)
    {
        // TODO
    }
    
    public String toString()
    {
        return "Contact {" + this.getName() + "}";
    }

    @Override
    public ContactMO toMO(boolean stub)
    {
        ContactMO mo = new ContactMO();
        super.toMO(mo, stub);
        mo.setEmail(this.getEmail());
        mo.setMobile(this.getMobile());
        mo.setPager(this.getPager());
        mo.setPhone(this.getPhone());
        if (!stub)
        {
            mo.setTeams(this.getTeams().stream().map(Team::toStubMO).collect(Collectors.toList()));
            mo.setNotifications(Util.nullable(this.getNotifications(), Notifications::toStubMO));
        }
        return mo;
    }
}
