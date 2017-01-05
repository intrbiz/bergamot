package com.intrbiz.bergamot.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.bergamot.ui.builder.GetBergamotSiteArgument;
import com.intrbiz.metadata.IsArgument;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@IsArgument(GetBergamotSiteArgument.class)
public @interface GetBergamotSite
{
}
