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

package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.SurchargeComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.SortUtil;

/**
 * This is a page handler for handling the display of Surcharges.
 */
public class SurchargesHandler extends PageHandler
{

    /**
     * Invokes this PageHandler
     * 
     * @param jspURL
     *            the URL of the JSP to be invoked
     * @param the
     *            original request sent from the browser
     * @param the
     *            original response object
     * @param context
     *            the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException,
            EnvoyServletException
    {
        try
        {
            String pageName = p_pageDescriptor.getPageName();
            NavigationBean detailsBean = new NavigationBean(
                    JobManagementHandler.DETAILS_BEAN, pageName);
            NavigationBean surchargesBean = new NavigationBean(
                    JobManagementHandler.SURCHARGES_BEAN, pageName);

            p_request.setAttribute(JobManagementHandler.DETAILS_BEAN,
                    detailsBean);
            p_request.setAttribute(JobManagementHandler.SURCHARGES_BEAN,
                    surchargesBean);
            JobSummaryHelper jobSummaryHelper = new JobSummaryHelper();
            Job job = jobSummaryHelper.getJobByRequest(p_request);
            p_request.setAttribute(JobManagementHandler.JOB_ID, job.getId()+"");
            p_request.setAttribute(JobManagementHandler.JOB_NAME_SCRIPTLET, job.getJobName());
            // Sort the Collection of Surcharges, then pass the resulting
            // sorted List to the JSP via the request object
            HttpSession p_session = p_request.getSession(false);
            SessionManager sessionMgr = (SessionManager) p_session
                    .getAttribute(WebAppConstants.SESSION_MANAGER);
            Locale uiLocale = (Locale) p_session
                    .getAttribute(WebAppConstants.UILOCALE);

            String surchargesFor = p_request
                    .getParameter(JobManagementHandler.SURCHARGES_FOR);
            if (surchargesFor != null)
            {
                sessionMgr.setAttribute(JobManagementHandler.SURCHARGES_FOR,
                        surchargesFor);
            }
            else
            {
                surchargesFor = (String) sessionMgr
                        .getAttribute(JobManagementHandler.SURCHARGES_FOR);
            }
            Cost cost = null;
            Cost revenue = null;
            Collection surchargesAll = null;
            String curr = (String) p_session
            		.getAttribute(JobManagementHandler.CURRENCY);
            Currency oCurrency = ServerProxy.getCostingEngine().getCurrency(
            		curr);

            if (surchargesFor.equals(EXPENSES))
            {
                // expenses
//              cost = (Cost) sessionMgr.getAttribute(JobManagementHandler.COST_OBJECT);
                // Calculate Expenses
                cost = ServerProxy.getCostingEngine().calculateCost(job,
                        oCurrency, true, Cost.EXPENSE);
                surchargesAll = cost.getSurcharges();
            }
            else
            {
                // revenue
//                revenue = (Cost) sessionMgr
//                        .getAttribute(JobManagementHandler.REVENUE_OBJECT);
                 // Calculate Revenue
                 revenue = ServerProxy.getCostingEngine().calculateCost(job, oCurrency,
                         true, Cost.REVENUE);
                surchargesAll = revenue.getSurcharges();
            }

            ArrayList surchargesList = new ArrayList(surchargesAll);

            // Sort by name
            SurchargeComparator comp = new SurchargeComparator(
                    SurchargeComparator.NAME, uiLocale);
            SortUtil.sort(surchargesList, comp);
            if (surchargesFor.equals(EXPENSES))
            {
                p_request.setAttribute(JobManagementHandler.SURCHARGES_ALL,
                        surchargesList);
            }
            else
            {
                p_request.setAttribute(
                        JobManagementHandler.REVENUE_SURCHARGES_ALL,
                        surchargesList);
            }

            super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                    p_context);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Overide getControlFlowHelper so we can do processing and redirect the
     * user correctly.
     * 
     * @return the name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {
        return new SurchargesControlFlowHelper(p_request, p_response);
    }
}
