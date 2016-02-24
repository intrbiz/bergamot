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
    public static final byte VERSION_1 = 0x00;
    
    public static final byte VERSION_2 = 0x42;
    
    public static final short MODE_NONE = 0x0000;
    
    public static final short MODE_AGENT = 0x0001;
    
    public static final short MODE_TEMPLATE = 0x0002;
    
    private static final byte V2_FORMAT_PREFIX = (byte) 0x5A;
    
    private final UUID id;

    private final short rev;
    
    private final byte version;
    
    private final short mode;

    public SerialNum(UUID id, short rev, byte version, short mode)
    {
        this.id = id;
        this.rev = rev;
        this.version = version;
        this.mode = mode;
    }
    
    public SerialNum(UUID id, short rev)
    {
        this(id, rev, VERSION_1, (short) 0);
    }
    
    public SerialNum(UUID id, int rev)
    {
        this(id, (short) rev, VERSION_1, (short) 0);
    }
    
    public SerialNum(UUID id, int rev, int version, int mode)
    {
        this(id, (short) rev, (byte) (version & 0xFF), (short) mode);
    }

    public UUID getId()
    {
        return this.id;
    }

    public short getRev()
    {
        return this.rev;
    }
    
    public byte getVersion()
    {
        return this.version;
    }
    
    public boolean isVersion1()
    {
        return this.version == VERSION_1;
    }
    
    public boolean isVersion2()
    {
        return this.version == VERSION_2;
    }
    
    public short getMode()
    {
        return this.mode;
    }
    
    public boolean isAgent()
    {
        return (this.mode & MODE_AGENT) == MODE_AGENT;
    }
    
    public boolean isTemplate()
    {
        return (this.mode & MODE_TEMPLATE) == MODE_TEMPLATE;
    }

    /**
     * Return a SerialNum which represents the next revision of this SerialNum, IE: rev + 1
     */
    public SerialNum revision()
    {
        return new SerialNum(this.id, this.rev + 1, this.version, this.mode);
    }

    public byte[] toBytes()
    {
        if (this.version == VERSION_1)
        {
            // old format
            ByteBuffer bb = ByteBuffer.wrap(new byte[18]);
            bb.putLong(this.id.getMostSignificantBits());
            bb.putLong(this.id.getLeastSignificantBits());
            bb.putShort(this.rev);
            return bb.array();
        }
        else
        {
            // version 2+ format
            // right most 18 bytes are the old version 1 format
            // the left most byte is a static marker (0x5A)
            // this stops the array being truncated when wrapped 
            // as a bignum.  Versioned data is filled between the 
            // left most marker byte and the version byte
            // this makes it easy to differentiate between version 
            // 1 and version 2+ structures
            ByteBuffer bb = ByteBuffer.wrap(new byte[22]);
            bb.put(V2_FORMAT_PREFIX); // marker
            bb.putShort(this.mode);
            bb.put(this.version);
            bb.putLong(this.id.getMostSignificantBits());
            bb.putLong(this.id.getLeastSignificantBits());
            bb.putShort(this.rev);
            return bb.array();
        }
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
        result = prime * result + mode;
        result = prime * result + rev;
        result = prime * result + version;
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
        if (mode != other.mode) return false;
        if (rev != other.rev) return false;
        if (version != other.version) return false;
        return true;
    }

    public static SerialNum fromBytes(byte[] array)
    {
        // validate the minimum length
        if (array.length < 18) throw new IllegalArgumentException("A serial num is at least 18 bytes long!");
        if (array.length < 20)
        {
            // old style format
            ByteBuffer bb = ByteBuffer.wrap(array);
            bb.position(bb.capacity() - 18);
            UUID id = new UUID(bb.getLong(), bb.getLong());
            short rev = bb.getShort();
            return new SerialNum(id, rev);
        }
        else
        {
            // version 2+ format
            ByteBuffer bb = ByteBuffer.wrap(array);
            // read out the uuid and rev (right aligned)
            bb.position(bb.capacity() - 18);
            UUID id = new UUID(bb.getLong(), bb.getLong());
            short rev = bb.getShort();
            // now look left wards and pull out the version
            bb.position(bb.capacity() - 19);
            byte version = bb.get();
            // parse the data based on version
            if (version == VERSION_2)
            {
                bb.position(bb.capacity() - 21);
                short mode = bb.getShort();
                return new SerialNum(id, rev, version, mode);
            }
            else if (version == VERSION_1)
            {
                return new SerialNum(id, rev);
            }
            else
            {
                throw new IllegalArgumentException("Failed to parse serial num binary format, maybe newer version");
            }
        }
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
        return randomSerialNum((short) 0);
    }
    
    public static SerialNum fromName(String name)
    {
        return fromName(name, (short) 0);
    }
    
    public static SerialNum randomSerialNum(short mode)
    {
        return new SerialNum(UUID.randomUUID(), 1, VERSION_2, mode);
    }
    
    public static SerialNum fromName(String name, short mode)
    {
        // build UUID from the name
        return new SerialNum(UUID.nameUUIDFromBytes(name.getBytes(Charset.forName("UTF8"))), 1, VERSION_2, mode);
    }
    
    public static SerialNum version2(UUID id, short rev, short mode)
    {
        return new SerialNum(id, rev, VERSION_2, mode);
    }
    
    public static SerialNum version2(UUID id, int rev, int mode)
    {
        return new SerialNum(id, (short) rev, VERSION_2, (short) mode);
    }
    
    public static SerialNum version2(UUID id, short rev)
    {
        return new SerialNum(id, rev, VERSION_2, (short) 0);
    }
    
    public static SerialNum version2(UUID id, int rev)
    {
        return new SerialNum(id, (short) rev, VERSION_2, (short) 0);
    }
    
    public static SerialNum version1(UUID id, short rev)
    {
        return new SerialNum(id, rev, VERSION_1, (short) 0);
    }
    
    public static SerialNum version1(UUID id, int rev)
    {
        return new SerialNum(id, (short) rev, VERSION_1, (short) 0);
    }
}
