package com.intrbiz.bergamot.nrpe.util;

/**
 * A port of the NRPE CRC32 implementation
 */
public class CRC32
{
    protected static final int[] TABLE = initTable();
    
    private static final int[] initTable()
    {
        int[] t = new int[256];
        int crc;
        int poly = 0xEDB88320;
        for (int i = 0; i < t.length; i++)
        {
            crc = i;
            for (int j = 8; j > 0; j--)
            {
                if ((crc & 1) > 0)
                {
                    crc = (crc >>> 1) ^ poly;
                }
                else
                {
                    crc >>>= 1;
                }
            }
            t[i] = crc;
        }   
        return t;
    }
    
    public static int computeCRC32(byte[] buffer)
    {
        return computeCRC32(buffer, 0, buffer.length);
    }
    
    public static int computeCRC32(byte[] buffer, int offset, int length)
    {
        int crc = 0xFFFFFFFF;
        int this_char;
        for (int i = offset; i < length; i++)
        {
            this_char = buffer[i] & 0xFF;
            crc = ((crc >>> 8) & 0x00FFFFFF) ^ TABLE[(crc ^ this_char) & 0xFF];
        }
        return (crc ^ 0xFFFFFFFF);
    }
}
