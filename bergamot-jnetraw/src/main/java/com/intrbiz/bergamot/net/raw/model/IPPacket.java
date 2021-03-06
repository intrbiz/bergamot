package com.intrbiz.bergamot.net.raw.model;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import com.intrbiz.bergamot.net.raw.model.payload.ICMPPacket;

public class IPPacket
{
    public static final byte IP_PROTO_ICMP = 1;
    public static final byte IP_PROTO_UDP = 17;
    
    private byte ipVersion;
    
    private byte ipHeaderLength;
    
    private byte dscp;
    
    private byte ecn;
    
    private short totalLength;
    
    private short id;
    
    private short flags;
    
    private short fragmentOffset;
    
    private byte ttl;
    
    private byte protocol;
    
    private short headerChecksum;
    
    private byte[] sourceIPAddress;
    
    private byte[] destinationIPAddress;
    
    private IPPayload payload;
    
    public IPPacket()
    {
        super();
        this.ipVersion = 4;
        this.ipHeaderLength = 5;
        this.ttl = 64;
    }
    
    public IPPacket(byte protocol, InetAddress source, InetAddress destination, IPPayload payload)
    {
        super();
        if (! (source instanceof Inet4Address && destination instanceof Inet4Address))
            throw new IllegalArgumentException("Source and destination must be IPv4 addresses!");
        this.ipVersion = 4;
        this.ipHeaderLength = 5;
        this.ttl = 64;
        this.protocol = protocol;
        this.sourceIPAddress = source.getAddress();
        this.destinationIPAddress = source.getAddress();
        this.payload = payload;
    }
    
    public IPPacket(ByteBuffer from)
    {
        byte b;
        short s;
        int start = from.position();
        // verify the checksum
        this.headerChecksum = this.verifyChecksum(from);
        // 0 - version and ihl
        b = from.get();
        this.ipHeaderLength = (byte) (b & 0xF);
        this.ipVersion = (byte) (b >> 4);
        // 1 - dscp and ecn
        b = from.get();
        this.dscp = (byte) (b >> 2);
        this.ecn = (byte) (b & 0x3);
        // 2 - length
        this.totalLength = from.getShort();
        // 4 - id
        this.id = from.getShort();
        // 6 - flags and frag offset
        s = from.getShort();
        this.flags = (short) (s >> 13);
        this.fragmentOffset = (short) (s & 0x1FFF);
        // 8 - ttl
        this.ttl = from.get();
        // 9 - protocol
        this.protocol = from.get();
        // 10 - checksum
        from.getShort();
        // 12 - src
        this.sourceIPAddress = new byte[4];
        from.get(this.sourceIPAddress);
        // 16 - dest
        this.destinationIPAddress = new byte[4];
        from.get(this.destinationIPAddress);
        // options
        if (this.ipHeaderLength > 5)
        {
            // seek to the end of the options
            from.position(start + (this.ipHeaderLength * 4));
        }
        // payload
        if (this.protocol == IP_PROTO_ICMP)
        {
            this.payload = new ICMPPacket(from);
        }
    }
    
    public int computeLength()
    {
        return 20 + (this.payload == null ? 0 : this.payload.computeLength());
    }
    
    public void pack(ByteBuffer to)
    {
        int start = to.position();
        // compute the total packet length
        this.totalLength = (short) (this.computeLength() & 0xFFFF);
        // version and ihl
        to.put((byte) ((this.ipVersion << 4) & (this.ipHeaderLength & 0xF)));
        // dscp and ecn
        to.put((byte) ((this.dscp << 2) & (this.ecn & 0x3)));
        // total length
        to.putShort(this.totalLength);
        // id
        to.putShort(this.id);
        // flags and fragment offset
        to.putShort((short) ((this.flags << 13) & (this.fragmentOffset & 0x1FFF)));
        // ttl
        to.put(this.ttl);
        // protocol
        to.put(this.protocol);
        // checksum
        to.putShort((short) 0);
        // src ip
        to.put(this.sourceIPAddress);
        // dst ip
        to.put(this.destinationIPAddress);
        // compute the header checksum
        int end = to.position();
        to.position(start);
        this.headerChecksum = this.computeChecksum(to, 20);
        to.putShort(start + 10, this.headerChecksum);
        to.position(end);
        // pack the payload
        this.payload.pack(to);
    }
    
    private short computeChecksum(ByteBuffer buffer, int headerLength)
    {
        int start = buffer.position();
        int end = start + headerLength;
        // ensure the checksum is zeroed
        buffer.putShort(start + 10, (short) 0);
        // compute
        int sum = 0;
        while (buffer.position() < end)
        {
            sum += ((int) buffer.getShort()) & 0xFFFF;
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
        int headerLength = (buffer.get(start) & 0xF) * 4;
        // get the checksum
        short checksum = buffer.getShort(start + 10);
        // compute the checksum
        short computedChecksum = this.computeChecksum(buffer, headerLength);
        // verify
        if (checksum != computedChecksum)
        {
            throw new RuntimeException("Invalid IP Header Checksum: got=" + checksum + ", expected=" + computedChecksum + ", ihl=" + headerLength);
        }
        return checksum;
    }

    public byte getIpVersion()
    {
        return ipVersion;
    }

    public void setIpVersion(byte ipVersion)
    {
        this.ipVersion = ipVersion;
    }

    public byte getIpHeaderLength()
    {
        return ipHeaderLength;
    }

    public void setIpHeaderLength(byte ipHeaderLength)
    {
        this.ipHeaderLength = ipHeaderLength;
    }

    public byte getDscp()
    {
        return dscp;
    }

    public void setDscp(byte dscp)
    {
        this.dscp = dscp;
    }

    public byte getEcn()
    {
        return ecn;
    }

    public void setEcn(byte ecn)
    {
        this.ecn = ecn;
    }

    public short getTotalLength()
    {
        return totalLength;
    }

    public void setTotalLength(short totalLength)
    {
        this.totalLength = totalLength;
    }

    public short getId()
    {
        return id;
    }

    public void setId(short id)
    {
        this.id = id;
    }

    public short getFlags()
    {
        return flags;
    }

    public void setFlags(short flags)
    {
        this.flags = flags;
    }

    public short getFragmentOffset()
    {
        return fragmentOffset;
    }

    public void setFragmentOffset(short fragmentOffset)
    {
        this.fragmentOffset = fragmentOffset;
    }

    public byte getTtl()
    {
        return ttl;
    }

    public void setTtl(byte ttl)
    {
        this.ttl = ttl;
    }

    public byte getProtocol()
    {
        return protocol;
    }
    
    public boolean isICMP()
    {
        return this.protocol == IP_PROTO_ICMP;
    }
    
    public boolean isUDP()
    {
        return this.protocol == IP_PROTO_UDP;
    }
    
    public String getProtocolStr()
    {
        switch (this.protocol)
        {
            case IP_PROTO_ICMP: return "ICMP";
            case IP_PROTO_UDP: return "UDP";
        }
        return "UNKNOWN";
    }

    public void setProtocol(byte protocol)
    {
        this.protocol = protocol;
    }

    public short getHeaderChecksum()
    {
        return headerChecksum;
    }

    public void setHeaderChecksum(short headerChecksum)
    {
        this.headerChecksum = headerChecksum;
    }

    public byte[] getSourceIPAddress()
    {
        return sourceIPAddress;
    }
    
    public InetAddress getSource()
    {
        try
        {
            return InetAddress.getByAddress(this.getSourceIPAddress());
        }
        catch (UnknownHostException e)
        {
        }
        return null;
    }

    public void setSourceIPAddress(byte[] sourceIPAddress)
    {
        this.sourceIPAddress = sourceIPAddress;
    }

    public byte[] getDestinationIPAddress()
    {
        return destinationIPAddress;
    }
    
    public InetAddress getDestination()
    {
        try
        {
            return InetAddress.getByAddress(this.getDestinationIPAddress());
        }
        catch (UnknownHostException e)
        {
        }
        return null;
    }

    public void setDestinationIPAddress(byte[] destinationIPAddress)
    {
        this.destinationIPAddress = destinationIPAddress;
    }
    
    public IPPayload getPayload()
    {
        return payload;
    }

    public void setPayload(IPPayload payload)
    {
        this.payload = payload;
    }

    public String toString()
    {
        return "ip-packet {\n" +
                "src: " + this.getSource() + "\n" +
                "dst: " + this.getDestination() + "\n" +
                "proto: " + this.getProtocolStr() + "(" + this.getProtocol() + ")\n" +
                "ttl: " + this.getTtl() + "\n" +
                "ihl: " + this.getIpHeaderLength() + "\n" + 
                "len: " + this.getTotalLength() + "\n" +
                this.getPayload() +
               "}";
    }
}
