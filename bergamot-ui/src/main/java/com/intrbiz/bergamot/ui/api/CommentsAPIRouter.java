package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Alert;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Comment;
import com.intrbiz.bergamot.model.Downtime;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.CommentMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.IsaLong;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;

@Prefix("/api/comment")
@RequireValidPrincipal()
public class CommentsAPIRouter extends Router<BergamotApp>
{

    @Get("/id/:id")
    @RequirePermission("api.read.comment")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CommentMO getComment(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getComment(id), Comment::toMO);
    }
    
    @Get("/id/:id/remove")
    @RequirePermission("api.write.comment.remove")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public Boolean removeComment(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        db.removeComment(id);
        return true;
    }
    
    @Get("/for-object/id/:id")
    @RequirePermission("api.read.comment")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public List<CommentMO> getCommentsForObject(
            BergamotDB db, 
            @IsaObjectId(session = false) UUID id, 
            @Param("offset") @IsaLong(min = 0, max = 1000, mandatory = true, defaultValue = 0, coalesce = CoalesceMode.ON_NULL) Long offset, 
            @Param("limit") @IsaLong(min = 0, max = 1000, mandatory = true, defaultValue = 10, coalesce = CoalesceMode.ON_NULL) Long limit
    )
    {
        return db.getCommentsForObject(id, offset, limit).stream().map(Comment::toMO).collect(Collectors.toList());
    }
    
    @Any("/add-comment-to-check/id/:id")
    @RequirePermission("api.write.comment.create")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public CommentMO addCommentToCheck(
            BergamotDB db, 
            @IsaObjectId(session = false) UUID id, 
            @Param("summary") @CheckStringLength(min = 1, max = 80, mandatory = true) String summary, 
            @Param("comment") @CheckStringLength(min = 1, max = 4096, mandatory = true) String message
    )
    {
        Check<?, ?> check = db.getCheck(id);
        if (check == null) throw new BalsaNotFound("No check with the id: " + id);
        // the comment
        Comment comment = new Comment().author(currentPrincipal()).on(check).summary(summary).message(message);
        db.setComment(comment);
        return comment.toMO();
    }
    
    @Any("/add-comment-to-alert/id/:id")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public CommentMO addCommentToAlert(
            BergamotDB db, 
            @IsaObjectId(session = false) UUID id, 
            @Param("summary") @CheckStringLength(min = 1, max = 80, mandatory = true) String summary, 
            @Param("comment") @CheckStringLength(min = 1, max = 4096, mandatory = true) String message
    )
    {
        Alert alert = db.getAlert(id);
        if (alert == null) throw new BalsaNotFound("No alert with the id: " + id);
        // the comment
        Comment comment = new Comment().author(currentPrincipal()).on(alert).summary(summary).message(message);
        db.setComment(comment);
        return comment.toMO();
    }
    
    @Any("/add-comment-to-downtime/id/:id")
    @RequirePermission("api.write.comment.create")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public CommentMO addCommentToDowntime(
            BergamotDB db, 
            @IsaObjectId(session = false) UUID id, 
            @Param("summary") @CheckStringLength(min = 1, max = 80, mandatory = true) String summary, 
            @Param("comment") @CheckStringLength(min = 1, max = 4096, mandatory = true) String message
    )
    {
        Downtime downtime = db.getDowntime(id);
        if (downtime == null) throw new BalsaNotFound("No downtime with the id: " + id);
        // the comment
        Comment comment = new Comment().author(currentPrincipal()).on(downtime).summary(summary).message(message);
        db.setComment(comment);
        return comment.toMO();
    }
    
    @Any("/add-comment-to-object/id/:id")
    @RequirePermission("api.write.comment.create")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public CommentMO addCommentToObject(
            BergamotDB db, 
            @Var("site") Site site,
            @IsaObjectId(session = false) UUID id, 
            @Param("summary") @CheckStringLength(min = 1, max = 80, mandatory = true) String summary, 
            @Param("comment") @CheckStringLength(min = 1, max = 4096, mandatory = true) String message
    )
    {
        // the comment
        Comment comment = new Comment().author(currentPrincipal()).on(site, id).summary(summary).message(message);
        db.setComment(comment);
        return comment.toMO();
    }
    
    @Get("/id/:id/render")
    @RequirePermission("api.read.comment")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String renderComment(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return var("comment", db.getComment(id)) == null ? null : encodeBuffered("include/comment");
    }
}
