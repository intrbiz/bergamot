package com.intrbiz.bergamot.call;

import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Response;

import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BaseBergamotClient;
import com.intrbiz.bergamot.crypto.util.PEMUtil;

public class SignAgentKey extends BergamotAPICall<List<Certificate>>
{
    private String commonName;
    
    private PublicKey key;
    
    public SignAgentKey(BaseBergamotClient client)
    {
        super(client);
    }
    
    public SignAgentKey commonName(String commonName)
    {
        this.commonName = commonName;
        return this;
    }
    
    public SignAgentKey publicKey(PublicKey key)
    {
        this.key = key;
        return this;
    }
    
    public List<Certificate> execute()
    {
        try
        {
            Response response = execute(post(url("/api/agent/sign-agent-key")).addHeader(authHeader())
                     .bodyForm(
                         Form.form()
                         .add("common-name", this.commonName)
                         .add("public-key", PEMUtil.savePublicKey(this.key))
                         .build()
                     ));
            return transcoder().decodeListFromString(response.returnContent().asString(), String.class).stream()
                    .map((s)->{
                        try { return PEMUtil.loadCertificate(s); } 
                        catch (Exception e) {} 
                        return null;
                    }).collect(Collectors.toList());
        }
        catch (Exception e)
        {
            throw new BergamotAPIException("Error signing agent key", e);
        }
    }
}
