package com.intrbiz.bergamot.model.message;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;

public interface ParameterisedMO
{
    List<ParameterMO> getParameters();

    void setParameters(List<ParameterMO> parameters);

    default void addParameter(String name, String value)
    {
        this.getParameters().add(new ParameterMO(name, value));
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
                .map(ParameterMO::getValue)
                .orElse(defaultValue);
    }
    
    default boolean containsParameter(String name)
    {
        return this.getParameter(name) != null;
    }
    
    default List<ParameterMO> getParameters(String name)
    {
        return this.getParameters().stream()
                .filter((p) -> {return name.equals(p.getName());})
                .collect(Collectors.toList());
    }
    
    default List<ParameterMO> getParametersStartingWith(final String prefix)
    {
        return this.getParameters().stream()
                .filter((p) -> {return p.getName().startsWith(prefix);})
                .collect(Collectors.toList());
    }
    
    default List<String> getParametersStartingWithValues(final String prefix)
    {
        return this.getParameters().stream()
                .filter((p) -> {return p.getName().startsWith(prefix);})
                .map(ParameterMO::getValue)
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
                .map(ParameterMO::getValue)
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
                .map(ParameterMO::getValue)
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
                .map(ParameterMO::getValue)
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
                .map(ParameterMO::getValue)
                .map(Float::parseFloat)
                .orElse(defaultValue);        
    }
    
    default double getPercentParameter(String name)
    {
        return this.getFloatParameter(name, -1);
    }
    
    /**
     * Get a normalised percentage, in the range of 0 to 1 by dividing the parameter by 100
     * @param name the parameter name
     * @param defaultValue (in the range 0 to 1)
     * @return the normalised percentage value of the parameter
     */
    default double getPercentParameter(String name, double defaultValue)
    {
        return this.getParameters().stream()
                .filter((p) -> {return name.equals(p.getName());})
                .findFirst()
                .map(ParameterMO::getValue)
                .map(Double::parseDouble)
                .map((v) -> { return v / 100F; })
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
                .map(ParameterMO::getValue)
                .map((v) -> { return "yes".equalsIgnoreCase(v) || "true".equalsIgnoreCase(v) || "on".equalsIgnoreCase(v); })
                .orElse(defaultValue);        
    }
    
    default UUID getUUIDParameter(String name)
    {
        return this.getUUIDParameter(name, null);
    }

    default UUID getUUIDParameter(String name, UUID defaultValue)
    {
        return this.getParameters().stream()
                .filter((p) -> {return name.equals(p.getName());})
                .findFirst()
                .map(ParameterMO::getValue)
                .map(UUID::fromString)
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
            if (! Util.isEmpty(s))
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
    
    /**
     * Get a range parameter, eg: '0:10' for 0 to 10
     */
    default Integer[] getIntRangeParameter(String name, Integer[] defaultValue)
    {
        String value = this.getParameter(name);
        if (value == null || value.length() == 0 || ! value.contains(":")) return defaultValue;
        String[] values = value.split(":");
        if (values.length != 2) return defaultValue;
        return new Integer[] { Integer.parseInt(values[0]), Integer.parseInt(values[1]), };
    }
    
    /**
     * Get a range parameter, eg: '0:10' for 0 to 10
     */
    default Long[] getLongRangeParameter(String name, Long[] defaultValue)
    {
        String value = this.getParameter(name);
        if (value == null || value.length() == 0 || ! value.contains(":")) return defaultValue;
        String[] values = value.split(":");
        if (values.length != 2) return defaultValue;
        return new Long[] { Long.parseLong(values[0]), Long.parseLong(values[1]), };
    }
    
    /**
     * Get a range parameter, eg: '0:10' for 0 to 10
     */
    default Float[] getFloatRangeParameter(String name, Float[] defaultValue)
    {
        String value = this.getParameter(name);
        if (value == null || value.length() == 0 || ! value.contains(":")) return defaultValue;
        String[] values = value.split(":");
        if (values.length != 2) return defaultValue;
        return new Float[] { Float.parseFloat(values[0]), Float.parseFloat(values[1]), };
    }
    
    /**
     * Get a range parameter, eg: '0:10' for 0 to 10
     */
    default Double[] getDoubleRangeParameter(String name, Double[] defaultValue)
    {
        String value = this.getParameter(name);
        if (value == null || value.length() == 0 || ! value.contains(":")) return defaultValue;
        String[] values = value.split(":");
        if (values.length != 2) return defaultValue;
        return new Double[] { Double.parseDouble(values[0]), Double.parseDouble(values[1]), };
    }
}
