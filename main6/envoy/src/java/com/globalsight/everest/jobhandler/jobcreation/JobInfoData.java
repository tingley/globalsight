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
package com.globalsight.everest.jobhandler.jobcreation;

import java.util.HashMap;
import java.util.Map;

/**
 * For aem 6.2. Support the due time.
 * 
 * The class is used to store the job information that will be used during creating.
 * 
 * @author Administrator
 *
 */
public class JobInfoData
{
    public static final String DUE_TIME = "dueTime";
    
    @SuppressWarnings("rawtypes")
    private static final Map<Long, Map> JOB_DATAS = new HashMap<>();
    
    @SuppressWarnings("rawtypes")
    public static Object getJobData(long jobId, String key)
    {
        Map m = JOB_DATAS.get(jobId);
        if (m == null)
            return null;
        
        return m.get(key);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void addJobData(long jobId, String key, Object value)
    {
        Map m = JOB_DATAS.get(jobId);
        if (m == null)
        {
            m = new HashMap<>();
            JOB_DATAS.put(jobId, m);
        }
        
        m.put(key, value);
    }
    
    public static void clearJobData(long jobId)
    {
        JOB_DATAS.remove(jobId);
    }
}
