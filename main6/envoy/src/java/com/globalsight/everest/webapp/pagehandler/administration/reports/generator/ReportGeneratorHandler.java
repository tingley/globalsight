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
package com.globalsight.everest.webapp.pagehandler.administration.reports.generator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.ErrorBean;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportSearchOptions;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSummaryHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

/**
 * Generate Report handler. The handle can be used for generating reports,
 * getting the percent of reports, cancelling the reports.
 */
public class ReportGeneratorHandler extends PageHandler implements
        ReportConstants
{
    static private final Logger logger = Logger
            .getLogger(ReportGeneratorHandler.class);

    private static Map<String, ReportsData> m_reportsDataMap = 
            new ConcurrentHashMap<String, ReportsData>();
    private static Map<String, ReportGenerator> m_generatorMap = 
            new ConcurrentHashMap<String, ReportGenerator>();
    private static Map<String, ReportInfo> m_reportResultMap = 
            new ConcurrentHashMap<String, ReportInfo>();

    @ActionHandler(action = ACTION_CANCEL_REPORT, formClass = "")
    public void cancelGenerateReport(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
    	HttpSession userSession = p_request.getSession();
        String userId = (String) userSession
                .getAttribute(WebAppConstants.USER_NAME);
        List<Long> reportJobIDS = ReportHelper.getListOfLong(p_request
                .getParameter(ReportConstants.JOB_IDS));
        String reportType = p_request.getParameter(ReportConstants.REPORT_TYPE);
        
        String key = ReportHelper.getKey(userId, reportJobIDS, reportType);
        ReportGenerator generator = m_generatorMap.get(key);
        
        if (generator != null)
        {
        	Cancelable cancelable = (Cancelable) generator;
        	cancelable.cancel();
        }
    }
    
    /**
     * Gets Reports
     */
    @ActionHandler(action = GET_REPORT, formClass = "")
    public void getReport(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        HttpSession userSession = p_request.getSession();
        String userId = (String) userSession
                .getAttribute(WebAppConstants.USER_NAME);
        List<Long> reportJobIDS = ReportHelper.getListOfLong(p_request
                .getParameter(ReportConstants.JOB_IDS));
        String reportType = p_request.getParameter(ReportConstants.REPORT_TYPE);
        
        String key = ReportHelper.getKey(userId, reportJobIDS, reportType);
        ReportInfo info = m_reportResultMap.get(key);
    	if (info != null)
    	{
    		File[] reports = info.getFiles();
    		ReportHelper.sendFiles(reports, null, p_response);
    	}
    }
    
    /**
     * Generate Reports
     */
    @ActionHandler(action = GENERATE_REPORT, formClass = "")
    public void generateOneReport(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        HttpSession userSession = p_request.getSession();
        String userId = (String) userSession
                .getAttribute(WebAppConstants.USER_NAME);
        List<Long> reportJobIDS = ReportHelper.getListOfLong(p_request
                .getParameter(ReportConstants.JOB_IDS));
        List<GlobalSightLocale> reportTargetLocales = ReportHelper
                .getTargetLocaleList(p_request
                        .getParameterValues(ReportConstants.TARGETLOCALE_LIST),
                        null);
        String reportType = p_request.getParameter(ReportConstants.REPORT_TYPE);
        
        String key = ReportHelper.getKey(userId, reportJobIDS, reportType);
        ReportInfo info = new ReportInfo();
    	m_reportResultMap.put(key, info);
    	
        try 
        {
            ReportGenerator generator = ReportGeneratorFactory
                    .getReportGenerator(reportType, p_request, p_response);
	        
	        if (generator instanceof Cancelable) 
	        {
				m_generatorMap.put(key, generator);
			}
	        
	        File[] files = generator.generateReports(reportJobIDS,
	                reportTargetLocales);
	        info.setFiles(files);
        }
        finally
        {
        	info.setFinished(true);
		}
    }
    
    /**
     * Generate Reports
     */
    @ActionHandler(action = GENERATE_REPORTS, formClass = "")
    public void generateReport(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        HttpSession userSession = p_request.getSession();
        String userId = (String) userSession
                .getAttribute(WebAppConstants.USER_NAME);
        List<Long> reportJobIDS = ReportHelper.getListOfLong(p_request
                .getParameter(ReportConstants.JOB_IDS));
        List<GlobalSightLocale> reportTargetLocales = ReportHelper
                .getTargetLocaleList(p_request
                        .getParameterValues(ReportConstants.TARGETLOCALE_LIST),
                        null);
        List<String> reportTypeList = ReportHelper.getListOfStr(
                p_request.getParameter(ReportConstants.REPORT_TYPE), ",");
        ReportGenerator generator;

        // Cancel Duplicate Request
        if (ReportHelper.checkReportsDataMap(m_reportsDataMap, userId,
                reportJobIDS, reportTypeList))
        {
            String message = "Cancel the request, due the report is generating, userID/reportTypeList/reportJobIDS:"
                    + userId + ", " + reportTypeList + ", " + reportJobIDS;
            logger.debug(message);
            p_response.sendError(p_response.SC_NO_CONTENT);
            return;
        }
        // Initial Reports percent and status for m_reportsDataMap.
        ReportHelper.setReportsDataMap(m_reportsDataMap, userId, reportJobIDS,
                reportTypeList, 0, ReportsData.STATUS_INPROGRESS);

        List<File> reports = new ArrayList<File>();
        String zipFileName = null;
        if (reportTypeList.size() == 1)
        {
            String reportType = reportTypeList.get(0);
            generator = ReportGeneratorFactory.getReportGenerator(reportType,
                    p_request, p_response);
            
            if (generator instanceof Cancelable)  
            {
				String key = ReportHelper.getKey(userId, reportJobIDS, reportType);
				m_generatorMap.put(key, generator);
			}
            
            File[] files = generator.generateReports(reportJobIDS,
                    reportTargetLocales);
            ReportHelper.addFiles(reports, files);
        }
        else if (reportTypeList.size() > 1)
        {
            for (String reportType : reportTypeList)
            {
                if (isCancelled(userId, reportJobIDS, reportType))
                {
                    logger.debug("cancelGenerateReports:" + userId
                            + reportJobIDS);
                    generator = null;
                    return;
                }
                generator = ReportGeneratorFactory.getReportGenerator(
                        reportType, p_request, p_response);
                File[] files = generator.generateReports(reportJobIDS,
                        reportTargetLocales);
                ReportHelper.addFiles(reports, files);
            }
            zipFileName = getReportName(reportJobIDS);
        }

        // Set Reports percent and status for m_reportsDataMap.
        ReportHelper.setReportsDataMap(m_reportsDataMap, userId, reportJobIDS,
                reportTypeList, 100, ReportsData.STATUS_FINISHED);
        generator = null;        
        if (reports == null || reports.size() == 0)
        {
            StringBuffer msg = new StringBuffer();
            msg.append("Can't create the report. Please check the options.");
            ErrorBean errorBean = new ErrorBean(0, msg.toString());
            p_request.setAttribute(WebAppConstants.ERROR_BEAN_NAME, errorBean);
            p_request.getRequestDispatcher(ReportConstants.ERROR_PAGE).forward(p_request, p_response);
        }
        
        ReportHelper.sendFiles(reports, zipFileName, p_response);
    }
    
    /**
     * Generate Summary Report
     */
    @ActionHandler(action = ACTION_GENERATE_SUMMARY_PERCENT, formClass = "")
    public void generateSummaryReport(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        SummaryReportGenerator generator = new SummaryReportGenerator();
        ReportSearchOptions searchOptions = generator.getSearchOptions(p_request);
        File[] file = generator.generateReports(searchOptions);
        
        generator = null;
        ReportHelper.sendFiles(file, null, p_response);
    }

    /**
     * Gets the ReportsData.
     */
    @ActionHandler(action = ACTION_GET_REPORTSDATA, formClass = "")
    public void getReportsData(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        HttpSession userSession = p_request.getSession();
        String userId = (String) userSession
                .getAttribute(WebAppConstants.USER_NAME);
        List<Long> reportJobIDS = ReportHelper.getListOfLong(p_request
                .getParameter(ReportConstants.JOB_IDS));
        List<String> reportTypeList = ReportHelper.getListOfStr(
                p_request.getParameter(ReportConstants.REPORT_TYPE), ",");

        String key = ReportHelper.getKey(userId, reportJobIDS, reportTypeList);
        ReportsData data = m_reportsDataMap.get(key);
        String json = "";
        if (data != null)
        {
            json = data.toJSON();
            logInfo("GETPERCENT METHOD:" + json);
        }
        
        p_response.getWriter().write(json);
    }
    
    /**
     * Gets the percent of Reports.
     */
    @ActionHandler(action = ACTION_GET_PERCENT, formClass = "")
    public void getPercent(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        HttpSession userSession = p_request.getSession();
        String userId = (String) userSession
                .getAttribute(WebAppConstants.USER_NAME);
        List<Long> reportJobIDS = ReportHelper.getListOfLong(p_request
                .getParameter(ReportConstants.JOB_IDS));
        List<String> reportTypeList = ReportHelper.getListOfStr(
                p_request.getParameter(ReportConstants.REPORT_TYPE), ",");

        String key = ReportHelper.getKey(userId, reportJobIDS, reportTypeList);
        ReportsData data = m_reportsDataMap.get(key);
        if (data != null)
        {
            String json = data.toJSON();
            logger.debug("GETPERCENT METHOD:" + json);
            if (data.getPercent() >= 100)
            {
                m_reportsDataMap.remove(key);
            }
            p_response.getWriter().write(json);
        }
    }

    /**
     * Set the cancel status for the Reports.
     */
    @ActionHandler(action = ACTION_CANCEL_REPORTS, formClass = "")
    public void cancelReports(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        HttpSession userSession = p_request.getSession();
        String userId = (String) userSession
                .getAttribute(WebAppConstants.USER_NAME);
        List<Long> reportJobIDS = ReportHelper.getListOfLong(p_request
                .getParameter(ReportConstants.JOB_IDS));
        List<String> reportTypeList = ReportHelper.getListOfStr(
                p_request.getParameter(ReportConstants.REPORT_TYPE), ",");

        String key = ReportHelper.getKey(userId, reportJobIDS, reportTypeList);
        ReportsData data = m_reportsDataMap.get(key);
        if (data != null)
        {
            data.setStatus(ReportsData.STATUS_CANCEL);
            logInfo("cancelReports:" + data + ", key:" + key);
            logInfo("cancelReports: , mapKey:" + m_reportsDataMap.keySet());
        }
    }


    /**
     * Set the Report percent by Report Generator.
     */
    public static void setReportsMapByGenerator(String p_userId,
            List<Long> p_reportJobIDS, double p_percent, String p_reportType)
    {
        String key = ReportHelper.getKey(p_userId, p_reportJobIDS, p_reportType);
        ReportsData data = m_reportsDataMap.get(key);

        if (data != null)
        {
            double percent = p_percent / data.getReportTypeList().size();
            data.addPercent(percent);
            m_reportsDataMap.put(key, data);
        }
    }

    /**
     * Gets ReportsData from m_reportsDataMap, for detect whether canceled the
     * reports.
     */
    public static ReportsData getReportsMap(String p_userId,
            List<Long> p_reportJobIDS, String p_reportType)
    {
        String key = ReportHelper.getKey(p_userId, p_reportJobIDS, p_reportType);
        ReportsData data = m_reportsDataMap.get(key);
        return data;
    }

    public static boolean isCancelled(String p_userId, List<Long> p_reportJobIDS, String p_reportType)
    {
        ReportsData data = getReportsMap(p_userId, p_reportJobIDS, p_reportType);
        
        if (data != null)
        {
            return data.isCancle();
        }
        else
        {
            logInfo("Handler-->isCancelled, mapKey:" + m_reportsDataMap.keySet());
            logInfo("Handler-->isCancelled, reportKey:" + ReportHelper.getKey(p_userId, p_reportJobIDS, p_reportType));
        }

        return false;
    }
    
    /**
     * Invokes this PageHandler
     * 
     * @param p_pageDescriptor
     *            the page descriptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        beforeAction(p_request, p_response);

        callAction(p_request, p_response);

        afterAction(p_request, p_response);

        if (!GENERATE_REPORTS.equalsIgnoreCase(p_request
                .getParameter("linkName")))
        {
            super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                    p_context);
        }
    }

    private void callAction(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {
        String action = p_request.getParameter("action");
        if (action == null)
        {
            //Job Details Page rewrite.Job Summary need.
            JobSummaryHelper jobSummaryHelper = new JobSummaryHelper();
            Job job = jobSummaryHelper.getJobByRequest(p_request);
            jobSummaryHelper.packJobSummaryInfoView(p_request, job);
            return;
        }

        Method[] ms = this.getClass().getMethods();
        for (Method m : ms)
        {
            if (m.isAnnotationPresent(ActionHandler.class))
            {
                ActionHandler handler = m.getAnnotation(ActionHandler.class);
                if (action.matches(handler.action()))
                {
                    try
                    {
                        m.invoke(this, p_request, p_response);
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage(), e);
                    }

                    break;
                }
            }
        }
    }

    public void beforeAction(HttpServletRequest p_request,
            HttpServletResponse response)
    {
    }

    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
    }

    public static String getReportName(List<Long> p_reportJobIDS)
            throws JobException, RemoteException, GeneralException,
            NamingException
    {
        if (p_reportJobIDS != null && p_reportJobIDS.size() == 1)
        {
            long jobId = p_reportJobIDS.get(0);
            Job job = ServerProxy.getJobHandler().getJobById(jobId);
            return ReportConstants.REPORTS_NAME + "-[" + job.getJobName()
                    + "][" + jobId + "]";
        }
        else
        {
            return ReportConstants.REPORTS_NAME;
        }
    }
    
    private static void logInfo(String p_msg)
    {
        //logger.info(p_msg);
    }
}
