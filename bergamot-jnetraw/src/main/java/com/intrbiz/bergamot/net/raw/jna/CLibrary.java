package com.intrbiz.bergamot.net.raw.jna;

import java.nio.ByteBuffer;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * JNA bindings to native raw sockets
 */
public interface CLibrary extends Library
{
    /*
     * Constants
     */

    public static final class ErroNo
    {
        public static final int EPERM = 1; /* Operation not permitted */

        public static final int ENOENT = 2; /* No such file or directory */

        public static final int ESRCH = 3; /* No such process */

        public static final int EINTR = 4; /* Interrupted system call */

        public static final int EIO = 5; /* I/O error */

        public static final int ENXIO = 6; /* No such device or address */

        public static final int E2BIG = 7; /* Argument list too long */

        public static final int ENOEXEC = 8; /* Exec format error */

        public static final int EBADF = 9; /* Bad file number */

        public static final int ECHILD = 10; /* No child processes */

        public static final int EAGAIN = 11; /* Try again */

        public static final int ENOMEM = 12; /* Out of memory */

        public static final int EACCES = 13; /* Permission denied */

        public static final int EFAULT = 14; /* Bad address */

        public static final int ENOTBLK = 15; /* Block device required */

        public static final int EBUSY = 16; /* Device or resource busy */

        public static final int EEXIST = 17; /* File exists */

        public static final int EXDEV = 18; /* Cross-device link */

        public static final int ENODEV = 19; /* No such device */

        public static final int ENOTDIR = 20; /* Not a directory */

        public static final int EISDIR = 21; /* Is a directory */

        public static final int EINVAL = 22; /* Invalid argument */

        public static final int ENFILE = 23; /* File table overflow */

        public static final int EMFILE = 24; /* Too many open files */

        public static final int ENOTTY = 25; /* Not a typewriter */

        public static final int ETXTBSY = 26; /* Text file busy */

        public static final int EFBIG = 27; /* File too large */

        public static final int ENOSPC = 28; /* No space left on device */

        public static final int ESPIPE = 29; /* Illegal seek */

        public static final int EROFS = 30; /* Read-only file system */

        public static final int EMLINK = 31; /* Too many links */

        public static final int EPIPE = 32; /* Broken pipe */

        public static final int EDOM = 33; /* Math argument out of domain of func */

        public static final int ERANGE = 34; /* Math result not representable */
    }

    /* Address Families */
    public static final int AF_INET = 2;

    public static final int AF_INET6 = 10;

    /* Socket types */
    public static final int SOCK_STREAM = 1;

    public static final int SOCK_DGRAM = 2;

    public static final int SOCK_RAW = 3;

    /* IP Protocols */
    public static final int IPPROTO_IP = 0;

    public static final int IPPROTO_ICMP = 1;

    public static final int IPPROTO_TCP = 6;

    public static final int IPPROTO_UDP = 17;

    public static final int IPPROTO_RAW = 255;

    /* Socket flags */
    public static final int MSG_DONTWAIT = 0x40;
    
    public static final int SO_RCVTIMEO  = 20;
    
    public static final int SOL_SOCKET   = 1;

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

    int sendto(int fd, ByteBuffer buffer, int length, int flags, SockAddrIn.ByReference addr, int addrlen);

    int recv(int fd, ByteBuffer buffer, int length, int flags);

    int close(int fd) throws LastErrorException;
}