package com.intrbiz.bergamot.engine;

import com.intrbiz.bergamot.Bergamot;
import com.intrbiz.configuration.Configurable;
import com.intrbiz.configuration.Configuration;

/**
 * A (major) component which makes up the Bergamot daemon
 */
public interface BergamotEngine<T extends Configuration> extends Configurable<T>
{
    void setBergamot(Bergamot daemon);
    
    Bergamot getBergamot();
    
    void start() throws Exception;
}
