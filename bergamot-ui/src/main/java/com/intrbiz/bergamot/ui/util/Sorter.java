package com.intrbiz.bergamot.ui.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.HostGroup;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.ServiceGroup;

public class Sorter
{
    public static <T extends Location> List<T> orderLocationsByStatus(Collection<T> toSort)
    {
        List<T> ret = new LinkedList<T>(toSort);
        Collections.sort(ret, new Comparator<T>(){
            @Override
            public int compare(T a, T b)
            {
                if (a.getState().getStatus() == b.getState().getStatus())
                {
                    return a.getDisplayName().compareTo(b.getDisplayName());
                }
                return b.getState().getStatus().compareTo(a.getState().getStatus());
            }
        });
        return ret;
    }
    
    public static <T extends HostGroup> List<T> orderHostGroupsByStatus(Collection<T> toSort)
    {
        List<T> ret = new LinkedList<T>(toSort);
        Collections.sort(ret, new Comparator<T>(){
            @Override
            public int compare(T a, T b)
            {
                if (a.getState().getStatus() == b.getState().getStatus())
                {
                    return a.getDisplayName().compareTo(b.getDisplayName());
                }
                return b.getState().getStatus().compareTo(a.getState().getStatus());
            }
        });
        return ret;
    }
    
    public static <T extends ServiceGroup> List<T> orderServiceGroupsByStatus(Collection<T> toSort)
    {
        List<T> ret = new LinkedList<T>(toSort);
        Collections.sort(ret, new Comparator<T>(){
            @Override
            public int compare(T a, T b)
            {
                if (a.getState().getStatus() == b.getState().getStatus())
                {
                    return a.getDisplayName().compareTo(b.getDisplayName());
                }
                return b.getState().getStatus().compareTo(a.getState().getStatus());
            }
        });
        return ret;
    }
    
    public static <T extends Host> List<T> orderHostsByStatus(Collection<T> toSort)
    {
        List<T> ret = new LinkedList<T>(toSort);
        Collections.sort(ret, new Comparator<T>(){
            @Override
            public int compare(T a, T b)
            {
                if (a.getState().getStatus() == b.getState().getStatus())
                {
                    return a.getDisplayName().compareTo(b.getDisplayName());
                }
                return b.getState().getStatus().compareTo(a.getState().getStatus());
            }
        });
        return ret;
    }
    
    public static <T extends Service> List<T> orderServicesByStatus(Collection<T> toSort)
    {
        List<T> ret = new LinkedList<T>(toSort);
        Collections.sort(ret, new Comparator<T>(){
            @Override
            public int compare(T a, T b)
            {
                if (a.getState().getStatus() == b.getState().getStatus())
                {
                    if (a.getDisplayName().equals(b.getDisplayName()))
                    {
                        return a.getHost().getDisplayName().compareTo(b.getHost().getDisplayName());
                    }
                    return a.getDisplayName().compareTo(b.getDisplayName());
                }
                return b.getState().getStatus().compareTo(a.getState().getStatus());
            }
        });
        return ret;
    }
    
    public static <T extends Check> List<T> orderCheckByStatus(Collection<T> toSort)
    {
        List<T> ret = new LinkedList<T>(toSort);
        Collections.sort(ret, new Comparator<T>(){
            @Override
            public int compare(T a, T b)
            {
                if (a.getState().getStatus() == b.getState().getStatus())
                {
                    return a.getDisplayName().compareTo(b.getDisplayName());
                }
                return b.getState().getStatus().compareTo(a.getState().getStatus());
            }
        });
        return ret;
    }
}
