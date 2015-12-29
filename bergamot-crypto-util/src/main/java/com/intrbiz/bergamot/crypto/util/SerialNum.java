package com.intrbiz.bergamot.crypto.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

import org.bouncycastle.util.encoders.Hex;

/**
 * A semantic certificate Serial number, this combines a UUID with a short revision.
 * 
 * This allows for certificate Serial numbers to have a meaning, which is useful in 
 * some situation and also allows for a certificate to be revised, without loosing 
 * the unique id. 
 * 
 */
public final class SerialNum
{
    private final UUID id;

    private final short rev;

    public SerialNum(UUID id, short rev)
    {
        this.id = id;
        this.rev = rev;
    }
    
    public SerialNum(UUID id, int rev)
    {
        this(id, (short) rev);
    }

    public UUID getId()
    {
        return this.id;
    }

    public short getRev()
    {
        return this.rev;
    }

    /**
     * Return a SerialNum which represents the next revision of this SerialNum, IE: rev + 1
     */
    public SerialNum revision()
    {
        return new SerialNum(this.id, this.rev + 1);
    }

    public byte[] toBytes()
    {
        ByteBuffer bb = ByteBuffer.wrap(new byte[18]);
        bb.putLong(this.id.getMostSignificantBits());
        bb.putLong(this.id.getLeastSignificantBits());
        bb.putShort(this.rev);
        return bb.array();
    }

    public BigInteger toBigInt()
    {
        return new BigInteger(1, this.toBytes());
    }

    public String toString()
    {
        return new String(Hex.encode(this.toBytes())).toUpperCase();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + rev;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SerialNum other = (SerialNum) obj;
        if (id == null)
        {
            if (other.id != null) return false;
        }
        else if (!id.equals(other.id)) return false;
        if (rev != other.rev) return false;
        return true;
    }

    public static SerialNum fromBytes(byte[] array)
    {
        if (array.length < 18) throw new IllegalArgumentException("A serial num is at least 18 bytes long!");
        ByteBuffer bb = ByteBuffer.wrap(array);
        bb.position(bb.capacity() - 18);
        UUID id = new UUID(bb.getLong(), bb.getLong());
        short rev = bb.getShort();
        return new SerialNum(id, rev);
    }

    public static SerialNum fromString(String serialNumHex)
    {
        return fromBytes(Hex.decode(serialNumHex));
    }

    public static SerialNum fromBigInt(BigInteger bigInt)
    {
        return fromBytes(bigInt.toByteArray());
    }
    
    public static SerialNum randomSerialNum()
    {
        return new SerialNum(UUID.randomUUID(), 1);
    }
    
    public static SerialNum fromName(String name)
    {
        // build UUID from the name
        return new SerialNum(UUID.nameUUIDFromBytes(name.getBytes(Charset.forName("UTF8"))), 1);
    }
}
