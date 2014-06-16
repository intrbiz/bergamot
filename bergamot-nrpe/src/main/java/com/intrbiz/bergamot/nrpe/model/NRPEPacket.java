package com.intrbiz.bergamot.nrpe.model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import com.intrbiz.bergamot.nrpe.util.CRC32;

/**
 * A packet in the NRPE protocol
 */
public class NRPEPacket
{
    public static final int PACKET_LENGTH = 1036;

    public static final byte NULL = (byte) 0;

    public static final String NRPE_HELLO = "_NRPE_CHECK";

    private PacketVersion version = PacketVersion.NRPE_2;

    private PacketType type = PacketType.QUERY;

    private short responseCode = 0;

    private String message;

    private double runtime;

    public NRPEPacket()
    {
        super();
    }

    public PacketVersion getVersion()
    {
        return version;
    }

    public void setVersion(PacketVersion version)
    {
        this.version = version;
    }

    public NRPEPacket version(PacketVersion version)
    {
        this.version = version;
        return this;
    }

    public NRPEPacket version2()
    {
        this.version = PacketVersion.NRPE_2;
        return this;
    }

    public PacketType getType()
    {
        return type;
    }

    public void setType(PacketType type)
    {
        this.type = type;
    }

    public NRPEPacket type(PacketType type)
    {
        this.type = type;
        return this;
    }

    public NRPEPacket query()
    {
        this.type = PacketType.QUERY;
        return this;
    }

    public NRPEPacket response()
    {
        this.type = PacketType.RESPONSE;
        return this;
    }

    public NRPEPacket response(short responseCode)
    {
        this.type = PacketType.RESPONSE;
        this.responseCode = responseCode;
        return this;
    }

    public short getResponseCode()
    {
        return responseCode;
    }

    public void setResponseCode(short responseCode)
    {
        this.responseCode = responseCode;
    }

    public NRPEPacket responseCode(short responseCode)
    {
        this.type = PacketType.RESPONSE;
        this.responseCode = responseCode;
        return this;
    }

    public String getMessage()
    {
        return message;
    }

    public String getOutput()
    {
        return this.message;
    }
    
    public String getCommand()
    {
        return this.message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public NRPEPacket message(String message)
    {
        this.message = message;
        return this;
    }

    public NRPEPacket command(String command)
    {
        this.type = PacketType.QUERY;
        this.message = command;
        return this;
    }

    public NRPEPacket command(String command, String... args)
    {
        this.type = PacketType.QUERY;
        StringBuilder sb = new StringBuilder(command);
        if (args != null)
        {
            for (String arg : args)
            {
                sb.append("!").append(arg);
            }
        }
        this.message = sb.toString();
        return this;
    }
    
    public NRPEPacket command(String command, List<String> args)
    {
        this.type = PacketType.QUERY;
        StringBuilder sb = new StringBuilder(command);
        if (args != null)
        {
            for (String arg : args)
            {
                sb.append("!").append(arg);
            }
        }
        this.message = sb.toString();
        return this;
    }

    public NRPEPacket hello()
    {
        this.type = PacketType.QUERY;
        this.message = NRPE_HELLO;
        return this;
    }

    public double getRuntime()
    {
        return runtime;
    }

    public void setRuntime(double runtime)
    {
        this.runtime = runtime;
    }

    /**
     * Encode this packet to its binary form
     * @return
     */
    public byte[] encodePacket()
    {
        byte[] buffer = new byte[PACKET_LENGTH];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        // put the version
        byteBuffer.putShort(this.version.getValue());
        // put the type
        byteBuffer.putShort(this.type.getValue());
        // skip the crc
        byteBuffer.putInt(0);
        // put the response code
        byteBuffer.putShort(this.responseCode);
        // write message
        byteBuffer.put(this.message.getBytes());
        // ensure null terminated
        byteBuffer.put(NULL);
        // compute the crc
        int crc = CRC32.computeCRC32(buffer);
        // set the crc
        byteBuffer.position(4);
        byteBuffer.putInt(crc);
        return buffer;
    }

    /**
     * Decode this packet from its binary form
     * @param buffer
     * @param offset
     * @param length
     * @throws IOException if the CRC is invalid
     */
    public void decodePacket(byte[] buffer, int offset, int length) throws IOException
    {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, offset, length);
        // get the version
        this.version = PacketVersion.valueOf(byteBuffer.getShort());
        // get the type
        this.type = PacketType.valueOf(byteBuffer.getShort());
        // get the crc
        int crc = byteBuffer.getInt();
        // get the response code
        this.responseCode = byteBuffer.getShort();
        // get the output
        // how much I loath C string :(
        int strLen = 0;
        while (byteBuffer.hasRemaining() && byteBuffer.get() != NULL)
        {
            strLen++;
        }
        this.message = new String(buffer, offset + 10, strLen);
        // validate the CRC
        byteBuffer.position(4);
        byteBuffer.putInt(0);
        int expectedCrc = CRC32.computeCRC32(buffer, offset, length);
        if (expectedCrc != crc) throw new IOException("Invalid CRC checksum on NRPE Packet");
    }
    
    public static NRPEPacket parse(byte[] buffer) throws IOException
    {
        return parse(buffer, 0, buffer.length);
    }

    public static NRPEPacket parse(byte[] buffer, int offset, int length) throws IOException
    {
        NRPEPacket packet = new NRPEPacket();
        packet.decodePacket(buffer, offset, length);
        return packet;
    }
}
