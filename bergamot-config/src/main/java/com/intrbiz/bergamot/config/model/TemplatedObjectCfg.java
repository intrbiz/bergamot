package com.intrbiz.bergamot.config.model;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.bergamot.config.adapter.CSVAdapter;
import com.intrbiz.bergamot.config.adapter.YesNoAdapter;
import com.intrbiz.bergamot.config.resolver.BeanResolver;
import com.intrbiz.bergamot.config.resolver.ObjectResolver;
import com.intrbiz.configuration.Configuration;

public abstract class TemplatedObjectCfg<P extends TemplatedObjectCfg<P>> extends Configuration
{
    private static final long serialVersionUID = 1L;
    
    public enum ObjectState { PRESENT, ABSENT }

    private Set<String> inheritedTemplates = new LinkedHashSet<String>();

    private Boolean template;

    private List<P> inherits = new LinkedList<P>();

    private File loadedFrom;

    private ObjectState state = null;

    public TemplatedObjectCfg()
    {
        super();
    }

    @XmlTransient
    public File getLoadedFrom()
    {
        return loadedFrom;
    }

    public void setLoadedFrom(File loadedFrom)
    {
        this.loadedFrom = loadedFrom;
    }

    /**
     * The state of this object: present (default) or absent.
     * This can be used to remove objects.
     */
    @XmlAttribute(name = "state")
    public ObjectState getState()
    {
        return state;
    }
    
    public ObjectState getOrDefaultState()
    {
        return state == null ? ObjectState.PRESENT : this.state;
    }

    public void setState(ObjectState state)
    {
        this.state = state;
    }

    @XmlJavaTypeAdapter(CSVAdapter.class)
    @XmlAttribute(name = "extends")
    public Set<String> getInheritedTemplates()
    {
        return inheritedTemplates;
    }

    public void setInheritedTemplates(Set<String> inheritedTemplates)
    {
        this.inheritedTemplates = inheritedTemplates;
    }

    public boolean isInheriting(String templateName)
    {
        return this.getInheritedTemplates().contains(templateName);
    }

    @XmlJavaTypeAdapter(YesNoAdapter.class)
    @XmlAttribute(name = "template")
    public Boolean getTemplate()
    {
        return template;
    }

    @XmlTransient
    public boolean getTemplateBooleanValue()
    {
        return this.template == null ? false : this.template.booleanValue();
    }

    public void setTemplate(Boolean template)
    {
        this.template = template;
    }

    @XmlTransient
    public List<P> getInherits()
    {
        return inherits;
    }

    public void setInherits(List<P> inherits)
    {
        this.inherits = inherits;
    }

    public void addInheritedObject(P inheritedObject)
    {
        if (this.getClass() != inheritedObject.getClass()) throw new IllegalArgumentException("Cannot inheirt from an object of a different type");
        this.inherits.add(inheritedObject);
    }

    public abstract List<TemplatedObjectCfg<?>> getTemplatedChildObjects();

    /**
     * Process the inheritance hierarchy and produce a resolved configuration
     */
    @SuppressWarnings("unchecked")
    public P resolve(ObjectResolver<P> resolver)
    {
        // nothing to resolve
        if (this.inherits == null || this.inherits.isEmpty()) return (P) this;
        // process the inheritance graph
        P parent = null;
        // resolve the parents
        Iterator<P> i = this.inherits.iterator();
        parent = i.next().resolve(resolver);
        while (i.hasNext())
        {
            parent = resolver.resolve(parent, i.next());
        }
        // resolve ourselves with the resolved state of our parents
        return resolver.resolve((P) this, parent);
    }

    /**
     * Process the inheritance hierarchy and produce a resolved configuration
     */
    @SuppressWarnings("unchecked")
    public final P resolve()
    {
        return this.resolve(BeanResolver.fromClass((Class<P>) this.getClass()));
    }
}
