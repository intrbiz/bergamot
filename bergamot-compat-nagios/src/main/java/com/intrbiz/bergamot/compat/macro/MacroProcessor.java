package com.intrbiz.bergamot.compat.macro;

import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.intrbiz.Util;

/**
 * Parse and apply Nagios macros
 */
public class MacroProcessor
{
    private static final Logger logger = Logger.getLogger(MacroProcessor.class);
    
    public static String applyMacros(String expression, MacroFrame macros)
    {
        StringBuilder result = new StringBuilder();
        // state
        boolean inMacro = false;
        StringBuilder macroNameBuffer = null;
        // parse the expression
        for (char c : expression.toCharArray())
        {
            if (inMacro)
            {
                if (c == '$')
                {
                    // end of macro
                    String macroName = macroNameBuffer.toString();
                    if (Util.isEmpty(macroName))
                    {
                        // correct?
                        result.append("$");
                    }
                    else
                    {
                        String value = macros.get(macroName);
                        if (value != null)
                        {
                            result.append(value);
                        }
                        else
                        {
                            logger.warn("Undefined macro: '" + macroName + "', while processing: '" + expression + "', ignoring.");
                        }
                    }
                    // reset state
                    inMacro = false;
                    macroNameBuffer = null;
                }
                else
                {
                    macroNameBuffer.append(c);
                }
            }
            else
            {
                if (c == '$')
                {
                    // start of macro
                    inMacro = true;
                    macroNameBuffer = new StringBuilder();
                }
                else
                {
                    result.append(c);
                }
            }
        }
        // sanity check
        if (inMacro)
        {
            throw new RuntimeException("Failed to apply macros to '" + expression + "', macro was not terminated!");
        }
        return result.toString();
    }
    
    public static Set<String> extractMacros(String expression)
    {
        Set<String> macros = new TreeSet<String>();
        // state
        boolean inMacro = false;
        StringBuilder macroNameBuffer = null;
        // parse the expression
        for (char c : expression.toCharArray())
        {
            if (inMacro)
            {
                if (c == '$')
                {
                    // end of macro
                    String macroName = macroNameBuffer.toString();
                    if (! Util.isEmpty(macroName))
                    {
                        macros.add(macroName);
                    }
                    // reset state
                    inMacro = false;
                    macroNameBuffer = null;
                }
                else
                {
                    macroNameBuffer.append(c);
                }
            }
            else
            {
                if (c == '$')
                {
                    // start of macro
                    inMacro = true;
                    macroNameBuffer = new StringBuilder();
                }
            }
        }
        // sanity check
        if (inMacro)
        {
            throw new RuntimeException("Failed to extract macros to '" + expression + "', macro was not terminated!");
        }
        return macros;
    }
}
