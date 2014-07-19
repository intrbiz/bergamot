package com.intrbiz.bergamot.model;

import java.util.List;

/**
 * An object which has comments
 */
public interface Commented
{
    List<Comment> getComments(int limit);
    
    List<Comment> getComments();
}
