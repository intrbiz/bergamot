package com.intrbiz.bergamot.compat.config.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.compat.config.parser.model.Directive;
import com.intrbiz.bergamot.compat.config.parser.model.IncludeDir;
import com.intrbiz.bergamot.compat.config.parser.model.IncludeFile;
import com.intrbiz.bergamot.compat.config.parser.model.ObjectDefinition;
import com.intrbiz.bergamot.compat.config.parser.model.ObjectParameter;

/**
 * A low-level parser for the Nagios configuration files
 */
public class NagiosConfigParser
{   
    private Logger logger = Logger.getLogger(NagiosConfigParser.class);
    
    private BufferedReader reader;
    
    private List<String> comments = new LinkedList<String>();
    
    public NagiosConfigParser(Reader in)
    {
        this.reader = new BufferedReader(in);
    }
    
    private String readLine() throws IOException
    {
        String l;
        while ((l = this.reader.readLine()) != null)
        {
            l = l.trim();
            // ignore blank lines
            if (l.length() == 0)
            {
                continue;
            }
            // ignore comments
            if (l.startsWith("#"))
            {
                this.comments.add(l.substring(1));
                continue;
            }
            // ignore tailing comments
            int scIdx = l.indexOf(";");
            if (scIdx > 0)
            {
                this.comments.add(l.substring(scIdx + 1));
                l = l.substring(0, scIdx);
            }
            return l;
        }
        return null;
    }
    
    private <T extends Directive> T attachComments(T d)
    {
        for (String comment : this.comments)
        {
            d.addComment(comment);
        }
        this.comments.clear();
        return d;
    }
    
    public Directive readNext() throws IOException
    {
        String l;
        while ((l = this.readLine()) != null)
        {
            // object definition
            if (l.startsWith("define"))
            {
                String type = l.substring(7).replace('{', ' ').trim();
                // construct an object definition
                ObjectDefinition def = new ObjectDefinition(type);
                this.attachComments(def);
                // parse the object contents
                String p;
                while ((p = this.readLine()) != null)
                {
                    if ("}".equals(p)) break;
                    def.addParameter(this.attachComments(ObjectParameter.read(p)));
                }
                // return the definition
                return def;
            }
            else if (l.startsWith("include_file"))
            {
                return this.attachComments(new IncludeFile(l.substring(13).trim()));
            }
            else if (l.startsWith("include_dir"))
            {
                return this.attachComments(new IncludeDir(l.substring(12).trim()));
            }
            else
            {
                logger.warn("Unsupported directive: " + l);
            }
        }
        return null;
    }
    
    public void close() throws IOException
    {
        this.reader.close();
    }
}
