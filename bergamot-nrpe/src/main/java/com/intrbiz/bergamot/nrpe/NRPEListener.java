package com.intrbiz.bergamot.nrpe;

import com.intrbiz.bergamot.nrpe.model.NRPEResponse;

public interface NRPEListener
{
    void onResponse(NRPEResponse response, Object userContext);
    
    void onError(Throwable cause, Object userContext);
}
