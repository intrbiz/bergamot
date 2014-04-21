package com.intrbiz.bergamot.compat.config.parser.model;

import java.util.LinkedList;
import java.util.List;

public abstract class Directive
{
    private final List<String> comments = new LinkedList<String>();

    public List<String> getComments()
    {
        return comments;
    }

    public void addComment(String comment)
    {
        this.comments.add(comment);
    }
}
