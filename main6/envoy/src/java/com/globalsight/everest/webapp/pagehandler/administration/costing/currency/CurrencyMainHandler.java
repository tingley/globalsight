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
import java.util.Locale;
import java.util.ResourceBundle;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.IsoCurrency;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.CurrencyComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;

public class CurrencyMainHandler extends PageHandler implements
        CurrencyConstants
{
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
        Currency pivot = CurrencyHandlerHelper.getPivotCurrency();
        String action = p_request.getParameter("action");

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
            clearSessionExceptTableInfo(session, CURRENCY_KEY);
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
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, currencies,
                new CurrencyComparator(uiLocale), 10, CURRENCY_LIST,
                CURRENCY_KEY);

        // Set pivot for enabling/disabling edit button in UI
        if (p_pivot != null)
            p_request.setAttribute("pivot", p_pivot.getIsoCode());
        else
            p_request.setAttribute("pivot", "");
    }
}
