package com.intrbiz.bergamot.nrpe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.nrpe.model.NRPEPacket;
import com.intrbiz.bergamot.nrpe.model.NRPEResponse;

/**
 * <p>
 * Query an NRPE daemon from Java
 * </p>
 * <p>
 * You should use the NRPE client, inside a try-with-resources statement, as follows:
 * </p>
 * 
 * <pre>
 * <code>
 * try (NRPEClient client = new NRPEClient("127.0.0.1"))
 * {
 *     System.out.println("NRPE Daemon Version: " + client.hello().getOutput());
 * }
 * catch (IOException e)
 * {
 *     e.printStackTrace();
 * }
 * </code>
 * </pre>
 * <p>
 * Note: currently *ONLY* SSL is supported
 * </p>
 */
public class NRPEClient implements AutoCloseable
{
    public static final String[] CIPHERS = new String[] { "TLS_DH_anon_WITH_AES_128_CBC_SHA" };

    public static final String[] PROTOCOLS = new String[] { "TLSv1" };

    private static final SSLSocketFactory FACTORY = (SSLSocketFactory) SSLSocketFactory.getDefault();

    private Logger logger = Logger.getLogger(NRPEClient.class);

    private SocketAddress address;

    private SSLSocket socket;

    public NRPEClient(String host, int port, int connectTimeout) throws IOException
    {
        super();
        this.address = new InetSocketAddress(host, port);
        this.socket = (SSLSocket) FACTORY.createSocket();
        this.socket.setEnabledProtocols(PROTOCOLS);
        this.socket.setEnabledCipherSuites(CIPHERS);
        this.socket.connect(this.address, connectTimeout);
    }

    public NRPEClient(String host) throws IOException
    {
        this(host, 5666, 2_500);
    }

    /**
     * Send an arbitrary NRPE packet
     * 
     * @param packet
     * @return
     * @throws IOException
     */
    public NRPEPacket send(NRPEPacket packet) throws IOException
    {
        try
        {
            InputStream in = this.socket.getInputStream();
            OutputStream out = this.socket.getOutputStream();
            // send the query
            long start = System.nanoTime();
            out.write(packet.encodePacket());
            out.flush();
            // read the response
            byte[] response = new byte[2048];
            int read = in.read(response);
            // validate the read
            if (read <= 0)
            {
                logger.warn("The NRPE daemon returned 0 bytes, check the daemon logs on " + this.address.toString());
                throw new IOException("The NRPE daemon returned 0 bytes");
            }
            // parse the response
            NRPEPacket responsePacket = NRPEPacket.parse(response, 0, read);
            long end = System.nanoTime();
            responsePacket.setRuntime(((double) (end - start)) / 1_000_000D);
            return responsePacket;
        }
        catch (SSLHandshakeException e)
        {
            logger.warn("Failed to perform SSL handshake, check allowed_hosts and the daemon logs on " + this.address.toString());
            throw new IOException("Failed to perform SSL handshake, check allowed_hosts", e);
        }
    }

    /**
     * Execute the NRPE hello command, this will return the NRPE daemon version
     * 
     * @return
     * @throws IOException
     */
    public NRPEResponse hello() throws IOException
    {
        NRPEPacket response = this.send(new NRPEPacket().version2().hello());
        return new NRPEResponse(response.getResponseCode(), response.getOutput(), response.getRuntime());
    }

    /**
     * Execute the given NRPE command
     * 
     * @param command
     * @return
     * @throws IOException
     */
    public NRPEResponse command(String command) throws IOException
    {
        if (command == null) throw new IllegalArgumentException("The command cannot be null");
        NRPEPacket response = this.send(new NRPEPacket().version2().command(command));
        return new NRPEResponse(response.getResponseCode(), response.getOutput(), response.getRuntime());
    }

    /**
     * Execute the given NRPE command with the given arguments
     * 
     * @param command
     * @param args
     * @return
     * @throws IOException
     */
    public NRPEResponse command(String command, String... args) throws IOException
    {
        if (command == null) throw new IllegalArgumentException("The command cannot be null");
        NRPEPacket response = this.send(new NRPEPacket().version2().command(command, args));
        return new NRPEResponse(response.getResponseCode(), response.getOutput(), response.getRuntime());
    }

    /**
     * Execute the given NRPE command with the given arguments (as a List)
     * 
     * @param command
     * @param args
     * @return
     * @throws IOException
     */
    public NRPEResponse command(String command, List<String> args) throws IOException
    {
        if (command == null) throw new IllegalArgumentException("The command cannot be null");
        NRPEPacket response = this.send(new NRPEPacket().version2().command(command, args));
        return new NRPEResponse(response.getResponseCode(), response.getOutput(), response.getRuntime());
    }

    /**
     * Close this connection
     */
    public void close() throws IOException
    {
        this.socket.close();
    }
}
