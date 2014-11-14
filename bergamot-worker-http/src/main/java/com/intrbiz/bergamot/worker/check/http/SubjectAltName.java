package com.intrbiz.bergamot.worker.check.http;

/**
 * A subject alternate name
 */
public class SubjectAltName
{
    private final int type;
    
    private final String name;
    
    public SubjectAltName(int type, String name)
    {
        this.type = type;
        this.name = name;
    }
    
    @SuppressWarnings("restriction")
    public boolean isDNSName()
    {
        return sun.security.x509.GeneralNameInterface.NAME_DNS == this.type;
    }

    public int getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }
    
    public String toString()
    {
        return this.isDNSName() ? "DNS NAME: " + this.name : "[" + this.type + "]: " + this.name; 
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + type;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SubjectAltName other = (SubjectAltName) obj;
        if (name == null)
        {
            if (other.name != null) return false;
        }
        else if (!name.equals(other.name)) return false;
        if (type != other.type) return false;
        return true;
    }
}
