import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.proxy.client.BergamotProxyClient;
import com.intrbiz.bergamot.proxy.model.AuthenticationKey;
import com.intrbiz.bergamot.proxy.model.ClientHeader;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;

public class TestProxyClient
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        BergamotProxyClient proxy = new BergamotProxyClient(new URI("ws://127.0.0.1:14080/proxy"));
        // Connect
        Future<Channel> connectFuture = proxy.connect(
            new ClientHeader()
                .userAgent("test 4.0.0")
                .proxyForWorker()
                .engines(new HashSet<>(Arrays.asList("dummy"))),
            new AuthenticationKey("G7Fq13geaxtCXqoRMgvt_3g1tML6wVOxWOdncRaWnjEyIsUSUaor11KFpw09HC27CFY"), 
            (msg) -> System.out.println("Got: " + msg)
        );
        // Wait to connect
        Channel channel = connectFuture.sync().get();
        System.out.println("Channel: " + channel);
        channel.closeFuture().addListener((future) -> System.out.println("Closed"));
    }
}
