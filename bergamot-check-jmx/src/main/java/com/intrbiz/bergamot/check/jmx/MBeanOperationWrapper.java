package com.intrbiz.bergamot.check.jmx;

import java.util.LinkedList;
import java.util.List;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

public class MBeanOperationWrapper
{
    private final MBeanWrapper mBean;
    
    private final MBeanOperationInfo info;
    
    private List<MBeanParameterWrapper> parameters;
    
    private String[] signature;
    
    public MBeanOperationWrapper(MBeanWrapper mBean, MBeanOperationInfo info)
    {
        this.mBean = mBean;
        this.info = info;
    }
    
    public String getName()
    {
        return this.info.getName();
    }
    
    public String getReturnType()
    {
        return this.info.getReturnType();
    }
    
    public String getDescription()
    {
        return this.info.getDescription();
    }
    
    public List<MBeanParameterWrapper> getParameter()
    {
        if (this.parameters == null)
        {
            this.parameters = new LinkedList<MBeanParameterWrapper>();
            for (MBeanParameterInfo info : this.info.getSignature())
            {
                this.parameters.add(new MBeanParameterWrapper(info));
            }
        }
        return this.parameters;
    }
    
    public String[] getSignature()
    {
        if (this.signature == null)
        {
            MBeanParameterInfo[] params = this.info.getSignature();
            this.signature = new String[params.length];
            for (int i = 0; i < params.length; i++)
            {
                this.signature[i] = params[i].getType();
            }
        }
        return this.signature;
    }
    
    String getOperationId()
    {
        return operationId(this.getName(), this.getSignature());
    }
    
    static String operationId(String name, String[] signature)
    {
        StringBuilder sb = new StringBuilder(name);
        for (String ss : signature)
        {
            sb.append(";").append(ss);
        }
        return sb.toString();
    }
    
    public <T> T invoke(Object... parameters)
    {
        return this.mBean.invokeOperation(this.getName(), this.getSignature(), parameters);
    }
}
