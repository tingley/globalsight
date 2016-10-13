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

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.tm.searchreplace.JobSearchReportQueryResult;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.GlobalSightLocale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import java.rmi.RemoteException;

public class SearchJobsResultsHandler
    extends PageHandler
{
    /**
     * Invokes this EntryPageHandler object
     *
     * @param pageDescriptor the description of the page to be produced.
     * @param request original request sent from the browser.
     * @param response original response object.
     * @param context the Servlet context.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
        HttpServletRequest request, HttpServletResponse response,
        ServletContext context)
        throws ServletException, IOException, RemoteException,
               EnvoyServletException
    {
        HttpSession session = (HttpSession)request.getSession(false);
        SessionManager sessionMgr = (SessionManager)
            session.getAttribute(WebAppConstants.SESSION_MANAGER);

        List targetLocales = getTargetLocales(request, sessionMgr);
        boolean isCaseSensitive = getCaseSensitive(request, sessionMgr);
        String queryString = getQueryString(request, sessionMgr);
        List jobIds = (List) sessionMgr.getAttribute("jobIds");

        List results = null;

        JobSearchReportQueryResult queryResults =
            SearchHandlerHelper.searchJobs(isCaseSensitive,
                queryString, targetLocales, jobIds);

        if (queryResults != null)
        {
            results = (List)queryResults.getJobInfos();
        }

        sessionMgr.setAttribute("searchResults", results);

        String action = (String)request.getParameter("action");
        if (!"self".equals(action))
        {
            reset_table_navigation(sessionMgr);
            sessionMgr.removeElement("searchnumPerPage");
        }

        // get target locales and store in hastable for quick look up
        // in the jsp
        ArrayList locales = (ArrayList)sessionMgr.getAttribute("targetLocales");
        Hashtable hash = new Hashtable(locales.size());
        for (int i = 0; i < locales.size(); i++)
        {
            GlobalSightLocale locale = (GlobalSightLocale)locales.get(i);
            hash.put(new Long(locale.getId()), locale);
        }
        sessionMgr.setAttribute("localeHash", hash);

        Locale locale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
        setTableNavigation(request, session,
            results,
            null,
            10,   // change this to be configurable!
            "searchnumPerPage",
            "searchnumPages", "results",
            "searchsorting",
            "searchreverseSort",
            "searchpageNum",
            "searchlastPageNum",
            "searchlistSize");

        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    private List getTargetLocales(HttpServletRequest request,
        SessionManager sessionMgr)
        throws EnvoyServletException
    {
        String localesString = (String)request.getParameter("selectedLocales");
        if (localesString != null)
        {
            String[] locales = localesString.split(" ");
            List targetLocales =  Arrays.asList(locales);
            sessionMgr.setAttribute("selectedLocales", targetLocales);
            return targetLocales;
        }
        else
        {
            // get it from session
            return (List)sessionMgr.getAttribute("selectedLocales");
        }
    }

    private boolean getCaseSensitive(HttpServletRequest request,
        SessionManager sessionMgr)
        throws EnvoyServletException
    {
        String isCaseSensitive = (String)request.getParameter("isCaseSensitive");
        if (isCaseSensitive != null)
        {
            sessionMgr.setAttribute("isCaseSensitive", isCaseSensitive);
        }
        else
        {
            isCaseSensitive = (String)
                sessionMgr.getAttribute("isCaseSensitive");
        }

        return new Boolean(isCaseSensitive).booleanValue();
    }

    private String getQueryString(HttpServletRequest request,
        SessionManager sessionMgr)
        throws EnvoyServletException
    {
        String queryString = (String)request.getParameter("queryString");
        if (queryString != null)
        {
            queryString = EditUtil.utf8ToUnicode(queryString);
            sessionMgr.setAttribute("queryString", queryString);
        }
        else
        {
            queryString = (String)sessionMgr.getAttribute("queryString");
        }

        return queryString;
    }

    private void reset_table_navigation(SessionManager sessionMgr)
    {
        sessionMgr.removeElement("searchnumPerPage");
        sessionMgr.removeElement("searchnumPages");
        sessionMgr.removeElement("searchsorting");
        sessionMgr.removeElement("searchreverseSort");
        sessionMgr.removeElement("searchpageNum");
        sessionMgr.removeElement("searchlastPageNum");
        sessionMgr.removeElement("searchlistSize");
    }
}
