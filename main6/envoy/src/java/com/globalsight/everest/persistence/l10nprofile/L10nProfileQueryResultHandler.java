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
package com.globalsight.everest.persistence.l10nprofile;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * L10nProfileQueryResultHandler provides functionality to convert a Collection
 * of ReportQueryResults into another Collection containing a single Hashtable
 * of L10nProfile ids & names.
 */
public class L10nProfileQueryResultHandler
{
    /**
     * Convert the given collection of ReportQueryResults into a Collection
     * containing a single Hashtable of L10nProfile ids & names.
     * 
     * @param p_collection
     *            the collection of ReportQueryResults that is created when the
     *            original query is executed
     * 
     * @return a collection containing a single hashtable of L10nProfile key
     *         (id) -> value (name) pairs
     */
    public static Hashtable handleResult(Collection p_collection)
    {
        Hashtable ht = new Hashtable();
        Iterator it = p_collection.iterator();
        while (it.hasNext())
        {
            Object[] result = (Object[]) it.next();
            Number id = (Number)result[0];
            ht.put(new Long(id.longValue()), result[1]);
        }

        return ht;
    }
}
