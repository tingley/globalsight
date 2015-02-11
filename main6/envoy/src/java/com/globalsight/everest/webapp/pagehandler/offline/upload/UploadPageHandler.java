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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.offline.OEMProcessStatus;
import com.globalsight.everest.edit.offline.OfflineEditManager;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

public class UploadPageHandler extends PageHandler implements WebAppConstants
{
	private static final Logger CATEGORY = Logger
			.getLogger(UploadPageHandler.class.getName());

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
		// Get state, must be non-null.
		String action = (String) p_request.getParameter(UPLOAD_ACTION);
		OEMProcessStatus status = (OEMProcessStatus) sessionMgr
				.getAttribute(UPLOAD_STATUS);

		// get reference to l10n profile and content type
		String contentType = p_request.getContentType();

		// process the uploaded content
		if (action != null)
		{
			if (action.equals(UPLOAD_ACTION_START_UPLOAD))
			{
				uploadStartPrepare(p_request, httpSession, status, contentType);
			}
			else if (action.equals(UPLOAD_ACTION_PROGRESS))
			{
				uploadProcess(p_request, p_response, sessionMgr);
				return;
			}
			else if (action.equals(UPLOAD_ACTION_REFRESH))
			{
				refreshProcessStatus(p_response, sessionMgr, status);
				return;
			}
		}
		super.invokePageHandler(p_pageDesc, p_request, p_response, p_context);
	}

	/**
     * Prepare works (eg: Copy the ori file to server named temp file etc)
     * before uploading process
     * 
     * @param p_request
     * @param httpSession
     * @param status
     *            ProcessStatus object
     * @param contentType
     *            file upload contentType
     */
	private void uploadStartPrepare(HttpServletRequest p_request,
			HttpSession httpSession, OEMProcessStatus status, String contentType)
	{
		String action;
		if (contentType != null
				&& contentType.toLowerCase().startsWith("multipart/form-data"))
		{
			processRequest(p_request, status);
			// now update the task in the session
			try
			{
				User user = TaskHelper.getUser(httpSession);
				long taskId = ((Task) TaskHelper.retrieveObject(httpSession,
						WORK_OBJECT)).getId();
				action = (String) TaskHelper.retrieveObject(httpSession,
						TASK_STATE);
				TaskHelper.updateTaskInSession(httpSession, user.getUserId(),
						taskId, Integer.parseInt(action));
			}
			catch (Exception e)
			{
				throw new EnvoyServletException(e);
			}
		}
	}

	/**
     * Uploading process includes read file contents, velidate the content,
     * compare file content and database content
     * 
     * @param p_request
     * @param p_response
     * @param sessionMgr
     */
	private void uploadProcess(HttpServletRequest p_request,
			HttpServletResponse p_response,
			SessionManager sessionMgr)
	{
		OEMProcessStatus status;
		OfflineEditManager OEM;
		User user = (User) sessionMgr.getAttribute(USER);
        
//      Setting Delay time for task complete after upload 
       Date taskUploadFileStartTime = new Date();
       String userId = user.getUserId();
       String taskId = (String)sessionMgr.getAttribute(TASK_ID);
       String delayTimeTableKey = userId + taskId;
       Hashtable delayTimeTable = (Hashtable)sessionMgr.getAttribute(WebAppConstants.TASK_COMPLETE_DELAY_TIME_TABLE);
       if(delayTimeTable == null)
       {
           delayTimeTable = new Hashtable();
       }     
       delayTimeTable.put(delayTimeTableKey, taskUploadFileStartTime);
       sessionMgr.setAttribute(WebAppConstants.TASK_COMPLETE_DELAY_TIME_TABLE, delayTimeTable);
       
		status = (OEMProcessStatus) sessionMgr.getAttribute(UPLOAD_STATUS);
		OEM = (OfflineEditManager) sessionMgr.getAttribute(UPLOAD_MANAGER);
		String fileName = (String) sessionMgr.getAttribute("tempFileName");
		File file = (File) sessionMgr.getAttribute("tempFile");
		String isReport = (String) sessionMgr.getAttribute("isReport");
		if (isReport != null && isReport.equals("yes"))
		{
			String reportType = (String) sessionMgr.getAttribute(REPORT_TYPE);
			processReportFileContents(file, user,
					fileName, p_request, status, OEM, reportType);
			sessionMgr.removeElement("isReport");
			sessionMgr.removeElement(REPORT_TYPE);
			p_response.setContentType("text/html");
		}
		else
		{
			processFileContents(file, user, fileName,
					p_request, status, OEM);
			p_response.setContentType("text/html");
		}
	}

	/**
     * The method is for refresh process status by ajax invoke, try to get the
     * process status, the ajax invoker will stop when the process is finished.
     * 
     * @param p_response
     * @param sessionMgr
     * @param status
     * @throws IOException
     */
	private void refreshProcessStatus(HttpServletResponse p_response,
			SessionManager sessionMgr, OEMProcessStatus status)
			throws IOException
	{
		// ProcessStatus m_status =
		// (ProcessStatus)sessionMgr.getAttribute(WebAppConstants.UPLOAD_STATUS);
		StringBuffer sb = new StringBuffer();
		String errorMsg = (String) status.getResults();
		sessionMgr.setAttribute(OfflineConstants.ERROR_MESSAGE, errorMsg);
		p_response.setContentType("text/html");
		if (status != null)
		{
			sb.append(status.getPercentage() + ",");
			sb.append(status.getCounter() + ",");
			sb.append(String.valueOf(status.getResults() == null) + ",");
			List list = status.giveMessages();

			if (list != null)
			{
				for (int i = 0; i < list.size(); i++)
				{
					sb.append((String) list.get(i) + ",");
				}
			}
			PrintWriter out = p_response.getWriter();
			out.write(sb.toString());
			out.close();
		}
	}

	// process uploaded file
	private void processRequest(HttpServletRequest p_request,
			OEMProcessStatus p_status) throws EnvoyServletException
	{
		OfflineEditManager OEM = null;
		File tempFile = null;
		HttpSession session = p_request.getSession(false);
		SessionManager sessionManager = (SessionManager) session
				.getAttribute(SESSION_MANAGER);

		String reportName = (String) p_request.getParameter(REPORT_TYPE);
		// read the uploaded file
		MultipartFormDataReader reader = new MultipartFormDataReader();

		try
		{
			p_status = new OEMProcessStatus();
			OEM = (OfflineEditManager) ServerProxy.getOfflineEditManager();
			SystemResourceBundle srb = SystemResourceBundle.getInstance();
			ResourceBundle res = srb.getResourceBundle(
					ResourceBundleConstants.LOCALE_RESOURCE_NAME,
					(Locale) p_request.getSession().getAttribute(
							WebAppConstants.UILOCALE));
			p_status.addMessage(res.getString("msg_upld_copy_to_server"));

			tempFile = reader.uploadToTempFile(p_request);
			OEM.attachListener(p_status);
			sessionManager.setAttribute(UPLOAD_STATUS, p_status);
			sessionManager.setAttribute(UPLOAD_MANAGER, OEM);
			sessionManager.setAttribute("tempFile", tempFile);
			sessionManager.setAttribute("tempFileName", reader.getFilename());
			if (reportName != null)
			{
				sessionManager.setAttribute("isReport", "yes");
				sessionManager.setAttribute(REPORT_TYPE, reportName);
			}
		}
		catch (Exception e)
		{
			CATEGORY.error(e);
			throw new EnvoyServletException(e);
		}

	}

	private void processReportFileContents(File p_tmpFile,
			User p_user, String p_fileName, HttpServletRequest p_request,
			OEMProcessStatus p_status, OfflineEditManager p_OEM,
			String p_reportName) throws EnvoyServletException
	{
		HttpSession session = p_request.getSession(false);
		SessionManager sessionManager = (SessionManager) session
				.getAttribute(SESSION_MANAGER);
		try
		{
			Task task = getTask(p_request);
			CATEGORY.debug("THE TASK ID WE ARE ATTEMPING TO UPLOAD INTO IS "
					+ task.getId());

			if (task == null)
			{
				EnvoyServletException e = new EnvoyServletException(
						"TaskNotFound", null, null);
				CATEGORY.error(e);
				throw e;
			}

			p_OEM.processUploadReportPage(p_tmpFile, p_user, task,
					p_fileName, p_reportName);
		}
		catch (Exception e)
		{
			CATEGORY.error(e);
			throw new EnvoyServletException(e);
		}
		finally
		{
			session = p_request.getSession(false);
			sessionManager = (SessionManager) session
					.getAttribute(SESSION_MANAGER);
			sessionManager.removeElement(OfflineConstants.ERROR_MESSAGE);
		}
	}

	/**
     * Process file contests control method
     * 
     * @param p_tmpFile
     * @param p_user
     * @param p_fileName
     * @param p_request
     * @param p_status
     * @param p_OEM
     * @throws EnvoyServletException
     */
	private void processFileContents(File p_tmpFile,
			User p_user, String p_fileName, HttpServletRequest p_request,
			OEMProcessStatus p_status, OfflineEditManager p_OEM)
			throws EnvoyServletException
	{
		// TODO: Now we need to return a result "object" containing this flag
		// and
		// the error page. For now we force the Cache to refresh itself no
		// matter if the uploaded file we extracted or not.

		HttpSession session = p_request.getSession(false);
		SessionManager sessionManager = (SessionManager) session
				.getAttribute(SESSION_MANAGER);

		try
		{
			Task task = getTask(p_request);
			CATEGORY.debug("THE TASK ID WE ARE ATTEMPING TO UPLOAD INTO IS "
					+ task.getId());

			if (task == null)
			{
				EnvoyServletException e = new EnvoyServletException(
						"TaskNotFound", null, null);
				CATEGORY.error(e);
				throw e;
			}

			p_OEM.processUploadPage(p_tmpFile, p_user, task,
					p_fileName);
		}
		catch (Exception e)
		{
			CATEGORY.error(e);
			throw new EnvoyServletException(e);
		}
		finally
		{
			session = p_request.getSession(false);
			sessionManager = (SessionManager) session
					.getAttribute(SESSION_MANAGER);
			sessionManager.removeElement(OfflineConstants.ERROR_MESSAGE);
		}
	}

	private Task getTask(HttpServletRequest p_request)
			throws EnvoyServletException
	{
		HttpSession session = p_request.getSession(false);
		Task task = (Task) TaskHelper.retrieveObject(session, WORK_OBJECT);
		if (task == null)
		{
			EnvoyServletException e = new EnvoyServletException("TaskNotFound",
					null, null);
			CATEGORY.error(e);
			throw e;
		}
		return task;
	}
}
