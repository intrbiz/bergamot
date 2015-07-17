package com.intrbiz.bergamot.worker;

import com.intrbiz.bergamot.worker.config.JDBCWorkerCfg;

/**
 * A worker to execute JDBC checks
 */
public class JDBCWorker extends DefaultWorker
{
    public JDBCWorker()
    {
        super(JDBCWorkerCfg.class, "/etc/bergamot/worker/jdbc.xml");
    }
    
    public static void main(String[] args) throws Exception
    {
        Worker worker = new JDBCWorker();
        worker.start();
    }
}
