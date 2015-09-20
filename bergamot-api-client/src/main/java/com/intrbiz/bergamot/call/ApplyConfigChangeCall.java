package com.intrbiz.bergamot.call;

import java.io.IOException;

import org.apache.http.Consts;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;

import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BaseBergamotClient;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.api.call.AppliedConfigChange;
import com.intrbiz.bergamot.model.message.api.error.APIError;

public class ApplyConfigChangeCall extends BergamotAPICall<AppliedConfigChange>
{   
    private BergamotCfg configChange;
    
    private BergamotTranscoder transcoder = new BergamotTranscoder();
    
    public ApplyConfigChangeCall(BaseBergamotClient client)
    {
        super(client);
    }
    
    public ApplyConfigChangeCall configChange(BergamotCfg configChange)
    {
        this.configChange = configChange;
        return this;
    }
    
    public AppliedConfigChange execute()
    {
        try
        {
            Response response = execute(post(url("/api/config/apply")).addHeader(authHeader()).bodyString(this.configChange.toString(), ContentType.create("application/xml", Consts.UTF_8)));
            if (response.returnResponse().getStatusLine().getStatusCode() == 200)
            {
                return this.transcoder.decodeFromString(response.returnContent().asString(), AppliedConfigChange.class);
            }
            else
            {
                // oopsie
                // look at the API error
                APIError error = this.transcoder.decodeFromString(response.returnContent().asString(), APIError.class);
                throw new BergamotAPIException("Failed to apply configuration change: " + error.getMessage());
            }
        }
        catch (IOException e)
        {
            throw new BergamotAPIException("Error apply configuration change", e);
        }
    }
}
