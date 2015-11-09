package com.intrbiz.bergamot.virtual;

import java.io.StringReader;

import com.intrbiz.bergamot.virtual.operator.VirtualCheckOperator;
import com.intrbiz.bergamot.virtual.parser.VirtualCheckExpressionParserInternal;

public class VirtualCheckExpressionParser
{
    public static final VirtualCheckOperator parseVirtualCheckExpression(String expression)
    {
        try
        {
            try (StringReader reader = new StringReader(expression))
            {
                VirtualCheckExpressionParserInternal parser = new VirtualCheckExpressionParserInternal(reader);
                return parser.readExpression();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to parse virtual check expression: '" + expression + "'", e);
        }
    }
}
