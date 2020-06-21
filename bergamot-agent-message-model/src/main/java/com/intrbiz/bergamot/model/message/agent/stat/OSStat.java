package com.intrbiz.bergamot.model.message.agent.stat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

@JsonTypeName("bergamot.agent.stat.os")
public class OSStat extends Message
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("arch")
    private String arch;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("machine")
    private String machine;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("patch-level")
    private String patchLevel;
    
    @JsonProperty("vendor")
    private String vendor;
    
    @JsonProperty("vendor-name")
    private String vendorName;
    
    @JsonProperty("vendor-code-name")
    private String vendorCodeName;
    
    @JsonProperty("vendor-version")
    private String vendorVersion;

    public OSStat()
    {
        super();
    }

    public OSStat(Message message)
    {
        super(message);
    }

    public String getArch()
    {
        return arch;
    }

    public void setArch(String arch)
    {
        this.arch = arch;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getMachine()
    {
        return machine;
    }

    public void setMachine(String machine)
    {
        this.machine = machine;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getPatchLevel()
    {
        return patchLevel;
    }

    public void setPatchLevel(String patchLevel)
    {
        this.patchLevel = patchLevel;
    }

    public String getVendor()
    {
        return vendor;
    }

    public void setVendor(String vendor)
    {
        this.vendor = vendor;
    }

    public String getVendorName()
    {
        return vendorName;
    }

    public void setVendorName(String vendorName)
    {
        this.vendorName = vendorName;
    }

    public String getVendorCodeName()
    {
        return vendorCodeName;
    }

    public void setVendorCodeName(String vendorCodeName)
    {
        this.vendorCodeName = vendorCodeName;
    }

    public String getVendorVersion()
    {
        return vendorVersion;
    }

    public void setVendorVersion(String vendorVersion)
    {
        this.vendorVersion = vendorVersion;
    }
}
