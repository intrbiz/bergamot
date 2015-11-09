package com.intrbiz.bergamot.virtual;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.intrbiz.bergamot.virtual.operator.VirtualCheckOperator;

public class TestVirtualCheckExpressionParser
{
    @Test
    public void testParseCase1()
    {
        VirtualCheckOperator op = VirtualCheckExpressionParser.parseVirtualCheckExpression("case when host 'vm1' is OK then OK when host 'vm1' is WARNING then OK else CRITICAL end");
        assertThat(op.toString(), is(equalTo("case when host 'vm1' is OK then OK when host 'vm1' is WARNING then OK else CRITICAL end")));
    }
    
    @Test
    public void testParseOneOf1()
    {
        VirtualCheckOperator op = VirtualCheckExpressionParser.parseVirtualCheckExpression("one of [ host 'vm1', host 'vm2', host 'vm3', host 'vm4' ] as WARNING");
        assertThat(op.toString(), is(equalTo("one of [host 'vm1', host 'vm2', host 'vm3', host 'vm4'] as WARNING")));
    }
    
    @Test
    public void testParseAnyOf1()
    {
        VirtualCheckOperator op = VirtualCheckExpressionParser.parseVirtualCheckExpression("any of [ host 'vm1', host 'vm2', host 'vm3', host 'vm4' ]");
        assertThat(op.toString(), is(equalTo("any of [host 'vm1', host 'vm2', host 'vm3', host 'vm4']")));
    }
    
    @Test
    public void testParseAllOf1()
    {
        VirtualCheckOperator op = VirtualCheckExpressionParser.parseVirtualCheckExpression("all of [ host 'vm1', host 'vm2', host 'vm3', host 'vm4' ]");
        assertThat(op.toString(), is(equalTo("all of [host 'vm1', host 'vm2', host 'vm3', host 'vm4']")));
    }
    
    @Test
    public void testParseCount1()
    {
        VirtualCheckOperator op = VirtualCheckExpressionParser.parseVirtualCheckExpression("count OK of [ host 'vm1', host 'vm2', host 'vm3', host 'vm4' ] is == 1");
        assertThat(op.toString(), is(equalTo("count OK of [host 'vm1', host 'vm2', host 'vm3', host 'vm4'] is == 1 as CRITICAL")));
    }
}
