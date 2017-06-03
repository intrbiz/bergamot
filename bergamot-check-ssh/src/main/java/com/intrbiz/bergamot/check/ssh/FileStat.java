package com.intrbiz.bergamot.check.ssh;

import com.jcraft.jsch.SftpATTRS;

public class FileStat
{   
    private final SftpATTRS attrs;
    
    public FileStat(SftpATTRS attrs)
    {
        this.attrs = attrs;
    }

    public String permissionsString()
    {
        return attrs.getPermissionsString();
    }

    public boolean isFile()
    {
        return attrs.isReg();
    }

    public boolean isDirectory()
    {
        return attrs.isDir();
    }
    
    public boolean isDir()
    {
        return attrs.isDir();
    }

    public boolean isDevice()
    {
        return attrs.isChr() || attrs.isBlk();
    }

    public boolean isFifo()
    {
        return attrs.isFifo();
    }

    public boolean isLink()
    {
        return attrs.isLink();
    }

    public boolean isSocket()
    {
        return attrs.isSock();
    }

    public long getSize()
    {
        return attrs.getSize();
    }

    public int userId()
    {
        return attrs.getUId();
    }

    public int groupId()
    {
        return attrs.getGId();
    }

    public int permissions()
    {
        return attrs.getPermissions();
    }

    public int aTime()
    {
        return attrs.getATime();
    }

    public int mTime()
    {
        return attrs.getMTime();
    }
}
