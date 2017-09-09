package com.intrbiz.bergamot.check.ssh;

import java.util.LinkedList;
import java.util.List;

import com.jcraft.jsch.ChannelSftp;

public class SFTPContext
{    
    private final ChannelSftp sftp;
    
    public SFTPContext(ChannelSftp sftp)
    {
        this.sftp = sftp;
    }
    
    /**
     * Get the current working directory of the remote SFTP server
     */
    public String pwd()
    {
        try
        {
            return this.sftp.pwd();
        }
        catch (Exception e)
        {
            throw new SSHException(e);
        }
    }
    
    /**
     * Get the home directory of the remote SFTP server
     */
    public String getHome()
    {
        try
        {
            return this.sftp.getHome();
        }
        catch (Exception e)
        {
            throw new SSHException(e);
        }
    }
    
    /**
     * List files at the given path on the remote SFTP server
     * @param path the path
     * @return a list of file names
     */
    public List<String> ls(String path)
    {
        try
        {
            List<String> files = new LinkedList<String>();
            for (Object o : this.sftp.ls(path))
            {
                files.add((String) o);
            }
            return files;
        }
        catch (Exception e)
        {
            throw new SSHException(e);
        }
    }
    
    /**
     * Get file metadata for the given path on the remote SFTP server
     * @param path the path
     * @return the file metadata
     */
    public FileStat stat(String path)
    {
        try
        {
            return new FileStat(this.sftp.stat(path));
        }
        catch (Exception e)
        {
            throw new SSHException(e);
        }
    }
    
    /**
     * Change the current working directory of the remote SFTP server
     * @param path the path
     */
    public SFTPContext cd(String path)
    {
        try
        {
            this.sftp.cd(path);
        }
        catch (Exception e)
        {
            throw new SSHException(e);
        }
        return this;
    }
}
