package com.intrbiz.bergamot.config;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;

import org.junit.Test;

import com.intrbiz.bergamot.config.model.ScheduleCfg;
import com.intrbiz.bergamot.util.TimeInterval;

public class TestScheduleCfg
{

    @Test
    public void test() throws Exception
    {
        JAXBContext ctx = JAXBContext.newInstance(new Class<?>[] { ScheduleCfg.class });
        ScheduleCfg cfg = (ScheduleCfg) ctx.createUnmarshaller().unmarshal(new StringReader("<schedule every=\"5\" retry-every=\"1m\" changing-every=\"30s\"/>"));
        assertThat(cfg, is(notNullValue()));
        assertThat(cfg.getEvery(), is(equalTo("5")));
        assertThat(cfg.getRetryEvery(), is(equalTo("1m")));
        assertThat(cfg.getChangingEvery(), is(equalTo("30s")));
        assertThat(cfg.getEveryTimeInterval(), is(equalTo(TimeInterval.minutes(5))));
        assertThat(cfg.getRetryEveryTimeInterval(), is(equalTo(TimeInterval.minutes(1))));
        assertThat(cfg.getChangingEveryTimeInterval(), is(equalTo(TimeInterval.seconds(30))));
        assertThat(cfg.getEveryTimeInterval().toMillis(), is(equalTo(300_000L)));
        assertThat(cfg.getRetryEveryTimeInterval().toMillis(), is(equalTo(60_000L)));
        assertThat(cfg.getChangingEveryTimeInterval().toMillis(), is(equalTo(30_000L)));
    }

}
