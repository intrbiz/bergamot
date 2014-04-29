package com.intrbiz.bergamot.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestStatus
{
    @Test
    public void testIsBetter()
    {
        assertTrue("PENDING  is better than OK",       Status.PENDING.isBetterThan(Status.OK));
        assertTrue("OK       is better than WARNING",  Status.OK.isBetterThan(Status.WARNING));
        assertTrue("WARNING  is better than CRITICAL", Status.WARNING.isBetterThan(Status.CRITICAL));
        assertTrue("CRITICAL is better than UNKNOWN",  Status.CRITICAL.isBetterThan(Status.UNKNOWN));
        assertTrue("UNKNOWN  is better than TIMEOUT",  Status.UNKNOWN.isBetterThan(Status.TIMEOUT));
        assertTrue("TIMEOUT  is better than ERROR", Status.TIMEOUT.isBetterThan(Status.ERROR));
    }
    
    @Test
    public void testIsWorse()
    {
        assertFalse("PENDING  is better than OK",       Status.PENDING.isWorseThan(Status.OK));
        assertFalse("OK       is better than WARNING",  Status.OK.isWorseThan(Status.WARNING));
        assertFalse("WARNING  is better than CRITICAL", Status.WARNING.isWorseThan(Status.CRITICAL));
        assertFalse("CRITICAL is better than UNKNOWN",  Status.CRITICAL.isWorseThan(Status.UNKNOWN));
        assertFalse("UNKNOWN  is better than TIMEOUT",  Status.UNKNOWN.isWorseThan(Status.TIMEOUT));
        assertFalse("TIMEOUT  is better than ERROR", Status.TIMEOUT.isWorseThan(Status.ERROR));
    }
}
