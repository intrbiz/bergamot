package com.intrbiz.bergamot.check.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.function.Consumer;

import org.apache.log4j.Logger;

public class TCPClientHandler extends ChannelInboundHandlerAdapter
{
    private Logger logger = Logger.getLogger(TCPClientHandler.class);

    private long opened;

    private long connected;

    private Consumer<TCPCheckResponse> responseHandler;

    private Consumer<Throwable> errorHandler;

    public TCPClientHandler(Consumer<TCPCheckResponse> responseHandler, Consumer<Throwable> errorHandler)
    {
        super();
        this.responseHandler = responseHandler;
        this.errorHandler = errorHandler;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception
    {
        System.out.println(System.currentTimeMillis() + ": Channel opened: " + ctx.channel().isOpen());
        this.opened = System.currentTimeMillis();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx)
    {
        System.out.println(System.currentTimeMillis() + ": TCP Connection connected");
        this.connected = System.currentTimeMillis();
        // invoke the response
        this.responseHandler.accept(new TCPCheckResponse(this.connected - this.opened));
        // close
        if (ctx.channel().isActive()) ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        // TODO
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        logger.debug("Error processing HTTP request: " + cause);
        // invoke the callback
        this.errorHandler.accept(cause);
    }
}
