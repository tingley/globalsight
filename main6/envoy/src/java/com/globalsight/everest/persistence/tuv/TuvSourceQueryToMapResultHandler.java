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
package com.globalsight.everest.persistence.tuv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.log.GlobalSightCategory;


/**
 * TuvSourceQueryToMapResultHandler provides functionality to convert a Collection
 * of Tuvs into a Map of Tuvs keyed by TuvId.
 */
public class TuvSourceQueryToMapResultHandler
{
     private static final GlobalSightCategory CATEGORY =
            (GlobalSightCategory)GlobalSightCategory.getLogger(
            TuvSourceQueryToMapResultHandler.class.getName());
        
    /**
     * Convert the given collection of Tuvs into a collection
     * containing a single Map
     * of Tuvs keyed by TuvId.
     * @param p_collection the collection of Tuvs that is
     * created when the original query is executed
     * @return  a collection
     * containing a single Map of Tuvs keyed by TuvId
     */
    public static Collection handleResult(Collection p_collection)
    {   
        Map map = new HashMap(1000);  // large enough to not rehash   
        Iterator it = p_collection.iterator();
        while (it.hasNext())
        {
            Tuv tuv = (Tuv)it.next();
            Long tuvId = new Long(tuv.getId());
            map.put(tuvId, tuv);
        } 
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("handleResult " + map.keySet().toString());
        }
        // return a map of the optimal performance
        Map exactMap = new HashMap(map.size()+1,1);
        exactMap.putAll(map);
        List list = new ArrayList(1);
        list.add(map);
        return list;
    }


    /**
     * Convert the given collection of ReportQueryResults into a Collection
     * of Tuv ids (for a given source page)
     *
     * @param p_collection the collection of ReportQueryResults that is created
     * when the original query is executed
     *
     * @return a collection of source Tuv ids.
     */
    public static Collection handleResultForTuvIds(Collection p_collection)
    {
        Collection c = new ArrayList(p_collection.size());
        Iterator it = p_collection.iterator();        
        while (it.hasNext())
        {
            TuvImpl result = (TuvImpl)it.next();
            Long id = result.getIdAsLong();
            c.add(id);                   
        }
        
        return c;
    }

}
