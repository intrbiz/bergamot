package com.intrbiz.bergamot.proxy.client.handler;

import java.util.Objects;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.Message;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MessageHandler extends SimpleChannelInboundHandler<Message>
{
    private static final Logger logger = Logger.getLogger(MessageHandler.class);
    
    protected final Consumer<Message> messageHandler;
    
    public MessageHandler(Consumer<Message> messageHandler)
    {
        super();
        this.messageHandler = Objects.requireNonNull(messageHandler);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception
    {
        if (logger.isTraceEnabled()) logger.trace("Got message: " + msg);
        messageHandler.accept(msg);
    }
}