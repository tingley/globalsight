/**
 *  Copyright 2013 Welocalize, Inc. 
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
import java.util.ArrayList;
import java.util.List;

import com.globalsight.smartbox.bo.CompanyConfiguration;
import com.globalsight.smartbox.bo.JobInfo;
import com.globalsight.smartbox.util.LogUtil;
import com.globalsight.smartbox.util.ZipUtil;

/**
 * Special downloading/post process for "Use case 03"
 * The job info(jobName, fpName, targetLocales) comes from file name.
 * 
 * @author Joey         2013-08-12
 * 
 */
public class Usecase03PostProcess implements PostProcess
{

    @Override
    public boolean process(JobInfo jobInfo, CompanyConfiguration cpConfig)
    {
        // Get zip file
        String fileName = jobInfo.getOriginFile();
        fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);        
        
        if (fileName.endsWith(".zip"))
        {
            try
            {
                // Get target files
                String targetFilesStr = jobInfo.getTargetFiles();
                String[] targetFiles = targetFilesStr.split("\\|");
                List<File> files = new ArrayList<File>();
                for (String str : targetFiles)
                {
                    File file = new File(str);
                    files.add(file);
                }
                String filePath = cpConfig.getTempBox() + File.separator + fileName;
                File file = new File(filePath);
                // add files to zip file                
                LogUtil.info("Add files to zip file: " + fileName);
                ZipUtil.addEntriesToZipFile(file, files);
                // set final result file
                jobInfo.setFinalResultFile(filePath);
                jobInfo.setTempFile(filePath);
            }
            catch (Exception e)
            {
                String message = "Add entries to zip file failed, File: " + fileName + ", Job Name: "
                        + jobInfo.getJobName() + ", Job Id: " + jobInfo.getId();
                LogUtil.fail(message, e);
                return false;
            }
        }
        else
        {
            String tempBox = cpConfig.getTempBox();
            String jobName = jobInfo.getJobName();
            String targetFileDir = tempBox + File.separator + jobName;
            // set final result file, ../jobName/...
            jobInfo.setFinalResultFile(targetFileDir);
        }   

        return true;
    }
}
