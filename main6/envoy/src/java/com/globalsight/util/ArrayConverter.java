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


import java.util.List;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Arrays;


/**
 * Converts arrays of primitives to Lists, and vis versa. 
 * <p>
 * For arrays of
 * Objects, use java.util.Arrays asList method.
 */
public final class ArrayConverter
{
    /**
     * Converts an array of longs to a List.
     * @param p_longArray an array of longs.
     * @return a List containing Longs created
     * in order from p_longArray.
     */ 
    public static List asList(long[] p_longArray)
    {
        List list = new ArrayList(p_longArray.length);
        for (int i = 0; i < p_longArray.length; i++)
        {
            list.add(new Long(p_longArray[i]));
        }
        return list;
    }


    /**
     * Converts an array of longs to a Vector.
     * @param p_longArray an array of longs.
     * @return a List containing Longs created
     * in order from p_longArray.
     */ 
    public static Vector asVector(long[] p_longArray)
    {
        Vector vector = new Vector(p_longArray.length);
        for (int i = 0; i < p_longArray.length; i++)
        {
            vector.add(new Long(p_longArray[i]));
        }
        return vector;
    }

    /**
     * Converts an array of ints to a List.
     * @param p_intArray an array of ints.
     * @return a List containing Intergers created
     * in order from p_intArray.
     */ 
    public static List asList(int[] p_intArray)
    {
        List list = new ArrayList(p_intArray.length);
        for (int i = 0; i < p_intArray.length; i++)
        {
            list.add(new Integer(p_intArray[i]));
        }
        return list;
    }  


    /**
     * Converts an array of ints to a Vector.
     * @param p_intArray an array of ints.
     * @return a List containing Intergers created
     * in order from p_intArray.
     */ 
    public static Vector asVector(int[] p_intArray)
    {
        Vector vector = new Vector(p_intArray.length);
        for (int i = 0; i < p_intArray.length; i++)
        {
            vector.add(new Integer(p_intArray[i]));
        }
        return vector;
    } 

    /**
     * Converts a Collections of Longs to an array of primitve longs.
     * @param p_longs Collection of Longs.
     * @return an array of longs
     */ 
    public static long[] asPrimitveLongArray(Collection p_longs)
    {
        int size = new ArrayList(p_longs).size();
        long [] longs = new long[size];
        Iterator it = p_longs.iterator();
        for (int i = 0; it.hasNext(); i++)
        {
            Long aLong = (Long)it.next();
            longs[i] = aLong.longValue();
        }
        return longs; 
    } 


    /**
     * Return the Object array with the elements shifted left
     * (i.e. decrement the index of each element) by the 
     * specified increment.
     * Elements shifted past index 0 are removed.
     * The length of the returned array is the length
     * of p_objectArray minus p_increment.
     */
    public static Object[] shiftLeft(Object[] p_objectArray, 
            int p_increment)
    {
        if (p_increment == 0 || p_objectArray == null 
                || p_objectArray.length == 0)
        {
            return p_objectArray;
        }
        List objects = Arrays.asList(p_objectArray);
        List returnList = new ArrayList(objects.size() - p_increment);
        for (int i = 0; i < objects.size(); i++)
        {
            if (i < p_increment)
            {
                continue;
            }
            returnList.add(objects.get(i)); 
        } 
        return returnList.toArray();
    }


    /**
     * Return the Object array with the elements at p_index
     * removed.  Removes p_numberOfObjectsToRemove 
     * starting at p_index.     
     * The length of the returned array is the length
     * of p_objectArray minus p_numberOfObjectsToRemove.
     */
    public static Object[] remove(Object[] p_objectArray, 
            int p_index, int p_numberOfObjectsToRemove)
    {
        if (p_numberOfObjectsToRemove == 0 || p_objectArray == null 
                || p_objectArray.length == 0)
        {
            return p_objectArray;
        }
        List objects = Arrays.asList(p_objectArray);
        List returnList = new ArrayList(objects.size() - 
                p_numberOfObjectsToRemove);
        for (int i = 0; i < objects.size(); i++)
        {
            if (i < p_index 
                    || i >= p_index + p_numberOfObjectsToRemove)
            {
                returnList.add(objects.get(i));
            } 
        } 
        return returnList.toArray();
    }
}
