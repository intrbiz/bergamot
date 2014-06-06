package com.intrbiz.bergamot.model.adapter;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.virtual.VirtualCheckOperator;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionParser;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class VirtualCheckOperatorAdapter implements DBTypeAdapter<String, VirtualCheckOperator>
{    
    @Override
    public String toDB(VirtualCheckOperator value)
    {
        return value == null ? null : value.toString();
    }

    @Override
    public VirtualCheckOperator fromDB(String value)
    {
        if (value == null) return null;
        try (BergamotDB db = BergamotDB.connect())
        {
            // exploit the fact we always serialise to UUIDs
            return VirtualCheckExpressionParser.parseVirtualCheckExpression(db.createVirtualCheckContext(null), value);
        }
    }
}
