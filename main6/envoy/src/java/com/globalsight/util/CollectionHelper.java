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


import java.util.Collection;
import java.util.Iterator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


/**
 * Provides utility methods to manipulate Collections.
 */
public final class CollectionHelper
{
    /**
    * Finds the difference between two HashMaps a and b.
    * Returns a HashMap containing elements in b that are not in a
    *
    * @return the difference as a HashMap
    */
    public static HashMap hashMapDifference(HashMap a, HashMap b)
    {
        Iterator bKeyIterator = b.keySet().iterator();
        Object key;
        Object value;
        HashMap difference = new HashMap();
        
        while (bKeyIterator.hasNext())
        {
            key = bKeyIterator.next();
            if (!a.containsKey(key))
            {
                value = b.get(key);
                difference.put(key, value);
            }
        }
        return difference;
    }

    /**
     * Removes null objects from the Collection.
     * @param p_collection a Collection
     * @returns true if one or more null objects were removed.
     */ 
    public static boolean removeNulls(Collection p_collection)
    {
        if (p_collection == null)
        {
            return false;
        }
        boolean removed = false;
        Iterator it = p_collection.iterator();
        while (it.hasNext())
        {
            Object o = it.next();
            if (o == null)
            {
                removed = true;
                it.remove();
            }
        }
        return removed; 
    }


    /**
     * Removes duplicate objects from the Collection.
     * @param p_collection a Collection
     * @returns true if one or more duplicate objects were removed.
     */ 
    public static boolean removeDuplicates(Collection p_collection)
    {
        if (p_collection == null)
        {
            return false;
        }
        HashSet set = new HashSet(p_collection.size());
        Iterator it = p_collection.iterator();
        while (it.hasNext())
        {
            set.add(it.next()); 
        }
        if (set.size() != p_collection.size())
        {
            p_collection.clear();
            p_collection.addAll(set);
            return true;
        }
        return false; 
    }


    /**
     * Removes the element at the specified position in 
     * the collection. 
     * Shifts any subsequent elements to the left 
     * (subtracts one from their indices). 
     * Returns the element that was removed from the Collection.
     * @param p_collection a Collection
     * @param p_index the index of the element to removed.
     * @param p_numberOfObjects the number of objects to remove.
     * @returns a collection with the elements at p_index removed.
     */ 
    public static Collection remove(Collection p_collection, 
            int p_index, int p_numberOfObjects)
    {
        if (p_collection == null)
        {
            return null;
        }
        List returnList = new ArrayList(p_collection.size() 
                - p_numberOfObjects);
        Iterator it = p_collection.iterator();
        for (int i = 0; it.hasNext(); i++)
        {
            if (i < p_index 
                    || i >= p_index + p_numberOfObjects)
            {
                returnList.add(it.next());
            }
            else
            {
                it.next();
            } 
        }
        return returnList;
    } 


    /**
     * Returns the element at the specified position in the 
     * collection. 
     * Shifts any subsequent elements to the left 
     * (subtracts one from their indices). 
     * Returns the element that was removed from the Collection.
     * @param p_collection a Collection
     * @param p_index index of element to return.
     * @returns the element at the specified position in the 
     * collection.
     */ 
    public static Object get(Collection p_collection, 
            int p_index)
    {
        if (p_collection == null)
        {
            return null;
        }
        Iterator it = p_collection.iterator();
        for (int i = 0; it.hasNext(); i++)
        {
            if (i == p_index)
            {  
                return it.next();
            }
        }
        return null;
    }
}
