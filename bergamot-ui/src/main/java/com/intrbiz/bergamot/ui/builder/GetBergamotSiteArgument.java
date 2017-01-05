package com.intrbiz.bergamot.ui.builder;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.balsa.engine.impl.route.exec.argument.ArgumentBuilder;
import com.intrbiz.bergamot.model.Contact;

public class GetBergamotSiteArgument extends ArgumentBuilder<GetBergamotSiteArgument>
{    
    protected Class<?> type;
    
    protected String variable;
    
    public GetBergamotSiteArgument()
    {
        super();
    }
    
    @Override
    public String getVariable()
    {
        return this.variable;
    }
    
    public GetBergamotSiteArgument type(Class<?> type)
    {
        this.type = type;
        return this;
    }
    
    @Override
    public void compile(ExecutorClass cls)
    {
        // allocate the variable we are going to use
        cls.addImport(Contact.class.getCanonicalName());
        cls.addImport(this.type.getCanonicalName());
        this.variable = cls.allocateExecutorVariable(this.type.getSimpleName(), "get_bergamot_site");
        // write the code
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // bind parameter ").append(this.index).append("\r\n");
        sb.append("    ").append(this.type.getSimpleName()).append(" ").append(this.variable).append(" = ").append("((Contact) context.currentPrincipal()).getSite();\r\n");
    }
    
    @Override
    public void fromAnnotation(Annotation a, Annotation[] parameterAnnotations, Class<?> parameterType)
    {
        this.type(parameterType);
    }

    @Override
    public void verify(Class<?> parameterType)
    {
    }
}
