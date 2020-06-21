package com.intrbiz.bergamot.proxy.handler.client;

import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.Message;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MessageHandler extends SimpleChannelInboundHandler<Message>
{
    private static final Logger logger = Logger.getLogger(MessageHandler.class);
    
    protected final BiConsumer<Message, Channel> messageHandler;
    
    public MessageHandler(BiConsumer<Message, Channel> messageHandler)
    {
        super();
        this.messageHandler = Objects.requireNonNull(messageHandler);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception
    {
        if (logger.isTraceEnabled()) logger.trace("Got message: " + msg);
        messageHandler.accept(msg, ctx.channel());
    }
}