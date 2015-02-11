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
package com.globalsight.everest.webapp.pagehandler.administration.config.teamsite;

// Envoy packages
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

/**
 * TeamSiteBranchNew2Handler, A page handler to launch the new teamsite page.
 * <p>
 * 
 * @version 1.0
 * @see com.globalsight.everest.webapp.pagehandler.PageHandler
 */

public class TeamSiteBranchNew2Handler extends PageHandler
{
    /**
     * Invokes this PageHandler
     * 
     * @param p_thePageDescriptor
     *            the page desciptor
     * @param p_theRequest
     *            the original request sent from the browser
     * @param p_theResponse
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_thePageDescriptor,
            HttpServletRequest p_theRequest, HttpServletResponse p_theResponse,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        dispatchJSP(p_thePageDescriptor, p_theRequest, p_theResponse, p_context);
        super.invokePageHandler(p_thePageDescriptor, p_theRequest,
                p_theResponse, p_context);
    }

    /**
     * Invoke the correct JSP for this page
     */
    protected void dispatchJSP(WebPageDescriptor p_thePageDescriptor,
            HttpServletRequest p_theRequest, HttpServletResponse p_theResponse,
            ServletContext p_context) throws ServletException,
            EnvoyServletException, IOException
    {
        // set up locale list
        Vector vec = new Vector();
        Vector targetlocales = null;

        Locale uiLocale = (Locale) p_theRequest.getSession().getAttribute(
                WebAppConstants.UILOCALE);
        // get all target locales
        try
        {
            ArrayList al = new ArrayList(ServerProxy.getLocaleManager()
                    .getAllTargetLocales());
            GlobalSightLocaleComparator comp = new GlobalSightLocaleComparator(
                    GlobalSightLocaleComparator.DISPLAYNAME, uiLocale);
            SortUtil.sort(al, comp);
            targetlocales = new Vector(al);

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

        // for each target locale, get language and country to display
        String targetLocaleStr;
        long locale_id;
        GlobalSightLocale locale;
        for (int i = 0; i < targetlocales.size(); i++)
        {
            locale = (GlobalSightLocale) targetlocales.elementAt(i);
            locale_id = locale.getId();
            targetLocaleStr = String.valueOf(locale_id);
            vec.addElement(targetLocaleStr);
            vec.addElement(locale.getDisplayLanguage() + " / "
                    + locale.getDisplayCountry());
        }

        // notify session of Target languages
        HttpSession session = p_theRequest.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        sessionMgr.setAttribute(TeamSiteBranchMainHandler.TARGET_LOCALES, vec);

        // gather the user input regarding selected source branch
        if (p_theRequest
                .getParameter(TeamSiteBranchMainHandler.TEAMSITE_SOURCE_BRANCH) != null)
        {
            sessionMgr
                    .setAttribute(
                            TeamSiteBranchMainHandler.TEAMSITE_SOURCE_BRANCH,
                            p_theRequest
                                    .getParameter(TeamSiteBranchMainHandler.TEAMSITE_SOURCE_BRANCH));
        }
    }
}
