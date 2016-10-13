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
package com.globalsight.everest.webapp.pagehandler.administration.reports;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.AttributeClone;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.SortUtil;

public class JobAttributeReportHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(JobAttributeReportHandler.class);
    private ArrayList<Project> projectList = null;

    @ActionHandler(action = "create", formClass = "")
    public void create(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        HttpSession session = request.getSession(false);
        User p_user = getUser(session);
        setProjects(p_user.getUserId(), request);
        setSubmitters(request);
        setJobAttributes(request);
    }

    private void setProjects(String p_user, HttpServletRequest request)
    {
        projectList = new ArrayList<Project>();
        try
        {
            projectList = (ArrayList<Project>) ServerProxy.getProjectHandler().getProjectsByUser(
                    p_user);
        }
        catch (Exception e)
        {
        }

        request.setAttribute("project", projectList);
    }

    private void setSubmitters(HttpServletRequest request)
    {
        Vector<User> users = new Vector<User>();
        try
        {
            for (Project project : projectList)
            {
                Set<String> userIds = project.getUserIds();
                for (String userid : userIds)
                {
                    if (!users.contains(ServerProxy.getUserManager().getUser(userid)))
                    {
                        users.add(ServerProxy.getUserManager().getUser(userid));
                    }
                }

            }

            request.setAttribute("users", users);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    private void setJobAttributes(HttpServletRequest request)
    {
        String hql = "from JobAttribute j";
        HashMap map = null;

        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            hql += " where j.job.companyId = :companyId";
            map = new HashMap();
            map.put("companyId", Long.parseLong(currentId));
        }

        List<JobAttribute> queryResult = (List<JobAttribute>) HibernateUtil
                .search(hql, map);
        if (queryResult != null && !queryResult.isEmpty())
        {
            filterAttributeInfoByJob(queryResult);
        }

        Set<AttributeItem> attributes = new HashSet<AttributeItem>();

        for (JobAttribute j : queryResult)
        {
            AttributeClone a = j.getAttribute();
            AttributeItem attribute = new AttributeItem(a);
            attributes.add(attribute);
        }

        List<AttributeItem> sortAttributes = new ArrayList<AttributeItem>();
        sortAttributes.addAll(attributes);
        SortUtil.sort(sortAttributes, new Comparator<AttributeItem>()
        {
            @Override
            public int compare(AttributeItem o1, AttributeItem o2)
            {
                if (o1.isFromSuper() && !o2.isFromSuper())
                    return -1;

                if (o2.isFromSuper() && !o1.isFromSuper())
                    return 1;

                return o1.getName().compareTo(o2.getName());
            }
        });

        request.setAttribute("attributes", sortAttributes);
    }

    private void filterAttributeInfoByJob(List<JobAttribute> queryResult)
    {
        Set<String> jobIds = getJobIdSet();
        for (Iterator<JobAttribute> it = queryResult.iterator(); it.hasNext();)
        {
            JobAttribute info = it.next();
            if (!jobIds.contains(String.valueOf(info.getJob().getJobId())))
            {
                it.remove();
            }
        }
    }

    private Set<String> getJobIdSet()
    {
        Set<String> jobIds = new HashSet<String>();
        ArrayList<String> stateList = ReportHelper.getAllJobStatusList();
        stateList.remove(Job.PENDING);
        List<ReportJobInfo> reportJobInfoList = new ArrayList<ReportJobInfo>(
                ReportHelper.getJobInfo(stateList).values());
        if (reportJobInfoList != null && !reportJobInfoList.isEmpty())
        {
            filterReportJobInfoByProject(reportJobInfoList);
        }
        if (reportJobInfoList != null && reportJobInfoList.size() > 0)
        {
            for (ReportJobInfo jobInfo : reportJobInfoList)
            {
                jobIds.add(String.valueOf(jobInfo.getJobId()));
            }
        }
        return jobIds;
    }

    private void filterReportJobInfoByProject(
            List<ReportJobInfo> reportJobInfoList)
    {
        Set<String> projectIds = getProjectIdSet();
        for (Iterator<ReportJobInfo> it = reportJobInfoList.iterator(); it
                .hasNext();)
        {
            ReportJobInfo info = it.next();
            if (!projectIds.contains(info.getProjectId()))
            {
                it.remove();
            }
        }
    }

    private Set<String> getProjectIdSet()
    {
        Set<String> projectIds = new HashSet<String>();
        if (projectList != null && projectList.size() > 0)
        {
            for (Project pro : projectList)
            {
                projectIds.add(String.valueOf(pro.getId()));
            }
        }
        return projectIds;
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        // TODO Auto-generated method stub
    }
}
