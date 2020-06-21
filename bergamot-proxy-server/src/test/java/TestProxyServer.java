import java.util.UUID;
import java.util.function.BiConsumer;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.AuthenticationKey;
import com.intrbiz.bergamot.proxy.auth.KeyResolver;
import com.intrbiz.bergamot.proxy.server.BergamotProxyServer;

public class TestProxyServer
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        BergamotProxyServer server = new BergamotProxyServer(14080, new KeyResolver(){
            @Override
            public void resolveKey(UUID keyId, BiConsumer<AuthenticationKey, UUID> callback)
            {
                callback.accept(new AuthenticationKey("G7Fq13geaxtCXqoRMgvt_3g1tML6wVOxWOdncRaWnjEyIsUSUaor11KFpw09HC27CFY"), null);
            }
            
        }, null);
        server.start();
    }
}
