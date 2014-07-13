package com.intrbiz.bergamot.nrpe.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.nrpe.model.NRPEPacket;

public class NRPEHandler extends ChannelInboundHandlerAdapter
{
    private Logger logger = Logger.getLogger(NRPEHandler.class);
    
    private long start;
    
    private long end;
    
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
        this.responseHandler.accept(p);
        // close
        if (ctx.channel().isActive()) ctx.close();
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx)
    {
        logger.debug("Sending NRPE request");
        this.start = System.currentTimeMillis();
        ctx.writeAndFlush(this.request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        logger.debug("Error processing NRPE request: " + cause);
        // invoke the callback
        this.errorHandler.accept(cause);
    }    
}
