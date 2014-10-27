package raw.model.payload;

import java.nio.ByteBuffer;

import raw.model.IPPayload;

public class ICMPPacket implements IPPayload
{
    public static final byte ICMP_TYPE_ECHO = 8;
    
    private byte type;
    
    private byte code;
    
    private short checksum;
    
    private short id;
    
    private short sequence;
    
    private byte[] data;
    
    public ICMPPacket()
    {
        super();
    }
    
    public ICMPPacket(ByteBuffer from)
    {
        super();
        // verify the checksum
        this.checksum = this.verifyChecksum(from);
        // decode
        this.type = from.get();
        this.code = from.get();
        from.getShort();
        this.id = from.getShort();
        this.sequence = from.getShort();
        this.data = new byte[from.remaining()];
        from.get(this.data);
    }
    
    private short computeChecksum(ByteBuffer buffer)
    {
        int start = buffer.position();
        // ensure the checksum is zeroed
        buffer.putShort(start + 2, (short) 0);
        // compute
        int sum = 0;
        while (buffer.hasRemaining())
        {
            sum += buffer.getShort() & 0xFFFF;
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
