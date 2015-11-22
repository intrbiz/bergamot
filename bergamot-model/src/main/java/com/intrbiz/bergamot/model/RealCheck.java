package com.intrbiz.bergamot.model;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.RealCheckCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.message.RealCheckMO;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.bergamot.model.state.CheckStats;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * A 'real' check has to know about hard vs soft states
 */
public abstract class RealCheck<T extends RealCheckMO, C extends RealCheckCfg<C>> extends Check<T, C>
{
    private static final long serialVersionUID = 1L;
    
    /**
     * How many check attempts to trigger a hard alert
     */
    @SQLColumn(index = 1, name = "alert_attempt_threshold", since = @SQLVersion({ 1, 0, 0 }))
    protected int alertAttemptThreshold = 3;

    /**
     * How many check attempts to trigger a hard recovery
     */
    @SQLColumn(index = 2, name = "recovery_attempt_threshold", since = @SQLVersion({ 1, 0, 0 }))
    protected int recoveryAttemptThreshold = 3;
    
    /**
     * Checks which this check depends upon for reachability
     */
    @SQLColumn(index = 10, name = "depends", type = "UUID[]", since = @SQLVersion({ 3, 21, 0 }))
    protected List<UUID> dependsIds = new LinkedList<UUID>();

    public RealCheck()
    {
        super();
    }

    public int getAlertAttemptThreshold()
    {
        return alertAttemptThreshold;
    }

    public void setAlertAttemptThreshold(int alertAttemptThreshold)
    {
        this.alertAttemptThreshold = alertAttemptThreshold;
    }

    public int getRecoveryAttemptThreshold()
    {
        return recoveryAttemptThreshold;
    }

    public void setRecoveryAttemptThreshold(int recoveryAttemptThreshold)
    {
        this.recoveryAttemptThreshold = recoveryAttemptThreshold;
    }
    
    public int getCurrentAttemptThreshold()
    {
        return this.computeCurrentAttemptThreshold(this.getState());
    }
    
    public int computeCurrentAttemptThreshold(CheckState currentState)
    {
        return currentState.isOk() ? this.getRecoveryAttemptThreshold() : this.getAlertAttemptThreshold();
    }
    
    
    public List<UUID> getDependsIds()
    {
        return dependsIds;
    }

    public void setDependsIds(List<UUID> dependsIds)
    {
        this.dependsIds = dependsIds;
    }
    
    public boolean hasDependencies()
    {
        return ! (this.dependsIds == null || this.dependsIds.isEmpty());
    }
    
    public List<Check<?,?>> getDepends()
    {
        List<Check<?,?>> depends = new LinkedList<Check<?,?>>();
        if (this.getDependsIds() != null && (! this.getDependsIds().isEmpty()))
        {
            try (BergamotDB db = BergamotDB.connect())
            {
                this.dependsIds.stream().map((id) -> db.getCheck(id)).filter((c) -> c != null).forEach(depends::add); 
            }
        }
        return depends;
    }
    
    public CheckCommand getCheckCommand()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getCheckCommand(this.getId());
        }
    }
    
    public CheckStats getStats()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getCheckStats(this.getId());
        }
    }
    
    protected void toMO(RealCheckMO mo, Contact contact, EnumSet<MOFlag> options)
    {
        super.toMO(mo, contact, options);
        mo.setAlertAttemptThreshold(this.getAlertAttemptThreshold());
        mo.setRecoveryAttemptThreshold(this.getRecoveryAttemptThreshold());
        mo.setCurrentAttemptThreshold(this.getCurrentAttemptThreshold());
        if (options.contains(MOFlag.STATS)) mo.setStats(this.getStats().toMO(contact));
        if (options.contains(MOFlag.COMMAND)) mo.setCheckCommand(Util.nullable(this.getCheckCommand(), (x) -> x.toStubMO(contact)));
        if (options.contains(MOFlag.DEPENDS)) mo.setDepends(this.getDepends().stream().map((c) -> (CheckMO) c.toStubMO(contact)).collect(Collectors.toList()));
    }
    
    @Override
    public void configure(C configuration, C resolvedConfiguration)
    {
        super.configure(configuration, resolvedConfiguration);
        // configure state thresholds
        if (resolvedConfiguration.getState() != null)
        {
            this.alertAttemptThreshold    = resolvedConfiguration.getState().getFailedAfter();
            this.recoveryAttemptThreshold = resolvedConfiguration.getState().getRecoversAfter();
        }
    }
}
