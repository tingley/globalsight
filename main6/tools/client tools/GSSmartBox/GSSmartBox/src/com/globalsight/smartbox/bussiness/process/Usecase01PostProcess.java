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
import java.util.ArrayList;
import java.util.List;

import com.globalsight.smartbox.bo.CompanyConfiguration;
import com.globalsight.smartbox.bo.JobInfo;
import com.globalsight.smartbox.util.LogUtil;
import com.globalsight.smartbox.util.ZipUtil;

/**
 * Special downloading/post process for "Use case 01"
 * 
 * @author Leon
 * 
 */
public class Usecase01PostProcess implements PostProcess
{

    @Override
    public boolean process(JobInfo jobInfo, CompanyConfiguration cpConfig)
    {
        // Get target files
        String targetFilesStr = jobInfo.getTargetFiles();
        String[] targetFiles = targetFilesStr.split("\\|");
        List<File> files = new ArrayList<File>();
        for (String str : targetFiles)
        {
//            if (str.endsWith("pdf"))
//            {
//                continue;
//            }
            File file = new File(str);
            files.add(file);
        }

        // Get zip file
        String zipFileName = jobInfo.getOriginFile();
        zipFileName = zipFileName.substring(zipFileName.lastIndexOf(File.separator) + 1);
        String zipFilePath = cpConfig.getTempBox() + File.separator + zipFileName;
        File zipFile = new File(zipFilePath);
        try
        {
            // add files to zip file
            LogUtil.info("Add files to zip file: " + zipFileName);
            ZipUtil.addEntriesToZipFile(zipFile, files);
        }
        catch (Exception e)
        {
            String message = "Add entries to zip file failed, File: "
                    + zipFileName + ", Job Name: " + jobInfo.getJobName()
                    + ", Job Id: " + jobInfo.getId();
            LogUtil.fail(message, e);
            return false;
        }

        // set final result file
        jobInfo.setFinalResultFile(zipFilePath);
        jobInfo.setTempFile(zipFilePath);

        return true;
    }
}
