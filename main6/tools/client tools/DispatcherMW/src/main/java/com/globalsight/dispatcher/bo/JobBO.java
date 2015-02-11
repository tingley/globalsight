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
package com.globalsight.dispatcher.bo;

import java.io.File;
import java.util.List;

/**
 * Job Status Business Object
 */
public class JobBO implements AppConstants
{
    private String jobID;                   // Translation Job ID
    private long accountId = -1;            // Account Id
    private String status = STATUS_QUEUED;  // Status: queued/running/completed/failed
    private String sourceLanguage;          // MT Source Language
    private String targetLanguage;          // MT Target Language
    private String[] srcSegments;           // MT Source Text List
    private String[] trgSegments;           // MT Target Text List
    private File srcFile;                   // Source File, which contained source segments
    private File trgFile;                   // Target File, which contained target segments   
    private long mtpLanguageID;
    
    public JobBO(String jobID) 
    {
        this.jobID = jobID;
    }
    
    public JobBO(String jobID, long accountId, File sourceFile) 
    {
        this.jobID = jobID;
        this.accountId = accountId;
        this.srcFile = sourceFile;
    }
    
    public String getJobID()
    {
        return jobID;
    }
    
    public long getAccountId()
    {
        return accountId;
    }
    
    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getSourceLanguage()
    {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage)
    {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage()
    {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage)
    {
        this.targetLanguage = targetLanguage;
    }
    
    public boolean canDoJob()
    {
        if(srcSegments == null || srcSegments.length == 0
                || sourceLanguage == null || sourceLanguage.length() == 0
                || targetLanguage == null || targetLanguage.length() == 0
                || accountId < 0)
            return false;
        
        return true;
    }
    
    public String[] getSourceSegments()
    {
        return srcSegments;
    }

    public void setSourceSegments(List<String> p_srcSegments)
    {
        if (p_srcSegments == null || p_srcSegments.size() == 0)
        {
            srcSegments = null;
        }
        else
        {
            srcSegments = p_srcSegments.toArray(new String[p_srcSegments.size()]);
        }
    }

    public String[] getTargetSegments()
    {
        return trgSegments;
    }

    public void setTargetSegments(String[] trgSegments)
    {
        this.trgSegments = trgSegments;
    }

    public File getSrcFile()
    {
        return srcFile;
    }

    public void setSrcFile(File srcFile)
    {
        this.srcFile = srcFile;
    }

    public File getTrgFile()
    {
        return trgFile;
    }

    public void setTrgFile(File trgFile)
    {
        this.trgFile = trgFile;
    }

    public long getMtpLanguageID()
    {
        return mtpLanguageID;
    }

    public void setMtpLanguageID(long mtpLanguageID)
    {
        this.mtpLanguageID = mtpLanguageID;
    }
    
}
