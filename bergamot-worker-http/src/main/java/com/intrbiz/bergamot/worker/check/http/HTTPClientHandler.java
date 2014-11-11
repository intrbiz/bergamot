package com.intrbiz.bergamot.worker.check.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.function.Consumer;

import org.apache.log4j.Logger;

public class HTTPClientHandler extends ChannelInboundHandlerAdapter
{
    private Logger logger = Logger.getLogger(HTTPClientHandler.class);
    
    private long start;
    
    private FullHttpRequest request;
    
    private Consumer<HTTPCheckResponse> responseHandler;
    
    private Consumer<Throwable> errorHandler;
    
    public HTTPClientHandler(FullHttpRequest request, Consumer<HTTPCheckResponse> responseHandler, Consumer<Throwable> errorHandler)
    {
        super();
        this.request = request;
        this.responseHandler = responseHandler;
        this.errorHandler = errorHandler;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        FullHttpResponse response = (FullHttpResponse) msg;
        long runtime = System.currentTimeMillis() - this.start;
        logger.debug("Got HTTP response: " + response.getStatus() + " in: " + runtime + "ms");
        // invoke the callback
        this.responseHandler.accept(new HTTPCheckResponse(runtime, response));
        // close
        if (ctx.channel().isActive()) ctx.close();
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx)
    {
        logger.debug("Sending HTTP request");
        this.start = System.currentTimeMillis();
        ctx.writeAndFlush(this.request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        logger.debug("Error processing HTTP request: " + cause);
        // invoke the callback
        this.errorHandler.accept(cause);
    }    
}
