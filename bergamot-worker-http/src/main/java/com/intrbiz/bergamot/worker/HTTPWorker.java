package com.intrbiz.bergamot.worker;

import com.intrbiz.bergamot.worker.config.HTTPWorkerCfg;

/**
 * A worker to execute HTTP checks
 */
public class HTTPWorker extends DefaultWorker
{
    public HTTPWorker()
    {
        super(HTTPWorkerCfg.class, "/etc/bergamot/worker/http.xml", "bergamot-worker-http");
    }
    
    public static void main(String[] args) throws Exception
    {
        Worker worker = new HTTPWorker();
        worker.start();
    }
}
