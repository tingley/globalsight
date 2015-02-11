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

package com.globalsight.everest.webapp.pagehandler.administration.config.uilocale;

import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

/**
 * Deals with some web requirements about ui locale
 */
public class UILocaleBasicHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(UILocaleBasicHandler.class);

    private ThreadLocal<Long> ID = new ThreadLocal<Long>();

    /**
     * Handle add ui locale request
     * 
     * @param request
     * @param response
     * @param form
     * @throws Exception
     */
    @ActionHandler(action = UILocaleConstant.ADD, formClass = "")
    public void add(HttpServletRequest request, HttpServletResponse response,
            Object form) throws Exception
    {
        HttpSession session = request.getSession(false);
        String action = request.getParameter("action");
        // gbs-1389: restrict direct access to create language configuration
        // without "New" permission
        PermissionSet userPerms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        if (!userPerms.getPermissionFor(Permission.UILOCALE_NEW))
        {
            request.setAttribute("restricted_access", true);
            if (userPerms.getPermissionFor(Permission.UILOCALE_VIEW))
            {
                response.sendRedirect("/globalsight/ControlServlet?activityName=uiLocaleConfiguration");
            }
            else
            {
                response.sendRedirect(request.getContextPath());
            }
            return;
        }

        try
        {
            if (action.equals(UILocaleConstant.ADD))
            {
                setValidLocales(session, request);
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
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
    }

    /**
     * Set valid locales in the request
     */
    private void setValidLocales(HttpSession p_session,
            HttpServletRequest p_request) throws NamingException,
            RemoteException, GeneralException
    {
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);
        Vector sources = UILocaleManager.getAvailableLocales();
        Vector srcsCopy = (Vector) sources.clone();
        // String availableLocales = "en_US,fr_FR,de_DE,es_ES,ja_JP";
        List<String> addedLocales = UILocaleManager.getSystemUILocaleStrings();

        for (int i = srcsCopy.size() - 1; i >= 0; i--)
        {
            GlobalSightLocale gsl = (GlobalSightLocale) srcsCopy.get(i);
            String gslStr = gsl.toString();

            if (addedLocales.contains(gslStr))
            {
                sources.remove(i);
            }
        }

        SortUtil.sort(sources, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((GlobalSightLocale) o1).getDisplayName(Locale.US)
                        .compareToIgnoreCase(
                                ((GlobalSightLocale) o2)
                                        .getDisplayName(Locale.US));
            }
        });
        p_request.setAttribute(UILocaleConstant.AVAILABLE_UILOCALES, sources);
    }
}
