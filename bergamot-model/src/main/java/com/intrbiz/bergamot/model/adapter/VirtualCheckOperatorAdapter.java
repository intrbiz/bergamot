package com.intrbiz.bergamot.model.adapter;

import com.intrbiz.bergamot.virtual.VirtualCheckExpressionParser;
import com.intrbiz.bergamot.virtual.operator.VirtualCheckOperator;
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
        return VirtualCheckExpressionParser.parseVirtualCheckExpression(value);
    }
}
