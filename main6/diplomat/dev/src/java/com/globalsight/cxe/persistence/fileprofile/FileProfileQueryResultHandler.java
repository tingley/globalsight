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
package com.globalsight.cxe.persistence.fileprofile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;

/**
 * FileProfileQueryResultHandler provides functionality to convert a Collection
 * of ReportQueryResults into a Collection of FileProfile id.
 */
public class FileProfileQueryResultHandler
{
    /**
     * Convert the given collection of ReportQueryResults into a Collection
     * containing a single FileProfile id as a Long object.
     * 
     * @param p_collection
     *            the collection of ReportQueryResults that is created when the
     *            original query is executed
     * 
     * @return a collection containing a single FileProfile id
     */
    public static Collection handleResult(Collection p_collection)
    {
        ArrayList list = new ArrayList();
        Iterator it = p_collection.iterator();
        while (it.hasNext())
        {
            FileProfileImpl f = (FileProfileImpl) it.next();
            list.add(f.getIdAsLong());
        }
        return list;
    }
}
