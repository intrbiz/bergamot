package com.intrbiz.bergamot.model.message.importer;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.MessageObject;

@JsonTypeName("bergamot.import.report")
public class BergamotImportReportMO extends MessageObject
{
    @JsonProperty("site")
    private String site;
    
    @JsonProperty("successful")
    private boolean successful = true;
    
    @JsonProperty("infos")
    private List<String> info = new LinkedList<String>();

    @JsonProperty("errors")
    private List<String> errors = new LinkedList<String>();

    public BergamotImportReportMO()
    {
    }

    public String getSite()
    {
        return site;
    }

    public void setSite(String site)
    {
        this.site = site;
    }

    public boolean isSuccessful()
    {
        return successful;
    }

    public void setSuccessful(boolean successful)
    {
        this.successful = successful;
    }

    public List<String> getInfo()
    {
        return info;
    }

    public void setInfo(List<String> info)
    {
        this.info = info;
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