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

package com.globalsight.everest.webapp.pagehandler.administration.customer;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.rmi.RemoteException;


/**
 * Holds object needed for MyJobs page.
 */
public class MyJob
{
    private ArrayList jobIds;   // list of Longs
    private String jobName;
    private GlobalSightLocale srcLocale;
    private GlobalSightLocale targLocale;
    private int wordCnt;
    private Date createDate;
    private Date plannedDate;

    public MyJob(long jobId, String jobName, GlobalSightLocale srcLocale,
                 GlobalSightLocale targLocale, int wordCnt, Date createDate,
                 Date plannedDate)
    {
        jobIds = new ArrayList();
        jobIds.add(new Long(jobId));
        this.jobName = jobName;
        this.srcLocale = srcLocale;
        this.targLocale = targLocale;
        this.wordCnt = wordCnt;
        this.createDate = createDate;
        this.plannedDate = plannedDate;
    }

    public void setJobId(long jobId)
    {
        jobIds.add(new Long(jobId));
    }

    public void setSourceLocale(GlobalSightLocale srcLocale)
    {
        this.srcLocale = srcLocale;
    }

    public void setTargetLocale(GlobalSightLocale targLocale)
    {
        this.targLocale = targLocale;
    }

    public void setWordCount(int wordCnt)
    {
        this.wordCnt += wordCnt;
    }

    public void setCreateDate(Date createDate)
    {
        if (createDate.before(this.createDate))
        {
            this.createDate = createDate;
        }
    }

    public void setPlannedDate(Date plannedDate)
    {
        if (plannedDate.after(this.plannedDate))
        {
            this.plannedDate = plannedDate;
        }
    }

    public List getJobIds()
    {
        return jobIds;
    }

    public String getJobName()
    {
        return jobName;
    }

    public GlobalSightLocale getSourceLocale()
    {
        return srcLocale;
    }

    public GlobalSightLocale getTargetLocale()
    {
        return targLocale;
    }

    public int getWordCount()
    {
        return wordCnt;
    }

    public Date getCreateDate()
    {
        return createDate;
    }

    public Date getPlannedDate()
    {
        return plannedDate;
    }
}
