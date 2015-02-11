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

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.tm.maintenance.TmSearchHelper;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tm.searchreplace.TmConcordanceResult;
import com.globalsight.everest.tm.searchreplace.SearchReplaceManager;
import com.globalsight.everest.tm.TmManager;
import com.globalsight.everest.tm.TmManagerException;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.progress.IProcessStatusListener;
import com.globalsight.util.progress.ProcessStatus;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;

import java.io.IOException;
import java.sql.Connection;
import java.util.Locale;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * ResultsHandler is responsible for displaying the Tm search page.
 */
public class TmReplaceHandler
    extends PageHandler
    implements WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            TmReplaceHandler.class);

    // I think we cannot have static member variables.......
    static private ProjectHandler s_manager = null;

    // Constructor
    public TmReplaceHandler()
    {
        super();

        if (s_manager == null)
        {
            try
            {
                s_manager = ServerProxy.getProjectHandler();
            }
            catch (Exception ignore)
            {
            }
        }
    }

    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager)session.getAttribute(
            WebAppConstants.SESSION_MANAGER);

        Locale uiLocale = (Locale)session.getAttribute(UILOCALE);
        ResourceBundle bundle = getBundle(session);

        String userId = getUser(session).getUserId();

        // Get state, must be non-null.
        String state = (String)p_request.getParameter(TM_SEARCH_STATE_PARAM);

        SearchReplaceManager manager = null;

        ProcessStatus status =
            (ProcessStatus)sessionMgr.getAttribute(TM_TM_STATUS);

        try
        {
            String tmid = (String)sessionMgr.getAttribute(TM_TM_ID);
            String name = (String)sessionMgr.getAttribute(TM_TM_NAME);

            if (state != null)
            {
                if (state.equals(TM_ACTION_REFRESH))
                {
                    // do nothing UI will take care of showing progress
                }
                else if (state.equals(TM_SEARCH))
                {
                    try
                    {
                        String tmp = null;
                        // get values from request and invoke the real search

                        // get find text
                        tmp = (String)p_request.getParameter(
                            TM_SOURCE_FIND_TEXT);

                        String sourceFindText = EditUtil.utf8ToUnicode(tmp);

                        // get case sensitive option
                        tmp = (String)p_request.getParameter(
                            TM_SOURCE_FIND_MATCH_CASE);

                        boolean sourceCaseSensitive = (tmp != null);

                        // get selected source locale
                        tmp = (String)p_request.getParameter(
                            TM_SOURCE_SEARCH_LOCALE_SELECTOR);

                        Long sourceLocaleId = new Long(tmp);
                        GlobalSightLocale sourceLocale =
                            TmSearchHelper.getLocaleById(sourceLocaleId);

                        // get selected target locale
                        tmp = (String)p_request.getParameter(
                            TM_TARGET_SEARCH_LOCALE_SELECTOR);

                        Long targetLocaleId =  new Long(tmp);
                        GlobalSightLocale targetLocale =
                            TmSearchHelper.getLocaleById(targetLocaleId);

                        TmManager mgr = ServerProxy.getTmManager();
                        ArrayList tmNames = new ArrayList();
                        tmNames.add(name);
                        manager = mgr.getSearchReplacer(tmNames);

                        status = new ProcessStatus();
                        status.setResourceBundle(bundle);
                        manager.attachListener((IProcessStatusListener)status);
                        manager.search(EditUtil.encodeXmlEntities(sourceFindText),
                            sourceLocale, targetLocale, sourceCaseSensitive, null);

                        // remember user choices
                        setSessionValues(p_request, manager, status,
                            sourceFindText, new Boolean(sourceCaseSensitive),
                            sourceLocale, targetLocale);
                    }
                    catch (Throwable ex)
                    {
                        // error here
                        CATEGORY.error("TM Maintenance Error Occured", ex);
                    }
                }
                // first time on the replace screen
                else if (state.equals(TM_SEARCH_STATE_NORMAL))
                {
                    // get parameters from the SESSION
                    String sourceFindText = (String)session.getAttribute(
                        TM_SOURCE_FIND_TEXT);

                    Boolean sourceCaseSensitive = (Boolean)session.getAttribute(
                        TM_SOURCE_FIND_MATCH_CASE);

                    // get concordance results
                    TmConcordanceResult results =
                        (TmConcordanceResult)status.getResults();
                    sessionMgr.setAttribute(TM_CONCORDANCE_SEARCH_RESULTS, results);

                    // - get the previous search manager
                    manager = (SearchReplaceManager)sessionMgr.getAttribute(
                        TM_CONCORDANCE_MANAGER);

                   // convert results to html table rows
                    String tableRows = buildTable(
                        EditUtil.encodeHtmlEntities(sourceFindText),
                        (sourceCaseSensitive != null) ?
                        sourceCaseSensitive.booleanValue() : false,
                        null, false, results, null);

                    setParameters(p_request, tableRows);
                }
                // Second time around, highlight, page, or replace
                else
                {
                    // Get parameters from the REQUEST
                    String targetFindText =
                        (String)p_request.getParameter(TM_TARGET_FIND_TEXT);

                    targetFindText = EditUtil.utf8ToUnicode(targetFindText);
                    p_request.setAttribute(TM_TARGET_FIND_TEXT, targetFindText);

                    String targetReplaceText =
                        (String)p_request.getParameter(TM_TARGET_REPLACE_TEXT);

                    targetReplaceText = EditUtil.utf8ToUnicode(targetReplaceText);
                    p_request.setAttribute(TM_TARGET_REPLACE_TEXT, targetReplaceText);

                    boolean targetCaseSensitive = ((String)p_request.getParameter(
                        TM_TARGET_FIND_MATCH_CASE) != null);

                    // get parameters from the SESSION
                    String sourceFindText =
                        (String)session.getAttribute(TM_SOURCE_FIND_TEXT);

                    Boolean sourceCaseSensitive =
                        (Boolean)session.getAttribute(TM_SOURCE_FIND_MATCH_CASE);

                    // - get the previous search manager
                    manager = (SearchReplaceManager)sessionMgr.getAttribute(
                        TM_CONCORDANCE_MANAGER);

                    // - get the previous search results
                    TmConcordanceResult results =
                        (TmConcordanceResult)sessionMgr.getAttribute(
                            TM_CONCORDANCE_SEARCH_RESULTS);

                    // get previous selections (if any)
                    String [] selections = p_request.getParameterValues(
                        TM_REPLACE_SEGMENT_CHKBOX);

                    if (selections == null)
                    {
                        // null means "the user" did not want anything selected
                        selections = new String[]{""};
                    }

                    String rows;

                    // Read previous X TUs
                    if (state.equals(TM_SEARCH_STATE_PREV))
                    {
                        results.readPreviousPage();

                        if (sourceFindText != null && sourceFindText.length() == 0)
                        {
                            sourceFindText = null;
                        }
                        if (targetFindText != null && targetFindText.length() == 0)
                        {
                            targetFindText = null;
                        }

                        rows = buildTable(sourceFindText, sourceCaseSensitive.booleanValue(),
                            targetFindText, targetCaseSensitive, results, null);
                    }
                    // Read next X TUs
                    else if (state.equals(TM_SEARCH_STATE_NEXT))
                    {
                        results.readNextPage();

                        if (sourceFindText != null && sourceFindText.length() == 0)
                        {
                            sourceFindText = null;
                        }
                        if (targetFindText != null && targetFindText.length() == 0)
                        {
                            targetFindText = null;
                        }

                        rows = buildTable(EditUtil.encodeHtmlEntities(sourceFindText),
                            sourceCaseSensitive.booleanValue(),
                            EditUtil.encodeHtmlEntities(targetFindText),
                            targetCaseSensitive, results, null);
                    }
                    // Just highlight in current results
                    else
                    {
                        rows = buildTable(EditUtil.encodeHtmlEntities(sourceFindText),
                            sourceCaseSensitive.booleanValue(),
                            EditUtil.encodeHtmlEntities(targetFindText),
                            targetCaseSensitive, results, selections);
                    }

                    // reset the results html (rows)
                    p_request.setAttribute(
                        TM_CONCORDANCE_SEARCH_RESULTS_HTML, rows);
                }
            }
        }
        catch (Throwable ex)
        {
            CATEGORY.error("state " + state, ex);

            // JSP needs to clear this.
            sessionMgr.setAttribute(TM_ERROR, ex.toString());
        }

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }

    //
    // Private Methods
    //
    
    private String buildTable(String sourceFindText, 
            boolean sourceCaseSensitive, String targetFindText,
            boolean targetCaseSensititve, TmConcordanceResult results,
            String[] p_selections) throws Exception 
    {
    	TableMaker tableMaker = new TableMaker();
		String tableRows = tableMaker.getTableRows(sourceFindText,
				sourceCaseSensitive, targetFindText, targetCaseSensititve,
				results, p_selections);

    	return tableRows;
    }

    private void setParameters(HttpServletRequest p_request, String p_rows)
        throws EnvoyServletException
    {
        // set results html (rows)
        p_request.setAttribute(TM_CONCORDANCE_SEARCH_RESULTS_HTML, p_rows);
    }

    private void setSessionValues(HttpServletRequest p_request,
        SearchReplaceManager p_manager, ProcessStatus p_status,
        String p_sourceFindText, Boolean p_sourceCaseSensitiveSearch,
        GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale)
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);


        // ON THE SESSION

        session.setAttribute(TM_SOURCE_FIND_TEXT,
            p_sourceFindText);
        session.setAttribute(TM_SOURCE_FIND_MATCH_CASE,
            p_sourceCaseSensitiveSearch);
        session.setAttribute(TM_SOURCE_SEARCH_LOCALE,
            p_sourceLocale.getIdAsLong());
        session.setAttribute(TM_TARGET_SEARCH_LOCALE,
            p_targetLocale.getIdAsLong());

        // ON THE SESSION MANGER

        // set concordance manager on the session manager
        sessionMgr.setAttribute(TM_CONCORDANCE_MANAGER, p_manager);

        sessionMgr.setAttribute(TM_TM_STATUS, p_status);

        // set source search locale on the session manager
        sessionMgr.setAttribute(TM_SOURCE_SEARCH_LOCALE, p_sourceLocale);

        // set target search locale on the session manager
        sessionMgr.setAttribute(TM_TARGET_SEARCH_LOCALE, p_targetLocale);
    }
}
