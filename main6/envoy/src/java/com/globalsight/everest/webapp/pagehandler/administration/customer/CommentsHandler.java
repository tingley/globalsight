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

package com.globalsight.everest.webapp.pagehandler.administration.customer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.comment.CommentManager;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.comment.PageCommentsSummary;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;


/**
 * This is the handler for displaying total number of open segment comments for a page
 */
public class CommentsHandler extends PageHandler
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
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = 
            (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

        ArrayList<PageCommentsSummary> pageSummaries = new ArrayList<PageCommentsSummary>();
        try
        {
            String value = (String)p_request.getParameter("value");
            if (value == null)
            {
                // returning from segment comments.  get list from session
                pageSummaries = (ArrayList) sessionMgr.getAttribute("pageSummaries");
                dataForTable(p_request, session, SourceFile.FILE_LIST,
                             SourceFile.FILE_KEY, null, pageSummaries);
                super.invokePageHandler(p_pageDescriptor, p_request, 
                             p_response, p_context);
                return;
            }
            StringTokenizer st = new StringTokenizer(value, ",");
            String jobName = st.nextToken();
            String targLocale = st.nextToken();
            st.nextToken();  // skip source locale
            sessionMgr.setAttribute("jobName", jobName);
            sessionMgr.setAttribute("targLocale", targLocale);

            CommentManager manager = ServerProxy.getCommentManager();
            while (st.hasMoreTokens())
            {
                Job job = ServerProxy.getJobHandler().getJobById(Long.parseLong(st.nextToken()));
                // Get the workflows and search for the workflow with this target locale
                for (Workflow wf : job.getWorkflows())
                {
                    if (!wf.getState().equals(Workflow.CANCELLED) && 
                        wf.getTargetLocale().toString().equals(targLocale))
                    {
                        List<TargetPage> tpages = wf.getTargetPages();

                        List<Long> tpIds = new ArrayList<Long>();
                        for (int j = 0; j < tpages.size(); j++)
                        {
                            TargetPage tPage = (TargetPage) tpages.get(j);
                        	tpIds.add(tPage.getIdAsLong());
                        }

                        HashMap<Long, Integer> openCounts = null;
                        try {
                            List<String> states = new ArrayList<String>();
                            states.add(Issue.STATUS_OPEN);
                            states.add(Issue.STATUS_QUERY);
                            states.add(Issue.STATUS_REJECTED);
							openCounts = manager.getIssueCountPerTargetPage(
									Issue.TYPE_SEGMENT, tpIds, states);
                        } catch (Exception ex) {
                        	throw new EnvoyServletException(ex);
                        }

                        if (openCounts == null || openCounts.size() == 0)
                        	continue;

                    	for (int k = 0; k < tpages.size(); k++)
                    	{
                    		TargetPage tPage = (TargetPage) tpages.get(k);
                    		Integer count = openCounts.get(tPage.getIdAsLong());
                    		if (count != null && count > 0)
                    		{
                    			PageCommentsSummary ps = new PageCommentsSummary(tPage);
                    			ps.setOpenCommentsCount(count);
                    			ps.setJobId(job.getJobId());
                    			pageSummaries.add(ps);
                    		}
                    	}
                    }
                }
            }

			dataForTable(p_request, session, SourceFile.FILE_LIST,
					SourceFile.FILE_KEY, null, pageSummaries);
            sessionMgr.setAttribute("pageSummaries", pageSummaries);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, 
                                p_response, p_context);
    }

    /**
     * Get list of files for displaying in table
     */
    private void dataForTable(HttpServletRequest p_request,
                              HttpSession p_session, String listname, String keyname,
                              MyJobComparator comparator,
                              List p_files)
        throws EnvoyServletException
    {
        try
        {
            setTableNavigation(p_request, p_session, p_files,
                comparator,
                20,
                listname,
                keyname);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }
}
