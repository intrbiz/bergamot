package com.intrbiz.bergamot.check.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Consumer;

public class JDBCCheckContext
{
    private Consumer<Throwable> onError;
    
    public JDBCCheckContext(Consumer<Throwable> onError)
    {
        this.onError = onError;
    }
    
    public void connect(String url, String username, String password, Consumer<JDBCConnection> onConnected)
    {
        try (Connection connection = DriverManager.getConnection(url, username, password))
        {
            onConnected.accept(new JDBCConnection(connection, this.onError));
        }
        catch (SQLException e)
        {
            this.onError.accept(e);
        }
    }
}
