package com.intrbiz.bergamot.call.config;

import java.io.IOException;

import org.apache.http.Consts;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;

import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BergamotClient;
import com.intrbiz.bergamot.config.model.BergamotCfg;

public class ApplyConfigChangeCall extends BergamotAPICall<String>
{   
    private BergamotCfg configChange;
    
    public ApplyConfigChangeCall(BergamotClient client)
    {
        super(client);
    }
    
    public ApplyConfigChangeCall configChange(BergamotCfg configChange)
    {
        this.configChange = configChange;
        return this;
    }
    
    public String execute()
    {
        try
        {
            Response response = execute(post(url("/config/apply")).addHeader(authHeader()).bodyString(this.configChange.toString(), ContentType.create("application/xml", Consts.UTF_8)));
            // TODO: parse the JSON
            return response.returnContent().asString();
        }
        catch (IOException e)
        {
            throw new BergamotAPIException("Error apply configuration change", e);
        }
    }
}
