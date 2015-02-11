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
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowHandlerHelper;
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
        
        String toJob = p_request.getParameter("toJob");
        String toTask = p_request.getParameter("toTask");
        if(toJob != null ){
        	String jobId = p_request.getParameter("jobId");
        	if(jobId != null)
            {
            	long contextMenuJobId = Long.valueOf(jobId);
            	Job contextMenuJob = WorkflowHandlerHelper
            			.getJobById(contextMenuJobId);
            	TaskHelper.storeObject(session, WebAppConstants.WORK_OBJECT,
            			contextMenuJob);
            }
        }else if (toTask != null ){
        	String taskIdParam = p_request.getParameter(TASK_ID);
            String taskStateParam = p_request.getParameter(TASK_STATE);
            if(taskIdParam != null && taskStateParam != null)
            {
            	long taskId = TaskHelper.getLong(taskIdParam);
            	int taskState = TaskHelper.getInt(taskStateParam, -10);
            	User user = TaskHelper.getUser(session);
            	Task task = TaskHelper.getTask(user.getUserId(), taskId,taskState);
            	
            	TaskHelper.storeObject(session, WORK_OBJECT, task);
            }
        }
        
        String commentStr = (String)p_request.getParameter(WebAppConstants.TASK_COMMENT);
        if(commentStr != null){
        	sessionMgr.setAttribute("taskComment",commentStr);
        }
        
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
            p_request.setAttribute("commentId", commentId);
            sessionMgr.setAttribute("comment", comment);
            sessionMgr.setAttribute("taskComment", comment.getComment());
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }
}

