package com.intrbiz.bergamot.model;

public class Permission implements Comparable<Permission>
{
    private final String permission;
    
    protected Permission(String permission)
    {
        this.permission = permission;
    }
    
    @Override
    public int compareTo(Permission o)
    {
        return this.permission.compareTo(o.permission);
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((permission == null) ? 0 : permission.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Permission other = (Permission) obj;
        if (permission == null)
        {
            if (other.permission != null) return false;
        }
        else if (!permission.equals(other.permission)) return false;
        return true;
    }

    public String toString()
    {
        return this.permission;
    }
    
    public static Permission of(String permission)
    {
        return new Permission(permission);
    }
    
    /**
     * Match a granted or revoked permission against this permission
     * @param granted the permission pattern which has been granted of revoked
     * @return true if the given granted permission equals or contains this permission
     */
    public boolean match(Permission grantedPermission)
    {
        String granted = grantedPermission.toString();
        if (granted.equals(permission))
        {
            return true;
        }
        else if (granted.endsWith("*") && permission.startsWith(granted.substring(0, granted.length() - 1)))
        {
            return true;
        }
        return false;
    }
}
