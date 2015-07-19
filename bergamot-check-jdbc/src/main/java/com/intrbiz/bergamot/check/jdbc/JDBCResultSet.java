package com.intrbiz.bergamot.check.jdbc;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class JDBCResultSet
{
    private ResultSet rs;
    
    public JDBCResultSet(ResultSet rs)
    {
        this.rs = rs;
    }
    
    public boolean next()
    {
        try
        {
            return rs.next();
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        }
    }
    
    public List<String> getColumns()
    {
        try
        {
            List<String> cols = new LinkedList<String>();
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++)
            {
                cols.add(md.getColumnLabel(i));
            }
            return cols;
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        }
    }
    
    public Object get(int index)
    {
        try
        {
            return this.rs.getObject(index);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        } 
    }
    
    public Object get(String name)
    {
        try
        {
            return this.rs.getObject(name);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        }
    }
    
    public String getString(int index)
    {
        try
        {
            return this.rs.getString(index);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        } 
    }
    
    public String getString(String name)
    {
        try
        {
            return this.rs.getString(name);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        }
    }
    
    public short getShort(int index)
    {
        try
        {
            return this.rs.getShort(index);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        } 
    }
    
    public short getShort(String name)
    {
        try
        {
            return this.rs.getShort(name);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        }
    }
    
    public int getInt(int index)
    {
        try
        {
            return this.rs.getInt(index);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        } 
    }
    
    public int getInt(String name)
    {
        try
        {
            return this.rs.getInt(name);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        }
    }
    
    public long getLong(int index)
    {
        try
        {
            return this.rs.getLong(index);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        } 
    }
    
    public long getLong(String name)
    {
        try
        {
            return this.rs.getLong(name);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        }
    }
    
    public float getFloat(int index)
    {
        try
        {
            return this.rs.getFloat(index);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        } 
    }
    
    public float getFloat(String name)
    {
        try
        {
            return this.rs.getFloat(name);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        }
    }
    
    public double getDouble(int index)
    {
        try
        {
            return this.rs.getDouble(index);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        } 
    }
    
    public double getDouble(String name)
    {
        try
        {
            return this.rs.getDouble(name);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        }
    }
    
    public Date getDate(int index)
    {
        try
        {
            return this.rs.getDate(index);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        } 
    }
    
    public Date getDate(String name)
    {
        try
        {
            return this.rs.getDate(name);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        }
    }
    
    public Time getTime(int index)
    {
        try
        {
            return this.rs.getTime(index);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        } 
    }
    
    public Time getTime(String name)
    {
        try
        {
            return this.rs.getTime(name);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        }
    }
    
    public Timestamp getTimestamp(int index)
    {
        try
        {
            return this.rs.getTimestamp(index);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        } 
    }
    
    public Timestamp getTimestamp(String name)
    {
        try
        {
            return this.rs.getTimestamp(name);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        }
    }
    
    public byte[] getBytes(int index)
    {
        try
        {
            return this.rs.getBytes(index);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        } 
    }
    
    public byte[] getBytes(String name)
    {
        try
        {
            return this.rs.getBytes(name);
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        }
    }
    
    public Optional<JDBCResultSet> first()
    {
        try
        {
            return rs.next() ? Optional.of(this) : Optional.empty();
        }
        catch (SQLException e)
        {
            throw new JDBCException(e);
        }
    }
}
