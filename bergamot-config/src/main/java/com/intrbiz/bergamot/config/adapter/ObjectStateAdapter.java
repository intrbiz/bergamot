package com.intrbiz.bergamot.config.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.intrbiz.bergamot.config.model.TemplatedObjectCfg.ObjectState;

public class ObjectStateAdapter extends XmlAdapter<String, ObjectState>
{   
    @Override
    public String marshal(ObjectState arg0) throws Exception
    {
        if (arg0 == null) return null;
        return  arg0.toString().toLowerCase();
    }

    @Override
    public ObjectState unmarshal(String arg0) throws Exception
    {
        if (arg0 == null) return null;
        return ObjectState.valueOf(arg0.toUpperCase());
    }
}
