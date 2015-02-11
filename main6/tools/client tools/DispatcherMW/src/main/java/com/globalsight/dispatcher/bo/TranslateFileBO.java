/**
 *  Copyright 2014 Welocalize, Inc. 
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
import java.text.SimpleDateFormat;
import java.util.Date;

import com.globalsight.dispatcher.util.FileUtil;

/**
 * Translate File Business Object, maybe Source File OR Target File.
 * 
 * The file path: 
 * Source file: {fileStorage}/{AccountName}/{Job ID}/source/*.xlf
 * Target file: {fileStorage}/{AccountName}/{Job ID}/target/*.xlf
 * 
 * @author Joey
 * 
 */
public class TranslateFileBO implements AppConstants
{
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private long accountID;                     // Account ID
    private String jobID;                       // Translation Job ID
    private File sourceFile;                    // Source File
    private File targetFile;                    // Target File
    private Date lastModifyDate;                // The last modify date of Job folder

    public TranslateFileBO(long accountID, String jobID, long lastModifyDate, File srcFile, File trgFile)
    {
        super();
        this.accountID = accountID;
        this.jobID = jobID;
        this.sourceFile = srcFile;
        this.targetFile = trgFile;
        setLastModifyDate(lastModifyDate);
    }

    public long getAccountID()
    {
        return accountID;
    }

    public void setAccountID(long accountID)
    {
        this.accountID = accountID;
    }

    public String getJobID()
    {
        return jobID;
    }

    public void setJobID(String jobID)
    {
        this.jobID = jobID;
    }

    public File getSourceFile()
    {
        return sourceFile;
    }

    public void setSourceFile(File sourcefile)
    {
        this.sourceFile = sourcefile;
    }

    public String getSourceFileName()
    {
        return FileUtil.isExists(sourceFile) ? sourceFile.getName() : "";
    }
    
    public File getTargetFile()
    {
        return targetFile;
    }
    
    public String getTargetFileName()
    {
        return FileUtil.isExists(targetFile) ? targetFile.getName() : "";
    }

    public void setTargetFile(File targetfile)
    {
        this.targetFile = targetfile;
    }

    public String getLastModifyDateStr()
    {
        return sdf.format(lastModifyDate);
    }
    
    public Date getLastModifyDate()
    {
        return lastModifyDate;
    }
    
    public void setLastModifyDate(Long lastModifyDate)
    {
        this.lastModifyDate = new Date(lastModifyDate);
    }
}
