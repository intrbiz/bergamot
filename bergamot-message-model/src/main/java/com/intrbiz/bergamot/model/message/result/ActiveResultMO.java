package com.intrbiz.bergamot.model.message.result;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.event.check.CheckEvent;

/**
 * The result of a active check
 */
@JsonTypeName("bergamot.result.active")
public class ActiveResultMO extends Result
{

    public ActiveResultMO()
    {
        super();
    }

    @Override
    public ActiveResultMO fromCheck(CheckEvent check)
    {
        return (ActiveResultMO) super.fromCheck(check);
    }

    @Override
    public ActiveResultMO passive(UUID checkId)
    {
        return (ActiveResultMO) super.passive(checkId);
    }

    @Override
    public ActiveResultMO pending(String output)
    {
        return (ActiveResultMO) super.pending(output);
    }

    @Override
    public ActiveResultMO ok(String output)
    {
        return (ActiveResultMO) super.ok(output);
    }

    @Override
    public ActiveResultMO warning(String output)
    {
        return (ActiveResultMO) super.warning(output);
    }

    @Override
    public ActiveResultMO critical(String output)
    {
        return (ActiveResultMO) super.critical(output);
    }

    @Override
    public ActiveResultMO unknown(String output)
    {
        return (ActiveResultMO) super.unknown(output);
    }

    @Override
    public ActiveResultMO error(Throwable t)
    {
        return (ActiveResultMO) super.error(t);
    }

    @Override
    public ActiveResultMO error(String message)
    {
        return (ActiveResultMO) super.error(message);
    }

    @Override
    public ActiveResultMO timeout(String message)
    {
        return (ActiveResultMO) super.timeout(message);
    }
}
