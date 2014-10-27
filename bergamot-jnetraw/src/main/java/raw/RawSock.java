package raw;

import java.nio.ByteBuffer;

import raw.jna.CLibrary;
import raw.model.IPPacket;

import com.sun.jna.ptr.IntByReference;

public class RawSock
{
    public static void main(String[] args)
    {
        boolean run = true;
        //
        System.out.println("Creating Socket\n");
        int sock = CLibrary.INSTANCE.socket(CLibrary.AF_INET, CLibrary.SOCK_RAW, CLibrary.IPPROTO_ICMP);
        System.out.println("Sock: " + sock);
        // set socket option
        IntByReference one = new IntByReference(1);
        CLibrary.INSTANCE.setsockopt(sock, CLibrary.IPPROTO_IP, CLibrary.IP_HDRINCL, one.getPointer(), 4);
        //
        ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
        while (run)
        {
            System.out.println("Recv");
            buffer.clear();
            long got = CLibrary.INSTANCE.recv(sock, buffer, 4096, 0);
            buffer.limit((int) got);
            System.out.println("Got " + got + " -> " + toHex(buffer));
            buffer.rewind();
            IPPacket packet = new IPPacket(buffer);
            System.out.println(packet);
        }
        //
        System.out.println("Closing: " + sock);
        int ret = CLibrary.INSTANCE.close(sock);
        System.out.println("Closed: " + ret);
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
