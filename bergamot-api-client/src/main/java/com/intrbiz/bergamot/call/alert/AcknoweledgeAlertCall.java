package com.intrbiz.bergamot.call.alert;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.http.client.fluent.Response;

import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BergamotClient;
import com.intrbiz.bergamot.model.message.AlertMO;

public class AcknoweledgeAlertCall extends BergamotAPICall<List<AlertMO>>
{   
    private UUID id;
    
    private String summary;
    
    private String comment;
    
    public AcknoweledgeAlertCall(BergamotClient client)
    {
        super(client);
    }
    
    public AcknoweledgeAlertCall id(UUID id)
    {
        this.id = id;
        return this;
    }
    
    public AcknoweledgeAlertCall summary(String summary)
    {
        this.summary = summary;
        return this;
    }
    
    public AcknoweledgeAlertCall comment(String comment)
    {
        this.comment = comment;
        return this;
    }
    
    public List<AlertMO> execute()
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
            return transcoder().decodeListFromString(response.returnContent().asString(), AlertMO.class);
        }
        catch (IOException e)
        {
            throw new BergamotAPIException("Error acknoweledging alert", e);
        }
    }
}
