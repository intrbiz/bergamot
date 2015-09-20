package com.intrbiz.bergamot.call;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.fluent.Response;
import org.apache.http.message.BasicNameValuePair;

import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BaseBergamotClient;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.NamedObjectCfg;
import com.intrbiz.configuration.Configuration;

public class BuildSiteConfigCall extends BergamotAPICall<BergamotCfg>
{   
    private List<Class<? extends NamedObjectCfg<?>>> types = new LinkedList<Class<? extends NamedObjectCfg<?>>>();
    
    public BuildSiteConfigCall(BaseBergamotClient client)
    {
        super(client);
    }
    
    public BuildSiteConfigCall type(Class<? extends NamedObjectCfg<?>> configurationType)
    {
        this.types.add(configurationType);
        return this;
    }
    
    public BergamotCfg execute()
    {
        try
        {
            Response response = execute(get(appendQuery(url("/api/config/site.xml"), types.stream().map((e) -> { return new BasicNameValuePair("type", Configuration.getRootElement(e));}).collect(Collectors.toList()))).addHeader(authHeader()));
            return BergamotCfg.fromString(BergamotCfg.class, response.returnContent().asString());
        }
        catch (IOException e)
        {
            throw new BergamotAPIException("Error building site configuration", e);
        }
    }
}
