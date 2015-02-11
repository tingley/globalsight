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
package com.globalsight.smartbox.bussiness.process;

import java.io.File;

import com.globalsight.smartbox.bo.CompanyConfiguration;
import com.globalsight.smartbox.bo.JobInfo;

/**
 * General use case
 * 
 * @author leon
 * 
 */
public class GeneralPostProcess implements PostProcess
{

    @Override
    public boolean process(JobInfo jobInfo, CompanyConfiguration cpConfig)
    {
        String tempBox = cpConfig.getTempBox();
        String jobName = jobInfo.getJobName();
        String targetFileDir = tempBox + File.separator + jobName;
        // set final result file, ../jobName/...
        jobInfo.setFinalResultFile(targetFileDir);
        return true;
    }
}
