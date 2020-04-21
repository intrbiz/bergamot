package com.intrbiz.bergamot.proxy.codec;

import java.util.List;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.Message;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

@Sharable
public class BergamotMessageEncoder extends MessageToMessageEncoder<Message>
{
    private final BergamotTranscoder transcoder = BergamotTranscoder.getDefaultInstance();
    
    public BergamotMessageEncoder()
    {
        super();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception
    {
        out.add(new TextWebSocketFrame(this.transcoder.encodeAsString(msg)));
    }
}
