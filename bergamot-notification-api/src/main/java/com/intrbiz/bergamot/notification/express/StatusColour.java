package com.intrbiz.bergamot.notification.express;

import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.operator.Function;

public class StatusColour extends Function
{
    public StatusColour()
    {
        super("status_colour");
    }

    @Override
    public boolean isIdempotent()
    {
        return true;
    }

    @Override
    public Object get(ExpressContext context, Object source) throws ExpressException
    {
        String status = ((String) this.getParameter(0).get(context, source)).toUpperCase() ;
        switch (status)
        {
            case "PENDING":
            case "INFO":          return "#D9E1D9";
            case "UP":
            case "OK":            return "#00BF00";
            case "WARNING":       return "#F3C300";
            case "CRITICAL":
            case "UNKNOWN":
            case "TIMEOUT":
            case "ERROR":
            case "DISCONNECTED":
            case "DOWN":
            case "ACTION":        return "#E85752";
        }
        return "#000";
    }

}
