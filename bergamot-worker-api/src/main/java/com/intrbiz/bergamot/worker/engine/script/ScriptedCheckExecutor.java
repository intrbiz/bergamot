package com.intrbiz.bergamot.worker.engine.script;

/**
 * Execute a Bergamot scripted check
 */
public interface ScriptedCheckExecutor
{
    /**
     * Bind additional variables into the script context
     * @param variableName
     * @param value
     */
    ScriptedCheckExecutor bind(String variableName, Object value);
    
    /**
     * Execute the script
     */
    void execute();
}
