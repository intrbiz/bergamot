package com.intrbiz.bergamot.util;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CommandTokeniser
{
    /**
     * Tokenise a command line to a list containing the command and its arguments.
     * The first element in the list will be the command, subsequent elements 
     * will be the arguments.  Quoted arguments are tokenised as a single element, 
     * as such any whitespace between quotes (' or ") will be kept.
     * 
     */
    public static List<String> tokeniseCommandLine(String commandLine) throws IOException
    {
        List<String> cmd = new LinkedList<String>();
        // state
        boolean inQuotes = false;
        char last = '\0';
        char quote = '\0';
        StringBuilder token = new StringBuilder();
        // parse
        for (char c : commandLine.toCharArray())
        {
            if (inQuotes)
            {
                token.append(c);
                // end of quoted string ?
                if (c == quote && last != '\\')
                {
                    inQuotes = false;
                    quote = '\0';
                }
            }
            else
            {
                if (c == ' ' || c == '\t' || c == '\r' || c == '\n')
                {
                    // token delimiter
                    if (token.length() > 0)
                    {
                        cmd.add(stripQuotes(token.toString()));
                        token = new StringBuilder();
                    }
                }
                else
                {
                    token.append(c);
                    // start of a quoted string ?
                    if (c == '\'' || c == '"')
                    {
                        inQuotes = true;
                        quote = c;
                    }
                }
            }
            last = c;
        }
        // last token
        if (token.length() > 0)
        {
            cmd.add(stripQuotes(token.toString()));
            token = new StringBuilder();
        }
        // sanity check
        if (inQuotes)
        {
            throw new IOException("The command '" + commandLine + "' failed to terminate a quoted token!");
        }
        return cmd;
    }
    
    private static String stripQuotes(String in)
    {
        return ((in.startsWith("\"") || in.startsWith("'")) && in.length() > 1) ? in.substring(1, in.length() -1) : in;
    }
}
