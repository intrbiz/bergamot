package com.intrbiz.bergamot.ui.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.Service;

public class Sorter
{
    public static <T extends Location> List<T> orderLocationsByStatus(Collection<T> toSort)
    {
        List<T> ret = new LinkedList<T>(toSort);
        Collections.sort(ret, (a, b) -> { return a.getState().getStatus() == b.getState().getStatus() ? a.getDisplayName().compareTo(b.getDisplayName()) : b.getState().getStatus().compareTo(a.getState().getStatus()); });
        return ret;
    }
    
    public static <T extends Group> List<T> orderGroupsByStatus(Collection<T> toSort)
    {
        List<T> ret = new LinkedList<T>(toSort);
        Collections.sort(ret, (a,b) -> { return a.getState().getStatus() == b.getState().getStatus() ? a.getDisplayName().compareTo(b.getDisplayName()) : b.getState().getStatus().compareTo(a.getState().getStatus()); });
        return ret;
    }
    
    public static <T extends Host> List<T> orderHostsByStatus(Collection<T> toSort)
    {
        List<T> ret = new LinkedList<T>(toSort);
        Collections.sort(ret, (a, b) -> { return a.getState().getStatus() == b.getState().getStatus() ? a.getDisplayName().compareTo(b.getDisplayName()) :b.getState().getStatus().compareTo(a.getState().getStatus()); });
        return ret;
    }
    
    public static <T extends Service> List<T> orderServicesByStatus(Collection<T> toSort)
    {
        List<T> ret = new LinkedList<T>(toSort);
        Collections.sort(ret, (a,b) -> { return a.getState().getStatus() == b.getState().getStatus() ? (a.getDisplayName().equals(b.getDisplayName()) ? a.getHost().getDisplayName().compareTo(b.getHost().getDisplayName()) : a.getDisplayName().compareTo(b.getDisplayName())) : b.getState().getStatus().compareTo(a.getState().getStatus()); });
        return ret;
    }
    
    public static <T extends Check> List<T> orderCheckByStatus(Collection<T> toSort)
    {
        List<T> ret = new LinkedList<T>(toSort);
        Collections.sort(ret, (a, b) -> { return a.getState().getStatus() == b.getState().getStatus() ? a.getDisplayName().compareTo(b.getDisplayName()) : b.getState().getStatus().compareTo(a.getState().getStatus()); });
        return ret;
    }
}
