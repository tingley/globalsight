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
import java.util.Iterator;
import java.util.Vector;

import com.globalsight.everest.foundation.BasicL10nProfileInfo;

/**
 * L10nProfileForGUIQueryResultHandler provides functionality to convert a 
 * Collection of ReportQueryResults into another Collection containing a single 
 * vector of BasicL10nProfileInfo object. The BasicL10nProfileInfo is used
 * mainly by GUI.
 */
public class L10nProfileForGUIQueryResultHandler
{
    /**
     * Convert the given collection of ReportQueryResults into a Collection
     * containing a single vector of BasicL10nProfileInfo objects.
     *
     * @param p_collection the collection of ReportQueryResults that is
     * created when the original query is executed
     *
     * @return a vector of BasicL10nProfileInfo
     */
    public static Vector handleResult(Collection p_collection)
    {
        Vector v = new Vector();
        Iterator it = p_collection.iterator();
        while (it.hasNext())
        {
            Object[] result = (Object[])it.next();
            
            long id = ((Number)result[0]).longValue();
            String name = (String)result[1];
            String description = (String)result[2];           
            String companyId = ((Number)result[3]).toString();
            int count  = ((Number)result[4]).intValue();
            BasicL10nProfileInfo basicL10nProfileInfo = 
                new BasicL10nProfileInfo(id, name, description, companyId);
            basicL10nProfileInfo.setWFTCount(count);
            v.add(basicL10nProfileInfo);
        }

        return v;
    }
}
