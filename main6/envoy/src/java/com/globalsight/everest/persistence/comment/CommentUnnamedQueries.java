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

// globalsight
import java.util.List;

/**
 * CommentUnnamedQueries.  This class contains and builds
 * the queries that can't be be built ahead with a name.
 * 
 */
public class CommentUnnamedQueries
{
    //
    // PRIVATE STATIC VARIABLES
    //
    
    private static final String TASKS_BY_JOB_ID_AND_LOCALES_PREFIX =  
        "select ti.name, c.id, c.create_date, c.creator_user_id, c.comment_text, " +
        "l.iso_lang_code, l.iso_country_code from locale l, " +
        " task_info ti, comments c, workflow w where ti.workflow_id = w.iflow_instance_id " +
        " and w.job_id = ";
    private static final String TASKS_BY_JOB_ID_AND_LOCALES_SUFFIX = 
        " and c.comment_object_type = 'T' and " +
        " c.comment_object_id = ti.task_id and w.target_locale_id = l.id";

    //
    // PUBLIC STATIC METHODS
    //

     /**  
      * 
      */
    public static String getTaskCommentsByJobIdAndLocales(long p_jobId,
                                                               List p_tLocaleIds)
    {
        StringBuffer sql = new StringBuffer(TASKS_BY_JOB_ID_AND_LOCALES_PREFIX);
        sql.append(p_jobId);
        sql.append(TASKS_BY_JOB_ID_AND_LOCALES_SUFFIX);

        // build a list of target locale ids if they are specified
        if (p_tLocaleIds != null && p_tLocaleIds.size() > 0)
        {
            sql.append(" and w.target_locale_id in (");
            sql.append(String.valueOf(p_tLocaleIds.get(0)));
            for (int i = 1 ; i < p_tLocaleIds.size() ; i++) 
            {
                sql.append(",");
                sql.append(String.valueOf(p_tLocaleIds.get(i)));
            }
            sql.append(")");
        }                                  

        return sql.toString();
    }
}
