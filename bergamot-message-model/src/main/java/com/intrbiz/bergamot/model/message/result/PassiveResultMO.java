package com.intrbiz.bergamot.model.message.result;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.event.check.CheckEvent;

/**
 * The result of a passive check
 */
@JsonTypeName("bergamot.result.passive")
public class PassiveResultMO extends Result
{
    public PassiveResultMO()
    {
        super();
    }

    @Override
    public PassiveResultMO fromCheck(CheckEvent check)
    {
        return (PassiveResultMO) super.fromCheck(check);
    }

    @Override
    public PassiveResultMO passive(UUID checkId)
    {
        return (PassiveResultMO) super.passive(checkId);
    }

    @Override
    public PassiveResultMO pending(String output)
    {
        return (PassiveResultMO) super.pending(output);
    }

    @Override
    public PassiveResultMO ok(String output)
    {
        return (PassiveResultMO) super.ok(output);
    }

    @Override
    public PassiveResultMO warning(String output)
    {
        return (PassiveResultMO) super.warning(output);
    }

    @Override
    public PassiveResultMO critical(String output)
    {
        return (PassiveResultMO) super.critical(output);
    }

    @Override
    public PassiveResultMO unknown(String output)
    {
        return (PassiveResultMO) super.unknown(output);
    }

    @Override
    public PassiveResultMO error(Throwable t)
    {
        return (PassiveResultMO) super.error(t);
    }

    @Override
    public PassiveResultMO error(String message)
    {
        return (PassiveResultMO) super.error(message);
    }

    @Override
    public PassiveResultMO timeout(String message)
    {
        return (PassiveResultMO) super.timeout(message);
    }
}
