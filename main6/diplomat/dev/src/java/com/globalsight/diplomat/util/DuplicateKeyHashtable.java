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

package com.globalsight.diplomat.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DuplicateKeyHashtable extends Hashtable
{
  public DuplicateKeyHashtable()
  {
    super();
  }

  public DuplicateKeyHashtable(int initialCapacity)
  {
    super(initialCapacity);
  }

  public DuplicateKeyHashtable(int initialCapacity, float loadFactor)
  {
    super(initialCapacity, loadFactor);
  }

  public DuplicateKeyHashtable(Map t)
  {
    super(t);
  }

  // Tests if some key maps into the specified value
  public boolean contains(Object value)
  {
    Enumeration e = keys();
    while(e.hasMoreElements())
    {
      Object key = e.nextElement();
      Set valueSet = (Set) get(key);
      if( valueSet.contains(value))
        return true;
    }
    return false;
  }


  // returns true if the hashtable contains one or more keys mapped to the value
  // it always returns null since it does not replace previously stored elements
  public boolean containsValue(Object value)
  {
    return contains(value);
  }

  // Maps the specified key to the specified value in this hash table
  public Object put(Object key, Object value)
  {
    // determine if the key is already in use
    Set keySet = (Set) get(key);
    if( keySet == null )
    {
      // need to create a new set for this key
      HashSet aNewKeySet = new HashSet();
      aNewKeySet.add(value);
      put(key, aNewKeySet);
      return null;
    }
    else
    {
      keySet.add(value);
      return null;
    }
  }

  // Removes the key (and all of its corresponding values) from this hashtable.
  public Object remove(Object key)
  {
    return super.remove(key);
  }

  // removes a specific key, value pair from this hashtable
  public void removeValue(Object key, Object value)
  {
    Set valueSet = (Set) get(key);
    if(valueSet != null)
      valueSet.remove(value);
  }
}
