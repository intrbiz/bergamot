package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Comment;
import com.intrbiz.bergamot.model.message.CommentMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.IsaLong;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;

@Prefix("/api/comment")
@RequireValidPrincipal()
public class CommentsAPIRouter extends Router<BergamotApp>
{

    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CommentMO getComment(BergamotDB db, @AsUUID UUID id)
    {
        return Util.nullable(db.getComment(id), Comment::toMO);
    }
    
    @Get("/id/:id/remove")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public Boolean removeComment(BergamotDB db, @AsUUID UUID id)
    {
        db.removeComment(id);
        return true;
    }
    
    @Get("/for-object/id/:id")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public List<CommentMO> getCommentsForObject(BergamotDB db, @AsUUID UUID id, @Param("offset") @IsaLong(min = 0, max = 1000, mandatory = true, defaultValue = 0, coalesce = CoalesceMode.ON_NULL) Long offset, @Param("limit") @IsaLong(min = 0, max = 1000, mandatory = true, defaultValue = 10, coalesce = CoalesceMode.ON_NULL) long limit)
    {
        return db.getCommentsForObject(id, offset, limit).stream().map(Comment::toMO).collect(Collectors.toList());
    }
}
