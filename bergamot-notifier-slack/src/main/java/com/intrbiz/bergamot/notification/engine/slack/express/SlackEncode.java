package com.intrbiz.bergamot.notification.engine.slack.express;

import com.intrbiz.bergamot.notification.engine.slack.model.SlackText;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.operator.Function;
import com.intrbiz.express.operator.Operator;

public class SlackEncode extends Function
{
    public SlackEncode()
    {
        super("slack_encode");
    }

    @Override
    public boolean isIdempotent()
    {
        return true;
    }

    @Override
    public Object get(ExpressContext context, Object source) throws ExpressException
    {
        SlackText.Simple message = new SlackText.Simple();
        for (Operator parameter : this.getParameters())
        {
            message.text(String.valueOf(parameter.get(context, source)));
        }
        return message.toString();
    }

}
