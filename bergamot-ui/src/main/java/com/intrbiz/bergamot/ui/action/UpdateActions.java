package com.intrbiz.bergamot.ui.action;

import java.util.UUID;

import com.intrbiz.balsa.action.BalsaAction;
import com.intrbiz.bergamot.model.message.event.update.Update;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.metadata.Action;

public class UpdateActions implements BalsaAction<BergamotUI>
{

    public UpdateActions()
    {
        super();
    }

    @Action("publish-update")
    public void publishUpdate(UUID siteId, Update update)
    {
        app().getProcessor().getUpdateTopic().publish(siteId, update);
    }
}
