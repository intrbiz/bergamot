package com.intrbiz.bergamot.config.resolver;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.intrbiz.bergamot.config.resolver.stratergy.Coalesce;

@Retention(RUNTIME)
@Target({METHOD})
public @interface ResolveWith {
    @SuppressWarnings("rawtypes")
    Class<? extends ObjectResolver> value() default Coalesce.class;
}
