package com.intrbiz.bergamot.model;

import java.io.StringReader;

import javax.xml.bind.JAXBException;

import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.TemplatedObjectCfg;
import com.intrbiz.bergamot.config.validator.BergamotCfgValidator;
import com.intrbiz.bergamot.config.validator.ValidatedBergamotConfiguration;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.configuration.Configuration;



public class ChangeSetApplier
{
    private final ChangeSet change;
    
    private final Site site;
    
    private ChangeSetValidationReport report;
    
    public ChangeSetApplier(ChangeSet change, Site site)
    {
        this.change = change;
        this.site = site;
    }
    
    public ChangeSet getChange()
    {
        return this.change;
    }
    
    public Site getSite()
    {
        return this.site;
    }
    
    /**
     * Validate the change set, reporting the changes 
     * that will be made and if the change is valid.
     */
    public ChangeSetValidationReport validate()
    {
        if (this.report == null)
        {
            this.report = new ChangeSetValidationReport(this.change.getSummary());
            // firstly we need to parse all the configuration
            for (Change change : this.change.getChanges())
            {
                ChangeValidationReport changeReport = new ChangeValidationReport(change);
                report.getChanges().add(changeReport);
                this.parse(changeReport);
            }
            // next we can validate each configuration
            for (ChangeValidationReport changeReport : report.getChanges())
            {
                if (changeReport.isParsed())
                {
                    // resolve and validate the object
                    ValidatedBergamotConfiguration validated = this.createValidator(changeReport.getConfiguration()).validate();
                    changeReport.setValidated(validated.getReport().isValid());
                    changeReport.setValidationReport(validated.getReport());
                }
            }
            // finally we can compute what the change will do
            for (ChangeValidationReport changeReport : report.getChanges())
            {
                if (changeReport.isParsed() && changeReport.isValidated())
                {
                    if (changeReport.getConfiguration().getTemplateBooleanValue())
                    {
                        this.computeChangeTemplate(changeReport);
                    }
                    else
                    {
                        this.computeChange(changeReport);
                    }
                }
            }
        }
        return this.report;
    }
    
    public void apply()
    {
        // ensure the change is validated
        ChangeSetValidationReport validatedReport = this.validate();
        if (! validatedReport.isValid()) throw new RuntimeException("Cannot apply an invalid configuration change, aborting!");
        // apply the change
        
    }
    
    private void parse(ChangeValidationReport report)
    {
        try
        {
            TemplatedObjectCfg<?> cfg = (TemplatedObjectCfg<?>) Configuration.read(BergamotCfg.OBJECT_TYPES, new StringReader(report.getChange().getConfiguration()));
            report.setParsed(true);
            report.setParseReport("Parsed ok");
            report.setConfiguration(cfg);
        }
        catch (JAXBException e)
        {
            report.setParsed(false);
            report.setParseReport(e.getMessage());
        }
    }
    
    private BergamotCfgValidator createValidator(TemplatedObjectCfg<?> forObject)
    {
        return new BergamotCfgValidator(new BergamotCfg(this.site.getName(), forObject))
        {
            @Override
            @SuppressWarnings("unchecked")
            public <T extends TemplatedObjectCfg<T>> T lookup(Class<T> type, String name)
            {
                // lookup in this change first
                // lookup in the db
                try (BergamotDB db = BergamotDB.connect())
                {
                    Config cfg = db.getConfigByName(site.getId(), Configuration.getRootElement(type), name);
                    return cfg == null ? null : (T) cfg.getConfiguration();
                }
            }            
        };
    }
    
    /* Compute what a change will do */
    
    private void computeChange(ChangeValidationReport change)
    {
        // are we updating an existing configuration
        try (BergamotDB db = BergamotDB.connect())
        {
            Config existing = db.getConfigByName(this.site.getId(), Configuration.getRootElement(change.getConfiguration().getClass()), change.getConfiguration().getName());
            if (existing == null)
            {
                change.setDescription("Add new " + Configuration.getRootElement(change.getConfiguration().getClass()) + ": " + change.getConfiguration().getName());
            }
            else
            {
                change.setDescription("Update existing " + Configuration.getRootElement(change.getConfiguration().getClass()) + ": " + change.getConfiguration().getName() + " (" + existing.getId() + ")");
            }
        }
    }
    
    private void computeChangeTemplate(ChangeValidationReport change)
    {
        // are we updating an existing configuration
        try (BergamotDB db = BergamotDB.connect())
        {
            Config existing = db.getConfigByName(this.site.getId(), Configuration.getRootElement(change.getConfiguration().getClass()), change.getConfiguration().getName());
            if (existing == null)
            {
                change.setDescription("Add new " + Configuration.getRootElement(change.getConfiguration().getClass()) + " template: " + change.getConfiguration().getName());
            }
            else
            {
                change.setDescription("Update existing " + Configuration.getRootElement(change.getConfiguration().getClass()) + " template: " + change.getConfiguration().getName() + " (" + existing.getId() + ")");
            }
            // TODO: cascade
        }
    }
    
    /* The hard work - apply a change */
}
