package com.intrbiz.bergamot.model.report;

import java.sql.Timestamp;
import java.util.EnumSet;
import java.util.UUID;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.BergamotObject;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.message.report.SLAReportMO;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLTable(schema = BergamotDB.class, name = "sla_report", virtual = true, since = @SQLVersion({4, 0, 0}))
public class SLAReport extends BergamotObject<SLAReportMO>
{
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 1, name = "check_id", since = @SQLVersion({4, 0, 0}))
    private UUID checkId;

    @SQLColumn(index = 2, name = "check_name", since = @SQLVersion({4, 0, 0}))
    private String checkName;

    @SQLColumn(index = 3, name = "check_summary", since = @SQLVersion({4, 0, 0}))
    private String checkSummary;

    @SQLColumn(index = 4, name = "check_description", since = @SQLVersion({4, 0, 0}))
    private String checkDescription;

    @SQLColumn(index = 5, name = "sla_id", since = @SQLVersion({4, 0, 0}))
    private UUID slaId;

    @SQLColumn(index = 6, name = "sla_name", since = @SQLVersion({4, 0, 0}))
    private String slaName;

    @SQLColumn(index = 7, name = "sla_summary", since = @SQLVersion({4, 0, 0}))
    private String slaSummary;

    @SQLColumn(index = 8, name = "sla_description", since = @SQLVersion({4, 0, 0}))
    private String slaDescription;

    @SQLColumn(index = 9, name = "sla_target", since = @SQLVersion({4, 0, 0}))
    private double slaTarget;

    @SQLColumn(index = 10, name = "period_name", since = @SQLVersion({4, 0, 0}))
    private String periodName;

    @SQLColumn(index = 11, name = "period_summary", since = @SQLVersion({4, 0, 0}))
    private String periodSummary;

    @SQLColumn(index = 12, name = "period_description", since = @SQLVersion({4, 0, 0}))
    private String periodDescription;

    @SQLColumn(index = 13, name = "period_start", since = @SQLVersion({4, 0, 0}))
    private Timestamp periodStart;

    @SQLColumn(index = 14, name = "period_end", since = @SQLVersion({4, 0, 0}))
    private Timestamp periodEnd;

    @SQLColumn(index = 15, name = "period_alerts", since = @SQLVersion({4, 0, 0}))
    private int periodAlerts;

    @SQLColumn(index = 16, name = "period_false_positives", since = @SQLVersion({4, 0, 0}))
    private int periodFalsePositives;

    @SQLColumn(index = 17, name = "period_value", since = @SQLVersion({4, 0, 0}))
    private double periodValue;

    @SQLColumn(index = 18, name = "period_breached", since = @SQLVersion({4, 0, 0}))
    private boolean periodBreached;

    public SLAReport()
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

    public Timestamp getPeriodStart()
    {
        return periodStart;
    }

    public void setPeriodStart(Timestamp periodStart)
    {
        this.periodStart = periodStart;
    }

    public Timestamp getPeriodEnd()
    {
        return periodEnd;
    }

    public void setPeriodEnd(Timestamp periodEnd)
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

    @Override
    public SLAReportMO toMO(Contact contact, EnumSet<MOFlag> options)
    {
        SLAReportMO mo = new SLAReportMO();
        mo.setCheckId(this.checkId);
        mo.setCheckName(this.checkName);
        mo.setCheckSummary(this.checkSummary);
        mo.setCheckDescription(this.checkDescription);
        mo.setSlaId(this.slaId);
        mo.setSlaName(this.slaName);
        mo.setSlaSummary(this.slaSummary);
        mo.setSlaDescription(this.slaDescription);
        mo.setSlaTarget(this.slaTarget);
        mo.setPeriodName(this.periodName);
        mo.setPeriodSummary(this.periodSummary);
        mo.setPeriodDescription(this.periodDescription);
        mo.setPeriodStart(this.periodStart.getTime());
        mo.setPeriodEnd(this.periodEnd.getTime());
        mo.setPeriodAlerts(this.periodAlerts);
        mo.setPeriodFalsePositives(this.periodFalsePositives);
        mo.setPeriodValue(this.periodValue);
        mo.setPeriodBreached(this.periodBreached);
        return mo;
    }
}
