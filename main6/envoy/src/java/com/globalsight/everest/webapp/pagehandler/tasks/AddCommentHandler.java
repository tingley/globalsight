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
package com.globalsight.everest.webapp.pagehandler.tasks;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

public class AddCommentHandler extends PageHandler
{
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        
        SessionManager sessionMgr =
        (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

        String action = p_request.getParameter("action");
        if ("editcomment".equals(action))
        {
            String commentId = p_request.getParameter("radioBtn"); 
            
            //GBS-1012: Added for edit job comment from Task/Activity
            if(commentId==null)
            {
            	commentId = p_request.getParameter("jobradioBtn");
            }
            
            Comment comment = TaskHelper.getComment(session, Long.parseLong(commentId));
            sessionMgr.setAttribute("comment", comment);
            sessionMgr.setAttribute("taskComment", comment.getComment());
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }
}

