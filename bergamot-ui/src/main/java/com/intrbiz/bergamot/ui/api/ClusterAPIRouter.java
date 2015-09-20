package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.ClusterCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
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
import com.intrbiz.metadata.ListOf;
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
    @ListOf(ClusterMO.class)
    public List<ClusterMO> getClusters(BergamotDB db, @Var("site") Site site)
    {
        return db.listClusters(site.getId()).stream().filter((c) -> permission("read", c)).map((x) -> x.toStubMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ClusterMO getClusterByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Cluster cluster = notNull(db.getClusterByName(site.getId(), name));
        require(permission("read", cluster));
        return cluster.toMO(currentPrincipal());
    }
    
    @Get("/name/:name/state")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getClusterStateByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Cluster cluster = notNull(db.getClusterByName(site.getId(), name));
        require(permission("read", cluster));
        return cluster.getState().toMO(currentPrincipal());
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ClusterMO getCluster(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Cluster cluster = notNull(db.getCluster(id));
        require(permission("read", cluster));
        return cluster.toMO(currentPrincipal());
    }
    
    @Get("/id/:id/state")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getClusterState(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Cluster cluster = notNull(db.getCluster(id));
        require(permission("read", cluster));
        return cluster.getState().toMO(currentPrincipal());
    }
    
    @Get("/name/:name/resources")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(ResourceMO.class)
    public List<ResourceMO> getClusterResourcesByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Cluster cluster = notNull(db.getClusterByName(site.getId(), name));
        require(permission("read", cluster));
        return cluster.getResources().stream().filter((r) -> permission("read", r)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/id/:id/resources")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(ResourceMO.class)
    public List<ResourceMO> getClusterResources(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Cluster cluster = notNull(db.getCluster(id));
        require(permission("read", cluster));
        return cluster.getResources().stream().filter((r) -> permission("read", r)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/name/:name/references")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(CheckMO.class)
    public List<CheckMO> getClusterReferencesByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Cluster cluster = notNull(db.getClusterByName(site.getId(), name));
        require(permission("read", cluster));
        return cluster.getReferences().stream().filter((c) -> permission("read", c)).map((c) -> {return (CheckMO) c.toMO(currentPrincipal());}).collect(Collectors.toList());
    }
    
    @Get("/id/:id/references")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(CheckMO.class)
    public List<CheckMO> getClusterReferences(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Cluster cluster = notNull(db.getCluster(id));
        require(permission("read", cluster));
        return cluster.getReferences().stream().filter((c) -> permission("read", c)).map((c) -> {return (CheckMO) c.toMO(currentPrincipal());}).collect(Collectors.toList());
    }
    
    @Get("/name/:name/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public ClusterCfg getClusterConfigByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Cluster cluster = notNull(db.getClusterByName(site.getId(), name));
        require(permission("read.config", cluster));
        return cluster.getConfiguration();
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public ClusterCfg getClusterConfig(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Cluster cluster = notNull(db.getCluster(id));
        require(permission("read.config", cluster));
        return cluster.getConfiguration();
    }
    
    @Get("/id/:id/suppress")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String suppressCluster(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Cluster cluster = notNull(db.getCluster(id));
        require(permission("suppress", cluster));
        action("suppress-check", cluster);
        return "Ok";
    }
    
    @Get("/id/:id/unsuppress")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String unsuppressCluster(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Cluster cluster = notNull(db.getCluster(id));
        require(permission("unsuppress", cluster));
        action("unsuppress-check", cluster);
        return "Ok";
    }
    
    @Get("/id/:id/suppress-resources")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String suppressResourcesOnCluster(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Cluster cluster = notNull(db.getCluster(id));
        require(permission("read", cluster));
        int suppressed = 0;
        for (Resource resource : cluster.getResources())
        {
            if (permission("suppress", resource))
            {
                action("suppress-check", resource);
                suppressed++;
            }
        }
        return "Ok, suppressed " + suppressed + " services";
    }
    
    @Get("/id/:id/unsuppress-resources")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String unsuppressResourcesOnCluster(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Cluster cluster = notNull(db.getCluster(id));
        require(permission("read", cluster));
        int unsuppressed = 0;
        for (Resource resource : cluster.getResources())
        {
            if (permission("unsuppress", resource))
            {
                action("unsuppress-check", resource);
                unsuppressed++;
            }
        }
        return "Ok, unsuppressed " + unsuppressed + " services";
    }
}
