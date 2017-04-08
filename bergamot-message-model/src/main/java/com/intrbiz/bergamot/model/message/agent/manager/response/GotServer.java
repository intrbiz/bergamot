package com.intrbiz.bergamot.model.message.agent.manager.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerResponse;

@JsonTypeName("bergamot.agent.manager.got_server")
public class GotServer extends AgentManagerResponse
{
    @JsonProperty("certificate_pem")
    private String certificatePEM;
    
    @JsonProperty("root_certificate_pem")
    private String rootCertificatePEM;
    
    @JsonProperty("key_pem")
    private String keyPEM;
    
    public GotServer()
    {
        super();
    }
    
    public GotServer(String certificatePEM)
    {
        this.certificatePEM = certificatePEM;
    }

    public String getCertificatePEM()
    {
        return certificatePEM;
    }

    public void setCertificatePEM(String certificatePEM)
    {
        this.certificatePEM = certificatePEM;
    }

    public String getRootCertificatePEM()
    {
        return rootCertificatePEM;
    }

    public void setRootCertificatePEM(String rootCertificatePEM)
    {
        this.rootCertificatePEM = rootCertificatePEM;
    }

    public String getKeyPEM()
    {
        return keyPEM;
    }

    public void setKeyPEM(String keyPEM)
    {
        this.keyPEM = keyPEM;
    }
}
