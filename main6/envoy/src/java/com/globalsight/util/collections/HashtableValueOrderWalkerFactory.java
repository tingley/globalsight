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
package com.globalsight.util.collections;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.globalsight.util.SortUtil;

/**
 * This class can be used to create HashtableValueOrderWalker objects. This
 * class is not JDK 1.1 safe, and uses Java2 collections.
 */
public class HashtableValueOrderWalkerFactory
{

    /**
     * Creates a HashtableValueOrderWalker that can be used to walk through the
     * keys in a Hashtable based on the sorted order of the values
     * 
     * @param p_hashtable
     * @return
     */
    public static HashtableValueOrderWalker createHashtableValueOrderWalker(
            Hashtable p_hashtable)
    {
        Hashtable copyWithValueKeyPairs = makeHashtableCopy(p_hashtable);
        ArrayList pairs = new ArrayList(copyWithValueKeyPairs.values());
        SortUtil.sort(pairs); // sort based on value order

        // now create one vector with the keys, and one with the values
        // both sorted in value order
        Vector sortedValues = new Vector();
        Vector sortedKeys = new Vector();
        for (int i = 0; i < pairs.size(); i++)
        {
            ValueHolder vh = (ValueHolder) pairs.get(i);
            sortedValues.add(vh.m_value);
            sortedKeys.add(vh.m_key);
        }

        return new HashtableValueOrderWalker(p_hashtable, sortedValues,
                sortedKeys);
    }

    // Makes a copy of the hashtable containing <key,value> pairs
    // indexed by the original key
    private static Hashtable makeHashtableCopy(Hashtable p_hashtable)
    {
        Object key;
        Object value;
        Enumeration keys = p_hashtable.keys();
        Hashtable copy = new Hashtable();

        while (keys.hasMoreElements())
        {
            key = keys.nextElement();
            value = p_hashtable.get(key);
            ValueHolder vh = new ValueHolder(key, value);
            copy.put(key, vh);
        }
        return copy;
    }

    /**
     * Private class to be used to hold a key,value combination. The natural
     * sorting order is based on the natural order of the values.
     */
    private static class ValueHolder implements Comparable
    {
        Object m_key;
        Object m_value;

        ValueHolder(Object p_key, Object p_value)
        {
            m_key = p_key;
            m_value = p_value;
        }

        // compare based on value
        public int compareTo(Object p_other)
        {
            ValueHolder o = (ValueHolder) p_other;
            Comparable a = (Comparable) m_value;
            Comparable b = (Comparable) o.m_value;
            return a.compareTo(b);
        }
    }
}
