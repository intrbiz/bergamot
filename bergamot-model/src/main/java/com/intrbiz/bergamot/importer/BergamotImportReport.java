package com.intrbiz.bergamot.importer;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class BergamotImportReport implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String site;

    private boolean successful = true;
    
    private List<String> info = new LinkedList<String>();

    private List<String> errors = new LinkedList<String>();

    public BergamotImportReport(String site)
    {
        this.site = site;
    }

    public String getSite()
    {
        return this.site;
    }

    public boolean isSuccessful()
    {
        return successful;
    }
    
    public List<String> getInfo()
    {
        return this.info;
    }

    public List<String> getErrors()
    {
        return this.errors;
    }

    public void error(String message)
    {
        this.successful = false;
        this.errors.add(message);
    }

    public void info(String message)
    {
        this.info.add(message);
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("The import was ").append(this.isSuccessful() ? "successful." : "unsuccessful!").append("\n");
        for (String error : this.errors)
        {
            sb.append("Error: ").append(error).append("\n");
        }
        for (String info : this.info)
        {
            sb.append("Info: ").append(info).append("\n");
        }
        return sb.toString();
    }
}