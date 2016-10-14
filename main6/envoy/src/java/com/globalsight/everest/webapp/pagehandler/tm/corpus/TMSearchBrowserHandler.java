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
package com.globalsight.everest.webapp.pagehandler.tm.corpus;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.util.StringUtil;

public class TMSearchBrowserHandler extends PageActionHandler
{
    @ActionHandler(action = TM_ACTION_TM_SEARCH, formClass = "")
    public void tmSearch(HttpServletRequest request, HttpServletResponse response, Object form)
            throws Exception
    {
        HttpSession session = request.getSession(false);
        ResourceBundle bundle = PageHandler.getBundle(session);
        User user = getUser(session);
        String userId = user.getUserId();
        // GBS-3990
        Company company = CompanyWrapper.getCompanyByName(user.getCompanyName());
        request.setAttribute("enableBlankTmSearch", company.getEnableBlankTmSearch());

        TMSearchBroswerHandlerHelper.setLable(request, bundle);
        // set locales
        TMSearchBroswerHandlerHelper.setLocales(request);
        // set tms
        TMSearchBroswerHandlerHelper.setTMs(request, userId);
        // set all TMProfile
        TMSearchBroswerHandlerHelper.setTMProfiles(request, userId);
        // Set permission for User
        TMSearchBroswerHandlerHelper.setPermission(request);

        // From term search, for search condition share
        String fromTermSearchPage = (String) request.getParameter("fromTermSearchPage");
        if (fromTermSearchPage != null)
        {
            request.setAttribute("fromTermSearchPage", fromTermSearchPage);
            String sourceLocale = (String) request.getParameter("sourceLocale");
            request.setAttribute("sourceLocale", sourceLocale);
            String targetLocale = (String) request.getParameter("targetLocale");
            request.setAttribute("targetLocale", targetLocale);
            String searchText = (String) request.getParameter("searchText");
            searchText = iso88591ToUtf8(searchText);
            request.setAttribute("searchText", searchText);
        }
    }

    @ActionHandler(action = TM_ACTION_SEARCH, formClass = "")
    public void search(HttpServletRequest request, HttpServletResponse response, Object form)
            throws Exception
    {
        String searchType = (String) request.getParameter("searchType");
        String searchText = (String) request.getParameter("searchText");
        String sourceLocale = (String) request.getParameter("sourceLocale");
        String targetLocale = (String) request.getParameter("targetLocale");
        String tms = (String) request.getParameter("tms");
        String tmps = (String) request.getParameter("tmps");
        Locale uiLocale = (Locale) request.getSession().getAttribute(WebAppConstants.UILOCALE);
        String maxEntriesPerPageStr = (String) request.getParameter("maxEntriesPerPage");
        String searchIn = (String) request.getParameter("searchIn");
        boolean advancedSearch = "true".equals((String) request.getParameter("advancedSearch"));
        String replaceText = null;
        if (advancedSearch)
        {
            // Advanced search for replace
            replaceText = (String) request.getParameter("replaceText");
            if ("".equals(replaceText))
            {
                replaceText = null;
            }
        }
        String searchResult = "";
        if ("matchSearch".equals(searchType))
        {
            // Exact fuzzy search
            searchResult = TMSearchBroswerHandlerHelper.searchExact(request, searchText,
                    sourceLocale, targetLocale, tmps, uiLocale, maxEntriesPerPageStr, searchIn,
                    replaceText);
        }
        else
        {
            // Full text search
            tms = tms.substring(tms.indexOf('"') + 1, tms.lastIndexOf('"'));
            // GBS-3990
            if (("*".equals(searchText) || StringUtil.isEmpty(searchText))
                    && isOneOrMoreFieldsNotEmpty(request))
            {
                // use blank search text to search. don't use lucene. straight
                // SQL search only.
                searchResult = TMSearchBroswerHandlerHelper.searchFullTextBySql(request, searchText,
                        sourceLocale, targetLocale, tms, uiLocale, maxEntriesPerPageStr, searchIn,
                        replaceText);
            }
            else
            {
                searchResult = TMSearchBroswerHandlerHelper.searchFullText(request, searchText,
                        sourceLocale, targetLocale, tms, uiLocale, maxEntriesPerPageStr, searchIn,
                        replaceText);
            }
        }

        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.write(searchResult);
        if (out != null)
        {
            out.close();
        }
        pageReturn();
    }

    @ActionHandler(action = WebAppConstants.TM_ACTION_REFRESH_PAGE, formClass = "")
    public void refreshPage(HttpServletRequest request, HttpServletResponse response, Object form)
            throws Exception
    {
        // For paging
        String result = TMSearchBroswerHandlerHelper.refreshPage(request);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.write(result);
        if (out != null)
        {
            out.close();
        }
        pageReturn();

    }

    @ActionHandler(action = WebAppConstants.TM_ACTION_APPLY_REPLACE, formClass = "")
    public void applyReplaced(HttpServletRequest request, HttpServletResponse response, Object form)
            throws Exception
    {
        // Apply replaced text
        HttpSession session = request.getSession(false);
        String userId = getUser(session).getUserId();
        String result = TMSearchBroswerHandlerHelper.applyReplaced(request, userId);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.write(result);
        if (out != null)
        {
            out.close();
        }
        pageReturn();

    }

    @ActionHandler(action = WebAppConstants.TM_ACTION_DELETE_ENTRIES, formClass = "")
    public void deleteEntries(HttpServletRequest request, HttpServletResponse response, Object form)
            throws Exception
    {
        // Delete entries
        String result = TMSearchBroswerHandlerHelper.deleteEntries(request);
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
    public void beforeAction(HttpServletRequest request, HttpServletResponse response)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterAction(HttpServletRequest request, HttpServletResponse response)
    {
        // TODO Auto-generated method stub

    }

    /**
     * Checks if one or more fields except Search Text is empty or not.
     * 
     * @since GBS-3990
     */
    private boolean isOneOrMoreFieldsNotEmpty(HttpServletRequest request)
    {
        String sourceLocale = (String) request.getParameter("sourceLocale");
        String targetLocale = (String) request.getParameter("targetLocale");
        String sids = (String) request.getParameter("sids");
        String tuIds = (String) request.getParameter("tuIds");
        String attributeName = (String) request.getParameter("attributeName");
        String attributeValue = (String) request.getParameter("attributeValue");
        String createUser = (String) request.getParameter("createUser");
        String modifyUser = (String) request.getParameter("modifyUser");
        String jobIds = (String) request.getParameter("jobIds");

        String createStartDate = (String) request.getParameter("createStartDate");
        String createEndDate = (String) request.getParameter("createEndDate");
        String modifyStartDate = (String) request.getParameter("modifyStartDate");
        String modifyEndDate = (String) request.getParameter("modifyEndDate");
        String lastUsageStartDate = (String) request.getParameter("lastUsageStartDate");
        String lastUsageEndDate = (String) request.getParameter("lastUsageEndDate");

        if (StringUtil.isNotEmpty(sourceLocale) || StringUtil.isNotEmpty(targetLocale)
                || StringUtil.isNotEmpty(sids) || StringUtil.isNotEmpty(tuIds)
                || StringUtil.isNotEmpty(attributeName) || StringUtil.isNotEmpty(attributeValue)
                || StringUtil.isNotEmpty(createUser) || StringUtil.isNotEmpty(modifyUser)
                || StringUtil.isNotEmpty(jobIds) || StringUtil.isNotEmpty(createStartDate)
                || StringUtil.isNotEmpty(createEndDate) || StringUtil.isNotEmpty(modifyStartDate)
                || StringUtil.isNotEmpty(modifyEndDate) || StringUtil.isNotEmpty(lastUsageStartDate)
                || StringUtil.isNotEmpty(lastUsageEndDate))
        {
            return true;
        }

        return false;
    }
}
