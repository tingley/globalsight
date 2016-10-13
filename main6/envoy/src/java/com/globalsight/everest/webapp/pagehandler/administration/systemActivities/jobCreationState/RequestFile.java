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
package com.globalsight.everest.webapp.pagehandler.administration.systemActivities.jobCreationState;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.globalsight.cxe.entity.customAttribute.DateCondition;

public class RequestFile
{
    public static SimpleDateFormat FORMAT = new SimpleDateFormat(
            DateCondition.FORMAT);

    private String company;
    private long jobId;
    private String jobName;
    private String file;
    private String fileProfile;
    private String priority;
    private Date requestTime;
    private String key;
    private long size;
    private String project;
    private long sortTime;
    private int sortPriority;
    private int sortIndex;

    public long getSortTime()
    {
        return sortTime;
    }

    public void setSortTime(long sortTime)
    {
        this.sortTime = sortTime;
    }
    
    public int getSortIndex()
    {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex)
    {
        this.sortIndex = sortIndex;
    }

    public int getSortPriority()
    {
        return sortPriority;
    }

    public void setSortPriority(int sortPriority)
    {
        this.sortPriority = sortPriority;
    }

    /**
     * @return the company
     */
    public String getCompany()
    {
        return company;
    }

    /**
     * @param company
     *            the company to set
     */
    public void setCompany(String company)
    {
        this.company = company;
    }

    /**
     * @return the jobId
     */
    public long getJobId()
    {
        return jobId;
    }

    /**
     * @param jobId
     *            the jobId to set
     */
    public void setJobId(long jobId)
    {
        this.jobId = jobId;
    }

    /**
     * @return the jobName
     */
    public String getJobName()
    {
        return jobName;
    }

    /**
     * @param jobName
     *            the jobName to set
     */
    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }

    /**
     * @return the file
     */
    public String getFile()
    {
        return file;
    }

    /**
     * @param file
     *            the file to set
     */
    public void setFile(String file)
    {
        this.file = file;
    }

    /**
     * @return the fileProfile
     */
    public String getFileProfile()
    {
        return fileProfile;
    }

    /**
     * @param fileProfile
     *            the fileProfile to set
     */
    public void setFileProfile(String fileProfile)
    {
        this.fileProfile = fileProfile;
    }

    /**
     * @return the priority
     */
    public String getPriority()
    {
        return priority;
    }

    /**
     * @param priority
     *            the priority to set
     */
    public void setPriority(String priority)
    {
        this.priority = priority;
    }

    /**
     * @return the requestTime
     */
    public String getRequestTime()
    {
        return FORMAT.format(requestTime);
    }

    public Date getRequestTimeAsDate()
    {
        return requestTime;
    }
    
    /**
     * @param requestTime
     *            the requestTime to set
     */
    public void setRequestTime(Date requestTime)
    {
        this.requestTime = requestTime;
    }

    /**
     * @return the key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(String key)
    {
        this.key = key;
    }

    /**
     * @return the size
     */
    public String getSize()
    {

        return getDataSize(size);
    }

    public static String getDataSize(long size)
    {
        DecimalFormat formater = new DecimalFormat("###0.00");
        if (size < 1024)
        {
            return formater.format(size * 0.001) + "KB";
        }
        else if (size < 1024 * 1024)
        {
            float kbsize = size / 1024f;
            return formater.format(kbsize) + "KB";
        }
        else if (size < 1024 * 1024 * 1024)
        {
            float mbsize = size / 1024f / 1024f;
            return formater.format(mbsize) + "MB";
        }
        else if (size < 1024 * 1024 * 1024 * 1024)
        {
            float gbsize = size / 1024f / 1024f / 1024f;
            return formater.format(gbsize) + "GB";
        }
        else
        {
            return "size: error";
        }
    }

    /**
     * 
     * @param size
     *            the size to set
     */
    public void setSize(long size)
    {
        this.size = size;
    }

    /**
     * @return the project
     */
    public String getProject()
    {
        return project;
    }

    /**
     * @param project the project to set
     */
    public void setProject(String project)
    {
        this.project = project;
    }
}
