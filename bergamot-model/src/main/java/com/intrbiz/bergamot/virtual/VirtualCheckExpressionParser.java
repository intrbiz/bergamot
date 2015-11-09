package com.intrbiz.bergamot.virtual;

import java.io.StringReader;
import java.util.List;

import com.intrbiz.bergamot.virtual.operator.VirtualCheckOperator;
import com.intrbiz.bergamot.virtual.parser.VirtualCheckExpressionParserInternal;
import com.intrbiz.bergamot.virtual.reference.CheckReference;

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
    
    public static final List<CheckReference> parseParentsExpression(String expression)
    {
        try
        {
            try (StringReader reader = new StringReader(expression))
            {
                VirtualCheckExpressionParserInternal parser = new VirtualCheckExpressionParserInternal(reader);
                return parser.readParents();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to parse parents expression: '" + expression + "'", e);
        }
    }
}
