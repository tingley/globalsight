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
package com.globalsight.everest.webapp.pagehandler.terminology.maintenance;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.util.GlobalSightLocale;

public class TermSearchHandler extends PageActionHandler implements
        WebAppConstants
{
    @ActionHandler(action = TERMBASE_ACTION_TERM_SEARCH, formClass = "")
    public void tmSearch(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        HttpSession session = request.getSession(false);
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        ResourceBundle bundle = PageHandler.getBundle(session);
        String userId = getUser(session).getUserId();

        TermSearchHandlerHelper.setLabel(request, bundle);
        // set locales
        TermSearchHandlerHelper.setLocales(request);
        // set tbs
        TermSearchHandlerHelper.setTBs(request, userId, uiLocale);
        // Set permission
        PermissionSet userPerms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        boolean hasTMSearchPermission = userPerms
                .getPermissionFor(Permission.TM_SEARCH)
                || userPerms.getPermissionFor(Permission.ACTIVITIES_TM_SEARCH);
        request.setAttribute("hasTMSearchPermission", hasTMSearchPermission);
        // From TM Search page
        String fromTMSearchPage = (String) request
                .getParameter("fromTMSearchPage");
        if (fromTMSearchPage != null)
        {
            request.setAttribute("fromTMSearchPage", fromTMSearchPage);
            String sourceLocale = (String) request.getParameter("sourceLocale");
            request.setAttribute("sourceLocale", sourceLocale);
            String targetLocale = (String) request.getParameter("targetLocale");
            request.setAttribute("targetLocale", targetLocale);
            String searchText = (String) request.getParameter("searchText");
            searchText = iso88591ToUtf8(searchText);
            request.setAttribute("searchText", searchText);
        }
    }

    @ActionHandler(action = TERMBASE_ACTION_SEARCH, formClass = "")
    public void search(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        // get parameters
        HttpSession session = request.getSession();
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        String sourceLocaleId = (String) request.getParameter("sourceLocale");
        String targetLocaleId = (String) request.getParameter("targetLocale");
        String searchText = (String) request.getParameter("searchText");
        String matchType = (String) request.getParameter("matchType");
        String selectedTBs = request.getParameter("tbs");

        LocaleManager lm = ServerProxy.getLocaleManager();

        GlobalSightLocale sourceGSL = lm.getLocaleById(Long
                .parseLong(sourceLocaleId));
        GlobalSightLocale targetGSL = lm.getLocaleById(Long
                .parseLong(targetLocaleId));

        String sourceLocale = sourceGSL.toString();
        String targetLocale = targetGSL.toString();
        String[] termbaseNames = selectedTBs.split(",");

        String result = TermSearchHandlerHelper.search(request, uiLocale,
                termbaseNames, searchText, sourceLocale, targetLocale,
                matchType);

        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.write(result);
        if (out != null)
        {
            out.close();
        }
        pageReturn();
    }

    @ActionHandler(action = TERMBASE_ACTION_TERM_SEARCH_PAGING, formClass = "")
    public void refreshPage(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String result = TermSearchHandlerHelper.refreshPage(request);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.write(result);
        if (out != null)
        {
            out.close();
        }
        pageReturn();
    }

    @ActionHandler(action = TERMBASE_ACTION_TERM_SEARCH_ORDERING, formClass = "")
    public void refreshOrder(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String result = TermSearchHandlerHelper.refreshOrder(request);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.write(result);
        if (out != null)
        {
            out.close();
        }
        pageReturn();
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        // TODO Auto-generated method stub

    }
}
