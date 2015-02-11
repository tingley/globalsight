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

/**
 * Provides key-value pair object caching. The cache maintains MRU
 * (Most Recently Used) objects. When the cache hits the max number of
 * objects, least recently used object will be discarded.
 *
 * This implementation is much faster than the one introduced in
 * "Advanced Programming for the Java 2 Platform" at
 * http://developer.java.sun.com/developer/onlineTraining/Programming/JDCBook/perf4.html
 */
public class MruCache
{
    private class Node
    {
        Node m_before = null;
        Node m_after = null;
        Object m_key = null;

        Node(Object p_key)
        {
            m_key = p_key;
        }

        // remove Node from the list
        void remove()
        {
            // unlink self from the MruList
            m_before.m_after = m_after;
            m_after.m_before = m_before;
            // for garbage collector
            m_after = null;
            m_before = null;
        }

        // set Node between before and after
        void set(Node before, Node after)
        {
            before.m_after = this;
            m_before = before;
            after.m_before = this;
            m_after = after;
        }

        // for debug
        public String toString()
        {
            return "{node " + m_key.toString() + "}";
        }

    }

    private class Pair
    {
        Object m_original;
        Node m_node;

        Pair(Object p_original, Node p_node)
        {
            m_original = p_original;
            m_node = p_node;
        }

        // for debug
        public String toString()
        {
            return "{" + m_original.toString() + ", " +
                m_node.toString() + "}";
        }

    }

    private int m_cacheSize;
    private Node m_first = null;
    private Node m_last = null;
    private Map m_objectCache = null;

    /**
     * Constructor
     * @param p_maxCacheSize size of cache
     */
    public MruCache(int p_maxCacheSize)
    {
        m_cacheSize = p_maxCacheSize;
        m_first = new Node(null);
        m_last = new Node(null);
        m_first.m_after = m_last;
        m_last.m_before = m_first;
        m_objectCache = new HashMap(m_cacheSize);
    }


    /**
     * Get a cached object assosiated with a key. The object found in
     * the cache is moved to the top of the Mru list.
     * @param p_key key whose assosiated value is to be returned.
     * @return the value to which this map maps the specified key. If
     * the value assosiated with p_key is not found, null is returned.
     */
    public synchronized Object get(Object p_key)
    {
        Object value = null;
        Pair pair = (Pair)m_objectCache.get(p_key);
        if(pair != null)
        {
            value = pair.m_original;
            Node node = pair.m_node;
            node.remove();
            node.set(m_first, m_first.m_after);
        }
        return value;
    }


    /**
     * Put an object assosiated with a key in the cache. The object is
     * put at the top of the Mru list. If the number of Object exceeds
     * max cache size, a least recently used object will be discarded.
     *
     * @param p_key key with which the specified value is to be associated.
     * @param p_value value to be associated with the specified key.
     */
    public synchronized void put(Object p_key, Object p_value)
    {
        // add the new object to the cache
        Node newNode = new Node(p_key);
        Pair oldPair
            = (Pair)m_objectCache.put(p_key, new Pair(p_value, newNode));
        newNode.set(m_first, m_first.m_after);

        // cached object is replaced
        if(oldPair != null)
        {
            Node oldNode = oldPair.m_node;
            oldNode.remove();
        }

        // the number of caches exceeds max cache size
        if(m_objectCache.size() > m_cacheSize)
        {
            // discard least recently used object
            Node lruNode = m_last.m_before;
            lruNode.remove();
            m_objectCache.remove(lruNode.m_key);
        }
    }


    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());
        str.append("{m_objectCache=");
        str.append(m_objectCache.toString());
        str.append(", mruList=");
        for(Node node = m_first.m_after;
            node.m_after != null; node = node.m_after)
        {
            str.append(node.toString());
            str.append(", ");
        }
        str.append("}");
        return str.toString();
    }


}
