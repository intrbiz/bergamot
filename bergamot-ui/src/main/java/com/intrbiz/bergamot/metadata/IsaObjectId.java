package com.intrbiz.bergamot.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.bergamot.ui.validator.ObjectIdValidator;
import com.intrbiz.converter.converters.UUIDConverter;
import com.intrbiz.metadata.UseConverter;
import com.intrbiz.metadata.UseValidator;

/**
 * Validate that the UUID is a valid object 
 * id for the given site
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
@UseValidator(ObjectIdValidator.class)
@UseConverter(UUIDConverter.class)
public @interface IsaObjectId
{   
    boolean session() default true;
}
