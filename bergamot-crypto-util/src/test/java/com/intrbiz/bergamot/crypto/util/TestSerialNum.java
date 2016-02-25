package com.intrbiz.bergamot.crypto.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.UUID;

import org.junit.Test;

public class TestSerialNum
{
    @Test()
    public void testSerialNumEquals()
    {
        UUID id = UUID.randomUUID();
        short rev = 23;
        SerialNum sn1 = new SerialNum(id, rev);
        SerialNum sn2 = new SerialNum(id, rev);
        assertThat(sn1, is(equalTo(sn2)));
    }
    
    @Test()
    public void testSerialNumHashcode()
    {
        UUID id = UUID.randomUUID();
        short rev = 53;
        SerialNum sn1 = new SerialNum(id, rev);
        SerialNum sn2 = new SerialNum(id, rev);
        assertThat(sn1.hashCode(), is(equalTo(sn2.hashCode())));
    }
    
    @Test()
    public void testSerialNumRevision()
    {
        UUID id = UUID.randomUUID();
        short rev = 53;
        SerialNum sn1 = new SerialNum(id, rev);
        SerialNum sn2 = sn1.revision();
        assertThat(sn1.getId(), is(equalTo(id)));
        assertThat(sn2.getId(), is(equalTo(id)));
        assertThat(sn1.getRev(), is(equalTo(rev)));
        assertThat(sn2.getRev(), is(equalTo((short) (rev + 1))));
    }
    
    @Test()
    public void testSerialNumToBytes()
    {
        UUID id = UUID.randomUUID();
        short rev = 13;
        // create our serial
        SerialNum sn1 = new SerialNum(id, rev);
        assertThat(sn1.getVersion(), is(equalTo((byte) 0)));
        // encode
        byte[] ba = sn1.toBytes();
        assertThat(ba, is(notNullValue()));
        assertThat(ba.length, is(equalTo(18)));
        // decode
        SerialNum sn2 = SerialNum.fromBytes(ba);
        // assert they match
        assertThat(sn1, is(equalTo(sn2)));
        assertThat(ba, is(equalTo(sn2.toBytes())));
    }
    
    @Test()
    public void testSerialNumToString()
    {
        UUID id = UUID.randomUUID();
        short rev = 16;
        // create our serial
        SerialNum sn1 = new SerialNum(id, rev);
        // encode
        String s = sn1.toString();
        // decode
        SerialNum sn2 = SerialNum.fromString(s);
        // assert they match
        assertThat(sn1, is(equalTo(sn2)));
        assertThat(s, is(equalTo(sn2.toString())));
    }
    
    @Test()
    public void testSerialNumToBigInt()
    {
        UUID id = UUID.randomUUID();
        short rev = 16;
        // create our serial
        SerialNum sn1 = new SerialNum(id, rev);
        // encode
        BigInteger bi = sn1.toBigInt();
        // decode
        SerialNum sn2 = SerialNum.fromBigInt(bi);
        // assert they match
        assertThat(sn1, is(equalTo(sn2)));
        assertThat(bi, is(equalTo(sn2.toBigInt())));
    }
    
    @Test()
    public void testSerialNumToBigIntV2()
    {
        UUID id = UUID.randomUUID();
        short rev = 16;
        // create our serial
        SerialNum sn1 = new SerialNum(id, rev, SerialNum.VERSION_2, 0x0001);
        // encode
        BigInteger bi = sn1.toBigInt();
        // decode
        SerialNum sn2 = SerialNum.fromBigInt(bi);
        // assert they match
        assertThat(sn1, is(equalTo(sn2)));
        assertThat(bi, is(equalTo(sn2.toBigInt())));
    }
    
    @Test()
    public void testSerialNumV2()
    {
        UUID id = UUID.randomUUID();
        short rev = 16;
        short mode = SerialNum.MODE_TEMPLATE;
        // create our serial
        SerialNum sn1 = SerialNum.version2(id, rev, mode);
        assertThat(sn1.getId(), is(equalTo(id)));
        assertThat(sn1.getRev(), is(equalTo(rev)));
        assertThat(sn1.getMode(), is(equalTo(mode)));
        assertThat(sn1.isVersion2(), is(equalTo(true)));
        assertThat(sn1.isTemplate(), is(equalTo(true)));
        // encode
        BigInteger bi = sn1.toBigInt();
        // decode
        SerialNum sn2 = SerialNum.fromBigInt(bi);
        // assert they match
        assertThat(sn1, is(equalTo(sn2)));
        assertThat(bi, is(equalTo(sn2.toBigInt())));
    }
}
