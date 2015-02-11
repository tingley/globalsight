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
package com.globalsight.smartbox.bo;

import java.util.HashMap;
import java.util.Vector;

/**
 * 
 * JobInfo
 * 
 * @author leon
 * 
 */
public class JobInfo
{
    // File in JobCreating Directory that copied from Inbox
    private String originFile = "";
    private String id = "";
    private String jobName = "";
    // source files path
    private Vector<String> sourceFiles = new Vector<String>();

    private HashMap<String, String> sourceMap = new HashMap<String, String>();
    // Temp directory or file, used to save converted files and unused files.
    // System will delete it when it is no used.
    private String tempFile;
    // targetFiles after download (use | interval)
    private String targetFiles = "";
    // final result file to outbox
    private String finalResultFile = "";
    private Vector<String> fileProfileIds = new Vector<String>();
    // targetLocales
    private Vector<String> targetLocales = new Vector<String>();
    private String status = "creating";
    // failed flag
    private boolean failedFlag = false;
    private String otherInfo = "no other info";

    public String getOriginFile()
    {
        return originFile;
    }

    public void setOriginFile(String originFile)
    {
        this.originFile = originFile;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getJobName()
    {
        return jobName;
    }

    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }

    public String getTargetFiles()
    {
        return targetFiles;
    }

    public void setTargetFiles(String targetFiles)
    {
        this.targetFiles = targetFiles;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public boolean isFailedFlag()
    {
        return failedFlag;
    }

    public void setFailedFlag(boolean failedFlag)
    {
        this.failedFlag = failedFlag;
    }

    public String getOtherInfo()
    {
        return otherInfo;
    }

    public void setOtherInfo(String otherInfo)
    {
        this.otherInfo = otherInfo;
    }

    public String getTempFile()
    {
        return tempFile;
    }

    public void setTempFile(String tempFile)
    {
        this.tempFile = tempFile;
    }

    public Vector<String> getSourceFiles()
    {
        return sourceFiles;
    }

    public void setSourceFiles(Vector<String> sourceFiles)
    {
        this.sourceFiles = sourceFiles;
    }

    public Vector<String> getFileProfileIds()
    {
        return fileProfileIds;
    }

    public void setFileProfileIds(Vector<String> fileProfileIds)
    {
        this.fileProfileIds = fileProfileIds;
    }

    public Vector<String> getTargetLocales()
    {
        return targetLocales;
    }

    public void setTargetLocales(Vector<String> targetLocales)
    {
        this.targetLocales = targetLocales;
    }

    public String getFinalResultFile()
    {
        return finalResultFile;
    }

    public void setFinalResultFile(String finalResultFile)
    {
        this.finalResultFile = finalResultFile;
    }
    
    public HashMap<String, String> getSourceMap()
    {
        return sourceMap;
    }

    public void setSourceMap(HashMap<String, String> sourceMap)
    {
        this.sourceMap = sourceMap;
    }
}
