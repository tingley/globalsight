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
package com.globalsight.everest.webapp.pagehandler.administration.reports.customize;

// Envoy packages
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.util.comparator.JobComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportJobInfo;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportJobInfoComparator;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.CurrencyThreadLocal;
import com.globalsight.everest.webapp.pagehandler.administration.vendors.ProjectComparator;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

/**
 * Provides ability to add customize report for Excel reports
 */
public class CustomizeReportsMainHandler extends PageHandler
{
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        ResourceBundle bundle = PageHandler.getBundle(session);

        Map paramMap = null;

        String action = p_request.getParameter("action");
        if (WebAppConstants.ACTION_JOB_RANGE.equalsIgnoreCase(action))
        {
            paramMap = (Map) session
                    .getAttribute(WebAppConstants.CUSTOMIZE_REPORTS_PARAMMAP);
            if (paramMap == null)
            {
                paramMap = new HashMap();
            }

            extractJobRangeParams(p_request, paramMap);
            session.setAttribute(WebAppConstants.CUSTOMIZE_REPORTS_PARAMMAP,
                    paramMap);

            // Prepare for job info param in customize report
            String xml = CustomizeReportsParamXmlHandler.getJobInfoParamXml();

            xml = CustomizeReportsParamXmlHandler.parseParamXml(bundle, xml);

            p_request.setAttribute(
                    WebAppConstants.CUSTOMIZE_REPORTS_JOB_INFO_PARAM_XML, xml);

            super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                    p_context);
        }
        else if (WebAppConstants.ACTION_JOB_CANCEL.equalsIgnoreCase(action))
        {
            session.removeAttribute(WebAppConstants.CUSTOMIZE_REPORTS_PARAMMAP);

            super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                    p_context);
        }
        else if (WebAppConstants.ACTION_JOB_INFO.equalsIgnoreCase(action))
        {
            paramMap = (Map) session
                    .getAttribute(WebAppConstants.CUSTOMIZE_REPORTS_PARAMMAP);
            // Clean garbage from session.
            // To avoid nullpointer exception.
            // session.removeAttribute(WebAppConstants.CUSTOMIZE_REPORTS_PARAMMAP);

            extractJobInfoParams(p_request, paramMap);

            // Get label bundle
            paramMap.put(WebAppConstants.LABEL_BUNDLE_PARAM, bundle);

            try
            {
                writeReports(p_request, p_response, paramMap);
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }

            // Never invoke super.invokePageHandler(...) or something like this
            // here.
        }
        else
        // Prepare data for customizeReportsJobRange.jsp
        {
            prepareJobRangData(p_request);
            super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                    p_context);
        }
    }

    /**
     * Generate the new reports and write the excel file back.
     * 
     * @throws JobException
     */
    private void writeReports(HttpServletRequest request,
            HttpServletResponse p_response, Map p_paramMap) throws IOException,
            JobException
    {
        // Set response header
        p_response.setHeader("Content-Disposition",
                "inline; filename=CustomizeReports.xls");
        p_response.setHeader("Expires", "0");
        p_response.setHeader("Cache-Control",
                "must-revalidate, post-check=0,pre-check=0");
        p_response.setHeader("Pragma", "public");
        p_response.setContentType("application/vnd.ms-excel");
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        ExcelReportWriter reportWriter = new ExcelReportWriter(
                p_response.getOutputStream(), bundle);

        String currency = request.getParameter("currency");
        CurrencyThreadLocal.setCurrency(currency);
        String userId = (String) request.getSession().getAttribute(
                WebAppConstants.USER_NAME);
        p_paramMap.put(WebAppConstants.USER_NAME, userId);

        CustomizeReportsGenerator generator = new CustomizeReportsGenerator(
                p_paramMap, reportWriter);
        generator.pupulate();

        reportWriter.commit();
    }

    /**
     * Extract parameters user choosed in the parameter UI from request and
     * return a hash map.
     * 
     * @param p_request
     * @return A <code>Map</code> contains the parameters user choosed
     */
    private Map extractJobRangeParams(HttpServletRequest p_request, Map paramMap)
    {
        // extract parameters from request
        String[] paramJobIds = p_request.getParameterValues("jobId");
        String[] paramProjectIds = p_request.getParameterValues("projectId");
        String[] paramStatus = p_request.getParameterValues("status");
        String[] paramTargetLocales = p_request
                .getParameterValues("targetLocale");

        List<JobSearchParameters> jobRangeParam = new ArrayList<JobSearchParameters>();

        //
        // Get JobSearchParameters
        //
        JobSearchParameters sp = new JobSearchParameters();

        // If sepcified job ids, then use job ids only.
        if ((paramJobIds != null) && !("*".equals(paramJobIds[0])))
        {
            // just get the specific jobs they chose
            for (int i = 0; i < paramJobIds.length; i++)
            {
                sp.setJobId(paramJobIds[i]);
                sp.setJobIdCondition(JobSearchParameters.EQUALS);
            }
        }

        // Get project ids
        if ((paramProjectIds != null) && !("*".equals(paramProjectIds[0])))
        {
            for (int i = 0; i < paramProjectIds.length; i++)
            {
                sp.setProjectId(paramProjectIds[i]);
            }
        }

        // Get job status.
        List<String> stateList = new ArrayList<String>();
        if ((paramStatus != null) && !("*".equals(paramStatus[0])))
        {
            for (int i = 0; i < paramStatus.length; i++)
            {
                stateList.add(paramStatus[i]);
            }
        }
        else
        {
            // just do a query for all in progress jobs, localized, and exported
            // Add ready and archived jobs for GBS-1971
            // Add export_fail jobs for GBS-2132
            stateList.add(Job.READY_TO_BE_DISPATCHED);
            stateList.add(Job.DISPATCHED);
            stateList.add(Job.LOCALIZED);
            stateList.add(Job.EXPORTED);
            stateList.add(Job.ARCHIVED);
            stateList.add(Job.EXPORT_FAIL);
        }
        sp.setJobState(stateList);

        // Get creation start
        String paramCreateDateStartCount = p_request
                .getParameter(JobSearchConstants.CREATION_START);
        String paramCreateDateStartOpts = p_request
                .getParameter(JobSearchConstants.CREATION_START_OPTIONS);
        if ("-1".equals(paramCreateDateStartOpts) == false)
        {
            sp.setCreationStart(new Integer(paramCreateDateStartCount));
            sp.setCreationStartCondition(paramCreateDateStartOpts);
        }

        // Get creation end
        String paramCreateDateEndCount = p_request
                .getParameter(JobSearchConstants.CREATION_END);
        String paramCreateDateEndOpts = p_request
                .getParameter(JobSearchConstants.CREATION_END_OPTIONS);
        if (SearchCriteriaParameters.NOW.equals(paramCreateDateEndOpts))
        {
            sp.setCreationEnd(new java.util.Date());
        }
        else if ("-1".equals(paramCreateDateEndOpts) == false)
        {
            sp.setCreationEnd(new Integer(paramCreateDateEndCount));
            sp.setCreationEndCondition(paramCreateDateEndOpts);
        }

        jobRangeParam.add(sp);
        paramMap.put(WebAppConstants.JOB_RANGE_PARAM, jobRangeParam);

        //
        // Get target locales
        //
        List targetLocaleList = new ArrayList();
        if (paramTargetLocales != null && !paramTargetLocales[0].equals("*"))
        {
            for (int i = 0; i < paramTargetLocales.length; i++)
            {
                targetLocaleList.add(paramTargetLocales[i]);
            }
        }
        paramMap.put(WebAppConstants.TARGET_LOCALE_PARAM, targetLocaleList);

        //
        // Get workflow status
        //
        paramMap.put(WebAppConstants.WORKFLOW_STATUS_PARAM, stateList);

        //
        // Get date format
        //
        String dateFormat = p_request.getParameter("dateFormat");
        SimpleDateFormat dateFormatParam = new SimpleDateFormat(dateFormat);

        paramMap.put(WebAppConstants.DATE_FORMAT_PARAM, dateFormatParam);

        return paramMap;
    }

    /**
     * Extract parameters user choosed in the job related info UI from request
     * and return a hash map.
     * 
     * @param p_request
     * @return A <code>Map</code> contains the parameters user choosed
     */
    private Map extractJobInfoParams(HttpServletRequest p_request, Map paramMap)
    {
        // extract parameters from request

        Enumeration item = p_request.getParameterNames();

        CustomizeReportParamInitiator paramManager = new CustomizeReportParamInitiator();

        while (item.hasMoreElements())
        {
            String name = (String) item.nextElement();
            if (name.startsWith("param."))
            {
                // strip off "param."
                paramManager.setParamByName(name.substring(6));
            }
            else if (name.startsWith("cat."))
            {
                // strip off "cat."
                paramManager.setParamByName(name.substring(4));
            }
        }
        paramMap.put(WebAppConstants.JOB_INFO_PARAM,
                paramManager.getRootParam());

        return paramMap;
    }

    @SuppressWarnings("unchecked")
    private void prepareJobRangData(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        // Gets job list
        List<ReportJobInfo> jobList = getReportJobInfo(p_request);

        // Gets project list and target locale list
        List<Project> projectList = new ArrayList<Project>();
        List<GlobalSightLocale> targetLocaleList = new ArrayList<GlobalSightLocale>();
        try
        {
            targetLocaleList.addAll(ServerProxy.getLocaleManager()
                    .getAllTargetLocales());
            SortUtil.sort(targetLocaleList, new GlobalSightLocaleComparator(
                    getUILocale(p_request)));

            projectList
                    .addAll(ServerProxy.getProjectHandler().getAllProjects());
            SortUtil.sort(projectList, new ProjectComparator(
                    getUILocale(p_request)));
        }
        catch (Exception e)
        {
            
        }

        p_request.setAttribute(WebAppConstants.CUSTOMIZE_REPORTS_JOB_LIST,
                jobList);
        p_request.setAttribute(
                WebAppConstants.CUSTOMIZE_REPORTS_TARGETLOCALE_LIST,
                targetLocaleList);
        p_request.setAttribute(WebAppConstants.CUSTOMIZE_REPORTS_PROJECT_LIST,
                projectList);
    }

    /**
     * Some reports need jobs for 6 states, no "pending".
     */
    private List<ReportJobInfo> getReportJobInfo(HttpServletRequest p_request)
    {
        ArrayList<String> stateList = ReportHelper.getAllJobStatusList();
        stateList.remove(Job.PENDING);
        List<ReportJobInfo> reportJobInfoList = new ArrayList<ReportJobInfo>(
                ReportHelper.getJobInfo(stateList).values());
        if (reportJobInfoList != null && !reportJobInfoList.isEmpty())
        {
            SortUtil.sort(reportJobInfoList, new ReportJobInfoComparator(
                    JobComparator.NAME, getUILocale(p_request)));
        }

        return reportJobInfoList;
    }

    private Locale getUILocale(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession();
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        if (uiLocale == null) {
            uiLocale = Locale.US;
        }
        return uiLocale;
    }
}
