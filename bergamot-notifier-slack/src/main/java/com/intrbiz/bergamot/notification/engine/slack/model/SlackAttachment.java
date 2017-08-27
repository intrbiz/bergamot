package com.intrbiz.bergamot.notification.engine.slack.model;

import com.intrbiz.json.JSObject;
import com.intrbiz.json.JSString;

public class SlackAttachment
{   
    private final SlackMessage parent;
    
    private final JSObject json;
    
    public SlackAttachment(SlackMessage parent, JSObject json)
    {
        super();
        this.parent = parent;
        this.json = json;
    }
    
    public SlackAttachment color(String color)
    {
        this.json.addMember("color", new JSString(color));
        return this;
    }
    
    public SlackAttachment authorName(String authorName)
    {
        this.json.addMember("author_name", new JSString(authorName));
        return this;
    }
    
    public SlackText<SlackAttachment> text()
    {
        return new SlackText<SlackAttachment>((text) -> { 
            this.json.addMember("text", new JSString(text));
            return this;
        });
    }
    
    public SlackAttachment text(String plainText)
    {
        this.text().text(plainText).build();
        return this;
    }
    
    public SlackAttachment link(String href, String display)
    {
        this.text().link(href, display).build();
        return this;
    }
    
    public SlackAttachment rawText(String text)
    {
        this.json.addMember("text", new JSString(text));
        return this;
    }
    
    public SlackMessage build()
    {
        return this.parent;
    }
}
