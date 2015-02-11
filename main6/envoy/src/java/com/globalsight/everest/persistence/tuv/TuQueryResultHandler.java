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


//import com.globalsight.everest.projecthandler.ProjectInfo;
//import com.globalsight.everest.tm.Tm;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.globalsight.everest.tuv.TuImpl;

/**
 * TuQueryResultHandler provides functionality to convert a Collection
 * of ReportQueryResults into a Collection containing the required result.
 */

public class TuQueryResultHandler 
{    
    /**
     * Convert the given collection of ReportQueryResults into a Collection
     * of Tu ids
     *
     * @param p_collection the collection of ReportQueryResults that is created
     * when the original query is executed
     *
     * @return a collection of Tu ids.
     */
    public static Collection handleResult(Collection p_collection)
    {
        Collection c = new ArrayList(p_collection.size());
        Iterator it = p_collection.iterator();        
        while (it.hasNext())
        {
            TuImpl result = (TuImpl)it.next();
            Long id = result.getIdAsLong();
            c.add(id);                   
        }
        
        return c;
    }
    
    /**
     * Convert the given collection of ReportQueryResults into a Collection
     * containing a single object representing number of tus for
     * a given source page.  Note that TOPLink returns the single object
     * as a BigDecimal.
     *
     * @param p_collection the collection of ReportQueryResults that is
     * created when the original query is executed
     *
     * @return a collection containing a single BigDecimal object 
     * representing number of tus.
     */
    public static Collection handleResultForCount(Collection p_collection)
    {        
        Collection c = new ArrayList(p_collection.size());
        Iterator it = p_collection.iterator();
        while (it.hasNext())
        {
            c.add(it.next());                   
        }
        
        return c;
    }
    
}
