package com.intrbiz.bergamot.worker.engine.script;

import java.util.ArrayList;
import java.util.List;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

/**
 * Some utility method for mapping Nashorn objects into Java objects 
 *
 */
public class NashornUtil
{
    public static String mapString(ScriptObjectMirror object, String property)
    {
        if (object == null) return null;
        Object prop = object.get(property);
        return prop == null ? null : prop.toString();
    }
    
    public static int mapInteger(ScriptObjectMirror object, String property, int defaultValue)
    {
        if (object != null)
        {
            Object prop = object.get(property);
            if (prop instanceof Number)
            {
                return ((Number) prop).intValue();
            }
        }
        return defaultValue;
    }
    
    public static long mapLong(ScriptObjectMirror object, String property, long defaultValue)
    {
        if (object != null)
        {
            Object prop = object.get(property);
            if (prop instanceof Number)
            {
                return ((Number) prop).longValue();
            }
        }
        return defaultValue;
    }
    
    public static float mapFloat(ScriptObjectMirror object, String property, float defaultValue)
    {
        if (object != null)
        {
            Object prop = object.get(property);
            if (prop instanceof Number)
            {
                return ((Number) prop).floatValue();
            }
        }
        return defaultValue;
    }
    
    public static double mapDouble(ScriptObjectMirror object, String property, double defaultValue)
    {
        if (object != null)
        {
            Object prop = object.get(property);
            if (prop instanceof Number)
            {
                return ((Number) prop).doubleValue();
            }
        }
        return defaultValue;
    }
    
    public static boolean mapBoolean(ScriptObjectMirror object, String property, boolean defaultValue)
    {
        if (object != null)
        {
            Object prop = object.get(property);
            if (prop instanceof Boolean)
            {
                return ((Boolean) prop).booleanValue();
            }
        }
        return defaultValue;
    }
    
    public static List<String> mapJsArrayOfStrings(ScriptObjectMirror object, String property, List<String> defaultValue)
    {
        if (object != null)
        {
            Object prop = object.get(property);
            if (prop instanceof ScriptObjectMirror)
            {
                ScriptObjectMirror array = (ScriptObjectMirror) prop;
                if (array.isArray())
                {
                    List<String> values = new ArrayList<String>(array.size());
                    for (int i = 0; i < array.size(); i++)
                    {
                        Object item = array.getSlot(i);
                        values.add(item == null ? null : item.toString());
                    }
                    return values;
                }
            }
        }
        return defaultValue;
    }
}
