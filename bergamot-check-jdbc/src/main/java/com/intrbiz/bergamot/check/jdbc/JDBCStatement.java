package com.intrbiz.bergamot.check.jdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public abstract class JDBCStatement
{
    private final String sql;
    
    private List<Binder> bindings = new LinkedList<Binder>();
    
    public JDBCStatement(String sql)
    {
        this.sql = sql;
    }
    
    public JDBCStatement bind(Object value)
    {
        bindings.add((s, i) -> s.setObject(i, value));
        return this;
    }
    
    public JDBCStatement bindString(String value)
    {
        bindings.add((s, i) -> s.setString(i, value));
        return this;
    }
    
    public JDBCStatement bindShort(short value)
    {
        bindings.add((s, i) -> s.setShort(i, value));
        return this;
    }
    
    public JDBCStatement bindInt(int value)
    {
        bindings.add((s, i) -> s.setInt(i, value));
        return this;
    }
    
    public JDBCStatement bindLong(long value)
    {
        bindings.add((s, i) -> s.setLong(i, value));
        return this;
    }
    
    public JDBCStatement bindFloat(float value)
    {
        bindings.add((s, i) -> s.setFloat(i, value));
        return this;
    }
    
    public JDBCStatement bindDouble(double value)
    {
        bindings.add((s, i) -> s.setDouble(i, value));
        return this;
    }
    
    public JDBCStatement bindDate(Date value)
    {
        bindings.add((s, i) -> s.setDate(i, value));
        return this;
    }
    
    public JDBCStatement bindTime(Time value)
    {
        bindings.add((s, i) -> s.setTime(i, value));
        return this;
    }
    
    public JDBCStatement bindTimestamp(Timestamp value)
    {
        bindings.add((s, i) -> s.setTimestamp(i, value));
        return this;
    }
    
    public JDBCStatement bindBytes(byte[] value)
    {
        bindings.add((s, i) -> s.setBytes(i, value));
        return this;
    }
    
    public int execute()
    {
        try (PreparedStatement stmt = this.getConnection().prepareStatement(this.sql))
        {
            // bind
            int i = 1;
            for (Binder binding : bindings)
            {
                binding.bind(stmt, i++);
            }
            // execute
            return stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        }
    }
    
    public <T> T query(Function<JDBCResultSet, T> resultProcessor)
    {
        try (PreparedStatement stmt = this.getConnection().prepareStatement(this.sql))
        {
            // bind
            int i = 1;
            for (Binder binding : bindings)
            {
                binding.bind(stmt, i++);
            }
            // execute
            try (ResultSet rs = stmt.executeQuery())
            {
                return resultProcessor.apply(new JDBCResultSet(rs));
            }
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        }
    }
    
    protected abstract Connection getConnection();
    
    private static interface Binder
    {
        void bind(PreparedStatement stmt, int index) throws SQLException;
    }
}
