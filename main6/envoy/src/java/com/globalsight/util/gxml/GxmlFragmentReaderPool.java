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
package com.globalsight.util.gxml;

import org.apache.log4j.Logger;

// globalsight imports
import com.globalsight.util.ObjectPool;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlException;


/**
 * Provides a class for pooling objects of the same class.
 */
public class GxmlFragmentReaderPool
    extends ObjectPool
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            GxmlFragmentReaderPool.class.getName());

    private static final int DEFAULT_INITIAL_SIZE = 30;
    private static final GxmlFragmentReaderPool
        GXML_FRAGMENT_READER_POOL_INSTANCE = new GxmlFragmentReaderPool(
            DEFAULT_INITIAL_SIZE);

    /**
     * Construct a pool of objects of a given class.
     * @param p_size initial number of objects for the pool.
     */
    private GxmlFragmentReaderPool(int p_size)
    {
        super(GxmlFragmentReader.class, p_size);
    }

    private GxmlFragmentReaderPool()
    {
        super(null, 0);
    }

    /**
     * Return the GxmlFragmentReaderPool.
     * @returns the GxmlFragmentReaderPool.
     */
    public static GxmlFragmentReaderPool instance()
    {
        return GXML_FRAGMENT_READER_POOL_INSTANCE;
    }

    /**
     * Get an instance of an object from the pool.
     * If none free, instantiate a new one.
     * @return an object of the initialized class.
     */
    public synchronized GxmlFragmentReader getGxmlFragmentReader()
    {
        return (GxmlFragmentReader)super.getInstance();
    }

    /**
     * Return an object to the free pool.
     * @param p_object an object to return to the free pool.
     */
    public synchronized void freeGxmlFragmentReader(
        GxmlFragmentReader p_gxmlFragmentReader)
    {
        super.freeInstance(p_gxmlFragmentReader);
    }

    /**
     * Construct a new instance of an object of the initailized class.
     * @return a new instance of an object.
     */
    protected Object newInstance()
    {
        try
        {
            return new GxmlFragmentReader();
        }
        catch (GxmlException ge)
        {
            CATEGORY.error(ge.getMessage(), ge);
        }
        return null;
    }
}
