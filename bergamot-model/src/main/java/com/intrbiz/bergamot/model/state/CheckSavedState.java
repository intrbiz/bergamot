package com.intrbiz.bergamot.model.state;

import java.io.Serializable;
import java.util.UUID;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLTable(schema = BergamotDB.class, name = "check_saved_state", since = @SQLVersion({ 3, 7, 0 }))
public class CheckSavedState implements Serializable
{
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 1, name = "check_id", since = @SQLVersion({ 3, 7, 0 }))
    @SQLPrimaryKey
    private UUID checkId;
    
    @SQLColumn(index = 2, name = "saved_state", since = @SQLVersion({ 3, 7, 0 }))
    private String savedState;
    
    public CheckSavedState()
    {
        super();
    }
    
    public CheckSavedState(UUID checkId, String savedState)
    {
        this.checkId = checkId;
        this.savedState = savedState;
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
    }

    public String getSavedState()
    {
        return savedState;
    }

    public void setSavedState(String savedState)
    {
        this.savedState = savedState;
    }
}
