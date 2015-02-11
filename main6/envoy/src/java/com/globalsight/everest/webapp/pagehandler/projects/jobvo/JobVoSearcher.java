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
package com.globalsight.everest.webapp.pagehandler.projects.jobvo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchHandlerHelper;
import com.globalsight.everest.webapp.webnavigation.LinkHelper;
import com.globalsight.ling.common.URLDecoder;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

public abstract class JobVoSearcher implements WebAppConstants
{
	public static final String SELECT_PART = "SELECT distinct j.id, j.name, j.priority,j.state, p.PROJECT_NAME, l.SOURCE_LOCALE_ID, j.CREATE_DATE ";
	protected static final String FROM_BASE_PART = getBaseFromSql();
	protected static final String WHERE_BASE_PART = getBaseWhereSql();
	protected Map map = new HashMap();
	protected SessionManager sessionMgr = null;
	
	protected PermissionSet userPerms;

	protected static String getBaseFromSql()
	{
		StringBuffer sb = new StringBuffer();
    	sb.append("FROM ");
    	sb.append("  JOB j LEFT OUTER JOIN L10N_PROFILE l ON j.L10N_PROFILE_ID = l.ID LEFT OUTER JOIN PROJECT p ON l.PROJECT_ID = p.PROJECT_SEQ ");
    	return sb.toString();
	}
	
	protected static String getBaseWhereSql()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("WHERE ");
    	sb.append("  1 = 1  ");

    	return sb.toString();
	}
	
	protected abstract String getSpecialFrom();
	protected abstract String getSpecialWhere();
	
	@SuppressWarnings("unchecked")
	protected String buildSql()
	{
		StringBuffer sql = new StringBuffer(SELECT_PART);
		sql.append(FROM_BASE_PART);
		sql.append(getSpecialFrom());
		sql.append(WHERE_BASE_PART);
		
    	String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
        	sql.append("  AND j.COMPANY_ID = :companyId ");
        	map.put("companyId", currentId);
        }              
        
        sql.append(getSpecialWhere());
		
		return sql.toString();
	}
	
	private void setSortValues(List<JobVo> jobVos, String p_criteria, Timestamp ts)
	{
		if (p_criteria != null)
		{
			int n = Integer.parseInt(p_criteria);
			switch (n )
	        {
	            case JobVoComparator.WORD_COUNT:
	            	setWordCount(jobVos);
	                break;
	           
	            case JobVoComparator.PLANNED_DATE:
	            	setPlannedDate(jobVos, ts);
	                break;

	            case JobVoComparator.EST_TRANSLATE_COMPLETION_DATE:
	            	setTranslateCompletionDate(jobVos, ts);
	                break;
	        }
		}
	}
	
	private void setPlannedDate(List<JobVo> jobVos, Timestamp ts)
	{
		if (jobVos.size() == 0)
			return;
		
		List<JobVo> loadJobVos = new ArrayList<JobVo>();
		for (JobVo j : jobVos)
		{
			String state = j.getStatues();
			if (!state.equals(Job.PENDING) && !state.equals(Job.BATCHRESERVED) &&
			                !state.equals(Job.IMPORTFAILED))
			{
				loadJobVos.add(j);
			}
			else
			{
				j.setPlannedCompletionDate("--");
			}
		}
		
		if (loadJobVos.size() == 0)
			return;
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT w.JOB_ID, max(w.ESTIMATED_COMPLETION_DATE) ");
		sb.append("FROM ");
		sb.append("  WORKFLOW w ");
    	sb.append("WHERE ");
    	sb.append("  W.STATE <> 'CANCELLED'  ");
    	sb.append("  AND W.STATE <> 'IMPORT_FAILED'  ");
    	sb.append("  AND w.JOB_ID in (");
    	for (int i = 0; i < loadJobVos.size() - 1; i++)
    	{
    		sb.append(loadJobVos.get(i).getId());
    		sb.append(",");
    	}
    	
    	sb.append(loadJobVos.get(loadJobVos.size() - 1).getId());
		sb.append(") group by w.JOB_ID");
		
		HashMap map = new HashMap();
		List result = HibernateUtil.searchWithSql(sb.toString(), null);
		for (int i = 0; i < result.size() ; i++) 
		{
			Object[] obs = (Object[]) result.get(i);
			map.put(obs[0].toString(), obs[1]);
		}
		
		for (JobVo j : jobVos)
		{
			Object ob = map.get(j.getId());
			if (ob != null)
			{
				ts.setDate((Date) ob);
				j.setPlannedCompletionDate(ts.toString());
			}
			else
			{
				j.setPlannedCompletionDate("--");
			}
		}
	}
	
	private void setTranslateCompletionDate(List<JobVo> jobVos, Timestamp ts)
	{
		if (jobVos.size() == 0)
			return;		
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT w.JOB_ID, max(w.ESTIMATED_COMPLETION_DATE) ");
		sb.append("FROM ");
		sb.append("  WORKFLOW w ");
    	sb.append("WHERE ");
//    	sb.append("  W.STATE <> 'CANCELLED'  ");
//    	sb.append("  AND  W.STATE <> 'IMPORT_FAILED'  ");
    	sb.append("  w.JOB_ID in (");
    	for (int i = 0; i < jobVos.size() - 1; i++)
    	{
    		sb.append(jobVos.get(i).getId());
    		sb.append(",");
    	}
    	
    	sb.append(jobVos.get(jobVos.size() - 1).getId());
		sb.append(") group by w.JOB_ID");
		
		HashMap map = new HashMap();
		List result = HibernateUtil.searchWithSql(sb.toString(), null);
		for (int i = 0; i < result.size() ; i++) 
		{
			Object[] obs = (Object[]) result.get(i);
			BigInteger jobId = (BigInteger) obs[0];
			map.put(obs[0].toString(), obs[1]);
		}
		
		for (JobVo j : jobVos)
		{
			Object ob = map.get(j.getId());
			if (ob != null)
			{
				ts.setDate((Date) ob);
				j.setEstimatedTranslateCompletionDate(ts.toString());
			}
			else
			{
				j.setEstimatedTranslateCompletionDate("--");
			}
		}
	}
	
	private void setWordCount(List<JobVo> jobVos)
	{
		if (jobVos.size() == 0)
			return;
		
		//has been setted
		if (jobVos.get(0).getWordcount() != null)
			return;
		
		StringBuffer sb = new StringBuffer();
		sb.append("select r.JOB_ID, sum(s.WORD_COUNT) from ");
		sb.append("  REQUEST r, ");
    	sb.append("  SOURCE_PAGE s ");
    	sb.append("WHERE ");
    	sb.append("  r.PAGE_ID = s.ID  ");
    	sb.append("  AND r.JOB_ID in (");
    	for (int i = 0; i < jobVos.size() - 1; i++)
    	{
    		sb.append(jobVos.get(i).getId());
    		sb.append(",");
    	}
    	
    	sb.append(jobVos.get(jobVos.size() - 1).getId());
		sb.append(") group by r.JOB_ID");
		
		HashMap map = new HashMap();
		List result = HibernateUtil.searchWithSql(sb.toString(), null);
		for (int i = 0; i < result.size() ; i++) 
		{
			Object[] obs = (Object[]) result.get(i);
			map.put(obs[0].toString(), obs[1]);
		}
		
		for (JobVo j : jobVos)
		{
			Object ob = map.get(j.getId());
			if (ob != null)
			{
				j.setWordcount(ob.toString());
			}
			else
			{
				j.setWordcount("0");
			}
		}
	}
	
    private JobSearchParameters getSearchParams(HttpServletRequest request,
            HttpSession session, User user, String searchType)
            throws EnvoyServletException
    {
        JobSearchParameters sp = new JobSearchParameters();
        sp.setUser(user);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        // adding search criteria
        if (request.getParameter("fromRequest") != null)
        {
            // New search
            sessionMgr.removeElement(JobManagementHandler.JOB_LIST_START);
            session.setAttribute(JobSearchConstants.LAST_JOB_SEARCH_TYPE,
                    searchType);
            return getRequestSearchParams(request, sessionMgr, sp,
                    user.getUserId(), searchType);
        }
        else
        {
            // Get search from session
            return getSessionSearchParams(request, session, sp, searchType);
        }
    }
    
    private JobSearchParameters getRequestSearchParams(
            HttpServletRequest request, SessionManager sessionMgr,
            JobSearchParameters sp, String userId, String searchType)
            throws EnvoyServletException
    {
        StringBuffer jobSearch = new StringBuffer();
        try
        {
            // set parameters

            // name
            String buf = (String) request
                    .getParameter(JobSearchConstants.NAME_FIELD);
            if (buf.trim().length() != 0)
            {
                sp.setJobName(buf);
                sp.setJobNameCondition((String) request
                        .getParameter(JobSearchConstants.NAME_OPTIONS));
                jobSearch
                        .append(JobSearchConstants.NAME_OPTIONS
                                + "="
                                + request
                                        .getParameter(JobSearchConstants.NAME_OPTIONS));
                jobSearch.append(":");
                jobSearch.append(JobSearchConstants.NAME_FIELD + "=" + buf);
                jobSearch.append(":");
            }

            // status
            List<String> list = new ArrayList<String>();
            String status = (String) request
                    .getParameter(JobSearchConstants.STATUS_OPTIONS);
            if (status.equals(Job.ALLSTATUS))
            {
                list.addAll(Job.ALLSTATUSLIST);
                list.addAll(Job.GRAY_STATUS_LIST);
            }
            else
            {
                if (status.equals(Job.PENDING))
                {
                    list.addAll(Job.PENDING_STATUS_LIST);
                }
                else if (status.equals(Job.EXPORTED))
                {
                    list.add(Job.EXPORTED);
                    list.add(Job.EXPORT_FAIL);
                }
                else
                {
                    list.add(status);
                }
            }
            sp.setJobState(list);
            jobSearch.append(JobSearchConstants.STATUS_OPTIONS + "=" + status);
            jobSearch.append(":");

            if (searchType.equals(JobSearchConstants.MINI_JOB_SEARCH_COOKIE))
            {
                String cookieName = JobSearchConstants.MINI_JOB_SEARCH_COOKIE
                        + userId.hashCode();
                Cookie cookie = new Cookie(cookieName, jobSearch.toString());
                sessionMgr.setAttribute(cookieName, cookie);
                return sp;
            }

            // id
            buf = (String) request.getParameter(JobSearchConstants.ID_FIELD);
            if (buf.trim().length() != 0)
            {
                sp.setJobId(buf);
                sp.setJobIdCondition(request
                        .getParameter(JobSearchConstants.ID_OPTIONS));
                jobSearch.append(JobSearchConstants.ID_OPTIONS + "="
                        + request.getParameter(JobSearchConstants.ID_OPTIONS));
                jobSearch.append(":");
                jobSearch.append(JobSearchConstants.ID_FIELD + "=" + buf);
                jobSearch.append(":");
            }

            // project
            buf = (String) request
                    .getParameter(JobSearchConstants.PROJECT_OPTIONS);
            if (!buf.equals("-1"))
            {
                sp.setProjectId(buf);
                jobSearch
                        .append(JobSearchConstants.PROJECT_OPTIONS + "=" + buf);
                jobSearch.append(":");
            }

            // source locale
            buf = (String) request.getParameter(JobSearchConstants.SRC_LOCALE);
            if (!buf.equals("-1"))
            {
                sp.setSourceLocale(ServerProxy.getLocaleManager()
                        .getLocaleById(Long.parseLong(buf)));
                jobSearch.append(JobSearchConstants.SRC_LOCALE + "=" + buf);
                jobSearch.append(":");
            }

            // target locale
            buf = (String) request.getParameter(JobSearchConstants.TARG_LOCALE);
            if (!buf.equals("-1"))
            {
                sp.setTargetLocale(ServerProxy.getLocaleManager()
                        .getLocaleById(Long.parseLong(buf)));
                jobSearch.append(JobSearchConstants.TARG_LOCALE + "=" + buf);
                jobSearch.append(":");
            }

            // priority
            buf = (String) request
                    .getParameter(JobSearchConstants.PRIORITY_OPTIONS);
            if (!buf.equals("-1"))
            {
                sp.setPriority(buf);
                jobSearch.append(JobSearchConstants.PRIORITY_OPTIONS + "="
                        + buf);
                jobSearch.append(":");
            }

            // Creation Date start num and condition
            buf = (String) request
                    .getParameter(JobSearchConstants.CREATION_START);
            if (buf.trim().length() != 0)
            {
                sp.setCreationStart(new Integer(buf));
                sp.setCreationStartCondition(request
                        .getParameter(JobSearchConstants.CREATION_START_OPTIONS));
                jobSearch.append(JobSearchConstants.CREATION_START + "=" + buf);
                jobSearch.append(":");
                jobSearch
                        .append(JobSearchConstants.CREATION_START_OPTIONS
                                + "="
                                + request
                                        .getParameter(JobSearchConstants.CREATION_START_OPTIONS));
                jobSearch.append(":");
            }

            // Creation Date end num
            buf = (String) request
                    .getParameter(JobSearchConstants.CREATION_END);
            if (buf.trim().length() != 0)
            {
                sp.setCreationEnd(new Integer(buf));
                jobSearch.append(JobSearchConstants.CREATION_END + "=" + buf);
                jobSearch.append(":");
            }

            // Creation Date end condition
            buf = (String) request
                    .getParameter(JobSearchConstants.CREATION_END_OPTIONS);
            if (!buf.equals("-1"))
            {
                sp.setCreationEndCondition(buf);
                jobSearch.append(JobSearchConstants.CREATION_END_OPTIONS + "="
                        + buf);
                jobSearch.append(":");
            }

            // Completion Date start num and condition
            buf = (String) request
                    .getParameter(JobSearchConstants.EST_COMPLETION_START);
            if (buf.trim().length() != 0)
            {
                sp.setEstCompletionStart(new Integer(buf));
                sp.setEstCompletionStartCondition(request
                        .getParameter(JobSearchConstants.EST_COMPLETION_START_OPTIONS));
                jobSearch.append(JobSearchConstants.EST_COMPLETION_START + "="
                        + buf);
                jobSearch.append(":");
                jobSearch
                        .append(JobSearchConstants.EST_COMPLETION_START_OPTIONS
                                + "="
                                + request
                                        .getParameter(JobSearchConstants.EST_COMPLETION_START_OPTIONS));
                jobSearch.append(":");
            }

            // Completion Date end num
            buf = (String) request
                    .getParameter(JobSearchConstants.EST_COMPLETION_END);
            if (buf.trim().length() != 0)
            {
                sp.setEstCompletionEnd(new Integer(buf));
                jobSearch.append(JobSearchConstants.EST_COMPLETION_END + "="
                        + buf);
                jobSearch.append(":");
            }

            // Completion Date end condition
            buf = (String) request
                    .getParameter(JobSearchConstants.EST_COMPLETION_END_OPTIONS);
            if (!buf.equals("-1"))
            {
                sp.setEstCompletionEndCondition(buf);
                jobSearch.append(JobSearchConstants.EST_COMPLETION_END_OPTIONS
                        + "=" + buf);
                jobSearch.append(":");
            }

            // Creation Date start num and condition
            buf = (String) request
                    .getParameter(JobSearchConstants.EXPORT_DATE_START);
            if (buf.trim().length() != 0)
            {
                sp.setExportDateStart(new Integer(buf));
                sp.setExportDateStartOptions(request
                        .getParameter(JobSearchConstants.EXPORT_DATE_START_OPTIONS));
                jobSearch.append(JobSearchConstants.EXPORT_DATE_START + "="
                        + buf);
                jobSearch.append(":");
                jobSearch
                        .append(JobSearchConstants.EXPORT_DATE_START_OPTIONS
                                + "="
                                + request
                                        .getParameter(JobSearchConstants.EXPORT_DATE_START_OPTIONS));
                jobSearch.append(":");
            }

            // Creation Date end num
            buf = (String) request
                    .getParameter(JobSearchConstants.EXPORT_DATE_END);
            if (buf.trim().length() != 0)
            {
                sp.setExportDateEnd(new Integer(buf));
                jobSearch
                        .append(JobSearchConstants.EXPORT_DATE_END + "=" + buf);
                jobSearch.append(":");
            }

            // Creation Date end condition
            buf = (String) request
                    .getParameter(JobSearchConstants.EXPORT_DATE_END_OPTIONS);
            if (!buf.equals("-1"))
            {
                sp.setExportDateEndOptions(buf);
                jobSearch.append(JobSearchConstants.EXPORT_DATE_END_OPTIONS
                        + "=" + buf);
                jobSearch.append(":");
            }

            String cookieName = JobSearchConstants.JOB_SEARCH_COOKIE
                    + userId.hashCode();
            Cookie cookie = new Cookie(cookieName, jobSearch.toString());
            sessionMgr.setAttribute(cookieName, cookie);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        return sp;
    }
    
    private JobSearchParameters getSessionSearchParams(
            HttpServletRequest request, HttpSession session,
            JobSearchParameters sp, String searchType)
            throws EnvoyServletException
    {
        try
        {
            Cookie cookie = JobSearchHandlerHelper.getJobSearchCookie(session,
                    request, searchType);
            if (cookie == null)
                return sp;

            String cookieValue = URLDecoder.decode(cookie.getValue());
            StringTokenizer strtok = new StringTokenizer(cookieValue, ":");
            while (strtok.hasMoreTokens())
            {
                String key = "";
                String value = "";
                try
                {
                    String tok = strtok.nextToken();
                    int idx = tok.indexOf("=");
                    key = tok.substring(0, idx);
                    value = tok.substring(idx + 1);
                }
                catch (Exception e)
                {
                    continue;
                }
                if (key.equals(JobSearchConstants.NAME_FIELD))
                {
                    if (!value.equals(""))
                        sp.setJobName(value);
                }
                else if (key.equals(JobSearchConstants.NAME_OPTIONS))
                {
                    sp.setJobNameCondition(value);
                }
                else if (key.equals(JobSearchConstants.ID_FIELD))
                {
                    if (!value.equals(""))
                        sp.setJobId(value);
                }
                else if (key.equals(JobSearchConstants.ID_OPTIONS))
                {
                    if (!value.equals("-1"))
                        sp.setJobIdCondition(value);
                }
                else if (key.equals(JobSearchConstants.STATUS_OPTIONS))
                {
                    List<String> list = new ArrayList<String>();
                    if (value.equals(Job.ALLSTATUS))
                    {
                        list.addAll(Job.ALLSTATUSLIST);
                        list.addAll(Job.GRAY_STATUS_LIST);
                    }
                    else
                    {
                        if (value.equals(Job.PENDING))
                        {
                            list.addAll(Job.PENDING_STATUS_LIST);
                        }
                        else if (value.equals(Job.EXPORTED))
                        {
                            list.add(Job.EXPORT_FAIL);
                        }
                        else
                        {
                            list.add(value);
                        }
                    }
                    sp.setJobState(list);
                }
                else if (key.equals(JobSearchConstants.PROJECT_OPTIONS))
                {
                    if (!value.equals("-1"))
                        sp.setProjectId(value);
                }
                else if (key.equals(JobSearchConstants.SRC_LOCALE))
                {
                    if (!value.equals("-1"))
                        sp.setSourceLocale(ServerProxy.getLocaleManager()
                                .getLocaleById(Long.parseLong(value)));
                }
                else if (key.equals(JobSearchConstants.TARG_LOCALE))
                {
                    if (!value.equals("-1"))
                        sp.setTargetLocale(ServerProxy.getLocaleManager()
                                .getLocaleById(Long.parseLong(value)));
                }
                else if (key.equals(JobSearchConstants.PRIORITY_OPTIONS))
                {
                    if (!value.equals("-1"))
                        sp.setPriority(value);
                }
                else if (key.equals(JobSearchConstants.CREATION_START))
                {
                    if (!value.equals(""))
                        sp.setCreationStart(new Integer(value));
                }
                else if (key.equals(JobSearchConstants.CREATION_START_OPTIONS))
                {
                    if (!value.equals("-1"))
                        sp.setCreationStartCondition(value);
                }
                else if (key.equals(JobSearchConstants.CREATION_END))
                {
                    if (!value.equals(""))
                        sp.setCreationEnd(new Integer(value));
                }
                else if (key.equals(JobSearchConstants.CREATION_END_OPTIONS))
                {
                    if (!value.equals("-1"))
                        sp.setCreationEndCondition(value);
                }
                else if (key.equals(JobSearchConstants.EST_COMPLETION_START))
                {
                    if (!value.equals(""))
                        sp.setEstCompletionStart(new Integer(value));
                }
                else if (key
                        .equals(JobSearchConstants.EST_COMPLETION_START_OPTIONS))
                {
                    if (!value.equals("-1"))
                        sp.setEstCompletionStartCondition(value);
                }
                else if (key.equals(JobSearchConstants.EST_COMPLETION_END))
                {
                    if (!value.equals(""))
                        sp.setEstCompletionEnd(new Integer(value));
                }
                else if (key
                        .equals(JobSearchConstants.EST_COMPLETION_END_OPTIONS))
                {
                    if (!value.equals("-1"))
                        sp.setEstCompletionEndCondition(value);
                }
                else if (key.equals(JobSearchConstants.EXPORT_DATE_START))
                {
                    if (!value.equals(""))
                        sp.setExportDateStart(new Integer(value));
                }
                else if (key
                        .equals(JobSearchConstants.EXPORT_DATE_START_OPTIONS))
                {
                    if (!value.equals("-1"))
                        sp.setExportDateStartOptions(value);
                }
                else if (key.equals(JobSearchConstants.EXPORT_DATE_END))
                {
                    if (!value.equals(""))
                        sp.setExportDateEnd(new Integer(value));
                }
                else if (key.equals(JobSearchConstants.EXPORT_DATE_END_OPTIONS))
                {
                    if (!value.equals("-1"))
                        sp.setExportDateEndOptions(value);
                }
            }
            return sp;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    public void setJobVos(HttpServletRequest request)
    {
    	setJobVos(request, false);
    }
	
    private List<JobVo> getContainsFailedWorkflowJob(List<JobVo> jobs)
    {
    	List<JobVo> result = new ArrayList<JobVo>();
    	if (jobs.size() == 0)
    		return result;
    	
    	List<String> jobIds = new ArrayList<String>();
    	for (JobVo j : jobs)
    	{
    		jobIds.add(j.getId());
    	}
    	
    	String ids = jobIds.toString();
    	ids = ids.substring(1, ids.length() - 1);
    	jobIds.clear();
    	
    	String sql = "SELECT w.JOB_ID FROM WORKFLOW w where w.JOB_ID in (" + ids + ") and w.STATE = 'IMPORT_FAILED'";
    	List r = HibernateUtil.searchWithSql(sql, null);
    	for (int i = 0; i < r.size() ; i++) 
		{
			Object ob = r.get(i);
			jobIds.add(ob.toString());
		}
    	
    	for (JobVo j : jobs)
    	{
    		if (jobIds.contains(j.getId()))
    		{
    			result.add(j);
    		}
    	}
    	
    	return result;
    }
    
	public void setJobVos(HttpServletRequest request, boolean setAllJobIds)
	{
		HttpSession session = request.getSession(false);
		
		String searchType = (String) request.getParameter("searchType");
        if (searchType == null)
        {
            searchType = (String) session.getAttribute("searchType");
        }
        else
        {
            session.setAttribute("searchType", searchType);
        }
        
		sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
		userPerms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);
        TimeZone timezone = (TimeZone) session.getAttribute(USER_TIME_ZONE);
        Timestamp ts = new Timestamp(Timestamp.DATE, timezone);
        ts.setLocale(uiLocale);
		
        //Gets job 
		List<JobVo> jobVos;
		if ("stateOnly".equals(searchType))
        {
			jobVos = getJobVos(ts, null);
        }
		else
		{
			User user = (User) sessionMgr.getAttribute(USER);
			JobSearchParameters searchParams = getSearchParams(request, session, user, searchType);
			jobVos = getJobVos(ts, searchParams);
		}
		
		String criteria = request.getParameter(JobManagementHandler.SORT_PARAM);
		setSortValues(jobVos, criteria, ts);
        sortJobVos(criteria, session, uiLocale, jobVos);
        int numOfJobs = jobVos.size();
        
        int jobListStart = determineStartIndex(request, session, sessionMgr);
        int jobListEnd = (jobListStart + JobManagementHandler.s_jobsPerPage) > numOfJobs ? numOfJobs
                : (jobListStart + JobManagementHandler.s_jobsPerPage);
        
        List<JobVo> nJobVo = jobVos.subList(jobListStart, jobListEnd);
        setWordCount(nJobVo);
        setPlannedDate(nJobVo, ts);
        setTranslateCompletionDate(nJobVo, ts);
        Map<String, GlobalSightLocale> locales = new HashMap<String, GlobalSightLocale>();
        boolean hasDetailPerm = userPerms
                .getPermissionFor(Permission.JOBS_DETAILS);
        List<JobVo> failedJobs = getContainsFailedWorkflowJob(nJobVo);
        
        for (JobVo job : nJobVo)
        {
        	job.setHasDetail(hasDetailPerm);
        	setDisplayStatues(job,uiLocale);
        	setStyle(job);
			setTextType(job, failedJobs);
			setDisplayLocale(job, locales, uiLocale);
        }
		
		request.setAttribute(JobManagementHandler.NUM_OF_JOBS, numOfJobs);
		request.setAttribute("jobVos", nJobVo);
		
		List<String> jobIds = new ArrayList<String>();
		if (setAllJobIds)
		{
			for (int i = 0; i < jobListStart; i++)
            {
				JobVo job = jobVos.get(i);
				jobIds.add(job.getId());
            }
            for (int i = jobListEnd + 1; i < numOfJobs; i++)
            {
            	JobVo job = jobVos.get(i);
				jobIds.add(job.getId());
            }
            
            request.setAttribute("otherJobIds", jobIds);
		}
	}
	
	private int determineStartIndex(HttpServletRequest p_request,
            HttpSession p_session, SessionManager p_sm)
    {
        int jobListStart;
        try
        {
            String jobListStartStr = (String) p_request
                    .getParameter(JobManagementHandler.JOB_LIST_START);

            jobListStart = Integer.parseInt(jobListStartStr);
            p_sm.setAttribute(JobManagementHandler.JOB_LIST_START, new Integer(jobListStart));
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
                jls = (Integer) p_sm.getAttribute(JobManagementHandler.JOB_LIST_START);
            }

            jobListStart = jls == null ? 0 : jls.intValue();
        }
        return jobListStart;
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
    protected void sortJobVos(String p_criteria, HttpSession p_session,
            Locale p_uiLocale, List p_jobs)
    {
        // first get job comparator from session
        JobVoComparator comparator = (JobVoComparator) p_session
                .getAttribute("jobVocomparator");
        if (comparator == null)
        {
            // Default: Sort by Job ID, descending, so the latest job
            // will be at the top of the list
            comparator = new JobVoComparator(JobVoComparator.JOB_ID, p_uiLocale,
                    false);
            p_session.setAttribute("jobVocomparator", comparator);
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

        Collections.sort(p_jobs, comparator);
        p_session.setAttribute(JobManagementHandler.SORT_COLUMN,
                new Integer(comparator.getSortColumn()));
        p_session.setAttribute(JobManagementHandler.SORT_ASCENDING,
                new Boolean(comparator.getSortAscending()));
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<JobVo> getJobVos(Timestamp ts, JobSearchParameters searchParams)
	{
		List result = null;
		
		if (searchParams != null)
		{
			JobVoSearchCriteria searcher = new JobVoSearchCriteria();
			result = searcher.search(searchParams);
		}
		else
		{
			String sql = buildSql();
			result = HibernateUtil.searchWithSql(sql.toString(), map);
		}
		
		List<JobVo> jobVos = new ArrayList<JobVo>();
		for (int i = 0; i < result.size() ; i++) 
		{
			Object[] obs = (Object[]) result.get(i);
			JobVo job = new JobVo();
			job.setId(obs[0].toString());
			job.setName(obs[1].toString());
			job.setPriority(obs[2].toString());
			job.setStatues(obs[3].toString());
			if (obs[4] == null)
			{
				job.setProject("--");
			}
			else
			{
				job.setProject(obs[4].toString());
			}
			
			if (obs[5] == null)
			{
				job.setSourceLocale("--");
			}
			else
			{
				job.setSourceLocale(obs[5].toString());
			}
			
			ts.setDate((Date) obs[6]);
			job.setCreateDate(ts.toString());
			
			jobVos.add(job);
		}
		
		return jobVos;
	}
    
    private void setDisplayLocale(JobVo job, Map<String, GlobalSightLocale> locales, Locale uiLocale)
    {
    	String localeId = job.getSourceLocale();
    	
    	if ("--".equals(localeId))
    	{
    	    job.setSourceLocale("--");
    	    return;
    	}
    	
    	GlobalSightLocale l = locales.get(localeId);
    	if (l == null)
    	{
    		l = HibernateUtil.get(GlobalSightLocale.class, Long.parseLong(localeId));
    		locales.put(localeId, l);
    	}
    	
    	job.setSourceLocale(l.getDisplayName(uiLocale));
    }
    
    private void setStyle(JobVo job)
    {
    	if (Job.GRAY_STATUS_LIST.contains(job.getStatues()))
        {
    		job.setStyle(" color:gray; ");
    		job.setHasDetail(false);
        }
    }
   
    private void setTextType(JobVo job, List<JobVo> failedJob)
    {
    	String state = job.getStatues();
    	if (state.equals(Job.EXPORT_FAIL)
                || failedJob.contains(job)
                || state.equals(Job.IMPORTFAILED))
        {
    		job.setTextType("warningText");
        }
    	else
    	{
    		job.setTextType("standardText");
    	}
    }
    
    @SuppressWarnings("unchecked")
    private void setDisplayStatues(JobVo job, Locale uiLocale)
    {
    	if (!job.getStatues().equals(Job.PROCESSING))
		{
			Job j = new JobImpl();
			j.setState(job.getStatues());
			job.setDisplayStatues(j.getDisplayStateByLocale(uiLocale));
		}
		else
		{
			String hql = "from RequestImpl r where r.job.id = ?";
			Collection<Request> requests = (Collection<Request>) HibernateUtil
					.search(hql, Long.parseLong(job.getId()));
			long fileCount = 0;
			Set<Long> pageNumbers = new HashSet<Long>(requests.size());
			for (Request r : requests) 
			{
				if (fileCount == 0) 
				{
					fileCount = r.getBatchInfo().getPageCount();
				}
				long pageNumber = r.getBatchInfo().getPageNumber();
				if (!pageNumbers.contains(pageNumber)) 
				{
					pageNumbers.add(pageNumber);
				}
			}
			int fileNumber = pageNumbers.size();
			job.setDisplayStatues("Processing (" + fileNumber + " of "
					+ fileCount + ")");
		}
    }
}
