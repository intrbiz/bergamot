package com.intrbiz.bergamot.nrpe.model;

public enum PacketVersion
{    
    NRPE_2(2);
    
    private final short value;
    
    private PacketVersion(int value)
    {
        this.value = (short) value;
    }
    
    public short getValue()
    {
        return this.value;
    }
    
    public static PacketVersion valueOf(short value)
    {
        for (PacketVersion version : PacketVersion.values())
        {
            if (version.value == value) return version;
        }
        return null;
    }
}