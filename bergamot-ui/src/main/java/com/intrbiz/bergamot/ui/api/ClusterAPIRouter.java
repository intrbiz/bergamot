package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.ClusterCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.model.Resource;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.message.ClusterMO;
import com.intrbiz.bergamot.model.message.ResourceMO;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.XML;


@Prefix("/api/cluster")
@RequireValidPrincipal()
public class ClusterAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    public List<ClusterMO> getClusters(BergamotDB db, @Var("site") Site site)
    {
        return db.listClusters(site.getId()).stream().map(Cluster::toStubMO).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ClusterMO getCluster(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getClusterByName(site.getId(), name), Cluster::toMO);
    }
    
    @Get("/name/:name/state")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getClusterState(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getClusterByName(site.getId(), name), (h)->{return h.getState().toMO();});
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ClusterMO getCluster(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getCluster(id), Cluster::toMO);
    }
    
    @Get("/id/:id/state")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getClusterState(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getCluster(id), (h)->{return h.getState().toMO();});
    }
    
    @Get("/name/:name/resources")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public List<ResourceMO> getClusterResources(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getClusterByName(site.getId(), name), (e)->{return e.getResources().stream().map(Resource::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:id/resources")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public List<ResourceMO> getClusterResources(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getCluster(id), (e)->{return e.getResources().stream().map(Resource::toMO).collect(Collectors.toList());});
    }
    
    @Get("/name/:name/references")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public List<CheckMO> getClusterReferences(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getClusterByName(site.getId(), name), (e)->{return e.getReferences().stream().map((c) -> {return (CheckMO) c.toMO();}).collect(Collectors.toList());});
    }
    
    @Get("/id/:id/references")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public List<CheckMO> getClusterReferences(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getCluster(id), (e)->{return e.getReferences().stream().map((c) -> {return (CheckMO) c.toMO();}).collect(Collectors.toList());});
    }
    
    @Get("/name/:name/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ClusterCfg getClusterConfig(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getClusterByName(site.getId(), name), Cluster::getConfiguration);
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ClusterCfg getClusterConfig(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getCluster(id), Cluster::getConfiguration);
    }
    
    @Get("/id/:id/suppress")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String suppressCluster(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Cluster cluster = db.getCluster(id);
        if (cluster == null) throw new BalsaNotFound("No cluster with id '" + id + "' exists.");
        action("suppress-check", cluster);
        return "Ok";
    }
    
    @Get("/id/:id/unsuppress")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String unsuppressCluster(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Cluster cluster = db.getCluster(id);
        if (cluster == null) throw new BalsaNotFound("No cluster with id '" + id + "' exists.");
        action("unsuppress-check", cluster);
        return "Ok";
    }
    
    @Get("/id/:id/suppress-resources")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String suppressResourcesOnCluster(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Cluster cluster = db.getCluster(id);
        if (cluster == null) throw new BalsaNotFound("No cluster with id '" + id + "' exists.");
        int suppressed = 0;
        for (Resource resource : cluster.getResources())
        {
            action("suppress-check", resource);
            suppressed++;
        }
        return "Ok, suppressed " + suppressed + " services";
    }
    
    @Get("/id/:id/unsuppress-resources")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String unsuppressResourcesOnCluster(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Cluster cluster = db.getCluster(id);
        if (cluster == null) throw new BalsaNotFound("No cluster with id '" + id + "' exists.");
        int unsuppressed = 0;
        for (Resource resource : cluster.getResources())
        {
            action("unsuppress-check", resource);
            unsuppressed++;
        }
        return "Ok, unsuppressed " + unsuppressed + " services";
    }
}
