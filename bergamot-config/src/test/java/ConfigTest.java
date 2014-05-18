import java.io.File;

import com.intrbiz.bergamot.config.BergamotConfigReader;
import com.intrbiz.bergamot.config.BergamotConfigWriter;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.HostCfg;


public class ConfigTest
{
    public static void main(String[] args) throws Exception
    {
        for (BergamotCfg cfg : new BergamotConfigReader().includeDir(new File("../cfg/local/")).build())
        {
            System.out.println(cfg.toString());
            new BergamotConfigWriter().baseDir(new File(new File("../cfg/" ), cfg.getSite())).config(cfg).write();
            // print the resolved config
            for (HostCfg host : cfg.getHosts())
            {
                System.out.println(host.resolve());
            }
        }
    }
}
