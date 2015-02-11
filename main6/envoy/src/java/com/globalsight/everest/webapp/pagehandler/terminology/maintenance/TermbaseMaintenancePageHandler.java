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

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.searchreplace.ITermbaseMaintance;
import com.globalsight.terminology.searchreplace.SearchReplaceParams;
import com.globalsight.util.GeneralException;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.progress.ProcessStatus;

/**
 * <p>PageHandler is responsible creating, deleting and modifying
 * termbases.</p>
 */

public class TermbaseMaintenancePageHandler
    extends PageHandler
    implements WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            TermbaseMaintenancePageHandler.class);

    //
    // Static Members
    //
    static private ITermbaseManager s_manager = null;

    //
    // Constructor
    //
    public TermbaseMaintenancePageHandler()
    {
        super();

        if (s_manager == null)
        {
            try
            {
                s_manager = ServerProxy.getTermbaseManager();
            }
            catch (GeneralException ex)
            {
                // ignore.
            }
        }
    }

    //
    // Interface Methods: PageHandler
    //

    /**
     * Invoke this PageHandler.
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException,
               IOException,
               EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager)session.getAttribute(
            SESSION_MANAGER);
        String userId = getUser(session).getUserId();
        String action = (String)p_request.getParameter(TERMBASE_ACTION);
        String tbid   = (String)p_request.getParameter(RADIO_BUTTON);
        String name = null;

        ProcessStatus status = (ProcessStatus)sessionMgr.getAttribute(TERMBASE_STATUS);

        try
        {
            if (tbid != null)
            {
                name = s_manager.getTermbaseName(Long.parseLong(tbid));
                sessionMgr.setAttribute(TERMBASE_TB_ID, tbid);
                sessionMgr.setAttribute(TERMBASE_TB_NAME, name);
            }
            else
            {
                tbid = (String)sessionMgr.getAttribute(TERMBASE_TB_ID);
                name = (String)sessionMgr.getAttribute(TERMBASE_TB_NAME);
            }
            String replace =
                (String)p_request.getParameter(TERMBASE_REPLACE);
            String smartReplace =
                (String)p_request.getParameter(TERMBASE_SMARTREPLACE);
            if (action == null || action.equals(TERMBASE_ACTION_MAINTENANCE))
            {
            	if (tbid == null
						|| p_request.getMethod().equalsIgnoreCase(
								REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
                // show main screen with search options
                String definition = s_manager.getDefinition(name, false);
                sessionMgr.setAttribute(TERMBASE_DEFINITION, definition);
            }
            else if (action.equals(TERMBASE_ACTION_SEARCH))
            {
                // Search the termbase for matching entries
                String search =
                    (String)p_request.getParameter(TERMBASE_SEARCH);
                String caseInsensitive =
                    (String)p_request.getParameter(TERMBASE_CASEINSENSITIVE);
                String level =
                    (String)p_request.getParameter(TERMBASE_LEVEL);
                String language =
                    (String)p_request.getParameter(TERMBASE_LANGUAGE);
                String field =
                    (String)p_request.getParameter(TERMBASE_FIELD);
                String fieldName =
                    (String)p_request.getParameter(TERMBASE_FIELDNAME);
                String searchType = p_request.getParameter("type");
                String isWholeWord = p_request.getParameter("wordOnly");

                search = EditUtil.utf8ToUnicode(search);
                
                language = EditUtil.utf8ToUnicode(language);
                fieldName = EditUtil.utf8ToUnicode(fieldName);

                SearchReplaceParams params = new SearchReplaceParams(
                    search, searchType, level, language, field, fieldName,
                    caseInsensitive, smartReplace, isWholeWord);
                sessionMgr.setAttribute(TERMBASE_SEARCHCONDITION, params);

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("initializing TB search & replace in TB " +
                        name + " -- " + params.toString());
                }

                ITermbase tb = s_manager.connect(name, userId, "");
                ArrayList list = tb.getTbMaintance(params).search();
                sessionMgr.setAttribute("searchResults", list);
            }
            else if (action.equals(TERMBASE_ACTION_REFRESH))
            {
                // do nothing
            }
            else
            {
            	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
                SearchReplaceParams params = (SearchReplaceParams)
                    sessionMgr.getAttribute(TERMBASE_SEARCHCONDITION);

                String indexes =
                    (String)p_request.getParameter(TERMBASE_REPLACEINDEX);
                replace = EditUtil.utf8ToUnicode(replace);
                params.setReplaceText(replace);
                params.setSmartReplace(smartReplace);

                if (action.equals(TERMBASE_ACTION_SHOWPREVIOUS))
                {
                    //SearchResults results = manager.getPreviousResults();
                    //sessionMgr.setAttribute(TERMBASE_SEARCHRESULTS, results);
                }
                else if (action.equals(TERMBASE_ACTION_REPLACE))
                {
                    int size = Integer.parseInt(p_request.getParameter("size"));
                    SearchReplaceParams rp = 
                        (SearchReplaceParams)sessionMgr.getAttribute(TERMBASE_SEARCHCONDITION);
                    String replaceContent = p_request.getParameter("replaceContent");
                    ITermbase tb = s_manager.connect(name, userId, "");
                    ITermbaseMaintance tbm = tb.getTbMaintance(params);

                    ArrayList failedReplace = new ArrayList();
                    
                    for (int i = 0; i < size; i++)
                    {
                        String cname = "checkbox" + i;
                        if (p_request.getParameter(cname) != null)
                        {
                            p_request.setAttribute("checkBoxChecked", true);

                            String value = p_request.getParameter(cname);

                            long id = Long.parseLong(value.split(",")[0]);
                            String oldContent = value.split(",")[1];
                            try {
                            tbm.replace(id, oldContent, replaceContent);
                            }catch(Exception e) {
                                failedReplace.add(oldContent);
                            }
                        }
                        else
                        {
                            p_request.setAttribute("checkBoxChecked", false);
                        }
                    }
                    
                    p_request.setAttribute("failedReplace", failedReplace);
                    
                }
            }
        }
        catch (TermbaseException ex)
        {
            // JSP needs to clear this.
            sessionMgr.setAttribute(TERMBASE_ERROR, ex.toString());
        }

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }
}

