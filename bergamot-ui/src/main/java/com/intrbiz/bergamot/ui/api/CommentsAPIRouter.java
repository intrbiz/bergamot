package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
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
import com.intrbiz.metadata.ListOf;
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
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CommentMO getComment(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Comment comment = notNull(db.getComment(id));
        require(permission("read.comment", comment.getObjectId()));
        return comment.toMO(currentPrincipal());
    }
    
    @Get("/id/:id/remove")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public Boolean removeComment(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Comment comment = db.getComment(id);
        if (comment != null)
        {
            require(permission("remove.comment", comment.getObjectId()));
            db.removeComment(id);
        }
        return true;
    }
    
    @Get("/for-object/id/:id")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    @ListOf(CommentMO.class)
    public List<CommentMO> getCommentsForObject(
            BergamotDB db, 
            @IsaObjectId(session = false) UUID id, 
            @Param("offset") @IsaLong(min = 0, max = 1000, mandatory = true, defaultValue = 0, coalesce = CoalesceMode.ON_NULL) Long offset, 
            @Param("limit") @IsaLong(min = 0, max = 1000, mandatory = true, defaultValue = 10, coalesce = CoalesceMode.ON_NULL) Long limit
    )
    {
        require(permission("read.comment", id));
        return db.getCommentsForObject(id, offset, limit).stream().map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Any("/add-comment-to-check/id/:id")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public CommentMO addCommentToCheck(
            BergamotDB db, 
            @IsaObjectId(session = false) UUID id, 
            @Param("summary") @CheckStringLength(min = 1, max = 80, mandatory = true) String summary, 
            @Param("comment") @CheckStringLength(min = 1, max = 4096, mandatory = true) String message
    )
    {
        Check<?, ?> check = notNull(db.getCheck(id));
        require(permission("write.comment", check));
        Comment comment = new Comment().author(currentPrincipal()).on(check).summary(summary).message(message);
        db.setComment(comment);
        return comment.toMO(currentPrincipal());
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
        Alert alert = notNull(db.getAlert(id));
        require(permission("write.comment", alert.getCheckId()));
        Comment comment = new Comment().author(currentPrincipal()).on(alert).summary(summary).message(message);
        db.setComment(comment);
        return comment.toMO(currentPrincipal());
    }
    
    @Any("/add-comment-to-downtime/id/:id")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public CommentMO addCommentToDowntime(
            BergamotDB db, 
            @IsaObjectId(session = false) UUID id, 
            @Param("summary") @CheckStringLength(min = 1, max = 80, mandatory = true) String summary, 
            @Param("comment") @CheckStringLength(min = 1, max = 4096, mandatory = true) String message
    )
    {
        Downtime downtime = notNull(db.getDowntime(id));
        require(permission("write.comment", downtime.getCheckId()));
        Comment comment = new Comment().author(currentPrincipal()).on(downtime).summary(summary).message(message);
        db.setComment(comment);
        return comment.toMO(currentPrincipal());
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
        require(permission("write.comment", id));
        Comment comment = new Comment().author(currentPrincipal()).on(site, id).summary(summary).message(message);
        db.setComment(comment);
        return comment.toMO(currentPrincipal());
    }
    
    @Get("/id/:id/render")
    @RequirePermission("api.read.comment")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public String renderComment(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Comment comment = var("comment", notNull(db.getComment(id)));
        require(permission("read.comment", comment.getObjectId()));
        return encodeBuffered("include/comment");
    }
}
