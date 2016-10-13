/**
 *  Copyright 2014 Welocalize, Inc. 
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
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.io.IOException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.cxe.entity.filterconfiguration.JsonUtil;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.StringUtil;

public class JobGroupHandler extends PageHandler
{
	public static String BASE_BEAN = "self";

	/**
	 * @throws ParseException
	 * @throws IOException
	 * @throws ServletException
	 * @throws
	 * @throws GeneralException
	 *             Invokes this EntryPageHandler object
	 *             <p>
	 * 
	 * @param p_ageDescriptor
	 *            the description of the page to be produced.
	 * @param p_request
	 *            original request sent from the browser.
	 * @param p_response
	 *            original response object.
	 * @param p_context
	 *            the Servlet context.
	 * @throws
	 */
	public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
			HttpServletRequest p_request, HttpServletResponse p_response,
			ServletContext p_context) throws ServletException, IOException
	{
		String action = p_request.getParameter(ACTION_STRING);
		if (StringUtil.isNotEmpty(action))
		{
			if (action.equals(ACTION_NEW))
			{
				JobGroupHandlerHelper.newJobGroup(p_request);
			}
			else if (action.equals(JOB_GROUP_SAVE))
			{
				JobGroupHandlerHelper.saveJobGroup(p_request);
				getJobGroups(p_pageDescriptor, p_request);
			}
			else if (action.equals(JOB_GROUP_CHECK))
			{
				Map<String, Object> map = JobGroupHandlerHelper
						.checkJobGroup(p_request);
				ServletOutputStream out = p_response.getOutputStream();
				out.write(JsonUtil.toJson(map).getBytes("UTF-8"));
				out.close();
				return;
			}
			else if (action.equals(ACTION_REMOVE))
			{
				JobGroupHandlerHelper.removeJobGroup(p_request);
				getJobGroups(p_pageDescriptor, p_request);
			}
		}
		else
		{
			getJobGroups(p_pageDescriptor, p_request);
		}
		super.invokePageHandler(p_pageDescriptor, p_request, p_response,
				p_context);
	}

	private void getJobGroups(WebPageDescriptor p_pageDescriptor,
			HttpServletRequest p_request)
	{
		HttpSession session = p_request.getSession(false);
		SessionManager sessionMgr = (SessionManager) session
				.getAttribute(SESSION_MANAGER);
		setJobGroupSearchFilters(sessionMgr, p_request);
		HashMap beanMap = invokeJobGroupControlPage(p_pageDescriptor,
				p_request, BASE_BEAN);
		JobGroupHandlerHelper.getAllJobGroups(beanMap, p_request);
	}

	protected void setJobGroupSearchFilters(SessionManager sessionMgr,
			HttpServletRequest p_request)
	{
		if (p_request.getParameter("fromRequest") != null)
		{
			String groupIdFilter = p_request.getParameter("groupId");
			if (groupIdFilter != null)
			{
				sessionMgr.setAttribute("groupIdFilter", groupIdFilter);
			}

			String groupNameFilter = p_request.getParameter("groupName");
			if (groupNameFilter != null)
			{
				try
				{
					groupNameFilter = new String(
							groupNameFilter.getBytes("ISO8859-1"), "UTF-8");
					sessionMgr.setAttribute("groupNameFilter", groupNameFilter);
				}
				catch (Exception e)
				{
				}
			}

			String groupProjectFilter = p_request.getParameter("groupProject");
			if (groupProjectFilter != null)
			{
				sessionMgr.setAttribute("groupProjectFilter",
						groupProjectFilter);
			}

			String groupLocaleFilter = p_request.getParameter("groupLocale");
			if (groupLocaleFilter != null)
			{
				sessionMgr
						.setAttribute("sourceLocaleFilter", groupLocaleFilter);
			}

			String npp = p_request.getParameter("npp");
			boolean isNewNpp = false;
			if (npp != null)
			{
				if (sessionMgr.getAttribute("numPerPage") != null)
				{
					int oldNpp = (Integer) sessionMgr
							.getAttribute("numPerPage");
					if (oldNpp != Integer.valueOf(npp))
						isNewNpp = true;
				}
				sessionMgr.setAttribute("numPerPage", Integer.valueOf(npp));
			}

			String jobListStart = p_request.getParameter("jobGroupListStart");
			if (isNewNpp)
				jobListStart = "0";
			if (jobListStart != null)
			{
				sessionMgr.setAttribute("jobGroupListStart", jobListStart);
			}

		}
		else
		{
			if (sessionMgr.getAttribute("groupIdFilter") == null)
			{
				sessionMgr.setAttribute("groupIdFilter", "");
				sessionMgr.setAttribute("groupNameFilter", "");
				sessionMgr.setAttribute("groupProjectFilter", "-1");
				sessionMgr.setAttribute("sourceLocaleFilter", "-1");
				sessionMgr.setAttribute("jobGroupListStart", "0");
			}
		}
	}

	public HashMap invokeJobGroupControlPage(
			WebPageDescriptor p_pageDescriptor, HttpServletRequest p_request,
			String p_baseBeanLinkName)
	{
		// BEGIN: Setting navigation beans.
		SessionManager sessionMgr = (SessionManager) p_request
				.getSession(false).getAttribute(SESSION_MANAGER);
		HttpSession session = p_request.getSession();
		Locale uiLocale = (Locale) session
				.getAttribute(WebAppConstants.UILOCALE);

		NavigationBean baseBean = null;
		NavigationBean detailsBean = null;
		Enumeration en = p_pageDescriptor.getLinkNames();

		while (en.hasMoreElements())
		{
			String linkName = (String) en.nextElement();
			String pageName = p_pageDescriptor.getPageName();
			if (linkName.equals(p_baseBeanLinkName))
			{
				baseBean = new NavigationBean(linkName, pageName);
			}
			else if (linkName.equals(JOB_GROUP_DETAIL))
			{
				detailsBean = new NavigationBean(linkName, pageName);
			}
			// create a navigation bean for each link
			NavigationBean aNavigationBean = new NavigationBean(linkName,
					pageName);
			// each navigation bean will be labelled with the name of the link
			p_request.setAttribute(linkName, aNavigationBean);
		}

		HashMap<String, NavigationBean> beanMap = new HashMap<String, NavigationBean>(
				2);
		beanMap.put(p_baseBeanLinkName, baseBean);
		beanMap.put(JOB_GROUP_DETAIL, detailsBean);

		return beanMap;
	}
}
