package com.intrbiz.bergamot.compat.config.model;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import com.intrbiz.bergamot.compat.config.builder.metadata.ParameterName;
import com.intrbiz.bergamot.compat.config.parser.model.ObjectParameter;

public abstract class ConfigObject<P extends ConfigObject<P>>
{
    private List<String> inherits = new LinkedList<String>();

    private List<P> inheritedObjects = new LinkedList<P>();

    private String name;

    private Boolean register = true;

    public ConfigObject()
    {
        super();
    }

    public List<P> getInheritedObjects()
    {
        return inheritedObjects;
    }

    public void setInheritedObjects(List<P> inheritedObjects)
    {
        this.inheritedObjects = inheritedObjects;
    }

    public void addInheritedObject(P inheritedObject)
    {
        if (this.getClass() != inheritedObject.getClass()) throw new IllegalArgumentException("Cannot inheirt from an object of a different type");
        this.inheritedObjects.add(inheritedObject);
    }

    public List<String> getInherits()
    {
        return inherits;
    }

    @ParameterName("use")
    public void setInherits(List<String> inherits)
    {
        this.inherits = inherits;
    }

    public void addInherit(String inherit)
    {
        this.inherits.add(inherit);
    }

    public Boolean isRegister()
    {
        return register;
    }

    @ParameterName("register")
    public void setRegister(Boolean register)
    {
        this.register = register;
    }

    public String getName()
    {
        return name;
    }

    @ParameterName("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public boolean unhandledObjectParameter(ObjectParameter parameter)
    {
        return false;
    }

    /**
     * Resolve a parameter based on the inheritance hierarchy
     * 
     * @param accessor
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T resolveProperty(Function<P, T> accessor)
    {
        T r = accessor.apply((P) this);
        if (r == null)
        {
            // iterate and recurse up the tree
            for (P parent : this.getInheritedObjects())
            {
                r = parent.resolveProperty(accessor);
                if (r != null) break;
            }
        }
        return r;
    }
}
