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

import java.util.Hashtable;
import java.util.Vector;
import java.io.Serializable;

/**
 * This is a JDK 1.1 safe object (for sending to applets)
 * that can be used to wrap a Hashtable and provide
 * a means to iterate through the Values based on the
 * natural sorting order of the Values
 */
public class HashtableValueOrderWalker implements Serializable
{
    private Hashtable m_hashtable = null;
    private Vector m_orderedValues = null;
    private Vector m_orderedKeys = null;

    /**
    * package level constructor.
    */
    HashtableValueOrderWalker(Hashtable p_hashtable,
			      Vector p_orderedValues,
			      Vector p_orderedKeys)
    {
	m_hashtable = p_hashtable;
	m_orderedKeys = p_orderedKeys;
	m_orderedValues = p_orderedValues;
    }

    /**
    * Wraps a call to the hashtable to get the value with the given key
    */
    public Object get(Object p_key)
    {
	return m_hashtable.get(p_key);
    }

    /**
    * Gets the given key at the specified index
    * based on the sorted order of the values
    */
    public Object getKey(int p_index)
    {
	return m_orderedKeys.elementAt(p_index);
    }

    /**
    * Gets the given key at the specified index
    * based on the sorted order of the values
    */
    public Object getValue(int p_index)
    {
	return m_orderedValues.elementAt(p_index);
    }

    public int size()
    {
	return m_orderedValues.size();
    }
}

