package com.intrbiz.bergamot.nrpe.model;

public enum PacketType
{    
    QUERY(1),
    RESPONSE(2);
    
    private final short value;
    
    private PacketType(int value)
    {
        this.value = (short) value;
    }
    
    public short getValue()
    {
        return this.value;
    }
    
    public static PacketType valueOf(short value)
    {
        for (PacketType type : PacketType.values())
        {
            if (type.value == value) return type;
        }
        return null;
    }
}