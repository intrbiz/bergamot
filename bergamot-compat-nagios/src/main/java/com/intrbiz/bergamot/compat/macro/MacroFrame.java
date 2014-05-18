package com.intrbiz.bergamot.compat.macro;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.intrbiz.bergamot.model.util.Parameter;

public final class MacroFrame
{
    public static final MacroFrame GLOBAL_MACROS = new MacroFrame(new MacroFrame[0]);
    
    private List<MacroFrame> prototypes = new LinkedList<MacroFrame>();

    private Map<String, String> macros = new TreeMap<String, String>();

    public MacroFrame()
    {
       this(GLOBAL_MACROS);
    }
    
    public MacroFrame(MacroFrame... prototypes)
    {
        super();
        this.setPrototypes(prototypes);
    }
    
    public List<MacroFrame> getPrototypes()
    {
        return Collections.unmodifiableList(this.prototypes);
    }
    
    public void addPrototype(MacroFrame prototype)
    {
        this.prototypes.add(prototype);
    }
    
    public void setPrototypes(MacroFrame... prototypes)
    {
        for (MacroFrame prototype : prototypes)
        {
            this.addPrototype(prototype);
        }
    }
    
    public void removePrototype(MacroFrame prototype)
    {
        this.prototypes.remove(prototype);
    }
    
    public void clearPrototypes()
    {
        this.prototypes.clear();
    }

    public void put(String macroName, String value)
    {
        this.macros.put(macroName, value);
    }

    public Set<String> getMacros()
    {
        Set<String> keys = new HashSet<String>();
        keys.addAll(macros.keySet());
        for (MacroFrame prototype : this.prototypes)
        {
            keys.addAll(prototype.getMacros());
        }
        return keys;
    }

    public Set<String> getLocalMacros()
    {
        Set<String> keys = new HashSet<String>();
        keys.addAll(macros.keySet());
        return keys;
    }

    public String get(String macroName)
    {
        String value = this.macros.get(macroName);
        if (value == null)
        {
            for (MacroFrame prototype : this.prototypes)
            {
                value = prototype.get(macroName);
                if (value != null) break;
            }
        }
        return value;
    }

    public String getLocal(String macroName)
    {
        return this.macros.get(macroName);
    }
    
    public boolean contains(String macroName)
    {
        if (this.macros.containsKey(macroName)) return true;
        for (MacroFrame prototype : this.prototypes)
        {
            if (prototype.contains(macroName)) return true;
        }
        return false;
    }
    
    public boolean containsLocal(String macroName)
    {
        return this.macros.containsKey(macroName);
    }

    public MacroFrame find(String macroName)
    {
        if (this.macros.containsKey(macroName)) return this;
        for (MacroFrame prototype : this.prototypes)
        {
            MacroFrame frame = prototype.find(macroName);
            if (frame != null) return frame;
        }
        return null;
    }
    
    public void remove(String macroName)
    {
        this.macros.remove(macroName);
    }
    
    public void removeAll(String macroName)
    {
        this.macros.remove(macroName);
        for (MacroFrame prototype : this.prototypes)
        {
            prototype.removeAll(macroName);
        }
    }
    
    public String toString()
    {
        return this.toString("");
    }
    
    private String toString(String padding)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\r\n");
        for (Entry<String, String> macro : this.macros.entrySet())
        {
            sb.append(padding).append(" ").append(macro.getKey()).append(" => ").append(macro.getValue()).append(",\r\n");
        }
        sb.append(padding).append(" prototypes => [\r\n");
        for (MacroFrame prototype : this.prototypes)
        {
            sb.append(padding).append(" ").append(prototype == null ? "null" : prototype.toString(padding + "  ")).append(",\r\n");
        }
        sb.append(padding).append("]}");
        return sb.toString();
    }
    
    public static MacroFrame fromParameters(List<Parameter> parameters)
    {
        MacroFrame checkFrame = new MacroFrame();
        for (Parameter parameter : parameters)
        {
            checkFrame.put(parameter.getName(), parameter.getValue());
        }
        return checkFrame;
    }
}
