package com.intrbiz.bergamot.config.resolver;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 * Resolve a Java POJO / Bean, by resolving any properties which are annotated with <code>ResolveWith</code>.  
 * </p>
 * <p>
 * Place the <code>ResolveWith</code> annotation onto the getter of any property which should be resolved, 
 * for example:
 * </p>
 * <pre><code>public class MyBean {
 *     private String name;
 *     
 *     public MyBean(String name)
 *     {
 *         this.name = name;
 *     }
 *     
 *     @ResolveWith(CoalesceEmptyString.class)
 *     public String getName() { return this.name; }
 *     
 *     public void setName(String name) { this.name = name; }
 * }
 * </code></pre>
 * <p>
 * Then you can resolve two objects as follows:
 * </p>
 * <pre><code>MyBean bean = BeanResolver.fromClass(MyBean.class).resolve(new MyBean(null), new MyBean("Hello"));</code></pre>
 */
public class BeanResolver<T> implements ObjectResolver<T>
{
    // cache of resolvers
    private static final ConcurrentMap<Class<?>, BeanResolver<?>> RESOLVERS = new ConcurrentHashMap<Class<?>, BeanResolver<?>>();

    private final Class<T> type;

    private final List<PropertyResolver<T>> properties;

    private BeanResolver(Class<T> type, List<PropertyResolver<T>> properties)
    {
        this.type = type;
        this.properties = properties;
    }

    @Override
    public T resolve(T most, T least)
    {
        // we need at least one
        if (most == null && least == null) return null;
        // if we only have one return the other
        if (least == null) return most;
        if (most == null) return least;
        // resolve
        try
        {
            T ret = this.type.newInstance();
            for (PropertyResolver<T> pr : this.properties)
            {
                pr.resolve(ret, most, least);
            }
            return ret;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to resolve " + this.type.getSimpleName(), e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <TT> BeanResolver<TT> fromClass(Class<TT> cls)
    {
        // check the cache
        BeanResolver resolver = RESOLVERS.get(cls);
        if (resolver == null)
        {
            // build the property resolvers
            List<PropertyResolver<TT>> properties = new ArrayList<PropertyResolver<TT>>();
            for (Method getter : cls.getMethods())
            {
                ResolveWith res = getter.getAnnotation(ResolveWith.class);
                if (res != null)
                {
                    // validate the name
                    if (!getter.getName().startsWith("get")) throw new RuntimeException("You can only use @ResolveWith on getter methods, annotation found on: " + getter);
                    Method setter;
                    ObjectResolver or;
                    // find the setter method
                    try
                    {
                        setter = cls.getMethod("s" + getter.getName().substring(1), getter.getReturnType());
                    }
                    catch (NoSuchMethodException | SecurityException e)
                    {
                        throw new RuntimeException("Failed to find setter method correlating to " + getter);
                    }
                    // load the resolver
                    try
                    {
                        if (res.value() == BeanResolver.class)
                        {
                            or = BeanResolver.fromClass(getter.getReturnType());
                        }
                        else
                        {
                            or = res.value().newInstance();
                        }
                    }
                    catch (InstantiationException | IllegalAccessException e)
                    {
                        throw new RuntimeException("Failed to load the ObjectResolver for " + getter);
                    }
                    properties.add(new PropertyResolver<TT>(setter, getter, or));
                }
            }
            // create the resolver
            resolver = new BeanResolver(cls, properties);
            RESOLVERS.put(cls, resolver);
        }
        return resolver;
    }

    private static class PropertyResolver<B>
    {
        public final Method setter;

        public final Method getter;

        @SuppressWarnings("rawtypes")
        public final ObjectResolver resolver;

        @SuppressWarnings("rawtypes")
        public PropertyResolver(Method setter, Method getter, ObjectResolver resolver)
        {
            this.setter = setter;
            this.getter = getter;
            this.resolver = resolver;
        }

        @SuppressWarnings("unchecked")
        public void resolve(B into, B most, B least) throws Exception
        {
            setter.invoke(into, (Object) this.resolver.resolve(this.getter.invoke(most), this.getter.invoke(least)));
        }
    }
}
