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

package com.globalsight.everest.webapp.pagehandler.administration.comment;

import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.comment.IssueOptions;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * PageHandler for showing segment comments from the job details comments page.
 */
public class SegmentCommentHandler
    extends PageHandler
    implements CommentConstants
{
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
        throws ServletException,
               IOException,
               EnvoyServletException
    {
        String value;

        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = 
        (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
        String jobId = (String)p_request.getParameter(WebAppConstants.JOB_ID);
        String taskId = (String)p_request.getParameter(WebAppConstants.TASK_ID);
        p_request.setAttribute(WebAppConstants.JOB_ID, jobId);
        p_request.setAttribute(WebAppConstants.TASK_ID, taskId);
        Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
        try
        {
            dataForTable(p_request, session, SEGMENT_COMMENT_LIST,
                             SEGMENT_COMMENT_KEY, new IssueComparator(uiLocale),
                             getIssues(p_request, sessionMgr));

            // Get values for filter selection box
            sessionMgr.setAttribute("statusList", IssueOptions.getAllStatus());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        if (p_request.getParameter(WebAppConstants.SOURCE_PAGE_ID) != null)
        {
            // coming from job comments page (rather than from sorting)
            sessionMgr.setAttribute(WebAppConstants.TARGET_PAGE_NAME,
                     p_request.getParameter(WebAppConstants.TARGET_PAGE_NAME));
            sessionMgr.setAttribute(WebAppConstants.SOURCE_PAGE_ID,
                     p_request.getParameter(WebAppConstants.SOURCE_PAGE_ID));
            sessionMgr.setAttribute(WebAppConstants.TARGET_PAGE_ID,
                     p_request.getParameter(WebAppConstants.TARGET_PAGE_ID));
            sessionMgr.setAttribute(WebAppConstants.JOB_ID,
                     p_request.getParameter(WebAppConstants.JOB_ID));
            sessionMgr.setAttribute("targLocale",
                     p_request.getParameter("targLocale"));
        }
        super.invokePageHandler(p_pageDescriptor, p_request, 
                                p_response, p_context);
    }


    /**
     * Get list of comments for displaying in table
     */
    private void dataForTable(HttpServletRequest p_request,
                              HttpSession p_session, String listname, String keyname, 
                              IssueComparator comparator,
                              List p_comments)
        throws EnvoyServletException
    {

        try
        {
            setTableNavigation(p_request, p_session, p_comments,
                comparator,
                9999,
                listname,
                keyname);
        }
        catch (Exception e)
        {
            // Config exception (already has message key...)
            throw new EnvoyServletException(e);
        }
    }

    private List<IssueImpl> getIssues(HttpServletRequest p_request, SessionManager p_sessionMgr)
            throws EnvoyServletException
    {
        // First check to see if user has a filter selected.
        String status = p_request.getParameter("setFilter");
        if (status != null && !status.equals("allStatus"))
        {
            p_request.setAttribute("segmentSelectedStatus", status);
        }
            
        String targPageId = (String) p_request.getParameter(WebAppConstants.TARGET_PAGE_ID);
        if (targPageId == null)
        {
            targPageId = (String) p_sessionMgr.getAttribute(WebAppConstants.TARGET_PAGE_ID);
        }
        else
        {
            p_sessionMgr.setAttribute(WebAppConstants.TARGET_PAGE_ID, targPageId);
        }
        try
        {
            return ServerProxy.getCommentManager().getIssues(
                        Issue.TYPE_SEGMENT, Long.parseLong(targPageId));
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }
}

