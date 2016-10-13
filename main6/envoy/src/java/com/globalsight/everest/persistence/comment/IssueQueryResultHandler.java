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
package com.globalsight.everest.persistence.comment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * IssueQueryResultHandler provides functionality to convert a
 * ReportQueryResults into a Collection containing the count of Issues.
 */
public class IssueQueryResultHandler
{

    /**
     * Convert the ReportQueryResults into a count.
     * 
     * @param p_collection
     *            the collection of ReportQueryResults that is created when the
     *            original query is executed
     * 
     * @return a collection containing one Integer - which is the count of
     *         issues.
     */
    public static Collection handleResult(Collection p_collection)
    {
        Collection c = new ArrayList(p_collection.size());
        Iterator it = p_collection.iterator();
        while (it.hasNext())
        {
            Object[] result = (Object[]) it.next();
            // TOPLink returns the value as a BigDecimal rather than Integer
            int count = ((Number) result[0]).intValue();
            c.add(new Integer(count));
        }

        return c;
    }
}
