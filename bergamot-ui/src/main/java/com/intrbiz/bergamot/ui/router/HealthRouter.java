package com.intrbiz.bergamot.ui.router;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.bergamot.cluster.ClusterManager;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.bergamot.ui.model.health.ClusterHealth;
import com.intrbiz.bergamot.ui.model.health.DatabaseHealth;
import com.intrbiz.bergamot.ui.model.health.DatabaseModuleHealth;
import com.intrbiz.bergamot.ui.model.health.HealthCheck;
import com.intrbiz.bergamot.ui.model.health.HealthError;
import com.intrbiz.bergamot.ui.model.health.ReleaseHealth;
import com.intrbiz.data.DataManager;
import com.intrbiz.data.db.DatabaseAdapter;
import com.intrbiz.lamplighter.data.LamplighterDB;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Text;

@Prefix("/health")
public class HealthRouter extends Router<BergamotApp>
{   
    @SuppressWarnings("rawtypes")
    private static final Class[] DATABASE_MODULES =  { BergamotDB.class, LamplighterDB.class };
    
    @Any("/ok")
    @Text
    public String ok()
    {
        return "OK";
    }
    
    @Any("/check")
    @JSON()
    public HealthCheck check()
    {
        HealthCheck health = new HealthCheck();
        health.setRelease(this.checkRelease());
        health.setCluster(this.checkCluster());
        health.setDatabase(this.checkDB());
        return health;
    }
    
    @Any("/check/release")
    @JSON()
    public ReleaseHealth checkRelease()
    {
        return new ReleaseHealth(BergamotApp.VERSION.NAME, BergamotApp.VERSION.NUMBER, BergamotApp.VERSION.CODE_NAME);
    }
    
    @Any("/check/db")
    @JSON()
    @SuppressWarnings("unchecked")
    public DatabaseHealth checkDB()
    {
        DatabaseHealth health = new DatabaseHealth();
        for (Class<? extends DatabaseAdapter> adpCls : DATABASE_MODULES)
        {
            try (DatabaseAdapter adp = DataManager.get().databaseAdapter(adpCls))
            {
                health.getModules().add(new DatabaseModuleHealth(adpCls.getSimpleName(), adp.getName(), adp.getVersion()));
            }
        }
        return health;
    }
    
    @Any("/check/cluster")
    @JSON()
    public ClusterHealth checkCluster()
    {
        ClusterHealth health = new ClusterHealth();
        ClusterManager manager = app().getClusterManager();
        health.setMembers(manager.getMemberCount());
        health.setProcessingPools(manager.getProcessPoolCount());
        return health;
    }
    
    @Catch()
    @Any("/check**")
    @JSON(status = HTTPStatus.InternalServerError)
    public HealthError checkError()
    {
        Throwable t = balsa().getException();
        return new HealthError(t == null ? "" : t.getMessage());
    }
}
