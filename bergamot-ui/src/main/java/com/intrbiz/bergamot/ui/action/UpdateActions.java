package com.intrbiz.bergamot.ui.action;

import java.util.UUID;

import com.intrbiz.balsa.action.BalsaAction;
import com.intrbiz.bergamot.model.message.update.Update;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Action;

public class UpdateActions implements BalsaAction<BergamotApp>
{

    public UpdateActions()
    {
        super();
    }

    @Action("publish-update")
    public void publishUpdate(UUID siteId, Update update)
    {
        app().getUpdateBroker().publish(siteId, update);
    }
}
