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
package com.globalsight.everest.persistence.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.projecthandler.ProjectInfo;

/**
 * ProjectQueryResultHandler provides functionality to convert a Collection of
 * ReportQueryResults into a Collection containing a single Hashtable of Project
 * ids & names.
 * 
 */
public class ProjectQueryResultHandler
{
    private static final Logger CATEGORY = Logger
            .getLogger(ProjectQueryResultHandler.class.getName());

    /**
     * Convert the given collection of ReportQueryResults into a Collection
     * containing a single Hashtable of Project ids & names.
     * 
     * @param p_collection
     *            the collection of ReportQueryResults that is created when the
     *            original query is executed
     * 
     * @return a collection containing a single Hashtable of Project key (id) ->
     *         value (name) pairs
     */
    public static Collection handleResult(Collection p_collection)
    {
        Hashtable ht = new Hashtable();
        Iterator it = p_collection.iterator();
        while (it.hasNext())
        {
            ProjectImpl result = (ProjectImpl) it.next();
            ht.put(result.getIdAsLong(), result.getName());
        }
        Collection c = new ArrayList();
        if (!ht.isEmpty())
        {
            c.add(ht);
        }
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("handleResult returns " + c.toString());
        }
        return c;
    }

    /**
     * Convert the given collection of ReportQueryResults into a Collection
     * containing a single vector of ProjectInfo objects.
     * 
     * @param p_collection
     *            the collection of ReportQueryResults that is created when the
     *            original query is executed
     * 
     * @return a collection containing a single vector of ProjectInfo
     */

    public static Collection handleResultForGUI(Collection p_collection)
    {
        Vector v = new Vector();
        Iterator it = p_collection.iterator();
        while (it.hasNext())
        {
            ProjectImpl result = (ProjectImpl) it.next();

            v.add(new ProjectInfo(result));
        }

        Collection c = new ArrayList();
        if (!v.isEmpty())
        {
            c.addAll(v);
        }
        return c;
    }
}
