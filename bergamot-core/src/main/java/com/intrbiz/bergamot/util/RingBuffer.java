package com.intrbiz.bergamot.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class RingBuffer<E> implements Iterable<E>
{
    private final int capacity;
    
    private final List<E> elements = new LinkedList<E>();
    
    public RingBuffer(int capacity)
    {
        this.capacity = capacity;
    }
    
    public boolean add(E element)
    {
        elements.remove(element);
        elements.add(element);
        if (elements.size() >= this.capacity) elements.remove(0);
        return true;
    }
    
    public boolean remove(E element)
    {
        return elements.remove(element);
    }

    @Override
    public Iterator<E> iterator()
    {
        final ListIterator<E> iter = this.elements.listIterator(this.elements.size());
        return new Iterator<E>() {
            @Override
            public boolean hasNext()
            {
                return iter.hasPrevious();
            }

            @Override
            public E next()
            {
                return iter.previous();
            }
        };
    }
    
    public List<E> toList()
    {
        List<E> ret = new LinkedList<E>();
        for (E e : this)
        {
            ret.add(e);
        }
        return ret;
    }

    public int size()
    {
        return this.elements.size();
    }

    public boolean isEmpty()
    {
        return this.elements.isEmpty();
    }

    public void clear()
    {
        this.elements.clear();
    }
}
