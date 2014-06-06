package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.model.Resource;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.message.ClusterMO;
import com.intrbiz.bergamot.model.message.ResourceMO;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;


@Prefix("/api/cluster")
public class ClusterAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    public List<ClusterMO> getClusters()
    {
        return null; //return this.app().getBergamot().getObjectStore().getClusters().stream().map(Cluster::toStubMO).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    public ClusterMO getCluster(String name)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupCluster(name), Cluster::toMO);
    }
    
    @Get("/name/:name/state")
    @JSON(notFoundIfNull = true)
    public CheckStateMO getClusterState(String name)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupCluster(name), (h)->{return h.getState().toMO();});
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    public ClusterMO getCluster(@AsUUID UUID id)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupCluster(id), Cluster::toMO);
    }
    
    @Get("/id/:id/state")
    @JSON(notFoundIfNull = true)
    public CheckStateMO getClusterState(@AsUUID UUID id)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupCluster(id), (h)->{return h.getState().toMO();});
    }
    
    @Get("/name/:name/resources")
    @JSON(notFoundIfNull = true)
    public List<ResourceMO> getClusterResources(String name)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupCluster(name), (e)->{return e.getResources().stream().map(Resource::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:id/resources")
    @JSON(notFoundIfNull = true)
    public List<ResourceMO> getClusterResources(@AsUUID UUID id)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupCluster(id), (e)->{return e.getResources().stream().map(Resource::toMO).collect(Collectors.toList());});
    }
    
    @Get("/name/:name/references")
    @JSON(notFoundIfNull = true)
    public List<CheckMO> getClusterReferences(String name)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupCluster(name), (e)->{return e.getReferences().stream().map(Check::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:id/references")
    @JSON(notFoundIfNull = true)
    public List<CheckMO> getClusterReferences(@AsUUID UUID id)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupCluster(id), (e)->{return e.getReferences().stream().map(Check::toMO).collect(Collectors.toList());});
    }
}
