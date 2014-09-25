package com.intrbiz.bergamot.config.validator;

import java.util.LinkedList;
import java.util.List;

public class BergamotValidationReport
{
    private final String site;
    
    private boolean valid = true;
    
    private List<String> errors = new LinkedList<String>();
    
    private List<String> warnings = new LinkedList<String>();
            
    public BergamotValidationReport(String site)
    {
        this.site = site;
    }
    
    public String getSite()
    {
        return this.site;
    }
    
    public boolean isValid()
    {
        return this.valid;
    }
    
    public List<String> getErrors()
    {
        return this.errors;
    }
    
    public List<String> getWarnings()
    {
        return this.warnings;
    }
    
    public void logError(String message)
    {
        this.valid = false;
        this.errors.add(message);
    }
    
    public void logWarn(String message)
    {
        this.errors.add(message);
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("The configuration is ").append(this.isValid() ? "valid." : "invalid!").append("\n");
        if (this.warnings.size() > 0 || this.errors.size() > 0) sb.append("Warnings: ").append(this.warnings.size()).append(", Errors: ").append(this.errors.size()).append("\n");
        for (String error : this.errors)
        {
            sb.append("Error: ").append(error).append("\n");
        }
        for (String warn : this.warnings)
        {
            sb.append("Warn: ").append(warn).append("\n");
        }
        return sb.toString();
    }
}