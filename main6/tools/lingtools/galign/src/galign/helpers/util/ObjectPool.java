/*
 * Copyright (c) 2004 GlobalSight Corporation.  All rights reserved.
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

package galign.helpers.util;

import java.util.ArrayList;


/**
 * Provides a class for pooling objects of the same class.
 */
public class ObjectPool
{
    private Class m_class = null;
    protected ArrayList m_free = null;

    /**
     * Construct a pool of objects of a given class.
     * @param p_class Class of the objects for this pool.
     */
    public ObjectPool(Class p_class)
    {
        m_class = p_class;
        m_free = new ArrayList();
    }

    /**
     * Construct a pool of objects of a given class.
     * Initialize the pool to have a given number
     * of objects.
     * @param p_class Class of the objects for this pool.
     * @param p_size initial number of objects for the pool.
     */
    public ObjectPool(Class p_class, int p_size)
    {
        m_class = p_class;
        m_free = new ArrayList(p_size);

        for (int i = 0; i < p_size; i++)
        {
            m_free.add(newInstance());
        }
    }

    private ObjectPool()
    {
    }

    /**
     * Get an instance of an object from the pool.
     * If none free, instantiate a new one.
     * @return an object of the initialized class.
     */
    public synchronized Object getInstance()
    {
        if (m_free.isEmpty())
        {
            return newInstance();
        }
        else
        {
            // Remove object from end of free pool.
            Object result = m_free.get(m_free.size() - 1);
            m_free.remove(m_free.size() - 1);
            return result;
        }
    }

    /**
     * Return an object to the free pool.
     * @param p_object an object to return to the free pool.
     */
    public synchronized void freeInstance(Object p_object)
    {
        // Make sure the object is of the correct type.
        if (m_class.isInstance(p_object))
        {
            m_free.add(p_object);
        }
        else
        {
            throw new IllegalArgumentException(
                "p_object class " + p_object.getClass().getName() +
                " invalid for pool " + m_class.getName());
        }
    }

    /**
     * Construct a new instance of an object of the initailized class.
     * @return a new instance of an object.
     */
    protected Object newInstance()
    {
        try
        {
            return m_class.newInstance();
        }
        catch (InstantiationException ex) {}
        catch (IllegalAccessException ex) {}

        // Throw unchecked exception for error in pool.
        throw new RuntimeException(
            "exception creating new instance for pool " + m_class.getName());
    }
}
