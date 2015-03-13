package com.intrbiz.bergamot.model.adapter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.data.DataException;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class ParametersAdapter implements DBTypeAdapter<String, List<Parameter>>
{
    private final ObjectMapper factory = new ObjectMapper();
    
    public ParametersAdapter()
    {
        this.factory.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        this.factory.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        this.factory.configure(SerializationFeature.INDENT_OUTPUT, true);
        this.factory.registerSubtypes(Parameter.class);
    }
    
    @Override
    public String toDB(List<Parameter> value)
    {
        // default to an empty list
        if (value == null) return "[]";
        // encode
        StringWriter sw = new StringWriter();
        try (JsonGenerator g = this.factory.getFactory().createGenerator(sw))
        {
            g.writeStartArray();
            for (Parameter p : value)
            {
                this.factory.writeValue(g, p);
            }
            g.writeEndArray();
        }
        catch (IOException e)
        {
            throw new DataException("Failed to encode parameter", e);
        }
        return sw.toString();
    }

    @Override
    public List<Parameter> fromDB(String value)
    {
        // default to an empty list
        if (value == null) return new LinkedList<Parameter>();
        // decode
        try (JsonParser p = this.factory.getFactory().createParser(new StringReader(value)))
        {
            List<Parameter> params = this.factory.readValue(p, this.factory.getTypeFactory().constructCollectionType(List.class, Parameter.class));
            return params == null ? new LinkedList<Parameter>() : params;
        }
        catch (IOException e)
        {
            throw new DataException("Failed to decode parameter", e);
        }
    }
}
