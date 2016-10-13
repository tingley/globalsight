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
package com.globalsight.everest.persistence.job;

// globalsight
import java.util.List;
import java.util.Vector;

import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * JobUnnamedQueries. This class contains and builds the queries that can't be
 * be built ahead with a name.
 * 
 */
public class JobUnnamedQueries
{
    /**
     * Build a query to get all jobs that are associated with one or more of the
     * workflow instance ids that are passed in. This is NOT a named query, but
     * can be called by any component.
     * 
     * @param p_roleNames
     *            A Vector containing a list of workflow instance ids (Longs)
     * @return The ReadQuery created to look for profiles according to a role.
     */
    public static List getJobsByWorkflowIdsQuery(Vector p_workflowIds)
    {
        StringBuffer hql = new StringBuffer(
                "select w.job from WorkflowRequestImpl w where w.id in (");
        for (int i = 0; i < p_workflowIds.size(); i++)
        {
            Long id = (Long) p_workflowIds.get(i);
            if (i != 0)
            {
                hql.append(",");

            }
            hql.append(id);
        }
        hql.append(")");
        
        return HibernateUtil.search(hql.toString());
    }
}
