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

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.jobhandler.JobGroup;
import com.globalsight.everest.jobhandler.JobGroupSearchParameters;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.projecthandler.ProjectInfo;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.localepairs.LocalePairConstants;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateHandlerHelper;
import com.globalsight.everest.webapp.webnavigation.LinkHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;

public class JobGroupHandlerHelper extends PageHandler
{
	protected static SessionManager sessionMgr = null;
	private static StringBuffer sql = null;
	private static Map<String, Object> params = null;
	public static String BASE_BEAN = "self";

	/**
	 * Get all the job groups.
	 * 
	 * @return List<JobGroup> return all the job groups
	 * @throws ParseException
	 */
	public static void getAllJobGroups(HashMap beanMap,
			HttpServletRequest p_request)
	{
		HttpSession session = p_request.getSession(false);
		Locale uiLocale = (Locale) session.getAttribute(UILOCALE);
		String currentId = CompanyThreadLocal.getInstance().getValue();
		JobGroupSearchParameters searchParams = getSearchParams(p_request,
				session);
		try
		{
			List<JobGroup> jobGroups = getJobGroups(p_request, currentId,
					searchParams);
			String criteria = p_request
					.getParameter(JobManagementHandler.SORT_PARAM);
			sortJobVos(criteria, session, uiLocale, jobGroups);
			getPage(p_request, jobGroups);
			p_request.setAttribute(
					JOB_GROUP_PAGING_SCRIPTLET,
					getPagingText(p_request, ((NavigationBean) beanMap
							.get(BASE_BEAN)).getPageURL()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		List<ProjectImpl> projectList = getAllProjects(currentId);
		List<GlobalSightLocale> localeList = getAllLocales(currentId);
		p_request.setAttribute("projects", projectList);
		p_request.setAttribute("sourceLocales", localeList);
	}

	@SuppressWarnings("unchecked")
	public static void newJobGroup(HttpServletRequest p_request)
	{
		HttpSession session = p_request.getSession(false);
		Locale uiLocale = (Locale) session.getAttribute(UILOCALE);
		List<ProjectInfo> projectInfos = WorkflowTemplateHandlerHelper
				.getAllProjectInfos(uiLocale);
		Map<String, List<GlobalSightLocale>> map = new HashMap<String, List<GlobalSightLocale>>();
		Collection<Project> p_projects = null;
		try
		{
			for (int i = 0; i < projectInfos.size(); i++)
			{
				p_projects = new ArrayList<Project>();
				long id = projectInfos.get(i).getProjectId();
				Project project = HibernateUtil.get(ProjectImpl.class, id);
				p_projects.add(project);
				Collection<L10nProfile> l10nProfiles = ServerProxy
						.getProjectHandler().getL10nProfiles(p_projects);
				List<GlobalSightLocale> list = new ArrayList<GlobalSightLocale>();
				Iterator it = l10nProfiles.iterator();
				while (it.hasNext())
				{
					L10nProfile profile = (L10nProfile) it.next();
					if (!list.contains(profile.getSourceLocale()))
						list.add(profile.getSourceLocale());
				}
				map.put(project.getId() + "", list);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		String existNames = getAllJobGroupNmes();
		p_request.setAttribute(CUSTOMIZE_REPORTS_PROJECT_LIST, projectInfos);
		p_request.setAttribute(LocalePairConstants.LOCALES, map);
		p_request.setAttribute(JOB_GROUP_EXISTNAMES, existNames);
		FormUtil.addSubmitToken(p_request, FormUtil.Forms.NEW_JOB_GROUP);
	}

	public static void saveJobGroup(HttpServletRequest p_request)
	{
		HttpSession session = p_request.getSession(false);
		SessionManager sessionMgr = (SessionManager) session.getAttribute(SESSION_MANAGER);
		if (FormUtil.isNotDuplicateSubmisson(p_request,
				FormUtil.Forms.NEW_JOB_GROUP))
		{
			String name = EditUtil.utf8ToUnicode((String) p_request
					.getParameter(JOB_GROUP_NAME));
			String project = EditUtil.utf8ToUnicode((String) p_request
					.getParameter(JOB_GROUP_PROJECT));
			String sourceLocale = EditUtil.utf8ToUnicode((String) p_request
					.getParameter(JOB_GROUP_SOURCELOCAL));
			String currentId = CompanyThreadLocal.getInstance().getValue();
			User user = (User) sessionMgr.getAttribute(USER);
			JobGroup group = new JobGroup();
			group.setName(name);
			group.setProject((ProjectImpl) HibernateUtil.get(ProjectImpl.class,
					Long.parseLong(project)));
			group.setSourceLocale((GlobalSightLocale) HibernateUtil.get(
					GlobalSightLocale.class, Long.parseLong(sourceLocale)));
			group.setCompanyId(Long.parseLong(currentId));
			group.setCreateDate(new Date());
			group.setCreateUserId(user.getUserId());
			try
			{
				HibernateUtil.save(group);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			FormUtil.removeSubmitToken(p_request, FormUtil.Forms.NEW_JOB_GROUP);
		}
	}
	public static Map<String,Object> checkJobGroup(HttpServletRequest p_request)
	{
		String errorId = "";
		String rightId = "";
		Map<String, String> param = null;
		String sql = "SELECT * FROM JOB WHERE GROUP_ID =:groupId";
		String selectIds = (String) p_request
				.getParameter(JOB_GROUP_SELECT_RADIO_BTN);
		if (StringUtil.isNotEmpty(selectIds))
		{
			String[] selectIdArr = selectIds.split(",");
			if (selectIdArr != null && selectIdArr.length > 0)
			{
				for (String id : selectIdArr)
				{
					param = new HashMap<String, String>();
					param.put("groupId", id);
					List list = HibernateUtil.searchWithSql(sql, param);
					if (list != null && list.size() > 0)
					{
						if (errorId != "")
							errorId += ",";
						errorId += id;
					}
					else
					{
						if (rightId != "")
							rightId += ",";
						rightId += id;
					}
				}
			}
		}
		Map<String, Object> map = new HashMap<String, Object>();
		if (errorId.trim().length() > 0)
		{
			map.put("wrong", errorId);
		}
		else
		{
			map.put("wrong", "needRemove");
		}
		if (rightId.trim().length() > 0)
		{
			map.put("right", rightId);
		}
		else
		{
			map.put("right", "noNeedRemove");
		}
		return map;
	}
	public static void removeJobGroup(HttpServletRequest p_request)
	{
		String selectIds = (String) p_request
				.getParameter(JOB_GROUP_SELECT_RADIO_BTN);
		if (StringUtil.isNotEmpty(selectIds))
		{
			String[] selectIdArr = selectIds.split(",");
			List<JobGroup> list = new ArrayList<JobGroup>();
			if (selectIdArr != null && selectIdArr.length > 0)
			{
				for (String id : selectIdArr)
				{
					JobGroup jobGroup = getJobGroup(id);
					list.add(jobGroup);
				}
				try
				{
					HibernateUtil.delete(list);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private static JobGroup getJobGroup(String id)
	{
		String hql = "from JobGroup where id = " + id;
		JobGroup jobGroup = (JobGroup) HibernateUtil.getFirst(hql);
		return jobGroup;
	}

	private static List<JobGroup> getJobGroups(HttpServletRequest request,
			String companyId, JobGroupSearchParameters searchParams)
	{
		sql = new StringBuffer();
		sql.append("SELECT JP.ID,JP.NAME,JP.PROJECT_ID,JP.SOURCE_LOCALE_ID,JP.COMPANY_ID,JP.TIMESTAMP,JP.CREATE_USER_ID ");
		sql.append(" FROM JOB_GROUP JP WHERE 1=1");
		if (searchParams != null)
		{
			getJobGroupsByParams(searchParams);
		}
		createCompanyExpression();
		HibernateUtil.closeSession();

		List<JobGroup> jobGroups = new ArrayList<JobGroup>();
		JobGroup jp = null;
		List result = HibernateUtil.searchWithSql(sql.toString(), params);
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
            jp.setCreateUserId(bs[6] == null ? "" : bs[6].toString());

			jobGroups.add(jp);
		}
		return jobGroups;
	}

	private static void getJobGroupsByParams(
			JobGroupSearchParameters searchParams)
	{
		params = new HashMap<String, Object>();
		Map<?, ?> criteria = searchParams.getParameters();
		// get the keys of the map
		Object[] keys = criteria.keySet().toArray();
		int mapSize = keys.length;

		// loop throught the parameters to create the sql statement.
		for (int i = 0; i < mapSize; i++)
		{
			switch (((Integer) (keys[i])).intValue())
			{
			// job name
				case JobGroupSearchParameters.JOB_GROUP_NAME:
					jobGroupName(keys[i], criteria);
					break;

				// job id
				case JobGroupSearchParameters.JOB_GROUP_ID:
					jobGroupId(keys[i], criteria);
					break;

				// job state
				case JobGroupSearchParameters.JOB_GROUP_PROJECT_ID:
					jobGroupProject(keys[i], criteria);
					break;

				// priority
				case JobGroupSearchParameters.JOB_GROUP_SOURCE_LOCALE:
					sourceLocale(keys[i], criteria);
					break;
				default:
					break;
			}
		}
	}

	private static void sortJobVos(String p_criteria, HttpSession p_session,
			Locale p_uiLocale, List p_jobs)
	{
		// first get job comparator from session
		JobGroupComparator comparator = (JobGroupComparator) p_session
				.getAttribute("jobGroupcomparator");
		if (comparator == null)
		{
			// Default: Sort by Job ID, descending, so the latest job
			// will be at the top of the list
			comparator = new JobGroupComparator(
					JobGroupComparator.JOB_GROUP_ID, p_uiLocale, false);
			p_session.setAttribute("jobGroupcomparator", comparator);
		}

		if (p_criteria != null)
		{
			int sortCriteria = Integer.parseInt(p_criteria);

			if (comparator.getSortColumn() == sortCriteria)
			{
				// just reverse the sort order
				comparator.reverseSortingOrder();
			}
			else
			{
				// set the sort column
				comparator.setSortColumn(sortCriteria);
			}
		}

		SortUtil.sort(p_jobs, comparator);
		p_session.setAttribute(JOB_GROUP_SORT_COLUMN,
				new Integer(comparator.getSortColumn()));
		p_session.setAttribute(JOB_GROUP_SORT_ASCENDING,
				new Boolean(comparator.getSortAscending()));
	}

	private static void jobGroupName(Object p_key, Map<?, ?> criteria)
	{
		String groupName = (String) criteria.get(p_key);
		groupName = dealWithCondition(groupName, "CT");
		sql.append(" and JP.NAME like :groupName ");
		params.put("groupName", groupName);
	}

	private static void jobGroupProject(Object p_key, Map<?, ?> criteria)
	{
		String projectId = (String) criteria.get(p_key);
		sql.append(" and JP.PROJECT_ID in (").append(Long.valueOf(projectId))
				.append(") ");
	}

	private static void jobGroupId(Object p_key, Map<?, ?> criteria)
	{
		String jobGroupId = (String) criteria.get(p_key);
		sql.append(" and JP.ID = :jobGroupId ");
		params.put("jobGroupId", Long.valueOf(jobGroupId));
	}

	private static void sourceLocale(Object p_key, Map<?, ?> criteria)
	{
		GlobalSightLocale srcLocale = (GlobalSightLocale) criteria.get(p_key);
		sql.append(" and JP.SOURCE_LOCALE_ID = :jobGroupSourceLocale ");
		params.put("jobGroupSourceLocale", srcLocale.getIdAsLong());
	}

	private static void createCompanyExpression()
	{
		String currentId = CompanyThreadLocal.getInstance().getValue();
		if (currentId == null || currentId.trim().length() == 0
				|| currentId.equals(CompanyWrapper.SUPER_COMPANY_ID))
		{
			return;
		}
		sql.append(" and JP.COMPANY_ID = " + Long.parseLong(currentId.trim()));
	}

	private static String dealWithCondition(String s, String condition)
	{
		s = s.toLowerCase().trim();
		// select values between p_firstValue and p_secondValue
		if (SearchCriteriaParameters.CONTAINS.equals(condition))
		{
			s = "%" + s + "%";
		}
		return s;
	}

	private static JobGroupSearchParameters getSearchParams(
			HttpServletRequest request, HttpSession session)
			throws EnvoyServletException
	{
		JobGroupSearchParameters jgsp = new JobGroupSearchParameters();
		SessionManager sessionMgr = (SessionManager) session
				.getAttribute(WebAppConstants.SESSION_MANAGER);

		// adding search criteria
		if (request.getParameter("fromRequest") != null)
		{
			// New search
			sessionMgr.removeElement(JobManagementHandler.JOB_LIST_START);
			return getRequestSearchParams(request, sessionMgr, jgsp);
		}
		else
		{
			// Get search from session
			return getSessionSearchParams(sessionMgr, session, jgsp);
		}
	}

	private static JobGroupSearchParameters getRequestSearchParams(
			HttpServletRequest request, SessionManager sessionMgr,
			JobGroupSearchParameters sp) throws EnvoyServletException
	{
		try
		{
			// id
			String buf = (String) request.getParameter("groupId");
			if (buf != null && !buf.equals("null") && buf.trim().length() != 0)
			{
				sp.setJobGroupId(buf);
			}
			// name
			buf = (String) request.getParameter("groupName");
			if (buf != null && !buf.equals("null") && buf.trim().length() != 0)
			{
				buf = new String(buf.getBytes("ISO8859-1"),"UTF-8");
				sp.setJobGroupName(buf);
			}
			// project
			buf = (String) request.getParameter("groupProject");
			if (buf != null && !buf.equals("null") && !buf.equals("-1"))
			{
				sp.setJobGroupProjectId(buf);
			}
			// source locale
			buf = (String) request.getParameter("groupLocale");
			if (buf != null && !buf.equals("null") && !buf.equals("-1"))
			{
				sp.setJobGroupSourceLocale(ServerProxy.getLocaleManager()
						.getLocaleById(Long.parseLong(buf)));
			}
		}
		catch (Exception e)
		{
			throw new EnvoyServletException(e);
		}

		return sp;
	}

	private static JobGroupSearchParameters getSessionSearchParams(
			SessionManager sessionMgr, HttpSession session,
			JobGroupSearchParameters sp) throws EnvoyServletException
	{
		try
		{
			String temp = (String) sessionMgr.getAttribute("groupIdFilter");
			if (!temp.equals(""))
			{
				sp.setJobGroupId(temp);
			}
			temp = (String) sessionMgr.getAttribute("groupNameFilter");
			if (!temp.equals(""))
			{
				sp.setJobGroupName(temp);
			}
			temp = (String) sessionMgr.getAttribute("groupProjectFilter");
			if (!temp.equals("-1"))
			{
				sp.setJobGroupProjectId((String) sessionMgr
						.getAttribute("groupProjectFilter"));
			}
			temp = (String) sessionMgr.getAttribute("sourceLocaleFilter");
			if (!temp.equals("-1"))
			{
				sp.setJobGroupSourceLocale(ServerProxy.getLocaleManager()
						.getLocaleById(Long.parseLong(temp)));
			}
			return sp;
		}
		catch (Exception e)
		{
			throw new EnvoyServletException(e);
		}
	}

	private static String getAllJobGroupNmes()
	{
		String currentId = CompanyThreadLocal.getInstance().getValue();
		String sql = "SELECT JG.NAME FROM JOB_GROUP JG WHERE JG.COMPANY_ID = "
				+ currentId;
		List<String> result = (List<String>) HibernateUtil.searchWithSql(
				sql.toString(), null);
		StringBuffer returnStr = new StringBuffer(",");
		for (int i = 0; i < result.size(); i++)
		{
			returnStr.append(result.get(i) + ",");
		}
		return returnStr.toString();
	}

	private static List<ProjectImpl> getAllProjects(String companyId)
	{
		String sql = "SELECT DISTINCT JG.PROJECT_ID FROM JOB_GROUP JG WHERE JG.COMPANY_ID = "
				+ companyId;
		List result = (List) HibernateUtil.searchWithSql(sql.toString(), null);
		List<ProjectImpl> list = new ArrayList<ProjectImpl>();
		for (int i = 0; i < result.size(); i++)
		{
			Object obs = (Object) result.get(i);
			ProjectImpl project = HibernateUtil.get(ProjectImpl.class,
					Long.parseLong(obs.toString()));
			list.add(project);
		}
		return list;
	}

	private static List<GlobalSightLocale> getAllLocales(String companyId)
	{
		String sql = "SELECT DISTINCT JG.SOURCE_LOCALE_ID FROM JOB_GROUP JG WHERE JG.COMPANY_ID = "
				+ companyId;
		List result = (List) HibernateUtil.searchWithSql(sql.toString(), null);
		List<GlobalSightLocale> list = new ArrayList<GlobalSightLocale>();
		for (int i = 0; i < result.size(); i++)
		{
			Object obs = (Object) result.get(i);
			GlobalSightLocale locale = HibernateUtil.get(
					GlobalSightLocale.class, Long.parseLong(obs.toString()));
			list.add(locale);
		}
		return list;
	}

	private static void getPage(HttpServletRequest p_request,
			List<JobGroup> jobGroups)
	{
		HttpSession session = p_request.getSession(false);
		sessionMgr = (SessionManager) session.getAttribute(SESSION_MANAGER);
		int numOfJobs = jobGroups.size();

		int numPerPage = 20;
		if (sessionMgr.getAttribute("numPerPage") != null)
			numPerPage = (Integer) sessionMgr.getAttribute("numPerPage");
		int jobGroupListStart = determineStartIndex(p_request, sessionMgr);
		int jobGroupListEnd = (jobGroupListStart + numPerPage) > numOfJobs ? numOfJobs
				: (jobGroupListStart + numPerPage);
		if (jobGroupListStart > jobGroupListEnd)
		{
			jobGroupListStart = jobGroupListEnd / numPerPage * numPerPage;
			if (jobGroupListEnd % numPerPage == 0)
			{
				jobGroupListStart = jobGroupListStart - numPerPage;
			}
		}
		else if (jobGroupListStart == jobGroupListEnd)
		{
			jobGroupListStart = (jobGroupListEnd / numPerPage - 1) * numPerPage;
		}

		List<JobGroup> returnJobGroups = new ArrayList<JobGroup>();
		if (jobGroupListEnd > 0)
		{
			returnJobGroups = jobGroups.subList(jobGroupListStart,
					jobGroupListEnd);
		}
		p_request.setAttribute(NUM_OF_JOB_GROUPS, numOfJobs);
		p_request.setAttribute("allGroups", returnJobGroups);
	}

	protected static String getPagingText(HttpServletRequest p_request,
			String p_baseURL) throws RemoteException, EnvoyServletException
	{
		HttpSession session = p_request.getSession(false);
		SessionManager sessionMgr = (SessionManager) session
				.getAttribute(SESSION_MANAGER);
		ResourceBundle bundle = getBundle(session);

		int numOfJobs = ((Integer) p_request.getAttribute(NUM_OF_JOB_GROUPS))
				.intValue();
		int jobGroupListStart = determineStartIndex(p_request, sessionMgr);

		int numPerPage = 20;
		if (sessionMgr.getAttribute("numPerPage") != null)
			numPerPage = (Integer) sessionMgr.getAttribute("numPerPage");
		int numOfPages = numOfJobs / numPerPage;
		if (numOfPages * numPerPage != numOfJobs)
			numOfPages++;
		int jobListEnd = (jobGroupListStart + numPerPage) > numOfJobs ? numOfJobs
				: (jobGroupListStart + numPerPage);
		if (jobGroupListStart > jobListEnd)
		{
			jobGroupListStart = jobListEnd / numPerPage * numPerPage;
			if (jobListEnd % numPerPage == 0)
			{
				jobGroupListStart = jobGroupListStart - numPerPage;
			}
		}
		else if (jobGroupListStart == jobListEnd)
		{
			jobGroupListStart = (jobListEnd / numPerPage - 1) * numPerPage;
		}
		int curPage = jobGroupListStart / numPerPage + 1;
		int numOfPagesInGroup = NUMBER_OF_PAGES_IN_GROUP;
		int pagesOnLeftOrRight = numOfPagesInGroup / 2;

		/*
		 * Note that these two integers, jobGroupListStart and jobListEnd, are
		 * the *indexes* of the list so for display we'll add 1 so it will look
		 * good for the user. This is also why I subtract 1 from jobListEnd
		 * below, so that jobListEnd will be an index value
		 */
		jobListEnd = jobListEnd - 1;

		StringBuffer sb = new StringBuffer();

		// Make the Paging widget
		if (numOfJobs > 0)
		{
			Object[] args = { new Integer(jobGroupListStart + 1),
					new Integer(jobListEnd + 1), new Integer(numOfJobs) };

			// "Displaying 1 - 20 of 35"
			sb.append(
					MessageFormat.format(
							bundle.getString("lb_displaying_records"), args))
					.append("&nbsp");

			// The "First" link
			if (jobGroupListStart == 0)
			{
				// Don't hyperlink "First" if it's the first page
				sb.append("<SPAN CLASS=standardTextGray>"
						+ bundle.getString("lb_first") + "</SPAN> | ");
			}
			else
			{
				sb.append("<A CLASS=standardHREF onclick='return addFilters(this)' HREF="
						+ p_baseURL
						+ "&jobGroupListStart=0>"
						+ bundle.getString("lb_first") + "</A> | ");
			}

			// The "Previous" link
			if (jobGroupListStart == 0)
			{
				// Don't hyperlink "Previous" if it's the first page
				sb.append("<SPAN CLASS=standardTextGray>"
						+ bundle.getString("lb_previous") + "</SPAN> | ");
			}
			else
			{
				sb.append("<A CLASS=standardHREF onclick='return addFilters(this)' HREF="
						+ p_baseURL
						+ "&jobGroupListStart="
						+ (jobGroupListStart - numPerPage)
						+ ">"
						+ bundle.getString("lb_previous") + "</A> | ");
			}

			// Show page numbers 1 2 3 4 5 etc...
			for (int i = 1; i <= numOfPages; i++)
			{
				int topJob = (numPerPage * i) - numPerPage;
				if (((curPage <= pagesOnLeftOrRight) && (i <= numOfPagesInGroup))
						|| (((numOfPages - curPage) <= pagesOnLeftOrRight) && (i > (numOfPages - numOfPagesInGroup)))
						|| ((i <= (curPage + pagesOnLeftOrRight)) && (i >= (curPage - pagesOnLeftOrRight))))
				{
					if (jobGroupListStart == topJob)
					{
						// Don't hyperlink this page if it's current
						sb.append("<SPAN CLASS=standardTextGray>" + i
								+ "</SPAN>&nbsp");
					}
					else
					{
						sb.append("<A CLASS=standardHREF onclick='return addFilters(this)' HREF="
								+ p_baseURL
								+ "&jobGroupListStart="
								+ (topJob)
								+ ">" + i + "</A>&nbsp");
					}
				}
			}

			// The "Next" link
			if ((jobGroupListStart + numPerPage) >= numOfJobs)
			{
				// Don't hyperlink "Next" if it's the last page
				sb.append("| &nbsp" + "<SPAN CLASS=standardTextGray>"
						+ bundle.getString("lb_next") + "</SPAN> | ");
			}
			else
			{
				sb.append("| &nbsp"
						+ "<A CLASS=standardHREF onclick='return addFilters(this)' HREF="
						+ p_baseURL + "&jobGroupListStart="
						+ (jobGroupListStart + numPerPage) + ">"
						+ bundle.getString("lb_next") + "</A> | ");
			}

			// The "Last" link
			int lastJob = numOfJobs - 1; // Index of last job
			int numJobsOnLastPage = numOfJobs % numPerPage == 0 ? numPerPage
					: numOfJobs % numPerPage;
			if ((lastJob - jobGroupListStart) < numPerPage)
			{
				// Don't hyperlink "Last" if it's the Last page
				sb.append("<SPAN CLASS=standardTextGray>"
						+ bundle.getString("lb_last") + "</SPAN>");
			}
			else
			{
				sb.append("<A CLASS=standardHREF onclick='return addFilters(this)' HREF="
						+ p_baseURL
						+ "&jobGroupListStart="
						+ (lastJob - (numJobsOnLastPage - 1))
						+ ">"
						+ bundle.getString("lb_last") + "</A>");
			}
		}
		else
		{
			sb.append(bundle.getString("lb_displaying_zero"));
		}

		return sb.toString();
	}

	private static int determineStartIndex(HttpServletRequest p_request,
			SessionManager p_sm)
	{
		int jobGroupListStart;
		try
		{
			String jobGroupListStartStr = (String) p_sm
					.getAttribute(JOB_GROUP_LIST_START);

			jobGroupListStart = Integer.parseInt(jobGroupListStartStr);
			p_sm.setAttribute(JOB_GROUP_LIST_START, jobGroupListStartStr);
		}
		// this exception happens if you go to My Jobs from menu items.
		// Also when you click on sorting columns.
		catch (NumberFormatException e)
		{
			String activityName = (String) p_request
					.getParameter(LinkHelper.ACTIVITY_NAME);
			Integer jls = null;
			// If user clicks on menu item, activityName in not null.
			// Therefore, we should not preserve paging.
			if (activityName == null)
			{
				jls = (Integer) p_sm.getAttribute(JOB_GROUP_LIST_START);
			}

			jobGroupListStart = jls == null ? 0 : jls.intValue();
		}
		return jobGroupListStart;
	}
}