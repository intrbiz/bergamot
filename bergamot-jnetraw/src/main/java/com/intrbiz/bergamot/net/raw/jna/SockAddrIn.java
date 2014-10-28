package com.intrbiz.bergamot.net.raw.jna;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * Inet Socket Address
 */
public class SockAddrIn extends Structure
{
    public short sin_family;

    public byte[] sin_port;

    public byte[] sin_addr;

    public byte[] sin_zero = new byte[8];

    public SockAddrIn(int family, byte[] addr, byte[] port)
    {
        super();
        this.sin_family = (short) (family & 0xFFFF);
        this.sin_port = port;
        this.sin_addr = addr;
    }

    public SockAddrIn(InetAddress address, int port)
    {
        super();
        this.sin_family = CLibrary.AF_INET;
        this.sin_port = new byte[] { (byte) ((port >> 8) & 0xFF), (byte) (port & 0xFF) };
        this.sin_addr = address.getAddress();
    }
    
    public SockAddrIn()
    {
        super();
        this.sin_family = (short) 0;
        this.sin_port = new byte[2];
        this.sin_addr = new byte[4];
        this.allocateMemory();
    }

    public InetAddress getAddress()
    {
        try
        {
            return InetAddress.getByAddress(this.sin_addr);
        }
        catch (UnknownHostException e)
        {
        }
        return null;
    }

    public void setAddress(InetAddress address)
    {
        this.sin_addr = address.getAddress();
    }

    public int getPort()
    {
        return ((this.sin_port[0] & 0xFF) << 8) | (this.sin_port[0] & 0xFF);
    }

    public void setPort(int port)
    {
        this.sin_port = new byte[] { (byte) ((port >> 8) & 0xFF), (byte) (port & 0xFF) };
    }

    @Override
    protected List<String> getFieldOrder()
    {
        return Arrays.asList("sin_family", "sin_port", "sin_addr", "sin_zero");
    }

    public static class ByReference extends SockAddrIn implements Structure.ByReference
    {
        public ByReference()
        {
            super();
        }

        public ByReference(InetAddress address, int port)
        {
            super(address, port);
        }

        public ByReference(int family, byte[] addr, byte[] port)
        {
            super(family, addr, port);
        }
    }
}
