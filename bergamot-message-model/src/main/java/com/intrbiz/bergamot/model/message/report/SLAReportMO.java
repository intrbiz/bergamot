package com.intrbiz.bergamot.model.message.report;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.MessageObject;

@JsonTypeName("bergamot.report.sla")
public class SLAReportMO extends MessageObject
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("check_id")
    private UUID checkId;
    
    @JsonProperty("check_name")
    private String checkName;
    
    @JsonProperty("check_summary")
    private String checkSummary;
    
    @JsonProperty("check_description")
    private String checkDescription;
    
    @JsonProperty("sla_id")
    private UUID slaId;
    
    @JsonProperty("sla_name")
    private String slaName;
    
    @JsonProperty("sla_summary")
    private String slaSummary;
    
    @JsonProperty("sla_description")
    private String slaDescription;
    
    @JsonProperty("sla_target")
    private double slaTarget;
    
    @JsonProperty("period_name")
    private String periodName;
    
    @JsonProperty("period_summary")
    private String periodSummary;
    
    @JsonProperty("period_description")
    private String periodDescription;
    
    @JsonProperty("period_start")
    private long periodStart;
    
    @JsonProperty("period_end")
    private long periodEnd;
    
    @JsonProperty("period_alerts")
    private int periodAlerts;
    
    @JsonProperty("period_false_positives")
    private int periodFalsePositives;
    
    @JsonProperty("period_value")
    private double periodValue;
    
    @JsonProperty("period_breached")
    private boolean periodBreached;
    
    public SLAReportMO()
    {
        super();
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
    }

    public String getCheckName()
    {
        return checkName;
    }

    public void setCheckName(String checkName)
    {
        this.checkName = checkName;
    }

    public String getCheckSummary()
    {
        return checkSummary;
    }

    public void setCheckSummary(String checkSummary)
    {
        this.checkSummary = checkSummary;
    }

    public String getCheckDescription()
    {
        return checkDescription;
    }

    public void setCheckDescription(String checkDescription)
    {
        this.checkDescription = checkDescription;
    }

    public UUID getSlaId()
    {
        return slaId;
    }

    public void setSlaId(UUID slaId)
    {
        this.slaId = slaId;
    }

    public String getSlaName()
    {
        return slaName;
    }

    public void setSlaName(String slaName)
    {
        this.slaName = slaName;
    }

    public String getSlaSummary()
    {
        return slaSummary;
    }

    public void setSlaSummary(String slaSummary)
    {
        this.slaSummary = slaSummary;
    }

    public String getSlaDescription()
    {
        return slaDescription;
    }

    public void setSlaDescription(String slaDescription)
    {
        this.slaDescription = slaDescription;
    }

    public double getSlaTarget()
    {
        return slaTarget;
    }

    public void setSlaTarget(double slaTarget)
    {
        this.slaTarget = slaTarget;
    }

    public String getPeriodName()
    {
        return periodName;
    }

    public void setPeriodName(String periodName)
    {
        this.periodName = periodName;
    }

    public String getPeriodSummary()
    {
        return periodSummary;
    }

    public void setPeriodSummary(String periodSummary)
    {
        this.periodSummary = periodSummary;
    }

    public String getPeriodDescription()
    {
        return periodDescription;
    }

    public void setPeriodDescription(String periodDescription)
    {
        this.periodDescription = periodDescription;
    }

    public long getPeriodStart()
    {
        return periodStart;
    }

    public void setPeriodStart(long periodStart)
    {
        this.periodStart = periodStart;
    }

    public long getPeriodEnd()
    {
        return periodEnd;
    }

    public void setPeriodEnd(long periodEnd)
    {
        this.periodEnd = periodEnd;
    }

    public int getPeriodAlerts()
    {
        return periodAlerts;
    }

    public void setPeriodAlerts(int periodAlerts)
    {
        this.periodAlerts = periodAlerts;
    }

    public int getPeriodFalsePositives()
    {
        return periodFalsePositives;
    }

    public void setPeriodFalsePositives(int periodFalsePositives)
    {
        this.periodFalsePositives = periodFalsePositives;
    }

    public double getPeriodValue()
    {
        return periodValue;
    }

    public void setPeriodValue(double periodValue)
    {
        this.periodValue = periodValue;
    }

    public boolean isPeriodBreached()
    {
        return periodBreached;
    }

    public void setPeriodBreached(boolean periodBreached)
    {
        this.periodBreached = periodBreached;
    }
}
