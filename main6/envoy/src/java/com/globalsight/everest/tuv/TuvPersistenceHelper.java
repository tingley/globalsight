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

package com.globalsight.everest.tuv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.globalsight.everest.persistence.PersistentObject;



/**
 * TuvPersistenceHelper is responsible for giving clients static
 * utility functions for accessing the persistance service.
 */
public final class TuvPersistenceHelper
{
    /**
     * Returns List of PersistentObject IDs as Longs from a Collection
     * of PersistentObjects.
     * @param p_persistentObjects PersistentObjects to get Ids from.
     * @return List of PersistentObject IDs as Longs from the
     * Collection of PersistentObjects.
     */
    public static List getIds(Collection p_persistentObjects)
    {
        if (p_persistentObjects.isEmpty())
        {
            return new ArrayList(0);
        }

        ArrayList list = new ArrayList(p_persistentObjects.size());
        Iterator it = p_persistentObjects.iterator();

        while (it.hasNext())
        {
            PersistentObject po = (PersistentObject)it.next();

            if (po != null)
            {
                list.add(po.getIdAsLong());
            }
        }

        return list;
    }
    

    /**
     * Returns Map of PersistentObjects keyed by ID as Longs from a
     * Collection of PersistentObjects.
     * @param p_persistentObjects PersistentObjects to get Ids from.
     * @return Map of PersistentObjects keyed by ID as Longs.
     */
    public static Map getPersistentObjectMap(
        Collection p_persistentObjects)
    {
        if (p_persistentObjects.isEmpty())
        {
            return new HashMap(0);
        }

        HashMap map = new HashMap(p_persistentObjects.size());
        Iterator it = p_persistentObjects.iterator();

        while (it.hasNext())
        {
            PersistentObject po = (PersistentObject)it.next();
            map.put(po.getIdAsLong(), po);
        }

        return map;
    }
}
