package com.intrbiz.bergamot.ui.router.global;

import java.io.Serializable;

public class InstallBean implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String siteName;
    
    private String siteSummary;
    
    private String userFirstName;
    
    private String userLastName;
    
    private String userEmail;
    
    private String userMobile;
    
    private String username;
    
    private String password;
    
    private String confirmPassword;
    
    public InstallBean()
    {
        super();
    }

    public String getSiteName()
    {
        return siteName;
    }

    public void setSiteName(String siteName)
    {
        this.siteName = siteName;
    }

    public String getSiteSummary()
    {
        return siteSummary;
    }

    public void setSiteSummary(String siteSummary)
    {
        this.siteSummary = siteSummary;
    }

    public String getUserFirstName()
    {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName)
    {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName()
    {
        return userLastName;
    }

    public void setUserLastName(String userLastName)
    {
        this.userLastName = userLastName;
    }

    public String getUserEmail()
    {
        return userEmail;
    }

    public void setUserEmail(String userEmail)
    {
        this.userEmail = userEmail;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getConfirmPassword()
    {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword)
    {
        this.confirmPassword = confirmPassword;
    }

    public String getUserMobile()
    {
        return userMobile;
    }

    public void setUserMobile(String userMobile)
    {
        this.userMobile = userMobile;
    }
}
