package raw.jna;

import java.nio.ByteBuffer;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public interface CLibrary extends Library
{
    /*
     * Constants 
     */
    
    /* Address Families */
    public static final int AF_INET  = 2;
    public static final int AF_INET6 = 10;
    
    /* Socket types */
    public static final int SOCK_STREAM = 1;
    public static final int SOCK_DGRAM  = 2;
    public static final int SOCK_RAW    = 3;
    
    /* IP Protocols */
    public static final int IPPROTO_IP   = 0;
    public static final int IPPROTO_ICMP = 1;
    public static final int IPPROTO_TCP  = 6;
    public static final int IPPROTO_UDP  = 17;
    public static final int IPPROTO_RAW  = 255;
    
    /*
     * Send includes the IP header
     */
    public static final int IP_HDRINCL   = 3;
    
    /*
     * Access the library
     */
    CLibrary INSTANCE = (CLibrary) Native.loadLibrary("c", CLibrary.class);
    
    /*
     * Marshalled functions 
     */
    
    int socket(int domain, int type, int protocol) throws LastErrorException;
    
    int getsockopt(int sockfd, int level, int optname, Pointer optval, Pointer optlen) throws LastErrorException;
    
    int setsockopt(int sockfd, int level, int optname, Pointer optval, int optlen) throws LastErrorException;
    
    int send(int fd, ByteBuffer buffer, int length, int flags) throws LastErrorException;
    
    int recv(int fd, ByteBuffer buffer, int length , int flags) throws LastErrorException;
    
    int close(int fd) throws LastErrorException;
}