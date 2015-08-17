package com.intrbiz.bergamot.model.message.api.call;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.api.APIResponse;
import com.intrbiz.bergamot.model.message.importer.BergamotImportReportMO;

@JsonTypeName("bergamot.api.applied-config-change")
public class AppliedConfigChange extends APIResponse
{
    @JsonProperty("report")
    private BergamotImportReportMO report;
    
    public AppliedConfigChange()
    {
        super();
    }
    
    public AppliedConfigChange(BergamotImportReportMO report)
    {
        super(Stat.OK);
        this.report = report;
    }

    public BergamotImportReportMO getReport()
    {
        return report;
    }

    public void setReport(BergamotImportReportMO report)
    {
        this.report = report;
    }
}
