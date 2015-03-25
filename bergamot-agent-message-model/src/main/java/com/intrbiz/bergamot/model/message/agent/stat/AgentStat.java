package com.intrbiz.bergamot.model.message.agent.stat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.stat.agent")
public class AgentStat extends AgentMessage
{
    @JsonProperty("processors")
    private int processors = 0;
    
    @JsonProperty("free_memory")
    private long freeMemory = 0;
    
    @JsonProperty("total_memory")
    private long totalMemory = 0;
    
    @JsonProperty("max_memory")
    private long maxMemory = 0;
    
    @JsonProperty("runtime")
    private String runtime;
    
    @JsonProperty("runtime_vendor")
    private String runtimeVendor;
    
    @JsonProperty("runtime_version")
    private String runtimeVersion;
    
    @JsonProperty("agent_vendor")
    private String agentVendor;
    
    @JsonProperty("agent_product")
    private String agentProduct;
    
    @JsonProperty("agent_version")
    private String agentVersion;
    
    @JsonProperty("user_name")
    private String userName;
    
    @JsonProperty("os_name")
    private String osName;
    
    @JsonProperty("os_version")
    private String osVersion;
    
    @JsonProperty("os_arch")
    private String osArch;

    public AgentStat()
    {
        super();
    }

    public AgentStat(AgentMessage message)
    {
        super(message);
    }

    public int getProcessors()
    {
        return processors;
    }

    public void setProcessors(int processors)
    {
        this.processors = processors;
    }

    public long getFreeMemory()
    {
        return freeMemory;
    }

    public void setFreeMemory(long freeMemory)
    {
        this.freeMemory = freeMemory;
    }

    public long getTotalMemory()
    {
        return totalMemory;
    }

    public void setTotalMemory(long totalMemory)
    {
        this.totalMemory = totalMemory;
    }

    public long getMaxMemory()
    {
        return maxMemory;
    }

    public void setMaxMemory(long maxMemory)
    {
        this.maxMemory = maxMemory;
    }

    public String getRuntime()
    {
        return runtime;
    }

    public void setRuntime(String runtime)
    {
        this.runtime = runtime;
    }

    public String getRuntimeVendor()
    {
        return runtimeVendor;
    }

    public void setRuntimeVendor(String runtimeVendor)
    {
        this.runtimeVendor = runtimeVendor;
    }

    public String getRuntimeVersion()
    {
        return runtimeVersion;
    }

    public void setRuntimeVersion(String runtimeVersion)
    {
        this.runtimeVersion = runtimeVersion;
    }

    public String getAgentVendor()
    {
        return agentVendor;
    }

    public void setAgentVendor(String agentVendor)
    {
        this.agentVendor = agentVendor;
    }

    public String getAgentProduct()
    {
        return agentProduct;
    }

    public void setAgentProduct(String agentProduct)
    {
        this.agentProduct = agentProduct;
    }

    public String getAgentVersion()
    {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion)
    {
        this.agentVersion = agentVersion;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getOsName()
    {
        return osName;
    }

    public void setOsName(String osName)
    {
        this.osName = osName;
    }

    public String getOsVersion()
    {
        return osVersion;
    }

    public void setOsVersion(String osVersion)
    {
        this.osVersion = osVersion;
    }

    public String getOsArch()
    {
        return osArch;
    }

    public void setOsArch(String osArch)
    {
        this.osArch = osArch;
    }
}
