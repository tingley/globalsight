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
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.config.UserParamNames;
import com.globalsight.everest.costing.BigDecimalHelper;
import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.CurrencyFormat;
import com.globalsight.everest.costing.FlatSurcharge;
import com.globalsight.everest.costing.Money;
import com.globalsight.everest.costing.PercentageSurcharge;
import com.globalsight.everest.costing.Surcharge;
import com.globalsight.everest.foundation.EmailInformation;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.CurrencyComparator;
import com.globalsight.everest.util.comparator.SurchargeComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.projects.ProjectHandlerHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.scheduling.SchedulerConstants;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

public class JobCostsHandler extends PageHandler implements UserParamNames
{
    private static final Logger CATEGORY = Logger
            .getLogger(JobCostsHandler.class);

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            RemoteException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        if (p_request.getParameter("currencyIsoCode") != null)
        {
            session.setAttribute(JobManagementHandler.CURRENCY,
                    p_request.getParameter("currencyIsoCode"));
        }
        JobSummaryHelper jobSummaryHelper = new JobSummaryHelper();
        Job job = jobSummaryHelper.getJobByRequest(p_request);
        boolean isOk = jobSummaryHelper.packJobSummaryInfoView(p_request,
                p_response, p_context, job);
        if (!isOk)
        {
            return;
        }

        // Here we have to calculate the job cost.
        if (JobSummaryHelper.s_isCostingEnabled)
        {
            packJobCostInfoView(p_request, session, job);
        }

        p_request.setAttribute("jobHasSetCostCenter", job.hasSetCostCenter());
        p_request.setAttribute("hasReadyWorkflow", hasReadyWorkflow(job));
        p_request.setAttribute("allreadyWorkfowIds", getReadyWorkflowIds(job));
        parseQuoteRequestParameter(p_request, session, job);

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private void packJobCostInfoView(HttpServletRequest p_request,
            HttpSession session, Job job)
    {
        p_request.setAttribute("CurrencyMap", getCurrencyMap(session));
        packCurrencyIsoCodeView(session);
        packJobCostExpenseAndRevenueInfos(p_request, job);
    }

    private Map<String, String> getCurrencyMap(HttpSession session)
    {
        Map<String, String> currencyMap = new HashMap<String, String>();
        try
        {
            Locale uiLocale = (Locale) session
                    .getAttribute(WebAppConstants.UILOCALE);
            Collection<Currency> allCurrencies = ServerProxy.getCostingEngine()
                    .getCurrencies();
            ArrayList<Currency> currencies = new ArrayList<Currency>(
                    allCurrencies);
            SortUtil.sort(currencies,
                    new CurrencyComparator(Locale.getDefault()));
            for (Currency currency : currencies)
            {
                currencyMap.put(currency.getIsoCode(),
                        currency.getDisplayName(uiLocale));
            }
        }
        catch (Exception e)
        {
            CATEGORY.error(
                    "JobCostsHandler::invokePageHandler():Problem getting Currencies from the system ",
                    e);
        }

        return currencyMap;
    }

    private String packCurrencyIsoCodeView(HttpSession session)
    {
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        String curr = (String) session
                .getAttribute(JobManagementHandler.CURRENCY);
        if (curr == null)
        {
            // Get the pivot currency;
            try
            {
                Currency c = ServerProxy.getCostingEngine().getPivotCurrency();
                curr = c.getIsoCode();
            }
            catch (Exception e)
            {
                CATEGORY.error("Problem getting pivot currency ", e);
            }
        }
        session.setAttribute(JobManagementHandler.CURRENCY, curr);
        // Job Cost Tab --> Cost Report button needed jobId
        sessionMgr.setAttribute(JobManagementHandler.CURRENCY, curr);
        return curr;
    }

    private void packJobCostExpenseAndRevenueInfos(
            HttpServletRequest p_request, Job job)
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        // clear the surchargesFor attribute here
        sessionMgr.setAttribute(JobManagementHandler.SURCHARGES_FOR, "");
        // Job Cost Tab --> Cost Report button needed jobId
        sessionMgr.setAttribute(JobManagementHandler.JOB_ID, job.getJobId());
        // source locale
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        String curr = (String) session
                .getAttribute(JobManagementHandler.CURRENCY);
        try
        {
            Currency oCurrency = ServerProxy.getCostingEngine().getCurrency(
                    curr);
            // Calculate Expenses
            Cost cost = ServerProxy.getCostingEngine().calculateCost(job,
                    oCurrency, true, Cost.EXPENSE);
            // cost is only null, when all workflows of a job are discarded
            // from
            // job details UI.
            if (cost != null)
            {
                // Get the Estimated cost
                String formattedEstimatedCost = (isInContextMatch(job)) ? cost
                        .getEstimatedCost().getFormattedAmount()
                        : cost.getNoUseEstimatedCost().getFormattedAmount();
                p_request.setAttribute("estimatedCost", formattedEstimatedCost);

                // Get the Actual cost
                String formattedActualCost = cost.getActualCost()
                        .getFormattedAmount();
                p_request.setAttribute("actualCost", formattedActualCost);

                // Get the Final cost
                String formattedFinalCost = cost.getFinalCost()
                        .getFormattedAmount();
                p_request.setAttribute("finalCost", formattedFinalCost);

                p_request.setAttribute("isCostOverriden", cost.isOverriden());

                // Put the cost object on the sessionMgr so other cost
                // screens
                // will have easy access to it
                sessionMgr.setAttribute(JobManagementHandler.COST_OBJECT, cost);
                sessionMgr.setAttribute(JobManagementHandler.CURRENCY_OBJECT,
                        oCurrency);

                // Sort the Surcharges as pass them as an ArrayList to the
                // JSP
                Collection surchargesAll = cost.getSurcharges();
                ArrayList<Surcharge> surchargesList = new ArrayList<Surcharge>(
                        surchargesAll);
                SurchargeComparator comp = new SurchargeComparator(
                        SurchargeComparator.NAME, uiLocale);
                SortUtil.sort(surchargesList, comp);
                Map<String, String> surchargesFlatMap = new HashMap<String, String>();
                Map<String, String> surchargesPercentageMap = new HashMap<String, String>();

                // tempCost includes actualCost & all flat surcharges.
                // tempCost will used to calculate the percentage surcharges.
                Money tempCost = cost.getActualCost();

                float surchargeAmount = 0, percentage = 0, percentageAmount = 0;
                for (Surcharge surcharge : surchargesList)
                {
                    if ("FlatSurcharge".equals(surcharge.getType()))
                    {
                        FlatSurcharge flatSurcharge = (FlatSurcharge) surcharge;
                        surchargeAmount = flatSurcharge.getAmount().getAmount();
                        tempCost = tempCost.add(flatSurcharge
                                .surchargeAmount(flatSurcharge.getAmount()));
                        surchargesFlatMap.put(flatSurcharge.getName(),
                                CurrencyFormat.getCurrencyFormat(oCurrency)
                                        .format(surchargeAmount));
                    }
                }
                for (Surcharge surcharge : surchargesList)
                {
                    if ("PercentageSurcharge".equals(surcharge.getType()))
                    {
                        PercentageSurcharge percentageSurcharge = (PercentageSurcharge) surcharge;
                        percentage = Money.roundOff(percentageSurcharge
                                .getPercentage() * 100);
                        percentageAmount = percentageSurcharge.surchargeAmount(
                                tempCost).getAmount();
                        surchargesPercentageMap.put(
                                percentageSurcharge.getName() + " ("
                                        + percentage + "%)",
                                CurrencyFormat.getCurrencyFormat(oCurrency)
                                        .format(percentageAmount));
                    }
                }
                p_request.setAttribute("SurchargesFlatMap", surchargesFlatMap);
                p_request.setAttribute("SurchargesPercentageMap",
                        surchargesPercentageMap);
            }

            if (JobSummaryHelper.s_isRevenueEnabled)
            {
                p_request.setAttribute("isRevenueEnabled",
                        JobSummaryHelper.s_isRevenueEnabled);
                packJobRevenueInfoView(p_request, job, oCurrency);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
        }
    }

    private void packJobRevenueInfoView(HttpServletRequest p_request, Job job,
            Currency oCurrency) throws Exception
    {
        HttpSession session = p_request.getSession(false);
        ResourceBundle bundle = PageHandler.getBundle(session);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        // source locale
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        Cost revenue = null;
        // Calculate Revenue
        revenue = ServerProxy.getCostingEngine().calculateCost(job, oCurrency,
                true, Cost.REVENUE);
        if (revenue != null)
        {
            // Get the Estimated cost
            String formattedEstimatedCost = (isInContextMatch(job)) ? revenue
                    .getEstimatedCost().getFormattedAmount()
                    : revenue.getNoUseEstimatedCost().getFormattedAmount();

            p_request.setAttribute("estimatedRevenue", formattedEstimatedCost);

            // Get the Actual cost
            String formattedActualCost = revenue.getActualCost()
                    .getFormattedAmount();
            p_request.setAttribute(JobManagementHandler.ACTUAL_REVENUE,
                    formattedActualCost);

            // Get the Final cost
            String formattedFinalCost = revenue.getFinalCost()
                    .getFormattedAmount();
            p_request.setAttribute("finalRevenue", formattedFinalCost);

            p_request.setAttribute("isRevenueOverriden", revenue.isOverriden());
            // Put the cost object on the sessionMgr so other cost
            // screens
            // will have easy access to it
            sessionMgr.setAttribute(JobManagementHandler.REVENUE_OBJECT,
                    revenue);

            // Sort the Surcharges as pass them as an ArrayList to
            // the JSP
            Collection surchargesAll = revenue.getSurcharges();
            ArrayList<Surcharge> surchargesList = new ArrayList<Surcharge>(
                    surchargesAll);
            SurchargeComparator comp = new SurchargeComparator(
                    SurchargeComparator.NAME, uiLocale);
            SortUtil.sort(surchargesList, comp);

            Map<String, String> surchargesRevenueFlatMap = new HashMap<String, String>();
            Map<String, String> surchargesRevenuePercentageMap = new HashMap<String, String>();

            // tempCost includes actualCost & all flat surcharges.
            // tempCost will used to calculate the percentage surcharges.
            Money tempCost = revenue.getActualCost();

            float surchargeAmount = 0, percentage = 0, perSurcharge = 0, fileCounts = 0, percentageAmount = 0;
            String flatSurchargeLocalName = "";
            for (Surcharge surcharge : surchargesList)
            {
                if ("FlatSurcharge".equals(surcharge.getType()))
                {
                    FlatSurcharge flatSurcharge = (FlatSurcharge) surcharge;
                    surchargeAmount = flatSurcharge.getAmount().getAmount();
                    tempCost = tempCost.add(flatSurcharge
                            .surchargeAmount(flatSurcharge.getAmount()));
                    flatSurchargeLocalName = flatSurcharge.getName();
                    perSurcharge = 0.0f;
                    fileCounts = job.getSourcePages().size();
                    if (flatSurchargeLocalName
                            .equals(SystemConfigParamNames.PER_FILE_CHARGE01_KEY))
                    {
                        perSurcharge = BigDecimalHelper.divide(surchargeAmount,
                                fileCounts);
                        flatSurchargeLocalName = bundle
                                .getString("lb_per_file_charge_01_detail")
                                + "<font color='blue'>&nbsp;&nbsp;$"
                                + perSurcharge + "</font>";
                    }
                    else if (flatSurchargeLocalName
                            .equals(SystemConfigParamNames.PER_FILE_CHARGE02_KEY))
                    {
                        perSurcharge = BigDecimalHelper.divide(surchargeAmount,
                                fileCounts);
                        flatSurchargeLocalName = bundle
                                .getString("lb_per_file_charge_02_detail")
                                + "<font color='blue'>&nbsp;&nbsp;$"
                                + perSurcharge + "</font>";
                    }
                    else if (flatSurchargeLocalName
                            .equals(SystemConfigParamNames.PER_JOB_CHARGE_KEY))
                    {
                        flatSurchargeLocalName = bundle
                                .getString("lb_per_job_charge_detail");
                    }

                    surchargesRevenueFlatMap.put(
                            flatSurchargeLocalName,
                            CurrencyFormat.getCurrencyFormat(oCurrency).format(
                                    surchargeAmount));
                }
            }
            for (Surcharge surcharge : surchargesList)
            {
                if ("PercentageSurcharge".equals(surcharge.getType()))
                {
                    PercentageSurcharge percentageSurcharge = (PercentageSurcharge) surcharge;
                    percentage = Money.roundOff(percentageSurcharge
                            .getPercentage() * 100);
                    percentageAmount = percentageSurcharge.surchargeAmount(
                            tempCost).getAmount();
                    surchargesRevenuePercentageMap.put(
                            percentageSurcharge.getName() + " (" + percentage
                                    + "%)",
                            CurrencyFormat.getCurrencyFormat(oCurrency).format(
                                    percentageAmount));
                }
            }
            p_request.setAttribute("SurchargesRevenueFlatMap",
                    surchargesRevenueFlatMap);
            p_request.setAttribute("SurchargesRevenuePercentageMap",
                    surchargesRevenuePercentageMap);
        }
    }

    private void parseQuoteRequestParameter(HttpServletRequest p_request,
            HttpSession session, Job job)
    {
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);

        String approveFlag = p_request
                .getParameter(JobManagementHandler.QUOTE_APPROVED_DATE_MODIFY_FLAG);

        // Update the Quote PO Number.
        if (p_request.getParameter(JobManagementHandler.QUOTE_PO_NUMBER) != null)
        {
            String quotePoNumber = (String) p_request
                    .getParameter(JobManagementHandler.QUOTE_PO_NUMBER);
            if (!quotePoNumber.equals(job.getQuotePoNumber())
                    || !user.getUserId().equals(job.getUserId()))
            {
                updateQuotePoNumber(job, quotePoNumber);
                p_request.setAttribute(
                        JobManagementHandler.QUOTE_SAVE_PO_NUMBER, "true");// send
                                                                           // mail
                                                                           // condition
            }
        }
        
        if (p_request.getParameter(JobManagementHandler.QUOTE_DATE) != null)
        {
            if ("false".equals(approveFlag))
            {
                String quoteDate = getDateString();
                
                if (!quoteDate.equals(job.getQuoteDate())
                        || !user.getUserId().equals(job.getUserId()))
                {
                    updateQuoteDate(job, quoteDate);
                    sendEmail(p_request, uiLocale, user, job);
                }
            }
        }

        // Before update the Quote Approved date
        if (p_request.getParameter(JobManagementHandler.DISPATCH_ALL_WF_PARAM) != null)
        {
            String readyWorkflowIds = p_request
                    .getParameter(JobManagementHandler.ALL_READY_WORKFLOW_IDS);
            if (readyWorkflowIds != null && readyWorkflowIds.length() > 0)
            {
                for (String id : readyWorkflowIds.split(","))
                {
                    WorkflowHandlerHelper.dispatchWF(WorkflowHandlerHelper
                            .getWorkflowById(Long.parseLong(id)));
                }
            }
        }

        // For "Quote process webEx" issue
        // Update the Quote Approved date
        if ("true".equals(approveFlag)
                && p_request
                        .getParameter(JobManagementHandler.QUOTE_APPROVED_DATE) != null)
        {
            String quoteApprovedDate = getDateString();
            if (!quoteApprovedDate.equals(job.getQuoteApprovedDate())
                    || !user.getUserId().equals(job.getUserId()))
            {
                updateAuthoriserUser(job, user);
                updateQuoteApprovedDate(job, quoteApprovedDate);

                if (job.getProject().getPoRequired() == 0)
                {
                    if (job.getQuoteDate() == null
                            || job.getQuoteDate().equals(""))
                    {
                        updateQuoteDate(job, quoteApprovedDate);
                    }
                }
                // Approved quote Date Default value, it will be set when
                // reset these charges.
                String quoteApproveDelaultValue = "0000";
                if (!quoteApprovedDate.equals(quoteApproveDelaultValue))
                {
                    sendEmail(p_request, uiLocale, user, job);
                }
            }
        }

    }

    /**
     * Format current time as MM/dd/yyyy HH:mm;
     * 
     * @return
     */
    private String getDateString()
    {
        Calendar now = Calendar.getInstance();

        Format f = new SimpleDateFormat("MM/dd/yyyy HH:mm");

        return f.format(now.getTime());
    }

    /**
     * 
     * @param p_job
     *            specifies the job to be updated
     * @param p_quoteDate
     *            specifies the quote date to be updated This method is used to
     *            update the quote date in database
     */
    private void updateQuoteDate(Job p_job, String p_quoteDate)
    {
        try
        {
            ServerProxy.getJobHandler().updateQuoteDate(p_job, p_quoteDate);
        }
        catch (Exception e)
        {
            CATEGORY.error("Problem updating quotation email date ", e);
        }
    }

    /**
     * This method is used to prepare data for email sending
     */
    private void sendEmail(HttpServletRequest p_request, Locale p_uiLocale,
            User p_user, Job p_job)
    {
        String newPO = p_job.getQuotePoNumber();

        if (p_request.getParameter(JobManagementHandler.QUOTE_PO_NUMBER) != null)
        {
            newPO = (String) p_request
                    .getParameter(JobManagementHandler.QUOTE_PO_NUMBER);
        }

        try
        {
            Project project = WorkflowHandlerHelper.getProjectById(p_job
                    .getL10nProfile().getProjectId());
            String companyIdStr = String.valueOf(project.getCompanyId());
            User pm = project.getProjectManager();
            User quotePerson = null;
            if (project.getQuotePersonId() != null
                    && !"".equals(project.getQuotePersonId()))
            {
                if ("0".equals(project.getQuotePersonId()))
                {
                    // "0" indicates the quote person is set to the job
                    // submitter.
                    quotePerson = p_job.getCreateUser();
                }
                else
                {
                    quotePerson = ProjectHandlerHelper.getUser(project
                            .getQuotePersonId());
                }
            }
            String approveFlag = p_request
                    .getParameter(JobManagementHandler.QUOTE_APPROVED_DATE_MODIFY_FLAG);
            String savePONumber = (String) p_request
                    .getAttribute(JobManagementHandler.QUOTE_SAVE_PO_NUMBER);
            EmailInformation from = ServerProxy.getUserManager()
                    .getEmailInformationForUser(p_user.getUserId());
            if (savePONumber != null && "true".equals(savePONumber))
            {
                if (!pm.getUserId().equals(p_user.getUserId()))
                {
                    EmailInformation pmEmailInfo = ServerProxy.getUserManager()
                            .getEmailInformationForUser(pm.getUserId());
                    // send email to PM
                    ServerProxy.getMailer().sendMail(
                            from,
                            pmEmailInfo,
                            SchedulerConstants.NOTIFY_PONUMBER_SUBJECT,
                            SchedulerConstants.NOTIFY_PONUMBER_BODY,
                            getArguments(p_request, p_uiLocale, p_user, p_job,
                                    newPO), companyIdStr);
                }
            }
            else if (approveFlag != null && approveFlag.equals("false"))
            {
                if (!pm.getUserId().equals(p_user.getUserId()))
                {
                    EmailInformation pmEmailInfo = ServerProxy.getUserManager()
                            .getEmailInformationForUser(pm.getUserId());
                    p_request.setAttribute(WebAppConstants.LOGIN_NAME_FIELD,
                            pm.getUserName());
                    // send email to PM
                    ServerProxy.getMailer().sendMail(
                            from,
                            pmEmailInfo,
                            SchedulerConstants.NOTIFY_QUOTE_PERSON_SUBJECT,
                            SchedulerConstants.NOTIFY_QUOTE_PERSON_BODY,
                            getArguments(p_request, p_uiLocale, p_user, p_job,
                                    newPO), companyIdStr);
                }

                if (quotePerson != null
                        && !quotePerson.getUserId().equals(p_user.getUserId()))
                {
                    EmailInformation qpEmailInfo = ServerProxy
                            .getUserManager()
                            .getEmailInformationForUser(quotePerson.getUserId());
                    p_request.setAttribute(WebAppConstants.LOGIN_NAME_FIELD,
                            quotePerson.getUserName());
                    // send email to quote person
                    ServerProxy.getMailer().sendMail(
                            from,
                            qpEmailInfo,
                            SchedulerConstants.NOTIFY_QUOTE_PERSON_SUBJECT,
                            SchedulerConstants.NOTIFY_QUOTE_PERSON_BODY,
                            getArguments(p_request, p_uiLocale, p_user, p_job,
                                    newPO), companyIdStr);
                }
            }
            else if (approveFlag != null && approveFlag.equals("true"))
            {
                if (quotePerson != null && quotePerson.getUserId() != null)
                {
                    EmailInformation qpEmailInfo = ServerProxy.getUserManager()
                            .getEmailInformationForUser(pm.getUserId());
                    // send email to PM for approving
                    ServerProxy.getMailer().sendMail(
                            from,
                            qpEmailInfo,
                            SchedulerConstants.NOTIFY_QUOTEAPPROVED_SUBJECT,
                            SchedulerConstants.NOTIFY_QUOTEAPPROVED_BODY,
                            getArguments(p_request, p_uiLocale, p_user, p_job,
                                    newPO), companyIdStr);
                }

            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Problem sending quotation email ", e);
        }
    }

    /**
     * 
     * @return the arguments of the email message.
     */
    private String[] getArguments(HttpServletRequest p_request,
            Locale p_uiLocale, User p_user, Job p_job, String p_po)
    {
        String[] messageArgs = new String[7];
        messageArgs[0] = p_job.getJobName();
        messageArgs[1] = String.valueOf(p_job.getJobId());
        Collection workflows = p_job.getWorkflows();
        StringBuffer sb = new StringBuffer();
        Workflow workflowInstance = null;
        GlobalSightLocale targetLocal = null;

        // Session dbSession = HibernateUtil.getSession();
        Iterator iterator = workflows.iterator();
        while (iterator.hasNext())
        {
            workflowInstance = (Workflow) iterator.next();
            // Remove the workflow instance that be cancelled
            if (workflowInstance.getState().equals(Workflow.CANCELLED))
            {
                continue;
            }
            targetLocal = workflowInstance.getTargetLocale();

            // targetLocal = (GlobalSightLocale) dbSession.get(
            // GlobalSightLocale.class, targetLocal.getIdAsLong());
            if (iterator.hasNext())
            {
                sb.append(targetLocal.getDisplayName(p_uiLocale) + ", ");
            }
            else
            {
                sb.append(targetLocal.getDisplayName(p_uiLocale));
            }
        }

        // dbSession.close();

        messageArgs[2] = sb.toString();
        messageArgs[3] = p_user.getUserName();
        messageArgs[4] = (String) p_request
                .getAttribute(JobManagementHandler.FINAL_REVENUE);
        messageArgs[5] = p_po;
        messageArgs[6] = makeUrlToJobDetail(p_request, p_job);

        return messageArgs;
    }

    /**
     * Makes a link to go to job detail page directly.
     * 
     * @param p_request
     * @param p_job
     * 
     * @return the url
     */
    private String makeUrlToJobDetail(HttpServletRequest p_request, Job p_job)
    {
        StringBuilder sb = new StringBuilder("");
        sb.append(getCapLoginUrl());
        sb.append("?");
        sb.append(WebAppConstants.LOGIN_FROM);
        sb.append("=");
        sb.append(WebAppConstants.LOGIN_FROM_EMAIL);
        sb.append("&");
        sb.append(WebAppConstants.LOGIN_NAME_FIELD);
        sb.append("=");
        sb.append(p_request.getAttribute(WebAppConstants.LOGIN_NAME_FIELD));
        sb.append("&");
        sb.append(WebAppConstants.LOGIN_FORWARD_URL);
        sb.append("=");
        String forwardUrl = "/ControlServlet?linkName=jobCosts&pageName=COSTS&jobId="
                + p_job.getId();
        forwardUrl = URLEncoder.encode(forwardUrl, "UTF-8");
        sb.append(forwardUrl);

        return sb.toString();
    }

    /**
     * Gets GlobalSight login URL.
     * 
     * @return the URL.
     */
    private String getCapLoginUrl()
    {
        SystemConfiguration config = SystemConfiguration.getInstance();
        return config.getStringParameter(SystemConfigParamNames.CAP_LOGIN_URL);
    }

    /**
     * For "Quote process webEx" issue Update the Quote PO Number.
     * 
     * @param p_job
     *            specifies the job to be updated
     * @param p_quotePoNumber
     *            specifies the quote PO Number to be updated This method is
     *            used to update the quote PO Number in database
     */
    private void updateQuotePoNumber(Job p_job, String p_quotePoNumber)
    {
        try
        {
            ServerProxy.getJobHandler().updateQuotePoNumber(p_job,
                    p_quotePoNumber);
        }
        catch (Exception e)
        {
            CATEGORY.error("Problem updating quotation email date ", e);
        }
    }

    private boolean hasReadyWorkflow(Job job)
    {
        Collection<Workflow> workflows = job.getWorkflows();
        if (workflows != null)
        {
            for (Workflow workflow : workflows)
            {
                if (Job.READY_TO_BE_DISPATCHED.equals(workflow.getState()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private String getReadyWorkflowIds(Job job)
    {
        StringBuffer ids = new StringBuffer();
        Collection<Workflow> workflows = job.getWorkflows();
        if (workflows != null)
        {
            for (Workflow workflow : workflows)
            {
                if (Job.READY_TO_BE_DISPATCHED.equals(workflow.getState()))
                {
                    if (ids.length() > 0)
                    {
                        ids.append(",");
                    }
                    ids.append(workflow.getId());
                }
            }
        }

        return ids.toString();
    }

    private void updateAuthoriserUser(Job p_job, User user)
    {
        try
        {
            ServerProxy.getJobHandler().updateAuthoriserUser(p_job, user);
        }
        catch (Exception e)
        {
            CATEGORY.error(
                    "Problem updating user who approved the cost of job.", e);
        }
    }

    /**
     * For "Quote process webEx" issue Update the Quote Approved date
     * 
     * @param p_job
     *            specifies the job to be updated
     * @param p_quoteApprovedDate
     *            specifies the quote date to be updated This method is used to
     *            update the quote approved date in database
     */
    private void updateQuoteApprovedDate(Job p_job, String p_quoteApprovedDate)
    {
        try
        {
            ServerProxy.getJobHandler().updateQuoteApprovedDate(p_job,
                    p_quoteApprovedDate);
        }
        catch (Exception e)
        {
            CATEGORY.error("Problem updating quotation email date ", e);
        }
    }
}
