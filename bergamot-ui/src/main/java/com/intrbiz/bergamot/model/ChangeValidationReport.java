package com.intrbiz.bergamot.model;

import java.util.LinkedList;
import java.util.List;

import com.intrbiz.bergamot.config.model.TemplatedObjectCfg;
import com.intrbiz.bergamot.config.validator.BergamotValidationReport;

public class ChangeValidationReport
{
    /* The change */
    private Change change;

    /* Configuration validation */
    private boolean parsed;

    private String parseReport;

    private boolean validated;

    private BergamotValidationReport validationReport;

    /* Parsed configuration */
    private TemplatedObjectCfg<?> configuration;

    /* Objects this change cascade too */
    private List<String> cascades = new LinkedList<String>();

    /* Description of the effects */
    private String description;

    public ChangeValidationReport()
    {
        super();
    }

    public ChangeValidationReport(Change change)
    {
        super();
        this.change = change;
    }

    public Change getChange()
    {
        return change;
    }

    public void setChange(Change change)
    {
        this.change = change;
    }

    public boolean isParsed()
    {
        return parsed;
    }

    public void setParsed(boolean parsed)
    {
        this.parsed = parsed;
    }

    public String getParseReport()
    {
        return parseReport;
    }

    public void setParseReport(String parseReport)
    {
        this.parseReport = parseReport;
    }

    public boolean isValidated()
    {
        return validated;
    }

    public void setValidated(boolean validated)
    {
        this.validated = validated;
    }

    public BergamotValidationReport getValidationReport()
    {
        return validationReport;
    }

    public void setValidationReport(BergamotValidationReport validationReport)
    {
        this.validationReport = validationReport;
    }

    public TemplatedObjectCfg<?> getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(TemplatedObjectCfg<?> configuration)
    {
        this.configuration = configuration;
    }

    public List<String> getCascades()
    {
        return cascades;
    }

    public void setCascades(List<String> cascades)
    {
        this.cascades = cascades;
    }

    public void addCascade(String cascade)
    {
        this.cascades.add(cascade);
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
    
    public boolean isValid()
    {
        return this.parsed && this.validated;
    }
}
