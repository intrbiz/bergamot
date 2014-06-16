package com.intrbiz.bergamot.nrpe.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.io.IOException;
import java.util.List;

import com.intrbiz.bergamot.nrpe.model.NRPEPacket;

public class NRPEDecoder extends ReplayingDecoder<Void>
{
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws IOException
    {
        // NRPE uses a fixed length packet and uses a CRC32,
        // so it is easiest to just read 1024 [sic] bytes into 
        // a buffer
        byte[] packet = new byte[NRPEPacket.PACKET_LENGTH];
        in.readBytes(packet);
        out.add(NRPEPacket.parse(packet));
    }
}
