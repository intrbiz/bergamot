package com.intrbiz.bergamot.credentials;

import com.intrbiz.bergamot.BaseBergamotClient;
import com.intrbiz.bergamot.model.message.AuthTokenMO;

public interface ClientCredentials
{
    AuthTokenMO auth(BaseBergamotClient client);
}
