package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.model.Resource;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.message.ClusterMO;
import com.intrbiz.bergamot.model.message.ResourceMO;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Var;


@Prefix("/api/cluster")
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
    public ClusterMO getCluster(BergamotDB db, @AsUUID UUID id)
    {
        return Util.nullable(db.getCluster(id), Cluster::toMO);
    }
    
    @Get("/id/:id/state")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getClusterState(BergamotDB db, @AsUUID UUID id)
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
    public List<ResourceMO> getClusterResources(BergamotDB db, @AsUUID UUID id)
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
    public List<CheckMO> getClusterReferences(BergamotDB db, @AsUUID UUID id)
    {
        return Util.nullable(db.getCluster(id), (e)->{return e.getReferences().stream().map((c) -> {return (CheckMO) c.toMO();}).collect(Collectors.toList());});
    }
}
