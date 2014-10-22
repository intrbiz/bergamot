package com.intrbiz.bergamot.config.model;

import java.io.Serializable;
import java.io.StringWriter;
import java.time.ZoneId;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The time zone of a time period
 */
@XmlType(name = "time-zone")
@XmlRootElement(name = "time-zone")
public class TimeZoneCfg implements Serializable, Comparable<TimeZoneCfg>
{
    private static final long serialVersionUID = 1L;

    private String id;

    public TimeZoneCfg()
    {
        super();
    }

    public TimeZoneCfg(String id)
    {
        super();
        this.id = id;
    }
    
    public TimeZoneCfg(ZoneId zone)
    {
        super();
        this.id = zone.getId();
    }

    /**
     * The time zone id, eg: Europe/London
     */
    @XmlAttribute(name = "id")
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public ZoneId toZoneId()
    {
        return this.getId() == null ? null : ZoneId.of(this.getId());
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TimeZoneCfg other = (TimeZoneCfg) obj;
        if (id == null)
        {
            if (other.id != null) return false;
        }
        else if (!id.equals(other.id)) return false;
        return true;
    }

    @Override
    public int compareTo(TimeZoneCfg o)
    {
        return this.id.compareTo(o.id);
    }

    public String toString()
    {
        StringWriter sw = new StringWriter();
        try
        {
            JAXBContext ctx = JAXBContext.newInstance(TimeZoneCfg.class);
            Marshaller m = ctx.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.setProperty(Marshaller.JAXB_FRAGMENT, true);
            m.marshal(this, sw);
        }
        catch (JAXBException e)
        {
        }
        return sw.toString();
    }

    public static Set<TimeZoneCfg> getAvailableTimeZones()
    {
        return new TreeSet<TimeZoneCfg>(ZoneId.getAvailableZoneIds().stream().map(TimeZoneCfg::new).collect(Collectors.toSet()));
    }
    
    public static TimeZoneCfg getSystemDefault()
    {
        return new TimeZoneCfg(ZoneId.systemDefault());
    }
}
