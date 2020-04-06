package com.intrbiz.bergamot.model.message.event.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.AlertMO;

/**
 * A alert state update
 */
@JsonTypeName("bergamot.alert_update")
public class AlertUpdate extends Update
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("alert")
    private AlertMO alert;

    public AlertUpdate()
    {
        super();
    }
    
    public AlertUpdate(AlertMO alert)
    {
        super(System.currentTimeMillis());
        this.alert = alert;
    }

    public AlertMO getAlert()
    {
        return alert;
    }

    public void setAlert(AlertMO alert)
    {
        this.alert = alert;
    }
}
