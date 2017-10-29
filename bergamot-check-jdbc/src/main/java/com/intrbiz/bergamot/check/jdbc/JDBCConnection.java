package com.intrbiz.bergamot.check.jdbc;

import java.sql.Connection;
import java.util.function.Function;

public class JDBCConnection 
{   
    private final JDBCCheckContext context;
    
    private final Connection connection;
    
    public JDBCConnection(JDBCCheckContext context, Connection connection)
    {
        this.context = context;
        this.connection = connection;
    }
    
    public JDBCCheckContext getContext()
    {
        return this.context;
    }
    
    public JDBCStatement prepare(String query)
    {
        return new JDBCStatement(query) {
            protected Connection getConnection()
            {
                return connection;
            }
        };
    }
    
    public <T> T query(String query, Function<JDBCResultSet, T> resultProcessor)
    {
        return this.prepare(query).query(resultProcessor);
    }
}
