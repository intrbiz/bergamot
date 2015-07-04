package com.intrbiz.bergamot.test;
import java.security.AccessControlException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.Before;
import org.junit.Test;

import com.intrbiz.scripting.RestrictedScriptEngineManager;

import static org.junit.Assert.*;

public class ManualTestScripting
{
    @Before
    public void setupSecurityManager()
    {
        // only setup the security manager if it is not already loaded
        if (System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManager());
        }
    }
    
    @Test
    public void testSimpleScript()
    {
        ScriptEngineManager engineManager = new RestrictedScriptEngineManager();
        ScriptEngine script = engineManager.getEngineByName("nashorn");
        try
        {
            Object value = script.eval("'abc' + 123;");
            assertTrue("Script returned abc123", "abc123".equals(value));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Script threw an exception: " + e.getMessage());
        }
    }
    
    @Test
    public void testPreventExit()
    {
        ScriptEngineManager manager = new RestrictedScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        try
        {
            engine.eval("exit(1);");
            fail("Exit didn't happen, yet a AccessControlException was not raised");
        }
        catch (Exception e)
        {
            assertTrue("Caught access exception when trying to call exit()", e instanceof AccessControlException);
        }
    }
}
