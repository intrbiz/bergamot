package com.intrbiz.bergamot.model.message.config;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.MessageObject;

@JsonTypeName("bergamot.import.report")
public class BergamotValidationReportMO extends MessageObject
{
    @JsonProperty("site")
    private String site;
    
    @JsonProperty("valid")
    private boolean valid = true;
    
    @JsonProperty("warnings")
    private List<String> warnings = new LinkedList<String>();

    @JsonProperty("errors")
    private List<String> errors = new LinkedList<String>();

    public BergamotValidationReportMO()
    {
        super();
    }
    
    public BergamotValidationReportMO(String site, boolean valid, List<String> warnings, List<String> errors)
    {
        super();
        this.site = site;
        this.valid = valid;
        this.warnings.addAll(warnings);
        this.errors.addAll(errors);
    }

    public String getSite()
    {
        return site;
    }

    public void setSite(String site)
    {
        this.site = site;
    }

    public boolean isValid()
    {
        return valid;
    }

    public void setValid(boolean valid)
    {
        this.valid = valid;
    }
    
    public List<String> getWarnings()
    {
        return warnings;
    }

    public void setWarnings(List<String> warnings)
    {
        this.warnings = warnings;
    }

    public List<String> getErrors()
    {
        return errors;
    }

    public void setErrors(List<String> errors)
    {
        this.errors = errors;
    }
}