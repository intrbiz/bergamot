import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.proxy.KeyResolver;
import com.intrbiz.bergamot.proxy.model.AuthenticationKey;
import com.intrbiz.bergamot.proxy.model.ClientHeader;
import com.intrbiz.bergamot.proxy.server.BergamotProxyServer;
import com.intrbiz.bergamot.proxy.server.MessageProcessor;

import io.netty.channel.Channel;

public class TestProxyServer
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        BergamotProxyServer server = new BergamotProxyServer(14080, new KeyResolver()
        {
            @Override
            public CompletionStage<AuthenticationKey> resolveKey(UUID keyId)
            {
                return CompletableFuture.completedFuture(new AuthenticationKey("G7Fq13geaxtCXqoRMgvt_3g1tML6wVOxWOdncRaWnjEyIsUSUaor11KFpw09HC27CFY"));
            }
            
        }, new MessageProcessor.Factory()
        {
            @Override
            public MessageProcessor create(ClientHeader client, Channel channel)
            {
                return new MessageProcessor(UUID.randomUUID(), client, channel)
                {
                    @Override
                    public void start()
                    {
                        System.out.println("Open: " + this.getId());
                    }

                    @Override
                    public void processMessage(Message msg)
                    {
                        System.out.println(this.getId() +": " + msg);
                    }

                    @Override
                    public void stop()
                    {
                    }
                };
            }

            @Override
            public void close(MessageProcessor processor)
            {
                System.out.println("Close: " + processor.getId());
            }
        });
        server.start();
    }
}
