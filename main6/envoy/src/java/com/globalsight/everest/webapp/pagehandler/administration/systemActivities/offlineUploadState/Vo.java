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
package com.globalsight.everest.webapp.pagehandler.administration.systemActivities.offlineUploadState;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import com.globalsight.cxe.entity.customAttribute.DateCondition;
import com.globalsight.everest.taskmanager.Task;

public class Vo
{
    public static SimpleDateFormat FORMAT = new SimpleDateFormat(
            DateCondition.FORMAT);

    private String company;
    private String fileSize;
    private String user;
    private Task task;
    private String fileName;

    public static SimpleDateFormat getFORMAT()
    {
        return FORMAT;
    }

    public static void setFORMAT(SimpleDateFormat fORMAT)
    {
        FORMAT = fORMAT;
    }

    public String getCompany()
    {
        return company;
    }

    public void setCompany(String company)
    {
        this.company = company;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public Task getTask()
    {
        return task;
    }

    public void setTask(Task task)
    {
        this.task = task;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFileSize()
    {
        long size = Long.parseLong(fileSize);
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
            return "--";
        }
    }

    public void setFileSize(String fileSize)
    {
        this.fileSize = fileSize;
    }

}
