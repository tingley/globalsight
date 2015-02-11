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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.AttributeClone;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class JobAttributeReportHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(JobAttributeReportHandler.class);

    @ActionHandler(action = "create", formClass = "")
    public void create(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        setProjects(request);
        setSubmitters(request);
        setJobAttributes(request);
    }

    private void setProjects(HttpServletRequest request)
    {
        String hql = "from ProjectImpl p where p.isActive = 'Y'";
        HashMap map = null;

        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            hql += " and p.companyId = :companyId";
            map = new HashMap();
            map.put("companyId", currentId);
        }

        hql += " order by p.name";

        List queryResult = HibernateUtil.search(hql, map);
        request.setAttribute("project", queryResult);
    }

    private void setSubmitters(HttpServletRequest request)
    {
        Vector<User> users;
        try
        {
            users = ServerProxy.getUserManager().getUsers();
            for (int i = users.size() - 1; i >=0;i--)
            {
                if (UserUtil.isSuperAdmin(users.get(i).getUserId()))
                {
                    users.remove(i);
                }
            }
            request.setAttribute("users", users);
        }
        catch (Exception e)
        {
            logger.error(e);
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
            map.put("companyId", currentId);
        }

        List<JobAttribute> queryResult = (List<JobAttribute>) HibernateUtil
                .search(hql, map);
        Set<AttributeItem> attributes = new HashSet<AttributeItem>();

        for (JobAttribute j : queryResult)
        {
            AttributeClone a = j.getAttribute();
            AttributeItem attribute = new AttributeItem(a);
            attributes.add(attribute);
        }

        List<AttributeItem> sortAttributes = new ArrayList<AttributeItem>();
        sortAttributes.addAll(attributes);
        Collections.sort(sortAttributes, new Comparator<AttributeItem>()
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