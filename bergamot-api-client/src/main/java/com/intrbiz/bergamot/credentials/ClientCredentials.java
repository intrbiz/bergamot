package com.intrbiz.bergamot.credentials;

import com.intrbiz.bergamot.BergamotClient;
import com.intrbiz.bergamot.model.message.AuthTokenMO;

public interface ClientCredentials
{
    AuthTokenMO auth(BergamotClient client);
}
