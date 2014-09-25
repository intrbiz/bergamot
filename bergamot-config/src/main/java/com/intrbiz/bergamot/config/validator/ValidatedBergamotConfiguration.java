package com.intrbiz.bergamot.config.validator;

import com.intrbiz.bergamot.config.model.BergamotCfg;

public class ValidatedBergamotConfiguration
{
    private final BergamotCfg config;
    
    private final BergamotValidationReport report;
    
    public ValidatedBergamotConfiguration(BergamotCfg config, BergamotValidationReport report)
    {
        this.config = config;
        this.report = report;
    }

    public BergamotCfg getConfig()
    {
        return config;
    }

    public BergamotValidationReport getReport()
    {
        return report;
    }
}