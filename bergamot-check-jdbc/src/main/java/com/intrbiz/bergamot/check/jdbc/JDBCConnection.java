package com.intrbiz.bergamot.check.jdbc;

import java.sql.Connection;
import java.util.function.Consumer;

public class JDBCConnection 
{
    private Consumer<Throwable> onError;
    
    private Connection connection;
    
    public JDBCConnection(Connection connection, Consumer<Throwable> onError)
    {
        this.connection = connection;
        this.onError = onError;
    }
    
    public JDBCStatement prepare(String query)
    {
        return null;
    }
    
    public JDBCResultSet query(String query)
    {
        return null;
    }
}
