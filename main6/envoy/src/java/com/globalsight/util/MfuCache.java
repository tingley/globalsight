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
 * Provides key-value pair object caching. The cache maintains MFU
 * (Most Frequent Used) objects. When the cache hits the max number of
 * objects, least frequently used object will be discarded.
 */
public class MfuCache
{
    private class Node
    {
        Node m_before = null;
        Node m_after = null;
        Object m_key = null;
        Object m_object = null;
        int m_referenceCnt = 0;
        
        private Node(Object p_key)
        {
            m_key = p_key;
        }

        // prepend p_node to this node. This node must NOT be the
        // first node of the link.
        private void prepend(Node p_node)
        {
            Node first = this.m_before;
            Node second = this;
            
            first.m_after = p_node;
            second.m_before = p_node;
            
            p_node.m_before = first;
            p_node.m_after = second;
        }
        
        // put this Node in place of p_replaced. p_replaced is
        // unlinked. p_replaced must NOT be the first or the last node
        // of the link.
        private void replace(Node p_replaced)
        {
            this.m_before = p_replaced.m_before;
            this.m_after = p_replaced.m_after;
            
            p_replaced.m_before.m_after = this;
            p_replaced.m_after.m_before = this;
            
            // for garbage collector
            p_replaced.m_after = null;
            p_replaced.m_before = null;
            p_replaced.m_object = null;

            // advance the node until the reference count of a node
            // ahead of this node is greater than this node's
            // reference count.
            if(m_before.m_referenceCnt < m_referenceCnt)
            {
                advance();
            }
        }


        // advance Node forward

        // This method assumes that the 2nd node and the last node
        // don't advance forward. The first and the last node are
        // assumed to be fixed anchor nodes.

        // After calling this method, a link 1 2 3 4 becomes 1 3 2 4.
        // So this node is the 3rd node in the link.
        private void advance()
        {
            Node first = m_before.m_before;
            Node second = m_before;
            Node third = this;
            Node fourth = m_after;
            
            first.m_after = third;
            second.m_before = third;
            second.m_after = fourth;
            third.m_before = first;
            third.m_after = second;
            fourth.m_before = second;
            
            // keep advancing until the reference count of a node
            // ahead of this node is greater than this node's
            // reference count.
            if(m_before.m_referenceCnt < m_referenceCnt)
            {
                advance();
            }
        }
        

        private void incrementCount()
        {
            m_referenceCnt++;
        }
        
        private void addObject(Object p_object)
        {
            m_object = p_object;
        }
        
        // for debug
        public String toString()
        {
            return "{key = " + m_key + "; refCnt = " + m_referenceCnt + "}";
        }

    }

    // DEBUG
    private long m_getTime = 0;
    private int m_hitCount = 0;
    // DEBUG

    private int m_cacheSize;
    private int m_actualSize;
    private Node m_first;
    private Node m_last;

    // key: object identifier
    // Value: Node
    private Map m_referencedObjects;

    private CacheDataRetriever m_dataRetriever;
    
    /**
     * Constructor
     * @param p_maxCacheSize size of cache
     */
    public MfuCache(CacheDataRetriever p_dataRetriever, int p_maxCacheSize)
    {
        m_dataRetriever = p_dataRetriever;
        
        m_cacheSize = p_maxCacheSize;
        m_actualSize = 0;
        
        m_first = new Node(null);
        m_first.m_referenceCnt = Integer.MAX_VALUE;
        m_last = new Node(null);

        m_first.m_after = m_last;
        m_last.m_before = m_first;

        m_referencedObjects = new HashMap();
    }


    /**
     * Get a cached or uncached object assosiated with p_key. If the
     * object is found in the cache, the object is returned,
     * referenced count of the object is incremented. If the object is
     * not in the cache, it is retrieved by m_dataRetriever.getData()
     * method. The object's reference count is incremented and if the
     * count is greater than the reference count of the least
     * referenced object in the cache, the retrieved object replaces
     * the object in the cache.
     *
     * @param p_key key whose assosiated value is to be returned.
     * @return object associated with p_key. If such object cannot be
     * found (even with m_dataRetriever), null is returned. 
     */
    public synchronized Object get(Object p_key)
        throws Exception
    {
        // DEBUG
        long start = System.currentTimeMillis();
        // DEBUG

        Object value = null;

        Node node = (Node)m_referencedObjects.get(p_key);
        if(node != null && node.m_object != null)
        {
            // DEBUG
            m_hitCount++;
            // DEBUG

            value = node.m_object;
            node.incrementCount();

            // If the reference count of a node before this node is
            // less than this node's, the node advances forward.
            if(node.m_before.m_referenceCnt < node.m_referenceCnt)
            {
                node.advance();
            }
        }
        else
        {
            // get data and put it in the cache if necessary
            value = m_dataRetriever.getData(p_key);
            if(value != null)
            {
                if(node == null)
                {
                    node = new Node(p_key);
                    m_referencedObjects.put(p_key, node);
                }
                
                node.incrementCount();
                
                if(m_actualSize < m_cacheSize)
                {
                    // prepend the node to the anchor last node
                    node.addObject(value);
                    m_last.prepend(node);
                    m_actualSize++;
                }
                else if(m_last.m_before.m_referenceCnt < node.m_referenceCnt)
                {
                    // the new node replaces the least referenced node
                    node.addObject(value);
                    node.replace(m_last.m_before);
                }
            }
        }

        // DEBUG
        m_getTime += System.currentTimeMillis() - start;
        // DEBUG

        return value;
    }



    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append("MfuList=");
        for(Node node = m_first.m_after;
            node.m_after != null; node = node.m_after)
        {
            str.append(node.toString());
            str.append(", ");
        }
        str.append(";");
        return str.toString();
    }


    // DEBUG
    public long getGetTime()
    {
        return m_getTime;
    }
    
    public int getHitCacheCount()
    {
        return m_hitCount;
    }
    // DEBUG

}
