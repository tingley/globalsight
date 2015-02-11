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
package com.globalsight.everest.coti;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.coti.util.COTIConstants;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.cotijob.CotiJobComparator;
import com.globalsight.everest.webapp.pagehandler.administration.cotijob.CotiJobsManagement;
import com.globalsight.everest.webapp.webnavigation.LinkHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

/**
 * COTI job searcher base class
 * @author Wayzou
 *
 */
public abstract class COTIJobSearcher implements WebAppConstants
{
    public static final String SELECT_PART = "SELECT distinct j.id, j.COTI_PROJECT_ID, j.COTI_PROJECT_NAME, j.SOURCE_LANG, j.TARGET_LANG, j.DIR_NAME, j.PACKAGE_ID, "
            + "j.GLOBALSIGHT_JOB_ID, j.STATUS, p.COMPANY_ID, p.CREATION_DATE, p.FILE_NAME ";
    protected static final String FROM_BASE_PART = getBaseFromSql();
    protected static final String WHERE_BASE_PART = getBaseWhereSql();
    protected Map map = new HashMap();
    protected SessionManager sessionMgr = null;

    protected PermissionSet userPerms;

    protected static String getBaseFromSql()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("FROM ");
        sb.append(" coti_project j LEFT OUTER JOIN coti_package p ON j.PACKAGE_ID = p.ID ");
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
            sql.append("  AND p.COMPANY_ID = :companyId ");
            map.put("companyId", currentId);
        }

        sql.append(getSpecialWhere());

        return sql.toString();
    }

    private void setSortValues(List<COTIJob> jobVos, HttpSession p_session,
            String p_criteria, Timestamp ts)
    {
        int n = -1;

        if (p_criteria != null)
        {
            n = Integer.parseInt(p_criteria);
        }
        else
        {
            CotiJobComparator comparator = (CotiJobComparator) p_session
                    .getAttribute("cotiJobcomparator");
            if (comparator != null)
            {
                n = comparator.getSortColumn();
            }
        }
    }

    public void setJobVos(HttpServletRequest request)
    {
        setJobVos(request, false);
    }

    public void setJobVos(HttpServletRequest request, boolean setAllJobIds)
    {
        HttpSession session = request.getSession(false);

        String searchType = (String) request.getParameter("searchType");
        if (searchType == null)
        {
            searchType = "";
        }

        sessionMgr = (SessionManager) session.getAttribute(SESSION_MANAGER);
        userPerms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);
        TimeZone timezone = (TimeZone) session.getAttribute(USER_TIME_ZONE);
        Timestamp ts = new Timestamp(Timestamp.DATE, timezone);
        ts.setLocale(uiLocale);

        // Gets job
        List<COTIJob> jobVos;
        User user = (User) sessionMgr.getAttribute(USER);

        // if ("stateOnly".equals(searchType))
        // {
        // jobVos = getJobVos(ts, null);
        // }
        // else
        // {
        jobVos = getJobVos(ts);
        // }

        String criteria = request.getParameter(CotiJobsManagement.SORT_PARAM);
        setSortValues(jobVos, session, criteria, ts);
        sortJobVos(criteria, session, uiLocale, jobVos);
        int numOfJobs = jobVos.size();

        int numPerPage = 10;
        if (sessionMgr.getMyjobsAttribute("cotinumPerPage") != null)
            numPerPage = (Integer) sessionMgr.getMyjobsAttribute("cotinumPerPage");
        int jobListStart = determineStartIndex(request, sessionMgr);
        int jobListEnd = (jobListStart + numPerPage) > numOfJobs ? numOfJobs
                : (jobListStart + numPerPage);
        if (jobListStart > jobListEnd)
        {
            jobListStart = jobListEnd / numPerPage * numPerPage;
            if (jobListEnd % numPerPage == 0)
            {
                jobListStart = jobListStart - numPerPage;
            }
        }
        else if (jobListStart == jobListEnd)
        {
            jobListStart = (jobListEnd / numPerPage - 1) * numPerPage;
        }

        List<COTIJob> nJobVo = new ArrayList<COTIJob>();
        if (jobListEnd > 0)
        {
            nJobVo = jobVos.subList(jobListStart, jobListEnd);
        }

        Map<String, GlobalSightLocale> locales = new HashMap<String, GlobalSightLocale>();

        for (COTIJob job : nJobVo)
        {
            setTextType(job);
            setGlobalSightJob(job, uiLocale);
            setDisplayLocale(job, locales, uiLocale);
        }

        request.setAttribute(CotiJobsManagement.NUM_OF_JOBS, numOfJobs);
        request.setAttribute("cotiJobVos", nJobVo);

        List<String> jobIds = new ArrayList<String>();
        if (setAllJobIds)
        {
            for (int i = 0; i < jobListStart; i++)
            {
                COTIJob job = jobVos.get(i);
                jobIds.add(job.getJobId());
            }
            for (int i = jobListEnd + 1; i < numOfJobs; i++)
            {
                COTIJob job = jobVos.get(i);
                jobIds.add(job.getJobId());
            }

            request.setAttribute("otherJobIds", jobIds);
        }
    }

    private int determineStartIndex(HttpServletRequest p_request,
            SessionManager p_sm)
    {
        int jobListStart;
        try
        {
            String jobListStartStr = (String) p_sm
                    .getMyjobsAttribute("cotijobListStart");

            jobListStart = Integer.parseInt(jobListStartStr);
            p_sm.setAttribute(CotiJobsManagement.JOB_LIST_START, new Integer(
                    jobListStart));
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
                jls = (Integer) p_sm
                        .getAttribute(CotiJobsManagement.JOB_LIST_START);
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
        CotiJobComparator comparator = (CotiJobComparator) p_session
                .getAttribute("cotiJobcomparator");
        if (comparator == null)
        {
            // Default: Sort by Job ID, descending, so the latest job
            // will be at the top of the list
            comparator = new CotiJobComparator(CotiJobComparator.JOB_ID,
                    p_uiLocale, false);
            p_session.setAttribute("cotiJobcomparator", comparator);
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
        p_session.setAttribute(CotiJobsManagement.SORT_COLUMN, new Integer(
                comparator.getSortColumn()));
        p_session.setAttribute(CotiJobsManagement.SORT_ASCENDING, new Boolean(
                comparator.getSortAscending()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<COTIJob> getJobVos(Timestamp ts)
    {
        List result = null;

        String sql = buildSql();
        result = HibernateUtil.searchWithSql(sql.toString(), map);

        List<COTIJob> jobVos = new ArrayList<COTIJob>();
        for (int i = 0; i < result.size(); i++)
        {
            Object[] obs = (Object[]) result.get(i);
            COTIJob job = new COTIJob();
            job.setJobId(obs[0].toString());
            job.setCotiProjectId(obs[1].toString());
            job.setCotiProjectName(obs[2].toString());

            job.setSourceLang(obs[3].toString());
            job.setTargetLang(obs[4].toString());

            job.setCompanyId(obs[0].toString());
            job.setCreationDate(obs[0].toString());
            job.setCotiPackageId(obs[6].toString());

            job.setStatus(obs[8].toString());
            if (obs[7] == null)
            {
                job.setGlobalsightJobId("-1");
            }
            else
            {
                job.setGlobalsightJobId(obs[7].toString());
            }

            ts.setDate((Date) obs[10]);
            job.setCreationDate(ts.toString());

            jobVos.add(job);
        }

        return jobVos;
    }

    private void setDisplayLocale(COTIJob job,
            Map<String, GlobalSightLocale> locales, Locale uiLocale)
    {
        // source locale
        String localeId = job.getSourceLang();

        if ("--".equals(localeId))
        {
            job.setSourceLang("--");
            return;
        }

        GlobalSightLocale l = locales.get(localeId);
        if (l == null)
        {
            l = HibernateUtil.get(GlobalSightLocale.class,
                    Long.parseLong(localeId));
            locales.put(localeId, l);
        }

        job.setSourceLang(l.getDisplayName(uiLocale));

        // target locale
        String targetlocaleId = job.getTargetLang();

        if ("--".equals(targetlocaleId))
        {
            job.setTargetLang("--");
            return;
        }

        GlobalSightLocale tl = locales.get(targetlocaleId);
        if (tl == null)
        {
            tl = HibernateUtil.get(GlobalSightLocale.class,
                    Long.parseLong(targetlocaleId));
            locales.put(targetlocaleId, tl);
        }

        job.setTargetLang(tl.getDisplayName(uiLocale));
    }

    private void setTextType(COTIJob job)
    {
        String state = job.getStatus();
        if (COTIJob.isWarningText(state))
        {
            job.setTextType("warningText");
        }
        else
        {
            job.setTextType("standardText");
        }
    }

    private void setGlobalSightJob(COTIJob job, Locale uiLocale)
    {
        String gid = job.getGlobalsightJobId();
        if (gid == null || "-1".equals(gid))
        {
            job.setGlobalsightJobId("N/A");
        }
        else
        {
            try
            {
                long gggid = Long.parseLong(gid);
                if (gggid > 0)
                {

                    job.setGlobalsightJobId(gid);
                    try
                    {
                        JobImpl gsjob = HibernateUtil.get(JobImpl.class, gggid);
                        job.setGlobalsightJobStatus(gsjob
                                .getDisplayStateByLocale(uiLocale));

                        String state = gsjob.getState();
                        if (state.equals(Job.EXPORT_FAIL)
                                || state.equals(Job.IMPORTFAILED))
                        {
                            job.setTextType("warningText");
                        }
                    }
                    catch (Exception ex)
                    {
                        job.setGlobalsightJobStatus(COTIConstants.project_status_unknown);
                    }
                }
                else
                {
                    job.setGlobalsightJobId("N/A");
                }
            }
            catch (Exception e)
            {
                job.setGlobalsightJobId("N/A");
            }
        }
    }
}
