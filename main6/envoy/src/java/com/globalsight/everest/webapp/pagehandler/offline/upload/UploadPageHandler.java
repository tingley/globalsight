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
package com.globalsight.everest.webapp.pagehandler.offline.upload;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.edit.offline.OEMProcessStatus;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskDetailHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

public class UploadPageHandler extends PageHandler
{
	/**
     * Invokes this EntryPageHandler object.
     * 
     * @param thePageDescriptor
     *            the description of the page to be produced
     * @param theRequest
     *            the original request sent from the browser
     * @param theResponse
     *            original response object
     * @param context
     *            the Servlet context
     */
	public void invokePageHandler(WebPageDescriptor p_pageDesc,
			HttpServletRequest p_request, HttpServletResponse p_response,
			ServletContext p_context) throws ServletException, IOException,
			EnvoyServletException
	{
		HttpSession httpSession = p_request.getSession();
		SessionManager sessionMgr = (SessionManager) httpSession
				.getAttribute(SESSION_MANAGER);

        // Fix for GBS-2291, from Task Upload Page
        sessionMgr.setAttribute(UPLOAD_ORIGIN, UPLOAD_FROMTASKUPLOAD);
        // Get state, must be non-null.
		String action = (String) p_request.getParameter(UPLOAD_ACTION);
		OEMProcessStatus status = (OEMProcessStatus) sessionMgr
				.getAttribute(UPLOAD_STATUS);

		// get reference to l10n profile and content type
		String contentType = p_request.getContentType();

		// process the uploaded content
		if (action != null)
		{
            UploadPageHandlerHelper uploadPageHandlerHelper = new UploadPageHandlerHelper();
			if (action.equals(UPLOAD_ACTION_START_UPLOAD))
			{
                uploadPageHandlerHelper.uploadStartPrepare(p_request,
                        httpSession, status, contentType);
			}
			else if (action.equals(UPLOAD_ACTION_PROGRESS))
			{
                uploadPageHandlerHelper.uploadProcess(p_request, p_response,
                        sessionMgr);
				return;
			}
			else if (action.equals(UPLOAD_ACTION_REFRESH))
			{
                uploadPageHandlerHelper.refreshProcessStatus(p_response,
                        sessionMgr, status);
				return;
			}
			else if(action.equals(TASK_ACTION_TRANSLATED_TEXT_RETRIEVE))
			{
				uploadPageHandlerHelper.translatedText(p_request,p_response);
				return;
			}
			else if (action.equals(UPLOAD_ACTION_CANCE_PROGRESS))
			{
				uploadPageHandlerHelper.cancelProcess(p_request, p_response, sessionMgr);
				return;
			}
			else if (action.equals(UPLOAD_ACTION_CONFIRM_CONTINUE))
			{
				String s = p_request.getParameter("isContinue");
				status.setIsContinue("y".equals(s));
				return;
			}
		}
		
        String taskId = p_request.getParameter("taskId");
        if(taskId != null && !taskId.equals(""))
        {
        	TaskDetailHelper taskDetailHelper = new TaskDetailHelper();
        	taskDetailHelper.prepareTaskData(p_request, p_response, httpSession, taskId);
        }
		
		super.invokePageHandler(p_pageDesc, p_request, p_response, p_context);
	}
}
