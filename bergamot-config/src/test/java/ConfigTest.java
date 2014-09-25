import java.io.File;

import com.intrbiz.bergamot.config.BergamotConfigReader;
import com.intrbiz.bergamot.config.BergamotConfigWriter;
import com.intrbiz.bergamot.config.model.HostCfg;
import com.intrbiz.bergamot.config.validator.ValidatedBergamotConfiguration;


public class ConfigTest
{
    public static void main(String[] args) throws Exception
    {
        for (ValidatedBergamotConfiguration cfg : new BergamotConfigReader().includeDir(new File("../cfg/local/")).build())
        {
            System.out.println(cfg.toString());
            new BergamotConfigWriter().baseDir(new File(new File("../cfg/" ), cfg.getConfig().getSite())).config(cfg.getConfig()).write();
            // print the resolved config
            for (HostCfg host : cfg.getConfig().getHosts())
            {
                System.out.println(host.resolve());
            }
        }
    }
}
