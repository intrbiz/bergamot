package com.intrbiz.bergamot.ui.router;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.bergamot.BergamotVersion;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.ui.BergamotUI;
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
public class HealthRouter extends Router<BergamotUI>
{   
    @SuppressWarnings("rawtypes")
    private static final Class[] DATABASE_MODULES =  { BergamotDB.class, LamplighterDB.class };
    
    @Any("/ok")
    @Text
    public String ok()
    {
        return "OK";
    }
    
    @Any("/alive")
    @Text
    public String alive()
    {
        return "OK";
    }
    
    @Any("/ready")
    @Text
    public String ready()
    {
        return "OK";
    }
    
    @Any("/check")
    @JSON()
    public HealthCheck check()
    {
        HealthCheck health = new HealthCheck();
        health.setRelease(this.checkRelease());
        health.setDatabase(this.checkDB());
        return health;
    }
    
    @Any("/check/release")
    @JSON()
    public ReleaseHealth checkRelease()
    {
        return new ReleaseHealth(BergamotVersion.NAME, BergamotVersion.numberString(), BergamotVersion.CODE_NAME);
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
    
    @Catch()
    @Any("/check**")
    @JSON(status = HTTPStatus.InternalServerError)
    public HealthError checkError()
    {
        Throwable t = balsa().getException();
        return new HealthError(t == null ? "" : t.getMessage());
    }
}
