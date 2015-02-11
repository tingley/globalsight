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
package com.globalsight.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.globalsight.cxe.entity.customAttribute.JobAttribute;

/**
 * Provides key-value pair object caching. No locker here, it is not thread safe. 
 * Use it carefully.
 */
public class RuntimeCache
{
    private static HashMap<String, List<JobAttribute>> m_jobAttsCache = null;
    
    
    public static void addJobAtttibutesCache(String jobUuid, List<JobAttribute> jobAtts)
    {
        if (m_jobAttsCache == null)
        {
            m_jobAttsCache = new HashMap<String, List<JobAttribute>>();
        }
        
        if (m_jobAttsCache.containsKey(jobUuid))
        {
            m_jobAttsCache.remove(jobUuid);
        }
        
        m_jobAttsCache.put(jobUuid, jobAtts);
    }
    
    public static List<JobAttribute> getJobAttributes(String jobUuid)
    {
        if (m_jobAttsCache != null)
        {
            return m_jobAttsCache.get(jobUuid);
        }
        
        return null;
    }
    
    public static void clearJobAttributes(String jobUuid)
    {
        if (m_jobAttsCache != null)
        {
            m_jobAttsCache.remove(jobUuid);
        }
    }
}
