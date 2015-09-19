package com.intrbiz.bergamot.call.alert;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.client.fluent.Response;

import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BaseBergamotClient;
import com.intrbiz.bergamot.model.message.AlertMO;

public class AcknowledgeAlertCall extends BergamotAPICall<AlertMO>
{   
    private UUID id;
    
    private String summary;
    
    private String comment;
    
    public AcknowledgeAlertCall(BaseBergamotClient client)
    {
        super(client);
    }
    
    public AcknowledgeAlertCall id(UUID id)
    {
        this.id = id;
        return this;
    }
    
    public AcknowledgeAlertCall summary(String summary)
    {
        this.summary = summary;
        return this;
    }
    
    public AcknowledgeAlertCall comment(String comment)
    {
        this.comment = comment;
        return this;
    }
    
    public AlertMO execute()
    {
        try
        {
            Response response = execute(
                post(url("/alert/id/" + this.id + "/acknowledge"))
                .addHeader(authHeader())
                .bodyForm(
                    param("summary", this.summary),
                    param("comment", this.comment)
                )
            );
            return transcoder().decodeFromString(response.returnContent().asString(), AlertMO.class);
        }
        catch (IOException e)
        {
            throw new BergamotAPIException("Error acknoweledging alert", e);
        }
    }
}
