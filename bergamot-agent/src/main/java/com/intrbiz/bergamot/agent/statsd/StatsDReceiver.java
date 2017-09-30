package com.intrbiz.bergamot.agent.statsd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.netty.util.CharsetUtil;

public class StatsDReceiver implements Runnable
{   
    private static final Logger logger = Logger.getLogger(StatsDReceiver.class);
    
    private final int port;
    
    private final StatsDMetricConsumer consumer;
    
    private volatile boolean run = true;
    
    private DatagramChannel channel;
    
    public StatsDReceiver(int port, StatsDMetricConsumer consumer) throws IOException
    {
        this.port = port;
        this.consumer = consumer;
        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(true);
        this.channel.socket().setReuseAddress(true);
    }
    
    public StatsDReceiver(StatsDMetricConsumer consumer) throws IOException
    {
        this(8125, consumer);
    }
    
    public void run()
    {
        try
        {
            // listen
            this.channel.socket().bind(new InetSocketAddress(this.port));
            logger.info("Listening for StatsD metrics on port " + this.port);
            // process the stats
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            while (this.run)
            {
                try
                {
                    buffer.rewind();
                    SocketAddress from = this.channel.receive(buffer);
                    buffer.flip();
                    this.decodeMetrics(from, buffer);
                }
                catch (Exception e)
                {
                    if (this.run)
                        logger.warn("Error receving StatsD message", e);
                }
            }
            this.channel.close();
        }
        catch (Exception e)
        {
            if (this.run) 
                logger.error("Failed to process StatsD messages", e);
        }
    }
    
    /**
     * Decode a StatsD message pull out all the metrics
     */
    protected void decodeMetrics(SocketAddress from, ByteBuffer buffer)
    {
        try
        {
            byte[] array = buffer.array();
            int startOfLine = buffer.arrayOffset();
            int endOfMetricName = 0;
            int endOfValue = 0;
            int startOfSample = 0;
            int limit = startOfLine + buffer.limit();
            // scan through the message finding the metrics
            for (int i = startOfLine; i < limit; i++)
            {
                switch (array[i])
                {
                    case 0x3A /*:*/:
                        // got end of metric name
                        endOfMetricName = i;
                        break;
                    case 0x7C /*|*/:
                        if (endOfValue == 0)
                            endOfValue = i;
                        break;
                    case 0x40 /*@*/:
                        startOfSample = i;
                        break;
                    case 0xD /*\r*/:
                        break;
                    case 0xA /*\n*/:
                        // end of metric
                        this.parseMetric(from, array, startOfLine, endOfMetricName, endOfValue, startOfSample, i);
                        // update state
                        startOfLine = i + 1;
                        endOfMetricName = 0;
                        endOfValue = 0;
                        startOfSample = 0;
                }
            }
            if (startOfLine < limit) 
                this.parseMetric(from, array, startOfLine, endOfMetricName, endOfValue, startOfSample, limit);
        }
        catch (Exception e)
        {
            logger.warn("Error decoding StatsD message", e);
        }
    }
    
    protected void parseMetric(SocketAddress from, byte[] array, int startOfLine, int endOfMetricName, int endOfValue, int startOfSample, int endOfLine)
    {
        try
        {
            // some basic validation of the metric
            int metricNameLength = endOfMetricName - startOfLine;
            int metricValueLength = endOfValue - (endOfMetricName + 1);
            int metricTypeLength = startOfSample > 0 ? startOfSample - (endOfValue + 2) : endOfLine - (endOfValue + 1);
            if (metricNameLength > 0 && metricValueLength > 0 && metricTypeLength > 0)
            {
                // get the metric name and value
                String metricName = new String(array, startOfLine, metricNameLength, CharsetUtil.UTF_8);
                String metricValue = new String(array, endOfMetricName + 1, metricValueLength, CharsetUtil.UTF_8);
                // get the metric type
                StatsDMetricType metricType = StatsDMetricType.fromEncoded(new String(array, (endOfValue + 1), metricTypeLength, CharsetUtil.UTF_8));
                // sample
                String sampleRate = null;
                if (startOfSample > 0)
                {
                    int sampleLength = endOfLine - (startOfSample + 1);
                    if (sampleLength > 0)
                    {
                        sampleRate = new String(array, startOfSample + 1, sampleLength, CharsetUtil.UTF_8);
                    }
                }
                // consume the metric
                this.consumer.processMetric(new StatsDMetric(getSourceName(from), metricName, metricValue, metricType, sampleRate));
            }
        }
        catch (Exception e)
        {
            logger.warn("Error parsing StatsD metric", e);
        }
    }
    
    private String getSourceName(SocketAddress from)
    {
        return ((InetSocketAddress) from).getAddress().getHostAddress().replace('.', '-');
    }
    
    public void shutdown()
    {
        this.run = false;
        try
        {
            this.channel.close();
        }
        catch (Exception e)
        {
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.DEBUG);
        //
        StatsDProcessor processor = new StatsDProcessor();
        //
        new StatsDReceiver(new MuxingConsumer(new LoggingConsumer(), processor)).run();
    }
}
