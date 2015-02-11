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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.CurrencyComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.costing.currency.CurrencyHandlerHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.SortUtil;

/**
 * Pagehandler for the new/edit Rate page
 */
public class RateBasicHandler extends PageHandler
{
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
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        try
        {
            initCommonData(session, p_request);
            if (action.equals(RateConstants.CREATE))
            {
                // get data for combo boxes in jsp
                p_request.setAttribute(RateConstants.ACTIVITIES,
                        RateHandlerHelper.getAllActivities(uiLocale));
                p_request.setAttribute(RateConstants.LPS,
                        RateHandlerHelper.getAllLocalePairs(uiLocale));
                FormUtil.addSubmitToken(p_request, FormUtil.Forms.NEW_RATE);
            }
            else if (action.equals(RateConstants.EDIT))
            {
                // Fetch the currency to edit and store in session
                SessionManager sessionMgr = (SessionManager) session
                        .getAttribute(WebAppConstants.SESSION_MANAGER);
                String rateId = (String) p_request
                        .getParameter(RateConstants.RATE_ID);
                if (rateId == null
                        || p_request.getMethod().equalsIgnoreCase(
                                WebAppConstants.REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=rate");
                    return;
                }
                sessionMgr.setAttribute(RateConstants.RATE,
                        RateHandlerHelper.getRate(Long.parseLong(rateId)));

                p_request.setAttribute("edit", "true");
            }

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
     * Set common data for edit/new in the request. This is the list of
     * currencies, the list of rate types, and the pivot currency.
     */
    private void initCommonData(HttpSession p_session,
            HttpServletRequest p_request) throws NamingException,
            RemoteException, GeneralException
    {
        ArrayList allRates = (ArrayList) RateHandlerHelper.getAllRates();
        List<String> rateNames = new ArrayList<String>();
        for (int i = 0; i < allRates.size(); i++)
        {
            String rateName = ((Rate) allRates.get(i)).getName();
            rateNames.add(rateName);
        }
        // fix for GBS-1693
        ArrayList<Currency> currencies = new ArrayList<Currency>();
        Collection currenciesCollection = RateHandlerHelper.getAllCurrencies();
        Iterator it = currenciesCollection.iterator();
        while (it.hasNext())
        {
            currencies.add((Currency) it.next());
        }

        SortUtil.sort(currencies, new CurrencyComparator(Locale.getDefault()));
        p_request.setAttribute(RateConstants.RATE_NAMES, rateNames);
        p_request.setAttribute(RateConstants.CURRENCIES, currencies);
        p_request.setAttribute(RateConstants.RATES, getRateTypes(p_session));
        Currency pivot = CurrencyHandlerHelper.getPivotCurrency();
        if (pivot != null)
            p_request.setAttribute("pivot", pivot.getDisplayName());
    }

    // Get a List of rate types.
    private ArrayList getRateTypes(HttpSession p_session)
    {
        ResourceBundle bundle = getBundle(p_session);
        Integer[] rateTypes = Rate.getRateTypes();
        ArrayList rates = new ArrayList(rateTypes.length);
        for (int i = 0; i < rateTypes.length; i++)
        {
            Integer type = rateTypes[i];
            rates.add(bundle.getString("lb_rate_type_" + type));
        }
        return rates;
    }
}
