package com.intrbiz.bergamot.model.message.agent.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface Parameterised
{
    List<Parameter> getParameters();

    void setParameters(List<Parameter> parameters);

    default void addParameter(String name, String value)
    {
        this.getParameters().add(new Parameter(name, value));
    }

    default void setParameter(String name, String value)
    {
        this.removeParameter(name);
        this.addParameter(name, value);
    }

    default void removeParameter(String name)
    {
        this.getParameters().removeIf((p) -> {return name.equals(p.getName());});
    }

    default void clearParameters()
    {
        this.getParameters().clear();
    }

    default String getParameter(String name)
    {
        return this.getParameter(name, null);
    }

    default String getParameter(String name, String defaultValue)
    {
        return this.getParameters().stream()
                .filter((p) -> {return name.equals(p.getName());})
                .findFirst()
                .map(Parameter::getValue)
                .orElse(defaultValue);
    }
    
    default boolean containsParameter(String name)
    {
        return this.getParameter(name) != null;
    }
    
    default List<Parameter> getParameters(String name)
    {
        return this.getParameters().stream()
                .filter((p) -> {return name.equals(p.getName());})
                .collect(Collectors.toList());
    }
    
    default List<Parameter> getParametersStartingWith(final String prefix)
    {
        return this.getParameters().stream()
                .filter((p) -> {return p.getName().startsWith(prefix);})
                .collect(Collectors.toList());
    }
    
    default List<String> getParametersStartingWithValues(final String prefix)
    {
        return this.getParameters().stream()
                .filter((p) -> {return p.getName().startsWith(prefix);})
                .map(Parameter::getValue)
                .collect(Collectors.toList());
    }
    
    default int getIntParameter(String name)
    {
        return this.getIntParameter(name, -1);
    }
    
    default int getIntParameter(String name, int defaultValue)
    {
        return this.getParameters().stream()
                .filter((p) -> {return name.equals(p.getName());})
                .findFirst()
                .map(Parameter::getValue)
                .map(Integer::parseInt)
                .orElse(defaultValue);        
    }
    
    default long getLongParameter(String name)
    {
        return this.getLongParameter(name, -1);
    }
    
    default long getLongParameter(String name, long defaultValue)
    {
        return this.getParameters().stream()
                .filter((p) -> {return name.equals(p.getName());})
                .findFirst()
                .map(Parameter::getValue)
                .map(Long::parseLong)
                .orElse(defaultValue);        
    }
    
    default double getDoubleParameter(String name)
    {
        return this.getDoubleParameter(name, -1);
    }
    
    default double getDoubleParameter(String name, double defaultValue)
    {
        return this.getParameters().stream()
                .filter((p) -> {return name.equals(p.getName());})
                .findFirst()
                .map(Parameter::getValue)
                .map(Double::parseDouble)
                .orElse(defaultValue);        
    }
    
    default float getFloatParameter(String name)
    {
        return this.getFloatParameter(name, -1);
    }
    
    default float getFloatParameter(String name, float defaultValue)
    {
        return this.getParameters().stream()
                .filter((p) -> {return name.equals(p.getName());})
                .findFirst()
                .map(Parameter::getValue)
                .map(Float::parseFloat)
                .orElse(defaultValue);        
    }
    
    default boolean getBooleanParameter(String name)
    {
        return this.getBooleanParameter(name, false);
    }
    
    default boolean getBooleanParameter(String name, boolean defaultValue)
    {
        return this.getParameters().stream()
                .filter((p) -> {return name.equals(p.getName());})
                .findFirst()
                .map(Parameter::getValue)
                .map((v) -> { return "yes".equalsIgnoreCase(v) || "true".equalsIgnoreCase(v) || "on".equalsIgnoreCase(v); })
                .orElse(defaultValue);        
    }
    
    
    default Set<String> getParameterCSV(String name)
    {
        return this.getParameterCSV(name, null);
    }
    
    default Set<String> getParameterCSV(String name, String defaultValue)
    {
        String value = this.getParameter(name, defaultValue);
        if (value == null) return new LinkedHashSet<String>(); 
        Set<String> r = new LinkedHashSet<String>();
        for (String s : value.split(", ?"))
        {
            s = s.trim();
            if (s != null && s.length() > 0)
            {
                r.add(s);
            }
        }
        return r;
    }
    
    default Set<Integer> getIntParameterCSV(String name)
    {
        return this.getIntParameterCSV(name, null);
    }
    
    default Set<Integer> getIntParameterCSV(String name, String defaultValue)
    {
        return this.getParameterCSV(name, defaultValue)
                .stream()
                .map(Integer::parseInt)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
    }
    
    default Set<Long> getLongParameterCSV(String name)
    {
        return this.getLongParameterCSV(name, null);
    }
    
    default Set<Long> getLongParameterCSV(String name, String defaultValue)
    {
        return this.getParameterCSV(name, defaultValue)
                .stream()
                .map(Long::parseLong)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
    }
    
    default Set<Double> getDoubleParameterCSV(String name)
    {
        return this.getDoubleParameterCSV(name, null);
    }
    
    default Set<Double> getDoubleParameterCSV(String name, String defaultValue)
    {
        return this.getParameterCSV(name, defaultValue)
                .stream()
                .map(Double::parseDouble)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
    }
    
    default Set<Float> getFloatParameterCSV(String name)
    {
        return this.getFloatParameterCSV(name, null);
    }
    
    default Set<Float> getFloatParameterCSV(String name, String defaultValue)
    {
        return this.getParameterCSV(name, defaultValue)
                .stream()
                .map(Float::parseFloat)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
    }
}
