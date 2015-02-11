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
package com.globalsight.everest.webapp.pagehandler.administration.costing.rate;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.RateComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;

public class RateMainHandler extends PageHandler
{

    private static final Logger logger = Logger
            .getLogger(RateMainHandler.class);

    /**
     * Invokes this PageHandler
     * 
     * @param pageDescriptor
     *            the page desciptor
     * @param request
     *            the original request sent from the browser
     * @param response
     *            the original response object
     * @param context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        try
        {
            if (RateConstants.CREATE.equals(action))
            {
                if (FormUtil.isNotDuplicateSubmisson(p_request,
                        FormUtil.Forms.NEW_RATE))
                {
                    createRate(p_request, session);
                }
            }
            else if (RateConstants.EDIT.equals(action))
            {
                editRate(p_request, session);
            }
            else if (RateConstants.REMOVE.equals(action))
            {
                removeRate(p_request, session);
            }
            else
            {
                // make sure required objects are there before allowing
                // user to create a Rate
                checkPreReqData(p_request, session);
            }

			String rateNameFilterValue = (String) sessionMgr
					.getAttribute(RateConstants.FILTER_RATE_NAME);
			String rateCompanyFilterValue = (String) sessionMgr
					.getAttribute(RateConstants.FILTER_RATE_COMPANY);
			String sourceLocaleValue = (String) sessionMgr
					.getAttribute(RateConstants.FILTER_RATE_SOURCE_LOCALE);
			String targetLocaleValue = (String) sessionMgr
					.getAttribute(RateConstants.FILTER_RATE_TARGET_LOCALE);

			clearSessionExceptTableInfo(session, RateConstants.RATE_KEY);

			sessionMgr.setAttribute(RateConstants.FILTER_RATE_NAME,
					rateNameFilterValue);
			sessionMgr.setAttribute(RateConstants.FILTER_RATE_COMPANY,
					rateCompanyFilterValue);
			sessionMgr.setAttribute(RateConstants.FILTER_RATE_SOURCE_LOCALE,
					sourceLocaleValue);
			sessionMgr.setAttribute(RateConstants.FILTER_RATE_TARGET_LOCALE,
					targetLocaleValue);

            dataForTable(p_request, session);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ne);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ge);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    /**
     * Create a new rate in the DB.
     */
    private void createRate(HttpServletRequest p_request, HttpSession p_session)
            throws RemoteException, NamingException, GeneralException
    {
        String[] localPairsID = p_request.getParameterValues("lp");
        String rateBasicName = p_request.getParameter("rateName");
        String rateName = "";
        Rate newRate = null;

        if (localPairsID.length == 1)
        {
            newRate = new Rate();
            getRequestParams(p_request, newRate, false, null);
            RateHandlerHelper.addRate(newRate);
        }
        else
        {
            Locale uiLocale = (Locale) p_request.getSession().getAttribute(
                    WebAppConstants.UILOCALE);
            Vector localePairs = (Vector) RateHandlerHelper
                    .getAllLocalePairs(uiLocale);
            for (int i = 0; i < localPairsID.length; i++)
            {
                Map map = new HashMap();
                newRate = new Rate();
                for (int j = 0; j < localePairs.size(); j++)
                {
                    LocalePair lp = (LocalePair) localePairs.get(j);
                    if (lp.getId() == Long.parseLong(localPairsID[i]))
                    {
                        rateName = rateBasicName + "_"
                                + lp.getSource().toString() + "_"
                                + lp.getTarget().toString();
                        rateName = rateName.trim();
                        break;
                    }
                }
                map.put(RateConstants.RATE_NAME, rateName);
                map.put(RateConstants.RATE_LOCALEPAIR, localPairsID[i]);
                getRequestParams(p_request, newRate, false, map);
                RateHandlerHelper.addRate(newRate);
            }
        }
    }

    /**
     * Update a rate in the DB.
     */
    private void editRate(HttpServletRequest p_request, HttpSession p_session)
            throws RemoteException, NamingException, GeneralException
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        Rate rate = (Rate) sessionMgr.getAttribute(RateConstants.RATE);
        getRequestParams(p_request, rate, true, null);
        RateHandlerHelper.modifyRate(rate);
    }

    private void removeRate(HttpServletRequest p_request, HttpSession p_session)
            throws RemoteException, NamingException, GeneralException
    {
        String rateId = (String) p_request.getParameter(RateConstants.RATE_ID);
        if (rateId == null
                || p_request.getMethod().equalsIgnoreCase(
                        WebAppConstants.REQUEST_METHOD_GET))
        {
            return;
        }

        RateHandlerHelper.removeRate(Long.parseLong(rateId));
    }

    /**
     * Update the Rate object with the request parameter values.
     */
    private void getRequestParams(HttpServletRequest p_request, Rate p_rate,
            boolean edit, Map map) throws RemoteException, NamingException,
            GeneralException
    {
        String name = p_request.getParameter("rateName");

        if (!edit)
        {
            String activityName = p_request.getParameter("activity");
            Activity act = (Activity) ServerProxy.getJobHandler().getActivity(
                    activityName);
            p_rate.setActivity(act);

            String localePairId;
            if (null == map || map.size() < 1)
            {
                localePairId = p_request.getParameter("lp");
            }
            else
            {
                localePairId = (String) map.get(RateConstants.RATE_LOCALEPAIR);
                name = (String) map.get(RateConstants.RATE_NAME);
            }
            LocalePair pair = (LocalePair) ServerProxy.getLocaleManager()
                    .getLocalePairById(Long.parseLong(localePairId));
            p_rate.setLocalePair(pair);

        }

        p_rate.setName(name);
        String isoCode = p_request.getParameter("currency");
        Currency currency = ServerProxy.getCostingEngine().getCurrency(isoCode);
        p_rate.setCurrency(currency);
        Integer rateType = Integer.valueOf(p_request.getParameter("rateType"));
        p_rate.setRateType(rateType);
        if (rateType.equals(Rate.UnitOfWork.WORD_COUNT))
        {
            p_rate.setInContextMatchRate(Float.parseFloat(p_request
                    .getParameter("inContextExact")));
            // p_rate.setContextMatchRate(Float.parseFloat(p_request.getParameter("context")));
            p_rate.setContextMatchRate(Float.parseFloat(p_request
                    .getParameter("inContextExact")));
            p_rate.setSegmentTmRate(Float.parseFloat(getValue(p_request
                    .getParameter("exact"))));
            p_rate.setLowFuzzyMatchRate(Float.parseFloat(getValue(p_request
                    .getParameter("band4"))));
            p_rate.setMedFuzzyMatchRate(Float.parseFloat(getValue(p_request
                    .getParameter("band3"))));
            p_rate.setMedHiFuzzyMatchRate(Float.parseFloat(getValue(p_request
                    .getParameter("band2"))));
            p_rate.setHiFuzzyMatchRate(Float.parseFloat(getValue(p_request
                    .getParameter("band1"))));
            p_rate.setNoMatchRate(Float.parseFloat(p_request
                    .getParameter("nomatch")));
            p_rate.setRepetitionRate(Float.parseFloat(p_request
                    .getParameter("repetition")));
            // p_rate.setFuzzyMatchRate(Float.parseFloat(getValue(p_request.getParameter("leverage"))));
        }
        else if (rateType.equals(Rate.UnitOfWork.FIXED))
        {
            p_rate.setUnitRate(Float.parseFloat(p_request.getParameter("fixed")));
        }
        else if (rateType.equals(Rate.UnitOfWork.HOURLY))
        {
            p_rate.setUnitRate(Float.parseFloat(p_request
                    .getParameter("hourly")));
        }
        else if (rateType.equals(Rate.UnitOfWork.PAGE_COUNT))
        {
            p_rate.setUnitRate(Float.parseFloat(p_request.getParameter("page")));
        }
        else if (rateType.equals(Rate.UnitOfWork.WORD_COUNT_BY))
        {
            p_rate.setInContextMatchRate(Float.parseFloat(getValue(p_request
                    .getParameter("inContextExactC"))));
            // p_rate.setContextMatchRate(Float.parseFloat(p_request.getParameter("contextC")));
            p_rate.setContextMatchRate(Float.parseFloat(getValue(p_request
                    .getParameter("inContextExactC"))));
            p_rate.setSegmentTmRate(Float.parseFloat(getValue(p_request
                    .getParameter("exactC"))));
            p_rate.setLowFuzzyMatchRate(Float.parseFloat(getValue(p_request
                    .getParameter("band4C"))));
            p_rate.setMedFuzzyMatchRate(Float.parseFloat(getValue(p_request
                    .getParameter("band3C"))));
            p_rate.setMedHiFuzzyMatchRate(Float.parseFloat(getValue(p_request
                    .getParameter("band2C"))));
            p_rate.setHiFuzzyMatchRate(Float.parseFloat(getValue(p_request
                    .getParameter("band1C"))));
            p_rate.setNoMatchRate(Float.parseFloat(getValue(p_request
                    .getParameter("baserate"))));
            p_rate.setRepetitionRate(Float.parseFloat(getValue(p_request
                    .getParameter("repetitionC"))));

            p_rate.setInContextMatchRatePer(Float.parseFloat(p_request
                    .getParameter("inContextExactPer")));
            // p_rate.setContextMatchRatePer(Float.parseFloat(p_request.getParameter("contextPer")));
            p_rate.setContextMatchRatePer(Float.parseFloat(p_request
                    .getParameter("inContextExactPer")));
            p_rate.setSegmentTmRatePer(Float.parseFloat(p_request
                    .getParameter("exactPer")));
            p_rate.setLowFuzzyMatchRatePer(Float.parseFloat(p_request
                    .getParameter("band4Per")));
            p_rate.setMedFuzzyMatchRatePer(Float.parseFloat(p_request
                    .getParameter("band3Per")));
            p_rate.setMedHiFuzzyMatchRatePer(Float.parseFloat(p_request
                    .getParameter("band2Per")));
            p_rate.setHiFuzzyMatchRatePer(Float.parseFloat(p_request
                    .getParameter("band1Per")));
            p_rate.setRepetitionRatePer(Float.parseFloat((p_request
                    .getParameter("repetitionPer"))));
        }
    }

    /**
     * Before being able to create a Rate, certain objects must exist. Check
     * that here.
     */
    private void checkPreReqData(HttpServletRequest p_request,
            HttpSession p_session) throws EnvoyServletException
    {
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);
        Vector allActivities = RateHandlerHelper.getAllActivities(uiLocale);
        Vector allLocalePairs = RateHandlerHelper.getAllLocalePairs(uiLocale);
        Vector allCurrencies = RateHandlerHelper.getAllCurrencies();

        if (allActivities == null || allActivities.size() < 1
                || allLocalePairs == null || allLocalePairs.size() < 1
                || allCurrencies == null || allCurrencies.size() < 1)
        {
            ResourceBundle bundle = getBundle(p_session);
            StringBuffer message = new StringBuffer();
            boolean addcomma = false;
            message.append(bundle.getString("msg_prereq_warning_1"));
            message.append(":  ");
            if (allActivities == null || allActivities.size() < 1)
            {
                message.append(bundle.getString("lb_activity_type"));
                addcomma = true;
            }
            if (allLocalePairs == null || allLocalePairs.size() < 1)
            {
                if (addcomma)
                    message.append(", ");
                message.append(bundle.getString("lb_locale_pair"));
                addcomma = true;
            }
            if (allCurrencies == null || allCurrencies.size() < 1)
            {
                if (addcomma)
                    message.append(", ");
                message.append(bundle.getString("lb_currency"));
            }
            message.append(".  ");
            message.append(bundle.getString("msg_prereq_warning_2"));

            p_request.setAttribute("preReqData", message.toString());
        }
    }

    /**
     * Get list of rates.
     */
    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session) throws RemoteException, NamingException,
            GeneralException
    {
        ArrayList allRates = (ArrayList) RateHandlerHelper.getAllRates();
        // Filter rates by company name
        filterRatesByCompanyName(p_request, p_session, allRates);
        // Filter rates by rate name
        filterRatesByRateName(p_request, p_session, allRates);
        // Filter rates by source locale
        filterRatesBySourceLocale(p_request, p_session, allRates);
        // Filter rates by target locale
        filterRatesByTargetLocale(p_request, p_session, allRates);

        int numPerPage = getNumPerPage(p_request, p_session);
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, allRates, new RateComparator(
                uiLocale, getBundle(p_session)), numPerPage,
                RateConstants.RATE_LIST, RateConstants.RATE_KEY);
    }

    private String getValue(String p_value)
    {
        if (p_value.trim().equals(""))
            return "0.0";
        return p_value;
    }

    private int getNumPerPage(HttpServletRequest p_request,
            HttpSession p_session)
    {
        int result = 10;

        SessionManager sessionManager = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        String rateNumPerPage = p_request.getParameter("numOfPageSize");
        if (StringUtil.isEmpty(rateNumPerPage))
        {
            rateNumPerPage = (String) sessionManager.getAttribute("rateNumPerPage");
        }

        if (rateNumPerPage != null)
        {
            sessionManager.setAttribute("rateNumPerPage", rateNumPerPage.trim());
            if ("all".equalsIgnoreCase(rateNumPerPage))
            {
                result = Integer.MAX_VALUE;
            }
            else
            {
                try
                {
                    result = Integer.parseInt(rateNumPerPage);
                }
                catch (NumberFormatException ignore)
                {
                    result = 10;
                }
            }
        }
        
        return result;
    }

    @SuppressWarnings("rawtypes")
    private void filterRatesByCompanyName(HttpServletRequest p_request,
            HttpSession p_session, ArrayList p_rates)
    {
        SessionManager sessionManager = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        String rateCompanyFilterValue = p_request
                .getParameter(RateConstants.FILTER_RATE_COMPANY);
        if (rateCompanyFilterValue == null)
        {
            rateCompanyFilterValue = (String) sessionManager
                    .getAttribute(RateConstants.FILTER_RATE_COMPANY);
        }
        if (rateCompanyFilterValue == null)
        {
            rateCompanyFilterValue = "";
        }
        sessionManager.setAttribute(RateConstants.FILTER_RATE_COMPANY,
                rateCompanyFilterValue.trim());

        if (!StringUtil.isEmpty(rateCompanyFilterValue))
        {
            for (Iterator it = p_rates.iterator(); it.hasNext();)
            {
                Rate rate = (Rate) it.next();
                String comName = CompanyWrapper.getCompanyNameById(
                        rate.getActivity().getCompanyId()).toLowerCase();
                if (comName.indexOf(rateCompanyFilterValue.trim().toLowerCase()) == -1)
                {
                    it.remove();
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void filterRatesByRateName(HttpServletRequest p_request,
            HttpSession p_session, ArrayList p_rates)
    {
        SessionManager sessionManager = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        String rateNameFilterValue = p_request
                .getParameter(RateConstants.FILTER_RATE_NAME);
        if (rateNameFilterValue == null)
        {
            rateNameFilterValue = (String) sessionManager
                    .getAttribute(RateConstants.FILTER_RATE_NAME);
        }
        if (rateNameFilterValue == null)
        {
            rateNameFilterValue = "";
        }
        sessionManager.setAttribute(RateConstants.FILTER_RATE_NAME,
                rateNameFilterValue.trim());

        if (!StringUtil.isEmpty(rateNameFilterValue))
        {
            for (Iterator it = p_rates.iterator(); it.hasNext();)
            {
                Rate rate = (Rate) it.next();
                if (rate.getName().toLowerCase()
                        .indexOf(rateNameFilterValue.trim().toLowerCase()) == -1)
                {
                    it.remove();
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void filterRatesBySourceLocale(HttpServletRequest p_request,
            HttpSession p_session, ArrayList p_rates)
    {
        SessionManager sessionManager = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);
        
        String sourceLocaleValue = p_request
                .getParameter(RateConstants.FILTER_RATE_SOURCE_LOCALE);
        if (sourceLocaleValue == null)
        {
            sourceLocaleValue = (String) sessionManager
                    .getAttribute(RateConstants.FILTER_RATE_SOURCE_LOCALE);
        }
        if (sourceLocaleValue == null)
        {
            sourceLocaleValue = "";
        }
        sessionManager.setAttribute(RateConstants.FILTER_RATE_SOURCE_LOCALE,
                sourceLocaleValue.trim());

        if (!StringUtil.isEmpty(sourceLocaleValue))
        {
            for (Iterator it = p_rates.iterator(); it.hasNext();)
            {
                Rate rate = (Rate) it.next();
                String srcLocale = rate.getLocalePair().getSource()
                        .getDisplayName(uiLocale).toLowerCase();
                if (srcLocale.indexOf(sourceLocaleValue.trim().toLowerCase()) == -1)
                {
                    it.remove();
                }
            }
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void filterRatesByTargetLocale(HttpServletRequest p_request,
            HttpSession p_session, ArrayList p_rates)
    {
        SessionManager sessionManager = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);
        
        String targetLocaleValue = p_request
                .getParameter(RateConstants.FILTER_RATE_TARGET_LOCALE);
        if (targetLocaleValue == null)
        {
            targetLocaleValue = (String) sessionManager
                    .getAttribute(RateConstants.FILTER_RATE_TARGET_LOCALE);
        }
        if (targetLocaleValue == null)
        {
            targetLocaleValue = "";
        }
        sessionManager.setAttribute(RateConstants.FILTER_RATE_TARGET_LOCALE,
                targetLocaleValue.trim());

        if (!StringUtil.isEmpty(targetLocaleValue))
        {
            for (Iterator it = p_rates.iterator(); it.hasNext();)
            {
                Rate rate = (Rate) it.next();
                String trgLocale = rate.getLocalePair().getTarget()
                        .getDisplayName(uiLocale).toLowerCase();
                if (trgLocale.indexOf(targetLocaleValue.trim().toLowerCase()) == -1)
                {
                    it.remove();
                }
            }
        }
    }
}
