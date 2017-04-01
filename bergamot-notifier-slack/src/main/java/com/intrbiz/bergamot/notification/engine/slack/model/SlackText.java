package com.intrbiz.bergamot.notification.engine.slack.model;

import java.util.function.Function;

public class SlackText<T>
{
    private final StringBuilder text = new StringBuilder();
    
    private final Function<String, T> consumer;
    
    public SlackText(Function<String, T> consumer)
    {
        super();
        this.consumer = consumer;
    }
    
    private void appendPlain(String plainText)
    {
        if (plainText != null)
        {
            for (int i = 0; i < plainText.length(); i++)
            {
                char c = plainText.charAt(i);
                switch (c)
                {
                    case '&':
                        this.text.append("&amp;");
                        break;
                    case '<':
                        this.text.append("&lt;");
                        break;
                    case '>':
                        this.text.append("&gt;");
                        break;
                    default:
                        this.text.append(c);
                }
            }
        }
    }
    
    private void appendRaw(String text)
    {
        this.text.append(text);
    }
    
    private void appendRaw(char c)
    {
        this.text.append(c);
    }
    
    public SlackText<T> raw(String rawMessage)
    {
        this.appendRaw(rawMessage);
        this.build();
        return this;
    }
    
    public SlackText<T> text(String text)
    {
        this.appendPlain(text);
        this.build();
        return this;
    }
    
    public SlackText<T> link(String href, String display)
    {
        this.appendRaw('<');
        this.appendRaw(href);
        this.appendRaw('|');
        this.appendPlain(display);
        this.appendRaw('>');
        this.build();
        return this;
    }
    
    public T build()
    {
        return this.consumer.apply(this.text.toString());
    }
    
    @Override
    public String toString()
    {
        return this.text.toString();
    }
    
    public static final class Simple extends SlackText<String>
    {
        public Simple()
        {
            super((t) -> { return t; });
        }
    }
}
