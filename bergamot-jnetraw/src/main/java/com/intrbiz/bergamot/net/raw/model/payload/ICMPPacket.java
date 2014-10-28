package com.intrbiz.bergamot.net.raw.model.payload;

import java.nio.ByteBuffer;

import com.intrbiz.bergamot.net.raw.model.IPPayload;

/**
 * Basic structure of an ICMP packet
 */
public class ICMPPacket implements IPPayload
{
    /* Useful constants */
    
    public static final byte ICMP_TYPE_ECHO = 8;
    
    public static final byte[] DEFAULT_DATA = createDefaultData();
    
    private static final byte[] createDefaultData()
    {
        byte[] data = new byte[56];
        for (int i = 0; i < data.length; i ++)
        {
            data[i] = 0x1B;
        }
        return data;
    }
    
    /* ICMP packet header - 8 bytes*/
    
    private byte type;
    
    private byte code;
    
    private short checksum;
    
    private short id;
    
    private short sequence;
    
    /* ICMP packet data */
    
    private byte[] data;
    
    public ICMPPacket()
    {
        super();
    }
    
    public ICMPPacket(byte type, short id, short seq)
    {
        super();
        this.type = type;
        this.id = id;
        this.sequence = seq;
        this.data = DEFAULT_DATA;
    }
    
    public ICMPPacket(ByteBuffer from)
    {
        super();
        // verify the checksum
        this.checksum = this.verifyChecksum(from);
        // decode
        this.type = from.get();
        this.code = from.get();
        /* checksum */ from.getShort();
        this.id = from.getShort();
        this.sequence = from.getShort();
        this.data = new byte[from.remaining()];
        from.get(this.data);
    }
    
    public int computeLength()
    {
        return 8 + (this.data == null ? 0 : this.data.length);
    }
    
    public void pack(ByteBuffer to)
    {
        int start = to.position();
        // pack
        to.put(this.type);
        to.put(this.code);
        to.putShort((short) /* checksum */ 0);
        to.putShort(this.id);
        to.putShort(this.sequence);
        // data
        to.put(this.data);
        // compute checksum
        int end = to.position();
        to.limit(end);
        to.position(start);
        short checksum = this.computeChecksum(to);
        to.putShort(start + 2, checksum);
        to.position(end);
    }
    
    private short computeChecksum(ByteBuffer buffer)
    {
        int start = buffer.position();
        // ensure the checksum is zeroed
        buffer.putShort(start + 2, (short) 0);
        // compute
        int sum = 0;
        while (buffer.remaining() > 1)
        {
            sum += ((int) buffer.getShort()) & 0xFFFF;
        }
        // handle any remaining single byte value
        if (buffer.hasRemaining())
        {
            // odd sized packet
            sum += ((((int) buffer.get()) & 0xFF) << 8);
        }
        sum = (sum >> 16) + (sum & 0xFFFF);
        sum = (sum >> 16) + (sum & 0xFFFF);
        // reset to start of the payload
        buffer.position(start);
        return  (short) ((~sum) & 0xFFFF);
    }
    
    private short verifyChecksum(ByteBuffer buffer)
    {
        int start = buffer.position();
        // get the checksum
        short checksum = buffer.getShort(start + 2);
        // compute the checksum
        short computedChecksum = this.computeChecksum(buffer);
        // verify
        if (checksum != computedChecksum) throw new RuntimeException("Invalid ICMP Checksum");
        return checksum;
    }
    
    public byte getType()
    {
        return type;
    }

    public void setType(byte type)
    {
        this.type = type;
    }

    public byte getCode()
    {
        return code;
    }

    public void setCode(byte code)
    {
        this.code = code;
    }

    public short getChecksum()
    {
        return checksum;
    }

    public void setChecksum(short checksum)
    {
        this.checksum = checksum;
    }

    public short getId()
    {
        return id;
    }

    public void setId(short id)
    {
        this.id = id;
    }

    public short getSequence()
    {
        return sequence;
    }

    public void setSequence(short sequence)
    {
        this.sequence = sequence;
    }

    public byte[] getData()
    {
        return data;
    }

    public void setData(byte[] data)
    {
        this.data = data;
    }

    public String toString()
    {
        return "icmp-packet {\n" +
                "type: " + this.getType() + "\n" +
                "code: " + this.getCode() + "\n" +
                "cksm: " + this.getChecksum() + "\n" +
                "id: " + this.getId() + "\n" +
                "seq: " + this.getSequence() + "\n" +
               "}";
    }
}
