package com.intrbiz.bergamot.ui.permissions;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "permissions")
@XmlRootElement(name = "permissions")
public class UIPermissions
{
    private List<UIPermission> permissions = new LinkedList<UIPermission>();
    
    public UIPermissions()
    {
        super();
    }

    @XmlElementRef(type = UIPermission.class)
    public List<UIPermission> getPermissions()
    {
        return permissions;
    }

    public void setPermissions(List<UIPermission> permissions)
    {
        this.permissions = permissions;
    }
    
    public static UIPermissions load()
    {
        try
        {
            JAXBContext ctx = JAXBContext.newInstance(UIPermissions.class, UIPermission.class);
            InputStream permissionsXML = UIPermissions.class.getResourceAsStream("permissions.xml");
            return (UIPermissions) ctx.createUnmarshaller().unmarshal(permissionsXML);
        }
        catch (JAXBException e)
        {
            throw new RuntimeException(e);
        }
    }
}
