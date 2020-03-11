package com.intrbiz.bergamot.cluster.model.info;

import java.util.LinkedList;
import java.util.List;

public class ClusterInfo
{
    private String localUUID;
    
    private String localAddress;
    
    private List<ProcessorInfo> processors = new LinkedList<ProcessorInfo>();
    
    public ClusterInfo()
    {
        super();
    }

    public ClusterInfo(String localUUID, String localAddress)
    {
        super();
        this.localUUID = localUUID;
        this.localAddress = localAddress;
    }

    public String getLocalUUID()
    {
        return localUUID;
    }

    public void setLocalUUID(String localUUID)
    {
        this.localUUID = localUUID;
    }

    public String getLocalAddress()
    {
        return localAddress;
    }

    public void setLocalAddress(String localAddress)
    {
        this.localAddress = localAddress;
    }

    public List<ProcessorInfo> getProcessors()
    {
        return processors;
    }

    public void setProcessors(List<ProcessorInfo> processors)
    {
        this.processors = processors;
    }
}
