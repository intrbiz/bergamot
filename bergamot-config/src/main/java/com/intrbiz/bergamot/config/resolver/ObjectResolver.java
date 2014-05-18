package com.intrbiz.bergamot.config.resolver;

/**
 * Given the most specific and least specific instances of something, resolve which should be used.
 * @param <T>
 */
@FunctionalInterface
public interface ObjectResolver<T>
{
    public T resolve(T most, T least);
}
