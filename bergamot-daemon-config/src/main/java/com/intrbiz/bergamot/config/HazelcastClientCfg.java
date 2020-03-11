package com.intrbiz.bergamot.config;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "hazelcast-client")
@XmlRootElement(name = "hazelcast-client")
public class HazelcastClientCfg
{    
    private List<String> nodes = new LinkedList<String>();

    public HazelcastClientCfg()
    {
        super();
    }

    public HazelcastClientCfg(String... nodes)
    {
        super();
        this.nodes.addAll(Arrays.asList(nodes));
    }
    
    @XmlElement(name = "node")
    public List<String> getNodes()
    {
        return nodes;
    }

    public void setNodes(List<String> nodes)
    {
        this.nodes = nodes;
    }
}
