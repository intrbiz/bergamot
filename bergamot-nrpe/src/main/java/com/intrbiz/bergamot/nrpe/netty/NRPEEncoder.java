package com.intrbiz.bergamot.nrpe.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import com.intrbiz.bergamot.nrpe.model.NRPEPacket;

public class NRPEEncoder extends MessageToByteEncoder<NRPEPacket>
{
    @Override
    public void encode(ChannelHandlerContext ctx, NRPEPacket msg, ByteBuf out) throws Exception
    {
        // simply encode to a buffer and send it
        out.writeBytes(msg.encodePacket());
    }
}
