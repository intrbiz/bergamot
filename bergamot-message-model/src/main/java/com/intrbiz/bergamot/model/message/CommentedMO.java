package com.intrbiz.bergamot.model.message;

import java.util.List;

public interface CommentedMO
{
    List<CommentMO> getComments();

    void setComments(List<CommentMO> comments);
}
