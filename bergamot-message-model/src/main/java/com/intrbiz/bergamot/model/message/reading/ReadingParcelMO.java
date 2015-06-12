package com.intrbiz.bergamot.model.message.reading;

import java.sql.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.ParameterMO;
import com.intrbiz.bergamot.model.message.ParameterisedMO;
import com.intrbiz.bergamot.model.message.SiteMO;
import com.intrbiz.bergamot.model.message.result.MatchOn;
import com.intrbiz.bergamot.model.message.result.MatchOnCheckId;
import com.intrbiz.bergamot.model.message.result.MatchableMO;
import com.intrbiz.gerald.polyakov.Reading;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.FloatGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.IntegerGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.LongGaugeReading;

/**
 * A collection of readings which related to a specific 
 * check
 */
@JsonTypeName("bergamot.reading.parcel")
public class ReadingParcelMO extends Message implements MatchableMO, ParameterisedMO
{    
    @JsonProperty("site_id")
    private UUID siteId;

    @JsonProperty("match_on")
    private MatchOn matchOn;
    
    /**
     * Metrics captured timestamp
     */
    @JsonProperty("captured")
    private long captured;
    
    /**
     * A collection of metric readings
     */
    @JsonProperty("readings")
    private List<Reading> readings = new LinkedList<Reading>();
    
    /**
     * Any additional metadata about this parcel
     */
    @JsonProperty("parameters")
    private List<ParameterMO> parameters = new LinkedList<ParameterMO>();
    
    public ReadingParcelMO()
    {
        super();
    }

    public UUID getSiteId()
    {
        return siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }

    public MatchOn getMatchOn()
    {
        return matchOn;
    }

    public void setMatchOn(MatchOn matchOn)
    {
        this.matchOn = matchOn;
    }

    public long getCaptured()
    {
        return captured;
    }

    public void setCaptured(long captured)
    {
        this.captured = captured;
    }

    public List<Reading> getReadings()
    {
        return readings;
    }

    public void setReadings(List<Reading> readings)
    {
        this.readings = readings;
    }

    public List<ParameterMO> getParameters()
    {
        return parameters;
    }

    public void setParameters(List<ParameterMO> parameters)
    {
        this.parameters = parameters;
    }
    
    //
    
    @JsonIgnore
    public ReadingParcelMO fromCheck(UUID checkId)
    {
        this.setSiteId(SiteMO.getSiteId(checkId));
        this.setMatchOn(new MatchOnCheckId(checkId));
        this.setId(UUID.randomUUID());
        this.setCaptured(System.currentTimeMillis());
        return this;
    }
    
    @JsonIgnore
    public ReadingParcelMO matchOn(UUID siteId, MatchOn match)
    {
        this.setSiteId(siteId);
        this.setMatchOn(match);
        this.setId(UUID.randomUUID());
        this.setCaptured(System.currentTimeMillis());
        return this;
    }
    
    @JsonIgnore
    public ReadingParcelMO captured(long captured)
    {
        this.setCaptured(captured);
        return this;
    }
    
    @JsonIgnore
    public ReadingParcelMO captured(Date captured)
    {
        this.setCaptured(captured.getTime());
        return this;
    }
    
    @JsonIgnore
    public ReadingParcelMO reading(Reading reading)
    {
        this.getReadings().add(reading);
        return this;
    }
    
    // reading helpers
    
    // double
    
    @JsonIgnore
    public ReadingParcelMO doubleGaugeReading(String name, String unit, Double value, Double warning, Double critical, Double min, Double max)
    {
        this.getReadings().add(new DoubleGaugeReading(name, unit, value, warning, critical, min, max));
        return this;
    }
    
    @JsonIgnore
    public ReadingParcelMO doubleGaugeReading(String name, String unit, Double value)
    {
        this.getReadings().add(new DoubleGaugeReading(name, unit, value));
        return this;
    }
    
    // float
    
    @JsonIgnore
    public ReadingParcelMO floatGaugeReading(String name, String unit, Float value, Float warning, Float critical, Float min, Float max)
    {
        this.getReadings().add(new FloatGaugeReading(name, unit, value, warning, critical, min, max));
        return this;
    }
    
    @JsonIgnore
    public ReadingParcelMO floatGaugeReading(String name, String unit, Float value)
    {
        this.getReadings().add(new FloatGaugeReading(name, unit, value));
        return this;
    }
    
    // long
    
    @JsonIgnore
    public ReadingParcelMO longGaugeReading(String name, String unit, Long value, Long warning, Long critical, Long min, Long max)
    {
        this.getReadings().add(new LongGaugeReading(name, unit, value, warning, critical, min, max));
        return this;
    }
    
    @JsonIgnore
    public ReadingParcelMO longGaugeReading(String name, String unit, Long value)
    {
        this.getReadings().add(new LongGaugeReading(name, unit, value));
        return this;
    }
    
    // int
    
    @JsonIgnore
    public ReadingParcelMO integerGaugeReading(String name, String unit, Integer value, Integer warning, Integer critical, Integer min, Integer max)
    {
        this.getReadings().add(new IntegerGaugeReading(name, unit, value, warning, critical, min, max));
        return this;
    }
    
    @JsonIgnore
    public ReadingParcelMO integerGaugeReading(String name, String unit, Integer value)
    {
        this.getReadings().add(new IntegerGaugeReading(name, unit, value));
        return this;
    }
}
