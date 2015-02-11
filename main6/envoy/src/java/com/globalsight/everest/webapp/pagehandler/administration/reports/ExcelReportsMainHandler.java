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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
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
    private ArrayList<Project> projectList = null;
    private ArrayList<BasicL10nProfile> l10nProfiles = null;
    private ArrayList<GlobalSightLocale> targetLocales = null;
    private Locale uiLocale = null;
    private static Logger LOGGER = 
        Logger.getLogger(ExcelReportsMainHandler.class.getName());

    @Override
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        String activityName = (String) p_request.getParameter(REPORTNAME);
        HttpSession session = p_request.getSession(false);
        uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);        
        initData();
        
        if ("xlsReportFileList".equals(activityName))
        {
            generateFileListReportWebForm(p_request, p_response);
        }
        else
        {
            prepareData(p_request, p_response);
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }
    
    /**
     * Prepare Data(reportJobInfoList/projectList/targetLocales) for Request.
     * Such as "Reviewers Comments Report", "Comments Analysis Report", 
     * "Character Count Report".
     */
    private void prepareData(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {
        SessionManager sessionMgr = (SessionManager) p_request.getSession(false)
                .getAttribute(SESSION_MANAGER);
        
        ArrayList<String> stateList = ReportHelper.getAllJobStatusList();
        List<ReportJobInfo> reportJobInfoList = new ArrayList<ReportJobInfo>(ReportHelper.getJobInfo(stateList).values());
        if (reportJobInfoList != null && !reportJobInfoList.isEmpty())
        {
            Collections.sort(reportJobInfoList, new ReportJobInfoComparator(JobComparator.NAME, getUILocale()));
        }
        
        sessionMgr.setAttribute(ReportConstants.REPORTJOBINFO_LIST, reportJobInfoList);
        sessionMgr.setAttribute(ReportConstants.PROJECT_LIST, projectList);
        sessionMgr.setAttribute(ReportConstants.L10N_PROFILES, l10nProfiles);
        sessionMgr.setAttribute(ReportConstants.TARGETLOCALE_LIST, targetLocales);
    }

    /**
     * Generate file list report web form
     */
    private void generateFileListReportWebForm(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        ArrayList<String> stateList = new ArrayList<String>();
        stateList.add(Job.DISPATCHED);
        stateList.add(Job.LOCALIZED);
        stateList.add(Job.EXPORTED);
        stateList.add(Job.PENDING);
        stateList.add(Job.EXPORT_FAIL);
        stateList.add(Job.ARCHIVED);
        stateList.add(Job.READY_TO_BE_DISPATCHED);

        sessionMgr.setAttribute("reportJobInfos", ReportHelper.getJobInfo(stateList));
        sessionMgr.setAttribute("projectList", projectList);
        sessionMgr.setAttribute(ReportConstants.L10N_PROFILES, l10nProfiles);
        sessionMgr.setAttribute("targetLocales", targetLocales);
    }
    
    private void initData()
    {
        try
        {
            targetLocales = new ArrayList<GlobalSightLocale>(ServerProxy
                    .getLocaleManager().getAllTargetLocales());
            Collections.sort(targetLocales, new GlobalSightLocaleComparator(getUILocale()));
            
            projectList = new ArrayList<Project>(ServerProxy
                    .getProjectHandler().getAllProjects());
            Collections.sort(projectList, new ProjectComparator(getUILocale()));

            l10nProfiles = new ArrayList<BasicL10nProfile>(ServerProxy
                    .getProjectHandler().getAllL10nProfilesData());

        }
        catch (Exception e)
        {
            LOGGER.error("Getting target locales or project error", e);
        }
    }
    
    protected Locale getUILocale()
    {
        return uiLocale == null ? Locale.US : uiLocale;
    }
}
