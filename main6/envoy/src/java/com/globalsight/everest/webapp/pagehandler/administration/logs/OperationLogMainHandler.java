/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.globalsight.everest.webapp.pagehandler.administration.logs;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.LogInfoComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.StringUtil;
import com.globalsight.util.system.LogInfo;
import com.globalsight.util.system.LogManager;

public class OperationLogMainHandler extends PageHandler
{
    public static final String LOGINFO_LIST ="operationLog";
    public static final String LOGINFO_KEY = "logInfo";
    public static final String FILTER_EVENT_TYPE = "filterEventType";
    public static final String FILTER_OBJECT_TYPE = "filterObjectType";
    public static final String FILTER_OPERATOR = "filterOperator";
    public static final String FILTER_MESSAGE = "filterMessage";
    public static final String FILTER_COMPANY_NAME = "filterCompanyName";

    static private int NUM_PER_PAGE = 20;

    private static Logger logger = Logger.getLogger(OperationLogMainHandler.class);

    /**
     * Invokes this PageHandler
     * 
     * @param pageDescriptor
     *            the page descriptor
     * @param request
     *            the original request sent from the browser
     * @param response
     *            the original response object
     * @param context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context)
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        
        String eventTypeFilterValue = (String) sessionManager
                .getAttribute(OperationLogMainHandler.FILTER_EVENT_TYPE);
        sessionManager.setAttribute(
                OperationLogMainHandler.FILTER_EVENT_TYPE,
                eventTypeFilterValue);
        
        String objectTypeFilterValue = (String) sessionManager
                .getAttribute(OperationLogMainHandler.FILTER_OBJECT_TYPE);
        sessionManager.setAttribute(
                OperationLogMainHandler.FILTER_OBJECT_TYPE,
                objectTypeFilterValue);
        
        String operatorFilterValue = (String) sessionManager
                .getAttribute(OperationLogMainHandler.FILTER_OPERATOR);
        sessionManager.setAttribute(
                OperationLogMainHandler.FILTER_OPERATOR,
                operatorFilterValue);
        
        String messageFilterValue = (String) sessionManager
                .getAttribute(OperationLogMainHandler.FILTER_MESSAGE);
        sessionManager.setAttribute(
                OperationLogMainHandler.FILTER_MESSAGE,
                messageFilterValue);
        
        String companyFilterValue = (String) sessionManager
                .getAttribute(OperationLogMainHandler.FILTER_COMPANY_NAME);
        sessionManager.setAttribute(
                OperationLogMainHandler.FILTER_COMPANY_NAME,
                companyFilterValue);

        try
        {
            LogManager logManager = new LogManager();

            List<LogInfo> logInfoList = (List<LogInfo>) logManager
                    .getAllOperationLogs(objectTypeFilterValue, eventTypeFilterValue, operatorFilterValue,
                            messageFilterValue);

            setNumberOfPerPage(p_request);

            dataForTable(p_request, session, logInfoList);
            
            super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                    p_context);
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }
    
    /**
     * Get list of opterationLog. Also set the pivot currency in the request.
     * @throws Exception 
     */
    
    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session, List<LogInfo> logInfoList) throws Exception
    {
        SessionManager sessionManager = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        // Filter opterationLog by eventType 
        String eventTypeFilterValue = p_request
                .getParameter(OperationLogMainHandler.FILTER_EVENT_TYPE);
        if (eventTypeFilterValue == null)
        {
            eventTypeFilterValue = (String) sessionManager
                    .getAttribute(OperationLogMainHandler.FILTER_EVENT_TYPE);
        }
        if (eventTypeFilterValue == null)
        {
            eventTypeFilterValue = "";
        }
        sessionManager.setAttribute(OperationLogMainHandler.FILTER_EVENT_TYPE,
                eventTypeFilterValue.trim());
        // Filter opterationLog by objectType
        String objectTypeFilterValue = p_request
                .getParameter(OperationLogMainHandler.FILTER_OBJECT_TYPE);
        if (objectTypeFilterValue == null)
        {
            objectTypeFilterValue = (String) sessionManager
                    .getAttribute(OperationLogMainHandler.FILTER_OBJECT_TYPE);
        }
        if (objectTypeFilterValue == null)
        {
            objectTypeFilterValue = "";
        }
        sessionManager.setAttribute(OperationLogMainHandler.FILTER_OBJECT_TYPE,
                objectTypeFilterValue.trim());
        // Filter opterationLog by operator
        String operatorFilterValue = p_request
                .getParameter(OperationLogMainHandler.FILTER_OPERATOR);
        if (operatorFilterValue == null)
        {
            operatorFilterValue = (String) sessionManager
                    .getAttribute(OperationLogMainHandler.FILTER_OPERATOR);
        }
        if (operatorFilterValue == null)
        {
            operatorFilterValue = "";
        }
        sessionManager.setAttribute(OperationLogMainHandler.FILTER_OPERATOR,
                operatorFilterValue.trim());
        // Filter opterationLog by message
        String messageFilterValue = p_request
                .getParameter(OperationLogMainHandler.FILTER_MESSAGE);
        if (messageFilterValue == null)
        {
            messageFilterValue = (String) sessionManager
                    .getAttribute(OperationLogMainHandler.FILTER_MESSAGE);
        }
        if (messageFilterValue == null)
        {
            messageFilterValue = "";
        }
        sessionManager.setAttribute(OperationLogMainHandler.FILTER_MESSAGE,
                messageFilterValue.trim());
        LogManager logManager = new LogManager();

        List<LogInfo> logInfoLists = (List<LogInfo>) logManager
                .getAllOperationLogs(objectTypeFilterValue, eventTypeFilterValue, operatorFilterValue,
                        messageFilterValue);
        
        // Filter opterationLog by companyName
        String companyFilterValue = p_request
                .getParameter(OperationLogMainHandler.FILTER_COMPANY_NAME);
        if (companyFilterValue == null)
        {
            companyFilterValue = (String) sessionManager
                    .getAttribute(OperationLogMainHandler.FILTER_COMPANY_NAME);
        }
        if (companyFilterValue == null)
        {
            companyFilterValue = "";
        }
        sessionManager.setAttribute(OperationLogMainHandler.FILTER_COMPANY_NAME,
                companyFilterValue.trim());
        if (StringUtil.isNotEmpty(companyFilterValue))
        {
            for (Iterator it = logInfoLists.iterator(); it.hasNext();)
            {
                LogInfo logInfo = (LogInfo) it.next();
                
                String comName = CompanyWrapper.getCompanyNameById(
                        logInfo.getCompanyId()).toLowerCase();

                if (comName.indexOf(companyFilterValue.trim().toLowerCase()) == -1)
                {
                    it.remove();
                }
            }
        }

        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);
        
        setTableNavigation(p_request, p_session, logInfoLists, new LogInfoComparator(uiLocale), NUM_PER_PAGE, LOGINFO_LIST,
                LOGINFO_KEY);
    }
    
    private void setNumberOfPerPage(HttpServletRequest req)
    {
        String pageSize = (String) req.getParameter("numOfPageSize");

        if (StringUtil.isNotEmpty(pageSize))
        {
            try
            {
                NUM_PER_PAGE = Integer.parseInt(pageSize);
            }
            catch (Exception e)
            {
                NUM_PER_PAGE = Integer.MAX_VALUE;
            }
        }
    }
    
    
}
