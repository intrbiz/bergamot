package com.intrbiz.bergamot.config.resolver;

/**
 * Given the most specific and least specific instances of something, resolve which should be used.
 * @param <T>
 */
public interface ObjectResolver<T>
{
    /**
     * Resolve which of the most or least specific instances of something should be used
     */
    T resolve(T most, T least);
    
    /**
     * Complete the resolution process by performing any post processing on resolved instance
     */
    default T finish(T resolved)
    {
        return resolved;
    }
}
