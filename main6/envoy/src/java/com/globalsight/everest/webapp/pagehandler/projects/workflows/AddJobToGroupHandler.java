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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobGroup;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;

public class AddJobToGroupHandler extends PageHandler
{

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
			if ("addJobToGroup".equals(action))
			{
				getJobGroup(p_request);
			}
			else if ("saveJobToGroup".equals(action))
			{
				String reslut = saveJobToGroup(p_request);
				p_response.getWriter().write(reslut);
				return;
			}
		}
		super.invokePageHandler(p_pageDescriptor, p_request, p_response,
				p_context);
	}

	private String saveJobToGroup(HttpServletRequest request)
	{
		String jobIds = request.getParameter("jobIds");
		String groupId = request.getParameter("jobGroupId");
		boolean success = false;
		if (jobIds != null && groupId != null)
		{
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE JOB SET ").append("GROUP_ID = ").append(groupId)
					.append(" WHERE ID IN (").append(jobIds).append(")");
			try
			{
				HibernateUtil.executeSql(sql.toString());
				success = true;
			}
			catch (Exception e)
			{
				success = false;
				e.printStackTrace();
			}
		}
		StringBuffer result = new StringBuffer();
		if (success)
		{
			result.append("{").append("\"message\":").append("\"success\"")
					.append("}");
		}
		else
		{
			result.append("{").append("\"message\":").append("\"failed\"")
					.append("}");
		}
		return result.toString();
	}

	private void getJobGroup(HttpServletRequest p_request)
	{
		String jobListStart = p_request.getParameter("jobListStart");
		if (jobListStart != null)
			p_request.setAttribute("jobListStart", jobListStart);

		String jobIds = p_request.getParameter("jobIds");
		String pageState = p_request.getParameter("pageState");
		if (StringUtil.isNotEmpty(pageState))
		{
			p_request.setAttribute("pageState", pageState);
		}
		String[] jobIdArr = jobIds.split(",");
		List<Long> projectIdList = new ArrayList<Long>();
		List<Long> sourceIdList = new ArrayList<Long>();
		boolean errorProject = false;
		boolean errorSource = false;
		for (String jobId : jobIdArr)
		{
			Job job = WorkflowHandlerHelper.getJobById(Long.parseLong(jobId));
			if (projectIdList.size() == 0)
			{
				projectIdList.add(job.getProject().getId());
			}
			else
			{
				if (!projectIdList.contains(job.getProject().getId()))
				{
					errorProject = true;
				}
			}

			if (sourceIdList.size() == 0)
			{
				sourceIdList.add(job.getSourceLocale().getId());
			}
			else
			{
				if (!sourceIdList.contains(job.getSourceLocale().getId()))
				{
					errorSource = true;
				}
			}
		}

		if (errorProject)
		{
			p_request
					.setAttribute("errorProject",
							"The selected jobs belong to different projects, cannot add them to same job group.");
		}
		if (errorSource)
		{
			p_request
					.setAttribute("errorSource",
							"The selected jobs have different source locales, can not add them to same job group.");
		}

		if (!errorProject && !errorSource)
		{
			List<JobGroup> list = getJobGroup(projectIdList, sourceIdList);
			p_request.setAttribute("jobGoupList", list);
			p_request.setAttribute("jobIds", jobIds);
		}
	}

	private List<JobGroup> getJobGroup(List<Long> projectIdList,
			List<Long> sourceIdList)
	{
		String currentId = CompanyThreadLocal.getInstance().getValue();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT JP.ID,JP.NAME,JP.PROJECT_ID,JP.SOURCE_LOCALE_ID,JP.COMPANY_ID,JP.TIMESTAMP ");
		sql.append(" FROM JOB_GROUP JP WHERE 1=1");
		sql.append(" AND JP.PROJECT_ID = ").append(projectIdList.get(0));
		sql.append(" AND JP.SOURCE_LOCALE_ID = ").append(sourceIdList.get(0));
		sql.append(" AND JP.COMPANY_ID = ").append(currentId);

		List<JobGroup> jobGroups = new ArrayList<JobGroup>();
		JobGroup jp = null;
		List result = HibernateUtil.searchWithSql(sql.toString(), null);
		for (int i = 0; i < result.size(); i++)
		{
			jp = new JobGroup();
			Object[] bs = (Object[]) result.get(i);
			jp.setId(Long.parseLong(bs[0].toString()));
			jp.setName(bs[1].toString());
			jp.setProject(HibernateUtil.get(ProjectImpl.class,
					Long.parseLong(bs[2].toString())));
			jp.setSourceLocale((GlobalSightLocale) HibernateUtil.get(
					GlobalSightLocale.class, Long.parseLong(bs[3].toString())));
			jp.setCompanyId(Long.parseLong(bs[4].toString()));
			jp.setCreateDate((Date) bs[5]);
			jobGroups.add(jp);
		}
		return jobGroups;
	}
}
