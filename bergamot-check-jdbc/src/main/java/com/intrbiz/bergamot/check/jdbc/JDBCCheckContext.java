package com.intrbiz.bergamot.check.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.function.Consumer;

public class JDBCCheckContext
{
    private final JDBCChecker checker;
    
    private final Consumer<Throwable> onError;
    
    public JDBCCheckContext(JDBCChecker checker, Consumer<Throwable> onError)
    {
        this.checker = checker;
        this.onError = onError;
    }
    
    public void connect(String url, String username, String password, Consumer<JDBCConnection> onConnected)
    {
        try (Connection connection = DriverManager.getConnection(url, username, password))
        {
            // TODO: timeouts
            onConnected.accept(new JDBCConnection(connection));
        }
        catch (Throwable t)
        {
            this.onError.accept(t);
        }
    }
    
    public void connect(String url, String username, String password, Consumer<JDBCConnection> onConnected, Consumer<Throwable> onError)
    {
        try (Connection connection = DriverManager.getConnection(url, username, password))
        {
            // TODO: timeouts
            onConnected.accept(new JDBCConnection(connection));
        }
        catch (Throwable t)
        {
            onError.accept(t);
        }
    }
}
