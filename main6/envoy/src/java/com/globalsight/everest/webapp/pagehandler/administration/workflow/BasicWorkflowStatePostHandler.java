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
package com.globalsight.everest.webapp.pagehandler.administration.workflow;

import java.io.IOException;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.WorkflowStatePosts;
import com.globalsight.util.GeneralException;

public class BasicWorkflowStatePostHandler extends PageHandler implements
        WorkflowStatePostConstants
{
    public BasicWorkflowStatePostHandler()
    {
    }

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        try
        {
            String action = (String) p_request
                    .getParameter(WorkflowStatePostConstants.ACTION);
            p_request.setAttribute(WorkflowTemplateConstants.ACTION, action);
            ArrayList<WorkflowStatePosts> wfStatePostList = (ArrayList<WorkflowStatePosts>) ServerProxy
                    .getProjectHandler().getAllWorkflowStatePostInfos();
            if (action != null && "edit".equals(action))
            {
                modifyWfStatePostProfile(p_request, p_response, wfStatePostList);
            }

            p_request.setAttribute("allWfStatePost", wfStatePostList);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ne);
        }
        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private void modifyWfStatePostProfile(HttpServletRequest p_request,
            HttpServletResponse p_response, ArrayList<WorkflowStatePosts> wfStatePostList) throws IOException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        String id = p_request.getParameter("radioBtn");
        if (id == null
                || p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
        {
            p_response
                    .sendRedirect("/globalsight/ControlServlet?activityName=workflowStatePost");
            return;
        }
        WorkflowStatePosts wfStatePostProfile = (WorkflowStatePosts) WorkflowStatePostHandlerHelper
                .getWfStatePostProfile(Long.parseLong(id));
        for (WorkflowStatePosts wfStatePost : wfStatePostList)
        {
            if (wfStatePost.getName().equals(wfStatePostProfile.getName()))
            {
                wfStatePostList.remove(wfStatePostProfile);
                break;
            }
        }
        p_request.setAttribute(WORKFLOW_STATE_POST_ID, new Long(
                wfStatePostProfile.getId()));
        p_request.setAttribute(WF_STATE_POST_INFO, wfStatePostProfile);

        String actionType = (String) p_request.getParameter(ACTION);
        sessionMgr.setAttribute(ACTION, actionType);
        sessionMgr.setAttribute(WF_STATE_POST_INFO, wfStatePostProfile);
    }

}
