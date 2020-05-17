package com.intrbiz.bergamot.nrpe.netty;

import java.io.EOFException;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.nrpe.model.NRPEPacket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;

public class NRPEHandler extends ChannelInboundHandlerAdapter
{
    private Logger logger = Logger.getLogger(NRPEHandler.class);
    
    private boolean complete = false;
    
    private long start = -1;
    
    private long end = -1;
    
    private NRPEPacket request;
    
    private Consumer<NRPEPacket> responseHandler;
    
    private Consumer<Throwable> errorHandler;
    
    public NRPEHandler(NRPEPacket request, Consumer<NRPEPacket> responseHandler, Consumer<Throwable> errorHandler)
    {
        super();
        this.request = request;
        this.responseHandler = responseHandler;
        this.errorHandler = errorHandler;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        this.end = System.currentTimeMillis();
        NRPEPacket p = (NRPEPacket) msg;
        p.setRuntime(this.end - this.start);
        logger.debug("Got NRPE response in: status: " + p.getResponseCode() + ", message: " + p.getMessage() + ", runtime: " + p.getRuntime() + "ms");
        // invoke the callback
        complete = true;
        this.responseHandler.accept(p);
        // close
        if (ctx.channel().isActive()) ctx.close();
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {
        if (evt instanceof SslHandshakeCompletionEvent)
        {
            SslHandshakeCompletionEvent sslEvt = (SslHandshakeCompletionEvent) evt;
            if (sslEvt.isSuccess())
            {
                logger.debug("SSL Handshake complete, sending NRPE request");
                this.start = System.currentTimeMillis();
                ctx.writeAndFlush(this.request);
            }
            else
            {
                logger.warn("SSL Handshake failed");
                complete = true;
                this.errorHandler.accept(sslEvt.cause());
                ctx.channel().close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        logger.debug("Error processing NRPE request: " + cause);
        // invoke the callback
        complete = true;
        this.errorHandler.accept(cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        if (! this.complete)
        {
            this.errorHandler.accept(new EOFException("Connection closed by NRPE before response received."));
        }
    }
}
