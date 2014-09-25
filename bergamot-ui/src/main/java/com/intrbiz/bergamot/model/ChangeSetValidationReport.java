package com.intrbiz.bergamot.model;

import java.util.LinkedList;
import java.util.List;

public class ChangeSetValidationReport
{
    private String summary;
    
    private List<ChangeValidationReport> changes = new LinkedList<ChangeValidationReport>();
    
    public ChangeSetValidationReport()
    {
        super();
    }
    
    public ChangeSetValidationReport(String summary)
    {
        super();
        this.summary = summary;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public List<ChangeValidationReport> getChanges()
    {
        return changes;
    }

    public void setChanges(List<ChangeValidationReport> changes)
    {
        this.changes = changes;
    }
    
    public boolean isValid()
    {
        return this.changes.stream().allMatch((e) -> { return e.isValid(); });
    }
}
