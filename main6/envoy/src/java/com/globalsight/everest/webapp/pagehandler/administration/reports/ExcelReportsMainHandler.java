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
package com.globalsight.everest.webapp.pagehandler.administration.reports;

// Envoy packages
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.JobComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.vendors.ProjectComparator;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GlobalSightLocale;

/**
 * Provides ability to add custom stuff for Excel reports
 */
public class ExcelReportsMainHandler extends PageHandler
{
    private final String REPORTNAME = "activityName";
    private ArrayList<JobImpl> jobList = null;
    private ArrayList<Project> projectList = null;
    private ArrayList<GlobalSightLocale> targetLocales = null;

    @Override
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        String activityName = (String) p_request.getParameter(REPORTNAME);
        if ("xlsReportFileList".equals(activityName))
        {
            generateFileListReportWebForm(p_request, p_response);
        }
        // TODO Auto-generated method stub
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    /*
     * Generate file list report web form
     */
    private void generateFileListReportWebForm(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        Vector<String> stateList = new Vector<String>();
        stateList.add(Job.DISPATCHED);
        stateList.add(Job.LOCALIZED);
        stateList.add(Job.EXPORTED);
        stateList.add(Job.PENDING);
        stateList.add(Job.EXPORT_FAIL);
        stateList.add(Job.ARCHIVED);
        stateList.add(Job.READY_TO_BE_DISPATCHED);
        Collection<JobImpl> jobs = null;
        try
        {
            jobs = ServerProxy.getJobHandler().getJobsByStateList(stateList);
            targetLocales = new ArrayList<GlobalSightLocale>(ServerProxy
                    .getLocaleManager().getAllTargetLocales());
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        jobList = new ArrayList<JobImpl>(jobs);
        projectList = new ArrayList<Project>();
        Iterator<JobImpl> iterJob = jobList.iterator();
        while (iterJob.hasNext())
        {
            Job j = iterJob.next();
            Project p = j.getL10nProfile().getProject();
            if (projectList.contains(p) == false)
            {
                projectList.add(p);
            }
        }

        Collections.sort(jobList, new JobComparator(JobComparator.NAME,
                uiLocale));
        Collections.sort(projectList, new ProjectComparator(uiLocale));

        sessionMgr.setAttribute("jobList", jobList);
        sessionMgr.setAttribute("projectList", projectList);
        sessionMgr.setAttribute("targetLocales", targetLocales);
    }
}
