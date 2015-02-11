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

package com.globalsight.everest.webapp.pagehandler.tm.maintenance;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tm.searchreplace.SearchReplaceManager;
import com.globalsight.everest.tm.searchreplace.TmConcordanceResult;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.progress.ProcessStatus;

/**
 *
 */
public class TmBatchReplaceHandler extends PageHandler
{
    private static final Logger CATEGORY = Logger
            .getLogger(TmBatchReplaceHandler.class);

    //
    // Constructor
    //
    public TmBatchReplaceHandler()
    {
    }

    /**
     * Invokes this PageHandler
     * 
     * @param p_pageDescriptor
     *            the page desciptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
        {
            p_response
                    .sendRedirect("/globalsight/ControlServlet?activityName=tm");
            return;
        }
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);

        String userId = getUser(session).getUserId();

        // From session Manger:
        // - get target search locale
        GlobalSightLocale targetLocale = (GlobalSightLocale) sessionMgr
                .getAttribute(TM_TARGET_SEARCH_LOCALE);

        // - get the previous search manager
        SearchReplaceManager manager = (SearchReplaceManager) sessionMgr
                .getAttribute(TM_CONCORDANCE_MANAGER);

        ProcessStatus status = (ProcessStatus) sessionMgr
                .getAttribute(TM_TM_STATUS);
        // - get the initial search results
        TmConcordanceResult searchResults = (TmConcordanceResult) status
                .getResults();

        // From the request:
        // - get target find text
        String oldTextUtf8 = (String) p_request
                .getParameter(TM_TARGET_FIND_TEXT);
        String oldText = EditUtil.utf8ToUnicode(oldTextUtf8);

        // - get target replacement text
        String newTextUtf8 = (String) p_request
                .getParameter(TM_TARGET_REPLACE_TEXT);
        String newText = EditUtil.utf8ToUnicode(newTextUtf8);

        // - get target case sensitive option
        boolean caseSensitiveSearch = ((String) p_request
                .getParameter(TM_TARGET_FIND_MATCH_CASE) != null);

        // - get user selected tu ids where the replacement will occur
        String selectedTuIds[] = p_request
                .getParameterValues(WebAppConstants.TM_REPLACE_SEGMENT_CHKBOX);

        // TODO: add a REPLACE_ALL flag and ignore the current selection.

        // Now do the replacement and get the results
        Collection replaceResults = doReplace(manager, oldText, newText,
        		caseSensitiveSearch, searchResults, selectedTuIds, targetLocale, userId);

        // remember result
        setSessionValues(p_request, replaceResults);

        // convert results to html table rows
        ReplaceResultTableMaker tableMaker = new ReplaceResultTableMaker();
        String html = tableMaker.getTableRows(searchResults, replaceResults);
        setParameters(p_request, html);

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    //
    // Private Methods
    //

    private void setParameters(HttpServletRequest p_request, String p_rows)
            throws EnvoyServletException
    {
        // set replace results HTML on the session manager
        p_request.setAttribute(TM_CONCORDANCE_REPLACE_RESULTS_HTML, p_rows);
    }

    private void setSessionValues(HttpServletRequest p_request,
            Collection p_results)
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        // set replace results on the session manager
        sessionMgr.setAttribute(TM_CONCORDANCE_REPLACE_RESULTS, p_results);
    }

	private ArrayList doReplace(SearchReplaceManager p_manager,
			String p_oldText, String p_newText, boolean p_caseSensitiveSearch,
			TmConcordanceResult p_searchResults, String[] p_selectedTuIds,
			GlobalSightLocale p_targetLocale, String p_userId)
			throws EnvoyServletException
    {
		ArrayList trgTuvs = getSelectedTargetTuvs(p_searchResults,
				p_selectedTuIds, p_targetLocale);

        try
        {
            return p_manager.replace(p_oldText, p_newText, trgTuvs,
                p_caseSensitiveSearch, p_userId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    private ArrayList getSelectedTargetTuvs(TmConcordanceResult p_searchResults,
        String[] p_selectedTuIds, GlobalSightLocale p_targetLocale)
    {
        return getTuvs(makeMap(p_searchResults), p_selectedTuIds, p_targetLocale);
    }

    private ArrayList getTuvs(Map p_searchResultsMap, String[] p_tuIds,
            GlobalSightLocale p_targetLocale)
    {
        ArrayList result = new ArrayList();

        SegmentTmTu tu = null;
        SegmentTmTuv tuv = null;

        for (int i = 0, max = p_tuIds.length; i < max; i++)
        {
            if ((tu = (SegmentTmTu) p_searchResultsMap
                    .get(new Long(p_tuIds[i]))) != null)
            {
                result.addAll(tu.getTuvList(p_targetLocale));
            }
        }

        return result;
    }

    /**
     * Returns a map from [tu id (Long)] to [SegmentTmTu].
     */
    private Map<Long, SegmentTmTu> makeMap(TmConcordanceResult p_searchResults)
    {
        Map<Long, SegmentTmTu> result = new HashMap<Long, SegmentTmTu>();
        List<SegmentTmTu> tus = p_searchResults.getTus();

        for (int i = 0, max = tus.size(); i < max; i++)
        {
            SegmentTmTu tu = tus.get(i);

            result.put(new Long(tu.getId()), tu);
        }

        return result;
    }
}
