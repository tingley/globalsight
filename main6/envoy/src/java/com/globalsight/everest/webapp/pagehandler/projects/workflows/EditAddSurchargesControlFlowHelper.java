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

import org.apache.log4j.Logger;

import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.FlatSurcharge;
import com.globalsight.everest.costing.Money;
import com.globalsight.everest.costing.PercentageSurcharge;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler;
import com.globalsight.util.GeneralException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * EditPagesControlFlowHelper, A page flow helper that 
 * saves the number of pages in a job then redirects
 * the user to the next JSP page.
 */
class EditAddSurchargesControlFlowHelper
    implements ControlFlowHelper, WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            SurchargesControlFlowHelper.class);

    // local variables
    private HttpServletRequest m_request = null;
    private HttpServletResponse m_response = null;

    public EditAddSurchargesControlFlowHelper(HttpServletRequest p_request,
        HttpServletResponse p_response)
    {
        m_request = p_request;
        m_response = p_response;
    }

    // returns the name of the link to follow
    public String determineLinkToFollow()
        throws EnvoyServletException
    {
        String destinationPage = JobManagementHandler.SURCHARGES_BEAN;

        if (m_request.getParameterValues("formAction")[0].equals("save"))  
        {
            // They clicked SAVE
            String surchargeAction = m_request.getParameterValues(JobManagementHandler.SURCHARGE_ACTION)[0];
            String surchargeOldName = m_request.getParameterValues(JobManagementHandler.SURCHARGE)[0];
            HttpSession session = m_request.getSession(false);
            SessionManager sessionMgr =
                    (SessionManager)session.getAttribute(SESSION_MANAGER);

            // Now find if they are saving an edit or an add
            // They want to save the NEW surcharge
            try
            {
                Job job = WorkflowHandlerHelper.getJobById(Long.parseLong(
                        m_request.getParameter(JobManagementHandler.JOB_ID)));
                // get the cost objet from the session
                Cost cost = null;
                int costType = Cost.EXPENSE;
                String surchargesFor = (String) sessionMgr.getAttribute(JobManagementHandler.SURCHARGES_FOR);
                String currStr = (String) m_request.getParameter(JobManagementHandler.CURRENCY);
                Currency oCurrency = ServerProxy.getCostingEngine().getCurrency(currStr);
                if(surchargesFor.equals(EXPENSES))
                {
//                    cost = (Cost)sessionMgr.getAttribute(JobManagementHandler.COST_OBJECT);
                    // Calculate Expenses
                    cost = ServerProxy.getCostingEngine().calculateCost(job,
                            oCurrency, true, Cost.EXPENSE);
                    costType = Cost.EXPENSE;
                }
                else
                {
//                    cost = (Cost)sessionMgr.getAttribute(JobManagementHandler.REVENUE_OBJECT);
                	cost = ServerProxy.getCostingEngine().calculateCost(job, oCurrency,
                            true, Cost.REVENUE);
                    costType = Cost.REVENUE;
                }
                float amount = Float.parseFloat(m_request.getParameterValues(JobManagementHandler.SURCHARGE_VALUE)[0]);
                if (m_request.getParameterValues(JobManagementHandler.SURCHARGE_TYPE)[0].equals("1"))
                {
                    // create surcharge with whatever currency is in the session
                    Currency curr = (Currency)sessionMgr.getAttribute(JobManagementHandler.CURRENCY_OBJECT);
                    Money money = new Money(amount, curr);
                    FlatSurcharge flatSurcharge = new FlatSurcharge(money);
                    flatSurcharge.setName(m_request.getParameterValues(JobManagementHandler.SURCHARGE_NAME)[0]);
                    if (surchargeAction.equals("add")) 
                    {
                        cost = ServerProxy.getCostingEngine().addSurcharge(cost.getId(), 
                                                                           flatSurcharge, costType);
                    }
                    else 
                    {
                        cost = ServerProxy.getCostingEngine().modifySurcharge(cost.getId(), 
                                                                              surchargeOldName,
                                                                              flatSurcharge, costType);
                    }
                }
                else
                {
                    // Make the amount a percentage
                    amount = amount/100;

                    PercentageSurcharge percentageSurcharge = new PercentageSurcharge(amount);
                    percentageSurcharge.setName(m_request.getParameterValues(JobManagementHandler.SURCHARGE_NAME)[0]);
                    if (surchargeAction.equals("add"))
                    {
                        cost = ServerProxy.getCostingEngine().addSurcharge(cost.getId(), 
                                                                           percentageSurcharge, costType);
                    }
                    else 
                    {
                        cost = ServerProxy.getCostingEngine().modifySurcharge(cost.getId(), 
                                                                              surchargeOldName,
                                                                           percentageSurcharge, costType);
                    }
                }

                // Put the new cost object in the session
                if(surchargesFor.equals(EXPENSES))
                {
                    sessionMgr.setAttribute(JobManagementHandler.COST_OBJECT, cost);
                }
                else
                {
                    sessionMgr.setAttribute(JobManagementHandler.REVENUE_OBJECT, cost);
                }
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
            }
        }
        else 
        {
            // They clicked CANCEL
            destinationPage = JobManagementHandler.SURCHARGES_BEAN;
        }
        return destinationPage;
    }
}
