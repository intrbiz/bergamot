import java.io.File;

import com.intrbiz.bergamot.config.BergamotConfigReader;
import com.intrbiz.bergamot.config.BergamotConfigWriter;
import com.intrbiz.bergamot.config.model.HostCfg;
import com.intrbiz.bergamot.config.validator.ValidatedBergamotConfiguration;


public class ConfigTest
{
    public static void main(String[] args) throws Exception
    {
        for (ValidatedBergamotConfiguration cfg : new BergamotConfigReader().includeDir(new File("../bergamot-site-config-template/")).build())
        {
            // write out the config
            // new BergamotConfigWriter().baseDir(new File(new File("../cfg/" ), cfg.getConfig().getSite())).config(cfg.getConfig()).write();
            // print the resolved config
            for (HostCfg host : cfg.getConfig().getHosts())
            {
                System.out.println(host.resolve());
            }
            // validation report
            System.out.println(cfg.getReport());
        }
    }
}
