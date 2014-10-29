package com.intrbiz.bergamot.net.raw;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

import com.intrbiz.bergamot.net.raw.jna.CLibrary;
import com.intrbiz.bergamot.net.raw.jna.SockAddrIn;
import com.intrbiz.bergamot.net.raw.jna.TimeVal;
import com.intrbiz.bergamot.net.raw.model.IPPacket;
import com.intrbiz.bergamot.net.raw.model.IPPayload;
import com.sun.jna.LastErrorException;
import com.sun.jna.Native;

/**
 * Handle sending and receiving ICMP packets.
 * 
 * One thread is used to send an receive ICMP packets, 
 * invoking appropriate callbacks. 
 * 
 */
public class RawEngine<UserContext>
{
    private LinkedBlockingQueue<QueuedMessage> sendQueue = new LinkedBlockingQueue<QueuedMessage>(1000);

    private volatile boolean run = false;

    private final Object shutdownLock = new Object();

    private Thread runner;

    private int socketFd;

    private ByteBuffer buffer = ByteBuffer.allocateDirect(2048);

    private OnRecv callback;
    
    private long recieveTimeout = 200L * 1000L;

    public RawEngine()
    {
        super();
        this.open(CLibrary.IPPROTO_ICMP);
    }

    public RawEngine(OnRecv callback)
    {
        super();
        this.callback = callback;
        this.open(CLibrary.IPPROTO_ICMP);
    }
    
    public RawEngine(long recieveTimeout, OnRecv callback)
    {
        super();
        this.recieveTimeout = recieveTimeout;
        this.callback = callback;
        this.open(CLibrary.IPPROTO_ICMP);
    }
    
    public RawEngine(int protocol)
    {
        super();
        this.open(protocol);
    }

    public RawEngine(int protocol, OnRecv callback)
    {
        super();
        this.callback = callback;
        this.open(protocol);
    }
    
    public RawEngine(int protocol, long recieveTimeout, OnRecv callback)
    {
        super();
        this.recieveTimeout = recieveTimeout;
        this.callback = callback;
        this.open(protocol);
    }

    /**
     * On receive callback
     */
    public OnRecv getCallback()
    {
        return callback;
    }

    /**
     * On receive callback
     */
    public void setCallback(OnRecv callback)
    {
        this.callback = callback;
    }
    
    /**
     * The receive timeout in microseconds (default 200ms)
     */
    public long getRecieveTimeout()
    {
        return recieveTimeout;
    }

    /**
     * Open the raw socket
     */
    private void open(int protocol)
    {
        try
        {
            this.socketFd = CLibrary.INSTANCE.socket(CLibrary.AF_INET, CLibrary.SOCK_RAW, protocol);
            // wait 20ms to recv a response
            TimeVal tv = new TimeVal(0L, this.recieveTimeout);
            tv.write();
            CLibrary.INSTANCE.setsockopt(this.socketFd, CLibrary.SOL_SOCKET, CLibrary.SO_RCVTIMEO, tv.getPointer(), tv.size());
        }
        catch (LastErrorException lee)
        {
            throw new RuntimeException("Failed to create raw socket, check capabilities", lee);
        }
    }

    /**
     * Create a thread for our run loop and start it
     */
    public void start()
    {
        synchronized (this)
        {
            if (this.run == false)
            {
                this.run = true;
                this.runner = new Thread(new Runnable()
                {
                    public void run()
                    {
                        RawEngine.this.runLoop();
                        RawEngine.this.close();
                    }
                });
                this.runner.start();
            }
        }
    }
    
    private void close()
    {
        try
        {
            CLibrary.INSTANCE.close(this.socketFd);
        }
        catch (LastErrorException lee)
        {
            lee.printStackTrace();
        }
        finally
        {
            synchronized (this.shutdownLock)
            {
                this.shutdownLock.notifyAll();
            }
        }
    }

    /**
     * The main run loop
     */
    private void runLoop()
    {
        while (this.run)
        {
            // send stuff
            this.send();
            // recv stuff
            this.recv();
        }
    }

    private void send()
    {
        QueuedMessage msg;
        while ((msg = this.sendQueue.poll()) != null)
        {
            // pack the message
            this.buffer.clear();
            msg.payload.pack(this.buffer);
            this.buffer.flip();
            // send
            int sent = CLibrary.INSTANCE.sendto(this.socketFd, this.buffer, this.buffer.limit(), 0, msg.addr, msg.addr.size());
            if (sent < 0)
            {
                // error sending
                int errNo = Native.getLastError();
                System.err.println("Error sending message: " + errNo);
                break;
            }
            // fire the send callback
            if (msg.callback != null)
            {
                msg.callback.sent(msg.context, msg.payload, msg.to, msg.port, msg.addr);
            }
        }
    }

    private void recv()
    {
        while (true)
        {
            // read a message
            this.buffer.clear();
            int got = CLibrary.INSTANCE.recv(this.socketFd, this.buffer, this.buffer.capacity(), 0 /*CLibrary.MSG_DONTWAIT*/);
            if (got < 0)
            {
                // got nothing or error
                int errNo = Native.getLastError();
                if (errNo != CLibrary.ErroNo.EAGAIN)
                {
                    System.err.println("Error receiving message: " + errNo);
                }
                break;
            }
            else
            {
                try
                {
                    // parse the message
                    this.buffer.limit(got);
                    this.buffer.rewind();
                    IPPacket packet = new IPPacket(buffer);
                    // invoke the recv callback
                    if (this.callback != null)
                    {
                        this.callback.recv(packet);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Send a message
     */
    public void send(UserContext context, IPPayload payload, InetAddress to, int port, OnSend<UserContext> sendCallback)
    {
        this.send(context, payload, to, port, new SockAddrIn.ByReference(to, port), sendCallback);
    }

    public void send(UserContext context, IPPayload payload, InetAddress to, int port, SockAddrIn.ByReference addr, OnSend<UserContext> sendCallback)
    {
        try
        {
            this.sendQueue.put(new QueuedMessage(context, payload, to, port, addr, sendCallback));
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Signal shutdown
     */
    public void shutdown()
    {
        // signal the sender to stop
        this.run = false;
        // wait until the send loop has shutdown
        synchronized (this.shutdownLock)
        {
            try
            {
                this.shutdownLock.wait();
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    /**
     * Are we running
     */
    public boolean isRunning()
    {
        return this.run;
    }

    /**
     * A message queues to be sent
     */
    private class QueuedMessage
    {
        public final UserContext context;
        
        public final IPPayload payload;

        public final InetAddress to;

        public final int port;

        public final SockAddrIn.ByReference addr;

        public final OnSend<UserContext> callback;

        public QueuedMessage(UserContext context, IPPayload payload, InetAddress to, int port, SockAddrIn.ByReference addr, OnSend<UserContext> callback)
        {
            this.context = context;
            this.payload = payload;
            this.to = to;
            this.port = port;
            this.addr = addr;
            this.callback = callback;
        }
    }

    /**
     * Callback invoked when a message is sent
     */
    public static interface OnSend<UserContext>
    {
        void sent(UserContext context, IPPayload payload, InetAddress to, int port, SockAddrIn.ByReference addr);
    }

    /**
     * Callback invoked when a message is received
     */
    public static interface OnRecv
    {
        void recv(IPPacket packet);
    }
}
