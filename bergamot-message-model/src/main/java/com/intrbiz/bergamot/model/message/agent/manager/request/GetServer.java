package com.intrbiz.bergamot.model.message.agent.manager.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;

@JsonTypeName("bergamot.agent.manager.get_agent")
public class GetServer extends AgentManagerRequest
{
    @JsonProperty("common_name")
    private String commonName;
    
    @JsonProperty("include_root")
    private boolean includeRoot = false;

    @JsonProperty("include_key")
    private boolean includeKey = false;

    @JsonProperty("generate")
    private boolean generate = false;

    public GetServer()
    {
        super();
    }

    public GetServer(String commonName)
    {
        super();
        this.commonName = commonName;
    }
    
    public GetServer withRoot()
    {
        this.includeRoot = true;
        return this;
    }
    
    public GetServer withKey()
    {
        this.includeKey = true;
        return this;
    }
    
    public GetServer withGenerate()
    {
        this.generate = true;
        return this;
    }

    public boolean isIncludeRoot()
    {
        return includeRoot;
    }

    public void setIncludeRoot(boolean includeRoot)
    {
        this.includeRoot = includeRoot;
    }

    public boolean isIncludeKey()
    {
        return includeKey;
    }

    public void setIncludeKey(boolean includeKey)
    {
        this.includeKey = includeKey;
    }

    public boolean isGenerate()
    {
        return generate;
    }

    public void setGenerate(boolean generate)
    {
        this.generate = generate;
    }

    public String getCommonName()
    {
        return commonName;
    }

    public void setCommonName(String commonName)
    {
        this.commonName = commonName;
    }
    
    
}
