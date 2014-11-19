package com.intrbiz.bergamot.check.http;

import java.io.IOException;

/**
 * Information about an X509 name in easy to access form
 */
public class NameInfo
{
    private String commonName;
    
    private String organisationUnit;
    
    private String organisation;
    
    private String locality;
    
    private String state;
    
    private String country;
    
    @SuppressWarnings("restriction")
    public NameInfo(sun.security.x509.X500Name name) throws IOException
    {
        this.commonName = name.getCommonName();
        this.organisation = name.getOrganization();
        this.organisationUnit = name.getOrganizationalUnit();
        this.locality = name.getLocality();
        this.state = name.getState();
        this.country = name.getCountry();
    }

    public String getCommonName()
    {
        return commonName;
    }

    public String getOrganisationUnit()
    {
        return organisationUnit;
    }

    public String getOrganisation()
    {
        return organisation;
    }

    public String getLocality()
    {
        return locality;
    }

    public String getState()
    {
        return state;
    }

    public String getCountry()
    {
        return country;
    }
    
    public String toString()
    {
        return "CN=" + this.commonName + ", OU=" + this.organisationUnit + ", O=" + this.organisation + ", L=" + this.locality + ", S=" + this.state + ", C=" + this.country;
    }
}
