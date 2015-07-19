package com.intrbiz.bergamot.check.jdbc;

import java.sql.Connection;
import java.util.function.Function;

public class JDBCConnection 
{    
    private Connection connection;
    
    public JDBCConnection(Connection connection)
    {
        this.connection = connection;
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
