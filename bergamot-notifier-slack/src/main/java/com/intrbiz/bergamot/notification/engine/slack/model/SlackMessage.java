package com.intrbiz.bergamot.notification.engine.slack.model;

import java.nio.charset.Charset;

import com.intrbiz.json.JSArray;
import com.intrbiz.json.JSObject;
import com.intrbiz.json.JSString;

public class SlackMessage
{
    private static final Charset UTF8 = Charset.forName("UTF-8");
    
    private JSObject json = new JSObject();
    
    public SlackMessage()
    {
        super();
    }
    
    public SlackMessage channel(String channel)
    {
        this.json.addMember("channel", new JSString(channel));
        return this;
    }
    
    public SlackMessage username(String username)
    {
        this.json.addMember("username", new JSString(username));
        return this;
    }
    
    public SlackMessage iconUrl(String iconUrl)
    {
        this.json.addMember("icon_url", new JSString(iconUrl));
        return this;
    }
    
    public SlackText<SlackMessage> text()
    {
        return new SlackText<SlackMessage>((text) -> { 
            this.json.addMember("text", new JSString(text));
            return this;
        });
    }
    
    public SlackMessage text(String plainText)
    {
        this.text().text(plainText).build();
        return this;
    }
    
    public SlackMessage rawText(String text)
    {
        this.json.addMember("text", new JSString(text));
        return this;
    }
    
    public SlackAttachment attachment()
    {
        JSArray attachments = (JSArray) this.json.getMember("attachments");
        if (attachments == null)
        {
            attachments = new JSArray();
            this.json.addMember("attachments", attachments);
        }
        JSObject attachment = new JSObject();
        attachments.addElement(attachment);
        return new SlackAttachment(this, attachment);
    }
    
    public String toString()
    {
        return this.json.toString();
    }
    
    public byte[] toBytes()
    {
        return this.toString().getBytes(UTF8);
    }
}
