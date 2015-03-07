package com.intrbiz.bergamot.util;

/**
 * Helper methods for working with various units
 */
public class UnitUtil
{
    public static long ki = 1024L;
    
    public static long Mi = 1024L * ki;
    
    public static long Gi = 1024L * Mi;
    
    public static long Ti = 1024L * Gi;
    
    public static long k = 1000L;
    
    public static long M = 1000L * k;
    
    public static long G = 1000L * M;
    
    public static long T = 1000L * G;
    
    /**
     * Parse an integer value respecting the SI prefix
     * @param value
     * @return a scaled integer
     */
    public static long parse(String value, long defaultValue)
    {
        try
        {
            if (value.endsWith("TiB") || value.endsWith("Tib"))
            {
                return Long.parseLong(value.substring(0, value.length() - 3)) * Ti;
            }
            else if (value.endsWith("GiB") || value.endsWith("Gib"))
            {
                return Long.parseLong(value.substring(0, value.length() - 3)) * Gi;
            }
            else if (value.endsWith("MiB") || value.endsWith("Mib"))
            {
                return Long.parseLong(value.substring(0, value.length() - 3)) * Mi;
            }
            else if (value.endsWith("kiB") || value.endsWith("kib"))
            {
                return Long.parseLong(value.substring(0, value.length() - 3)) * ki;
            }
            else if (value.endsWith("Ti"))
            {
                return Long.parseLong(value.substring(0, value.length() - 2)) * Ti;
            }
            else if (value.endsWith("Gi"))
            {
                return Long.parseLong(value.substring(0, value.length() - 2)) * Gi;
            }
            else if (value.endsWith("Mi"))
            {
                return Long.parseLong(value.substring(0, value.length() - 2)) * Mi;
            }
            else if (value.endsWith("ki"))
            {
                return Long.parseLong(value.substring(0, value.length() - 2)) * ki;
            }
            else if (value.endsWith("TB") || value.endsWith("Tb"))
            {
                return Long.parseLong(value.substring(0, value.length() - 2)) * T;
            }
            else if (value.endsWith("GB") || value.endsWith("Gb"))
            {
                return Long.parseLong(value.substring(0, value.length() - 2)) * G;
            }
            else if (value.endsWith("MB") || value.endsWith("Mb"))
            {
                return Long.parseLong(value.substring(0, value.length() - 2)) * M;
            }
            else if (value.endsWith("KB") || value.endsWith("kb"))
            {
                return Long.parseLong(value.substring(0, value.length() - 2)) * k;
            }
            else if (value.endsWith("T"))
            {
                return Long.parseLong(value.substring(0, value.length() - 1)) * T;
            }
            else if (value.endsWith("G"))
            {
                return Long.parseLong(value.substring(0, value.length() - 1)) * G;
            }
            else if (value.endsWith("M"))
            {
                return Long.parseLong(value.substring(0, value.length() - 1)) * M;
            }
            else if (value.endsWith("k"))
            {
                return Long.parseLong(value.substring(0, value.length() - 1)) * k;
            }
            else
            {
                return Long.parseLong(value);
            }
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }
    
    /**
     * Parse an real value respecting the SI prefix
     * @param value
     * @return a scaled real
     */
    public static double parse(String value, double defaultValue)
    {
        try
        {
            if (value.endsWith("TiB") || value.endsWith("Tib"))
            {
                return Double.parseDouble(value.substring(0, value.length() - 3)) * ((double) Ti);
            }
            else if (value.endsWith("GiB") || value.endsWith("Gib"))
            {
                return Double.parseDouble(value.substring(0, value.length() - 3)) * ((double) Gi);
            }
            else if (value.endsWith("MiB") || value.endsWith("Mib"))
            {
                return Double.parseDouble(value.substring(0, value.length() - 3)) * ((double) Mi);
            }
            else if (value.endsWith("kiB") || value.endsWith("kib"))
            {
                return Double.parseDouble(value.substring(0, value.length() - 3)) * ((double) ki);
            }
            else if (value.endsWith("Ti"))
            {
                return Double.parseDouble(value.substring(0, value.length() - 2)) * ((double) Ti);
            }
            else if (value.endsWith("Gi"))
            {
                return Double.parseDouble(value.substring(0, value.length() - 2)) * ((double) Gi);
            }
            else if (value.endsWith("Mi"))
            {
                return Double.parseDouble(value.substring(0, value.length() - 2)) * ((double) Mi);
            }
            else if (value.endsWith("ki"))
            {
                return Double.parseDouble(value.substring(0, value.length() - 2)) * ((double) ki);
            }
            else if (value.endsWith("TB") || value.endsWith("Tb"))
            {
                return Double.parseDouble(value.substring(0, value.length() - 2)) * ((double) T);
            }
            else if (value.endsWith("GB") || value.endsWith("Gb"))
            {
                return Double.parseDouble(value.substring(0, value.length() - 2)) * ((double) G);
            }
            else if (value.endsWith("MB") || value.endsWith("Mb"))
            {
                return Double.parseDouble(value.substring(0, value.length() - 2)) * ((double) M);
            }
            else if (value.endsWith("KB") || value.endsWith("kb"))
            {
                return Double.parseDouble(value.substring(0, value.length() - 2)) * ((double) k);
            }
            else if (value.endsWith("T"))
            {
                return Double.parseDouble(value.substring(0, value.length() - 1)) * ((double) T);
            }
            else if (value.endsWith("G"))
            {
                return Double.parseDouble(value.substring(0, value.length() - 1)) * ((double) G);
            }
            else if (value.endsWith("M"))
            {
                return Double.parseDouble(value.substring(0, value.length() - 1)) * ((double) M);
            }
            else if (value.endsWith("k"))
            {
                return Double.parseDouble(value.substring(0, value.length() - 1)) * ((double) k);
            }
            else
            {
                return Double.parseDouble(value);
            }
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }
    
    /**
     * Convert the given value in range 0 to 1 to a percentage of range 0 - 100
     */
    public static double toPercent(double value)
    {
        return value * 100D;
    }
    
    /**
     * Convert the given value within the given scale to a percentage, ie: (value / scale) * 100
     */
    public static double toPercent(double value, double scale)
    {
        return (value / scale) * 100D;
    }
    
    /**
     * Convert the given value within the given scale to a percentage, ie: (value / scale) * 100
     */
    public static double toPercent(long value, long scale)
    {
        return (((double) value) / ((double) scale)) * 100D;
    }
    
    /**
     * Convert the given percentage in range 0 to 100 to a value of range 0 - 1
     */
    public static double fromPercent(double value)
    {
        return value / 100D;
    }
    
    public static double fromPercent(long value)
    {
        return ((double) value) / 100D;
    }
    
    /**
     * Convert the given value within the given scale to a ratio, ie: (value / scale)
     */
    public static double toRatio(double value, double scale)
    {
        return (value / scale) * 100D;
    }
    
    /**
     * Convert the given value within the given scale to a ratio, ie: (value / scale)
     */
    public static double toRatio(long value, long scale)
    {
        return (((double) value) / ((double) scale)) * 100D;
    }
}
