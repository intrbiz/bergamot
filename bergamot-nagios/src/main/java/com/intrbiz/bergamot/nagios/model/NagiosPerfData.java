package com.intrbiz.bergamot.nagios.model;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intrbiz.gerald.polyakov.Reading;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;

public class NagiosPerfData
{
    private static final Pattern VALUE_UOM_PATTERN = Pattern.compile("\\A([-0-9.]+)(%|[num]?s|[KkMGT]i?[Bb]|c)?\\z");
    
    private String label;

    private String value;
    
    private String unit;

    private String warning;

    private String critical;

    private String min;

    private String max;

    public NagiosPerfData()
    {
        super();
    }
    
    public NagiosPerfData(String label, String value, String unit, String warning, String critical, String min, String max)
    {
        super();
        this.label    = label;
        this.value    = value;
        this.unit     = unit;
        this.warning  = warning;
        this.critical = critical;
        this.min      = min;
        this.max      = max;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public String getWarning()
    {
        return warning;
    }

    public void setWarning(String warning)
    {
        this.warning = warning;
    }

    public String getCritical()
    {
        return critical;
    }

    public void setCritical(String critical)
    {
        this.critical = critical;
    }

    public String getMin()
    {
        return min;
    }

    public void setMin(String min)
    {
        this.min = min;
    }

    public String getMax()
    {
        return max;
    }

    public void setMax(String max)
    {
        this.max = max;
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.label).append("=");
        sb.append(this.value);
        if (this.unit != null) sb.append(this.unit);
        if (this.warning != null) sb.append(";").append(this.warning);
        if (this.critical != null) sb.append(";").append(this.critical);
        if (this.min != null) sb.append(";").append(this.min);
        if (this.max != null) sb.append(";").append(this.max);
        return sb.toString();
    }
    
    public Reading toReading()
    {
        if (this.unit == null || this.unit.length() == 0)
        {
            // this is a dimension-less measure, as such default to double gauge
            DoubleGaugeReading reading = new DoubleGaugeReading();
            reading.setName(this.label);
            reading.setValue(Double.parseDouble(this.value));
            if (this.warning != null && this.warning.length() > 0) reading.setWarning(Double.parseDouble(this.warning));
            if (this.critical != null && this.critical.length() > 0) reading.setCritical(Double.parseDouble(this.critical));
            if (this.min != null && this.min.length() > 0) reading.setMin(Double.parseDouble(this.min));
            if (this.max != null && this.max.length() > 0) reading.setMax(Double.parseDouble(this.max));
            return reading;
        }
        else
        {
            // look at the unit to better select the data type
            if ("B".equals(unit) || "kB".equals(unit) || "KB".equals(unit) || "MB".equals(unit) || "GB".equals(unit) || "TB".equals(unit) || "c".equals(unit))
            {
                DoubleGaugeReading reading = new DoubleGaugeReading();
                reading.setName(this.label);
                reading.setUnit(this.unit);
                reading.setValue(Double.parseDouble(this.value));
                if (this.warning != null && this.warning.length() > 0) reading.setWarning(Double.parseDouble(this.warning));
                if (this.critical != null && this.critical.length() > 0) reading.setCritical(Double.parseDouble(this.critical));
                if (this.min != null && this.min.length() > 0) reading.setMin(Double.parseDouble(this.min));
                if (this.max != null && this.max.length() > 0) reading.setMax(Double.parseDouble(this.max));
                return reading;
            }
            else if ("s".equals(unit) || "ms".equals(unit) || "us".equals(unit) || "ns".equals(unit) || "%".equals(unit))
            {
                DoubleGaugeReading reading = new DoubleGaugeReading();
                reading.setName(this.label);
                reading.setUnit(this.unit);
                reading.setValue(Double.parseDouble(this.value));
                if (this.warning != null && this.warning.length() > 0) reading.setWarning(Double.parseDouble(this.warning));
                if (this.critical != null && this.critical.length() > 0) reading.setCritical(Double.parseDouble(this.critical));
                if (this.min != null && this.min.length() > 0) reading.setMin(Double.parseDouble(this.min));
                if (this.max != null && this.max.length() > 0) reading.setMax(Double.parseDouble(this.max));
                return reading;
            }
        }
        return null;
    }
    
    // helpers
    
    public static List<NagiosPerfData> parsePerfData(String perfData) throws IOException
    {
        List<NagiosPerfData> perfs = new LinkedList<NagiosPerfData>();
        // parse sample: load1=0.490;15.000;30.000;0; load5=0.760;10.000;25.000;0; load15=0.850;5.000;20.000;0;
        CharBuffer buffer = CharBuffer.wrap(perfData.toCharArray());
        while (buffer.hasRemaining())
        {
            // read the label
            skipWhitespace(buffer);
            String label = readLabel(buffer);
            String valueString = readValue(buffer);
            String[] values = valueString.split(";");
            // the values
            String valueUOM = values.length > 0 ? values[0] : null;
            String warn     = values.length > 1 ? values[1] : null;
            String crit     = values.length > 2 ? values[2] : null;
            String min      = values.length > 3 ? values[3] : null;
            String max      = values.length > 4 ? values[4] : null;
            // parse the value and unit
            Matcher matcher = VALUE_UOM_PATTERN.matcher(valueUOM);
            if (matcher.matches())
            {
                String value = matcher.group(1);
                String unit  = matcher.group(2);
                // yay!
                perfs.add(new NagiosPerfData(label, value, unit, warn, crit, min, max));
            }
            else
            {
                throw new IOException("Malformed perfdata value: " + valueUOM);
            }
        }
        //
        return perfs;
    }
    
    public static void skipWhitespace(CharBuffer buffer)
    {
        while (buffer.hasRemaining())
        {
            if (! Character.isWhitespace(buffer.get()))
            {
                buffer.position(buffer.position() - 1);
                break;
            }
        }
    }
    
    public static String readValue(CharBuffer buffer) throws IOException
    {
        StringBuilder value = new StringBuilder();
        char c;
        while (buffer.hasRemaining())
        {
            c = buffer.get();
            if (Character.isWhitespace(c)) break;
            value.append(c);
        }
        return value.toString();
    }
    
    public static String readLabel(CharBuffer buffer) throws IOException
    {
        StringBuilder label = new StringBuilder();
        char start = buffer.get();
        if (start == '\'' || start == '"')
        {
            char c;
            while (buffer.hasRemaining())
            {
                c = buffer.get();
                if (c == start)
                {                    
                    if (buffer.hasRemaining())
                    {
                        char n = buffer.get();
                        if (n == start)
                        {
                            label.append(c);
                        }
                        else if (n == '=')
                        {
                            break;
                        }
                        else
                        {
                            throw new IOException("Reached end of label, was expecting: =");
                        }
                    }
                }
                else
                {
                    label.append(c);
                }
            }
        }
        else
        {
            label.append(start);
            char c;
            while ((c = buffer.get()) != '=' && buffer.hasRemaining())
            {
                label.append(c);
            }
        }
        return label.toString();
    }
}
