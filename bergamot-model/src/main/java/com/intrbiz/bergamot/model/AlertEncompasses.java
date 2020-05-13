package com.intrbiz.bergamot.model;

import java.sql.Timestamp;
import java.util.EnumSet;
import java.util.UUID;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.AlertEncompassesMO;
import com.intrbiz.data.db.compiler.meta.PartitionMode;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLPartition;
import com.intrbiz.data.db.compiler.meta.SQLPartitioning;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLTable(schema = BergamotDB.class, name = "alert_encompasses", since = @SQLVersion({4, 0, 0}) )
@SQLPartitioning(
    @SQLPartition(mode = PartitionMode.RANGE, on = "raised")
)
public class AlertEncompasses extends BergamotObject<AlertEncompassesMO>
{
    private static final long serialVersionUID = 1L;

    /**
     * The alert
     */
    @SQLColumn(index = 1, name = "alert_id", since = @SQLVersion({4, 0, 0}) )
    @SQLPrimaryKey()
    private UUID alertId;

    /**
     * The unique id of this escalation
     */
    @SQLColumn(index = 2, name = "check_id", since = @SQLVersion({4, 0, 0}) )
    @SQLPrimaryKey()
    private UUID checkId;

    /**
     * When did this check get encompassed under this alert
     */
    @SQLColumn(index = 3, name = "raised", since = @SQLVersion({4, 0, 0}) )
    private Timestamp raised;

    public AlertEncompasses()
    {
        super();
    }
    
    public AlertEncompasses(UUID alertId, UUID checkId, Timestamp raised)
    {
        super();
        this.alertId = alertId;
        this.checkId = checkId;
        this.raised = raised;
    }

    public UUID getAlertId()
    {
        return alertId;
    }

    public void setAlertId(UUID alertId)
    {
        this.alertId = alertId;
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
    }

    public Timestamp getRaised()
    {
        return raised;
    }

    public void setRaised(Timestamp raised)
    {
        this.raised = raised;
    }
    
    public Check<?,?> getCheck()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getCheck(this.getCheckId());
        }
    }

    @Override
    public AlertEncompassesMO toMO(Contact contact, EnumSet<com.intrbiz.bergamot.model.BergamotObject.MOFlag> options)
    {
        AlertEncompassesMO mo = new AlertEncompassesMO();
        if (contact == null || contact.hasPermission("read", this.getCheckId())) mo.setCheck(this.getCheck().toStubMO(contact));
        mo.setRaised(this.getRaised().getTime());
        return mo;
    }
}
