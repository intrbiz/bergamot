package com.intrbiz.bergamot.virtual;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.junit.Test;

import com.intrbiz.bergamot.virtual.operator.VirtualCheckOperator;
import com.intrbiz.bergamot.virtual.reference.AnonymousService;
import com.intrbiz.bergamot.virtual.reference.AnonymousTrap;
import com.intrbiz.bergamot.virtual.reference.CheckReference;
import com.intrbiz.bergamot.virtual.reference.HostByName;
import com.intrbiz.bergamot.virtual.reference.ServiceByName;
import com.intrbiz.bergamot.virtual.reference.TrapByName;

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
    
    @Test
    public void testParseAnonymousServiceParent()
    {
        List<CheckReference> refs = VirtualCheckExpressionParser.parseParentsExpression("service 'check_sshd'");
        assertThat(refs, is(notNullValue()));
        assertThat(refs.size(), is(equalTo(1)));
        assertThat(refs.get(0), is(notNullValue()));
        assertThat(refs.get(0), is(instanceOf(AnonymousService.class)));
        assertThat(refs.get(0), is(hasProperty("name", equalTo("check_sshd"))));
    }
    
    @Test
    public void testParseAnonymousTrapParent()
    {
        List<CheckReference> refs = VirtualCheckExpressionParser.parseParentsExpression("trap 'port_e1/1'");
        assertThat(refs, is(notNullValue()));
        assertThat(refs.size(), is(equalTo(1)));
        assertThat(refs.get(0), is(notNullValue()));
        assertThat(refs.get(0), is(instanceOf(AnonymousTrap.class)));
        assertThat(refs.get(0), is(hasProperty("name", equalTo("port_e1/1"))));
    }
    
    @Test
    public void testParseAnonymousServiceAndTrapParent()
    {
        List<CheckReference> refs = VirtualCheckExpressionParser.parseParentsExpression("service 'check_sshd', trap 'port_g1/48'");
        assertThat(refs, is(notNullValue()));
        assertThat(refs.size(), is(equalTo(2)));
        assertThat(refs.get(0), is(notNullValue()));
        assertThat(refs.get(0), is(instanceOf(AnonymousService.class)));
        assertThat(refs.get(0), is(hasProperty("name", equalTo("check_sshd"))));
        assertThat(refs.get(1), is(notNullValue()));
        assertThat(refs.get(1), is(instanceOf(AnonymousTrap.class)));
        assertThat(refs.get(1), is(hasProperty("name", equalTo("port_g1/48"))));
    }
    
    @Test
    public void testParseServiceAndAnonymousServiceParent()
    {
        List<CheckReference> refs = VirtualCheckExpressionParser.parseParentsExpression("service 'ping', service 'ping' on host 'default_gateway', service 'ping'");
        assertThat(refs, is(notNullValue()));
        assertThat(refs.size(), is(equalTo(3)));
        assertThat(refs.get(0), is(notNullValue()));
        assertThat(refs.get(0), is(instanceOf(AnonymousService.class)));
        assertThat(refs.get(0), is(hasProperty("name", equalTo("ping"))));
        assertThat(refs.get(1), is(notNullValue()));
        assertThat(refs.get(1), is(instanceOf(ServiceByName.class)));
        assertThat(refs.get(1), is(hasProperty("name", equalTo("ping"))));
        assertThat(refs.get(1), is(hasProperty("host", is(instanceOf(HostByName.class)))));
        assertThat(refs.get(1), is(hasProperty("host", hasProperty("name", equalTo("default_gateway")))));
        assertThat(refs.get(2), is(notNullValue()));
        assertThat(refs.get(2), is(instanceOf(AnonymousService.class)));
        assertThat(refs.get(2), is(hasProperty("name", equalTo("ping"))));
    }
    
    @Test
    public void testParseTrapAndAnonymousTrapParent()
    {
        List<CheckReference> refs = VirtualCheckExpressionParser.parseParentsExpression("trap 'port_1', trap 'port_2' on host 'sw1', trap 'port_3'");
        assertThat(refs, is(notNullValue()));
        assertThat(refs.size(), is(equalTo(3)));
        assertThat(refs.get(0), is(notNullValue()));
        assertThat(refs.get(0), is(instanceOf(AnonymousTrap.class)));
        assertThat(refs.get(0), is(hasProperty("name", equalTo("port_1"))));
        assertThat(refs.get(1), is(notNullValue()));
        assertThat(refs.get(1), is(instanceOf(TrapByName.class)));
        assertThat(refs.get(1), is(hasProperty("name", equalTo("port_2"))));
        assertThat(refs.get(1), is(hasProperty("host", is(instanceOf(HostByName.class)))));
        assertThat(refs.get(1), is(hasProperty("host", hasProperty("name", equalTo("sw1")))));
        assertThat(refs.get(2), is(notNullValue()));
        assertThat(refs.get(2), is(instanceOf(AnonymousTrap.class)));
        assertThat(refs.get(2), is(hasProperty("name", equalTo("port_3"))));
    }
}
