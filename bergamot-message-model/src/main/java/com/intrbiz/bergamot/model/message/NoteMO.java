package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.note")
public class NoteMO extends MessageObject
{
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("url")
    private String url;
    
    @JsonProperty("content")
    private String content;
    
    public NoteMO()
    {
        super();
    }

    public NoteMO(String title, String url, String content)
    {
        super();
        this.title = title;
        this.url = url;
        this.content = content;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }
}
