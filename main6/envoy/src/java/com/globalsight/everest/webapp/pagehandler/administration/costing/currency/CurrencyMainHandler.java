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
package com.globalsight.everest.webapp.pagehandler.administration.costing.currency;

// java
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.IsoCurrency;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.CurrencyComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;

public class CurrencyMainHandler extends PageHandler implements
        CurrencyConstants
{
	static private int NUM_PER_PAGE = 10;
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
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        Currency pivot = CurrencyHandlerHelper.getPivotCurrency();
        String action = p_request.getParameter("action");

        setNumberOfPerPage(p_request);

        try
        {
            if (isPost(p_request))
            {
                if (CREATE.equals(action))
                {
                    createCurrency(p_request, session);
                }
                else if (EDIT.equals(action))
                {
                    editCurrency(p_request, session);
                }
            }
            else
            {
                checkPreReqData(p_request, session, pivot);
            }

            String currencyCompanyFilterValue = (String) sessionManager
                    .getAttribute(CurrencyConstants.FILTER_CURRENCY_COMPANY);
            String currencyNameFilterValue = (String) sessionManager
                    .getAttribute(CurrencyConstants.FILTER_CURRENCY_NAME);
            clearSessionExceptTableInfo(session, CURRENCY_KEY);
            sessionManager.setAttribute(
                    CurrencyConstants.FILTER_CURRENCY_COMPANY,
                    currencyCompanyFilterValue);
            sessionManager.setAttribute(CurrencyConstants.FILTER_CURRENCY_NAME,
                    currencyNameFilterValue);

            dataForTable(p_request, session, pivot);
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

    private void createCurrency(HttpServletRequest p_request,
            HttpSession p_session) throws RemoteException, NamingException,
            GeneralException, EnvoyServletException
    {
        // Get data for currency from request
        String code = (String) p_request.getParameter("displayCurr");
        String conversion = (String) p_request.getParameter("conversion");
        IsoCurrency iso = CurrencyHandlerHelper.getIsoCurrency(code);
        String companyId = CompanyThreadLocal.getInstance().getValue();

        // Create currency and add to db
        Currency currency = new Currency(iso, Float.parseFloat(conversion),
                Long.parseLong(companyId));
        CurrencyHandlerHelper.addOrModifyCurrency(currency);
    }

    private void editCurrency(HttpServletRequest p_request,
            HttpSession p_session) throws RemoteException, NamingException,
            GeneralException, EnvoyServletException
    {
        // Get currency to update
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        Currency currency = (Currency) sessionMgr.getAttribute(CURRENCY);

        // Get conversion factor and set in Currency
        String conversion = (String) p_request.getParameter("conversion");
        currency.setConversionFactor(Float.parseFloat(conversion));

        // Update db
        CurrencyHandlerHelper.addOrModifyCurrency(currency);
    }

    /**
     * Before being able to create a Currency, certain objects must exist. Check
     * that here.
     */
    private void checkPreReqData(HttpServletRequest p_request,
            HttpSession p_session, Currency p_pivot)
            throws EnvoyServletException
    {
        if (p_pivot == null)
        {
            ResourceBundle bundle = getBundle(p_session);
            String message = bundle.getString("msg_prereq_warning_1") + ":  "
                    + bundle.getString("lb_pivot_currency") + ".  "
                    + bundle.getString("msg_prereq_warning_2");
            p_request.setAttribute("preReqData", message);
        }
    }

    /**
     * Get list of currencies. Also set the pivot currency in the request.
     */
    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session, Currency p_pivot) throws RemoteException,
            NamingException, GeneralException
    { 
        ArrayList currencies = (ArrayList) CurrencyHandlerHelper
                .getAllCurrencies();
        // Filter currencies by company name
        filterCurrenciesByCompanyName(p_request, p_session, currencies);
        // Filter currencies by currencies name
        filterCurrenciesByCurrencyName(p_request, p_session, currencies);
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, currencies,
                new CurrencyComparator(uiLocale), NUM_PER_PAGE, CURRENCY_LIST,
                CURRENCY_KEY);

        // Set pivot for enabling/disabling edit button in UI
        if (p_pivot != null)
            p_request.setAttribute("pivot", p_pivot.getIsoCode());
        else
            p_request.setAttribute("pivot", "");
    }
   
    private void filterCurrenciesByCurrencyName(HttpServletRequest p_request,
            HttpSession p_session, ArrayList p_currency)
    {
        SessionManager sessionManager = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        String currencyNameFilterValue = p_request
                .getParameter(CurrencyConstants.FILTER_CURRENCY_NAME);
        if (currencyNameFilterValue == null)
        {
        	currencyNameFilterValue = (String) sessionManager
                    .getAttribute(CurrencyConstants.FILTER_CURRENCY_NAME);
        }
        if (currencyNameFilterValue == null)
        {
        	currencyNameFilterValue = "";
        }
        sessionManager.setAttribute(CurrencyConstants.FILTER_CURRENCY_NAME,
        		currencyNameFilterValue.trim());

        if (StringUtil.isNotEmpty(currencyNameFilterValue))
        {
            for (Iterator it = p_currency.iterator(); it.hasNext();)
            {
            	Currency currency = (Currency) it.next();
            	String name = currency.getDisplayName().toLowerCase();
                if (name.indexOf(currencyNameFilterValue.trim().toLowerCase()) == -1)
                {
                    it.remove();
                }
            }
        }
    }

    
    private void filterCurrenciesByCompanyName(HttpServletRequest p_request,
            HttpSession p_session, ArrayList p_currency)
    {
        SessionManager sessionManager = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        String companyFilterValue = p_request
                .getParameter(CurrencyConstants.FILTER_CURRENCY_COMPANY);
        if (companyFilterValue == null)
        {
            companyFilterValue = (String) sessionManager
                    .getAttribute(CurrencyConstants.FILTER_CURRENCY_COMPANY);
        }
        if (companyFilterValue == null)
        {
            companyFilterValue = "";
        }
        sessionManager.setAttribute(CurrencyConstants.FILTER_CURRENCY_COMPANY,
                companyFilterValue.trim());

        if (StringUtil.isNotEmpty(companyFilterValue))
        {
            for (Iterator it = p_currency.iterator(); it.hasNext();)
            {
                Currency currency = (Currency) it.next();
                String comName = CompanyWrapper.getCompanyNameById(
                        currency.getCompanyId()).toLowerCase();

                if (comName.indexOf(companyFilterValue.trim().toLowerCase()) == -1)
                {
                    it.remove();
                }
            }
        }
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
