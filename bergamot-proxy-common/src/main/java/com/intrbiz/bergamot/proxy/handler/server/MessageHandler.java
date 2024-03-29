package com.intrbiz.bergamot.proxy.handler.server;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.proxy.model.ClientHeader;
import com.intrbiz.bergamot.proxy.processor.MessageProcessor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MessageHandler extends SimpleChannelInboundHandler<Message>
{
    private static final Logger logger = Logger.getLogger(MessageHandler.class);
    
    protected final BiFunction<ClientHeader, Channel, MessageProcessor> creator;
    
    protected final Consumer<MessageProcessor> closer;
    
    protected MessageProcessor processor;
    
    public MessageHandler(BiFunction<ClientHeader, Channel, MessageProcessor> creator, Consumer<MessageProcessor> closer)
    {
        super();
        this.creator = Objects.requireNonNull(creator);
        this.closer = Objects.requireNonNull(closer);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        if (this.processor != null)
        {
            try
            {
                this.processor.stop();
            }
            finally
            {
                this.closer.accept(this.processor);
            }
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception
    {
        if (logger.isTraceEnabled()) logger.trace("Got message: " + msg);
        if (this.processor != null)
            this.processor.processMessage(msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {
        if (evt instanceof ClientHeader)
        {
            this.processor = this.creator.apply((ClientHeader) evt, ctx.channel());
            if (this.processor == null)
            {
                logger.warn("Failed to create message processor, closing connection.");
                ctx.channel().close();
            }
            else
            {
                this.processor.start();
            }
        }
    }
}