/**
 *  Copyright 2009, 2011 Welocalize, Inc. 
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

package com.globalsight.selenium.testcases.dataprepare.smoketest.job;

import java.util.Hashtable;

import com.globalsight.selenium.testcases.ConfigUtil;

import jodd.util.StringUtil;

/**
 * Utility class for saving created job
 * @author Vincent
 *
 */
public class CreatedJob
{
    private static Hashtable<String, String> jobs = new Hashtable<String, String>();
    
    public static String getCreatedJobName(String jobName)
    {
        if (StringUtil.isEmpty(jobName))
            return null;
        
        String autoGenerateJobNameStr = ConfigUtil.getConfigData("autoGenerateJobName").trim();
        if (!"true".equalsIgnoreCase(autoGenerateJobNameStr))
            return jobName;
        else
            return jobs.get(jobName);
    }

    public static void addCreatedJob(String jobName, String createdJobName) {
        if (!StringUtil.isEmpty(jobName))
            jobs.put(jobName, createdJobName);
    }
}
