package com.intrbiz.bergamot.model.adapter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.data.DataException;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class ParametersAdapter implements DBTypeAdapter<String, LinkedHashMap<String, Parameter>>
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
    public String toDB(LinkedHashMap<String, Parameter> value)
    {
        // default to an empty list
        if (value == null) return "[]";
        // encode
        StringWriter sw = new StringWriter();
        try (JsonGenerator g = this.factory.getFactory().createGenerator(sw))
        {
            g.writeStartArray();
            for (Entry<String, Parameter> p : value.entrySet())
            {
                this.factory.writeValue(g, p.getValue());
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
    public LinkedHashMap<String, Parameter> fromDB(String value)
    {
        // default to an empty list
        LinkedHashMap<String, Parameter> ret = new LinkedHashMap<String, Parameter>();
        if (value != null)
        {
            try (JsonParser p = this.factory.getFactory().createParser(new StringReader(value)))
            {
                Parameter parameter;
                if (p.nextToken() == JsonToken.START_ARRAY)
                {
                    while (p.nextToken() == JsonToken.START_OBJECT)
                    {
                        parameter = this.factory.readValue(p, Parameter.class);
                        ret.put(parameter.getName(), parameter);
                    }
                }
            }
            catch (IOException e)
            {
                throw new DataException("Failed to decode parameter", e);
            }
        }
        return ret;
    }
}
