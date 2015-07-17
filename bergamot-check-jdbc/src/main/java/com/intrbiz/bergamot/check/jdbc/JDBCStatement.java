package com.intrbiz.bergamot.check.jdbc;

import java.sql.PreparedStatement;
import java.util.function.Consumer;

public class JDBCStatement
{
    private PreparedStatement stmt;
    
    public JDBCStatement(PreparedStatement stmt)
    {
        this.stmt = stmt;
    }
    
    public void bind(int index, Object value)
    {
        
    }
    
    public int execute()
    {
        return 0;
    }
    
    public void query()
    {
    }
}
