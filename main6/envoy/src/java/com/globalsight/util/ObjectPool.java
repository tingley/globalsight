/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.util;

// globalsight imports


import java.util.ArrayList;

import org.apache.log4j.Logger;


/**
 * Provides a class for pooling objects of the same class.
 */
public class ObjectPool
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            ObjectPool.class.getName());

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
        for (int i = 0 ; i < p_size; i++)
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
        if (m_class.isInstance(p_object)) {
            m_free.add(p_object);
        }
        else
        {
            IllegalArgumentException ie =
                new IllegalArgumentException(
                    "p_object class " + p_object.getClass().getName()
                    + " invalid for pool " + m_class.getName());
            CATEGORY.error(ie.getMessage(), ie);
            throw ie;
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
        RuntimeException re = new RuntimeException(
            "exception creating new instance for pool "
            + m_class.getName());
        CATEGORY.error(re.getMessage(), re);
        throw re;
    }

    /**
     * Return a string representation of the object.
     * @returns a string representation of the object.
     */
    public String toString()
    {
        return getClass().getName()
            + " pool " +  m_class.getName()
            + " size " + Integer.toString(m_free.size());
    }
}
