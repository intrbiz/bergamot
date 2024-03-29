package com.intrbiz.bergamot.model.message.agent.check;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

@JsonTypeName("bergamot.agent.check.diskio")
public class CheckDiskIO extends Message
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("devices")
    private List<String> devices = new LinkedList<String>();
    
    public CheckDiskIO()
    {
        super();
    }

    public CheckDiskIO(Message message)
    {
        super(message);
    }
    
    public CheckDiskIO(String... devices)
    {
        super();
        for (String name : devices)
        {
            this.devices.add(name);
        }
    }
    
    public CheckDiskIO(Collection<String> devices)
    {
        super();
        this.devices.addAll(devices);
    }

    public List<String> getDevices()
    {
        return devices;
    }

    public void setDevices(List<String> devices)
    {
        this.devices = devices;
    }
}
