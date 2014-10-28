package com.intrbiz.bergamot.net.raw;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

import com.intrbiz.bergamot.net.raw.jna.CLibrary;
import com.intrbiz.bergamot.net.raw.jna.SockAddrIn;
import com.intrbiz.bergamot.net.raw.model.IPPacket;
import com.intrbiz.bergamot.net.raw.model.payload.ICMPPacket;
import com.sun.jna.Native;

public class RawSock
{
    public static void main(String[] args) throws Exception
    {
        boolean run = true;
        //
        InetAddress dst = InetAddress.getByName("172.30.13.42");
        System.out.println("Ping: " + dst);
        //
        SockAddrIn.ByReference dstAddr = new SockAddrIn.ByReference(dst, 8);
        //
        int sock = CLibrary.INSTANCE.socket(CLibrary.AF_INET, CLibrary.SOCK_RAW, CLibrary.IPPROTO_ICMP);
        //
        ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
        short seq = 0;
        short id = (short) (new SecureRandom()).nextInt();
        while (run)
        {
            // Thread.sleep(50);
            // send
            buffer.clear();
            ICMPPacket payload = new ICMPPacket(ICMPPacket.ICMP_TYPE_ECHO, id, seq++);
            payload.pack(buffer);
            buffer.flip();
            long sentAt = System.nanoTime();
            int sent = CLibrary.INSTANCE.sendto(sock, buffer, buffer.limit(), 0, dstAddr, dstAddr.size());
            if (sent < 0)
            {
                int errNo = Native.getLastError();
                System.out.println("Send error: " + errNo);
            }
            // recv
            buffer.clear();
            int got = CLibrary.INSTANCE.recv(sock, buffer, buffer.capacity(), 0 /*| CLibrary.MSG_DONTWAIT*/);
            if (got < 0)
            {
                int errNo = Native.getLastError();
                if (errNo != CLibrary.ErroNo.EAGAIN)
                {
                    System.out.println("Recv error: " + errNo);
                }
            }
            else
            {
                long gotAt = System.nanoTime();
                buffer.limit(got);
                buffer.rewind();
                IPPacket packet = new IPPacket(buffer);
                // System.out.println(packet);
                if ((seq % 100) == 0)
                    System.out.println("Got ICMP reply from " + packet.getSource() + " type " + ((ICMPPacket) packet.getPayload()).getType() + " code " + ((ICMPPacket) packet.getPayload()).getCode() + " id " + ((ICMPPacket) packet.getPayload()).getId() + " seq " + ((ICMPPacket) packet.getPayload()).getSequence() + " in " + (((double) (gotAt - sentAt)) / 1_000_000D) + "ms");
            }
            if ((seq % 1000) == 0)
            {
                System.gc();
            }
        }
        //
        CLibrary.INSTANCE.close(sock);
    }
    
    public static String toHex(ByteBuffer buf)
    {
        StringBuilder sb = new StringBuilder();
        while (buf.hasRemaining())
        {
            byte b = buf.get();
            sb.append(toHex((b >> 4) & 0xF));
            sb.append(toHex(b & 0xF));
        }
        return sb.toString();
    }

    public static char toHex(int nibble)
    {
        switch (nibble)
        {
            case 0:
                return '0';
            case 1:
                return '1';
            case 2:
                return '2';
            case 3:
                return '3';
            case 4:
                return '4';
            case 5:
                return '5';
            case 6:
                return '6';
            case 7:
                return '7';
            case 8:
                return '8';
            case 9:
                return '9';
            case 10:
                return 'A';
            case 11:
                return 'B';
            case 12:
                return 'C';
            case 13:
                return 'D';
            case 14:
                return 'E';
            case 15:
                return 'F';
        }
        return 0;
    }
}
