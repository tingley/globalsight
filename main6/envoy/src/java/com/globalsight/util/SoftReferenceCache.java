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

import java.util.HashMap;
import java.util.Map;
import java.lang.ref.SoftReference;


/**
 * Provides key-value pair object caching. The value part is
 * implemented as SoftReference. Whenever the memory gets tight, the
 * value objects get garbage collected. When a value is requested and
 * it couldn't be found either by the garbage collection or just
 * because it has not been requested before, an attempt will be made
 * to get the value through CacheDataRetriever object that is passed
 * in the constructor.
 */
public class SoftReferenceCache
{
    // key: object identifier
    // Value: SoftReference that contains a value object
    private Map m_referencedObjects;

    private CacheDataRetriever m_dataRetriever;
    
    /**
     * Constructor
     * @param p_dataRetriever CacheDataRetriever object for obtaining values
     */
    public SoftReferenceCache(CacheDataRetriever p_dataRetriever)
    {
        m_dataRetriever = p_dataRetriever;
        m_referencedObjects = new HashMap();
    }


    /**
     * Get a cached object assosiated with p_key. If the value object
     * is found in the SoftReference, the object is returned.  If the
     * value object is not found or not in the SoftReference due to
     * the garbage collection , it is retrieved by
     * m_dataRetriever.getData() method and set in the SoftReference
     * for the later use.
     *
     * @param p_key key whose assosiated value is to be returned.
     * @return object associated with p_key. If such object cannot be
     * found (even with m_dataRetriever), null is returned. 
     */
    public synchronized Object get(Object p_key)
        throws Exception
    {
        Object value = null;

        SoftReference ref = (SoftReference)m_referencedObjects.get(p_key);
        if(ref == null || (value = ref.get()) == null)
        {
            // get data and put it in the cache
            value = m_dataRetriever.getData(p_key);
            if(value != null)
            {
                m_referencedObjects.put(p_key, new SoftReference(value));
            }
        }

        return value;
    }

}
