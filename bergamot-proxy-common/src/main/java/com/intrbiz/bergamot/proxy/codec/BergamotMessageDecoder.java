package com.intrbiz.bergamot.proxy.codec;

import java.util.List;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.Message;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

@Sharable
public class BergamotMessageDecoder extends MessageToMessageDecoder<WebSocketFrame>
{
    private static final Logger logger = Logger.getLogger(BergamotMessageDecoder.class);
    
    private final BergamotTranscoder transcoder = BergamotTranscoder.getDefaultInstance();
    
    public BergamotMessageDecoder()
    {
        super();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) throws Exception
    {
        if (msg instanceof TextWebSocketFrame)
        {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) msg;
            Message message = this.transcoder.decodeFromString(textFrame.text(), Message.class);
            out.add(message);
        }
        else
        {
            logger.warn("Dropping unexcepted websocket frame: " + msg);
        }
    }
}
