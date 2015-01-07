package com.intrbiz.bergamot.ui.security.password.check;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class WordReader implements AutoCloseable
{
    private BufferedReader reader;
    
    public WordReader(Reader reader)
    {
        this.reader = new BufferedReader(reader);
    }
    
    public WordReader(InputStream input)
    {
        this(new InputStreamReader(input));
    }
    
    public String next() throws IOException
    {
        String word;
        for (;;)
        {
            word = this.reader.readLine();
            if (word == null) return null;
            word = word.trim();
            if (! word.startsWith("#")) return word;
        }
    }
    
    @Override
    public void close()
    {
        try
        {
            this.reader.close();
        }
        catch (IOException e)
        {
        }
    }
}
