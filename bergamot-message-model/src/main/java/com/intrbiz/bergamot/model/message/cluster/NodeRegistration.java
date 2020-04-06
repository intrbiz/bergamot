package com.intrbiz.bergamot.model.message.cluster;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.MessageObject;

/**
 * Registration information of a Hazelcast node.
 */
@JsonTypeName("bergamot.cluster.node")
public class NodeRegistration extends MessageObject
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("private_address")
    private String privateAddress;
    
    @JsonProperty("private_port")
    private int privatePort;
    
    @JsonProperty("public_address")
    private String publicAddress;
    
    @JsonProperty("public_port")
    private int publicPort;
    
    public NodeRegistration()
    {
        super();
    }

    public String getPrivateAddress()
    {
        return this.privateAddress;
    }

    public void setPrivateAddress(String privateAddress)
    {
        this.privateAddress = privateAddress;
    }

    public int getPrivatePort()
    {
        return this.privatePort;
    }

    public void setPrivatePort(int privatePort)
    {
        this.privatePort = privatePort;
    }

    public String getPublicAddress()
    {
        return this.publicAddress;
    }

    public void setPublicAddress(String publicAddress)
    {
        this.publicAddress = publicAddress;
    }

    public int getPublicPort()
    {
        return this.publicPort;
    }

    public void setPublicPort(int publicPort)
    {
        this.publicPort = publicPort;
    }
}
