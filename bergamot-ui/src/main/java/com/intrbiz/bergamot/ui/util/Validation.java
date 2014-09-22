package com.intrbiz.bergamot.ui.util;

import java.util.Collection;

import com.intrbiz.balsa.error.BalsaValidationError;

public class Validation
{
    public static void sameSize(String message, Collection<?>... collections) throws BalsaValidationError
    {
        int size = -1;
        for (Collection<?> collection : collections)
        {
            if (size == -1)
            {
                size = collection.size();
            }
            else if (collection.size() != size)
            {
                throw new BalsaValidationError(message);
            }
        }
    }
}
