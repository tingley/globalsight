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
package com.globalsight.everest.workflowmanager;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class WorkflowExportingHelper
{
    static private final Logger logger = Logger
            .getLogger(WorkflowExportingHelper.class);

    private static Object LOCK = new Object();

    public static boolean isExporting(long workflowId)
    {
        return getWorkflowExporting(workflowId) != null;
    }

    private static WorkflowExporting getWorkflowExporting(long workflowId)
    {
        String hql = "from WorkflowExporting w where w.workflowId = ?";
        Object ob = HibernateUtil.getFirst(hql, workflowId);

        return (WorkflowExporting) ob;
    }

    public static void setAsExporting(ArrayList<Long> workflowIds)
    {
        synchronized (LOCK)
        {
            for (Long id : workflowIds)
            {
                if (!isExporting(id))
                {
                    WorkflowExporting w = new WorkflowExporting();
                    w.setWorkflowId(id);
                    HibernateUtil.saveOrUpdate(w);
                }
            }
        }
    }

    public static void setAsNotExporting(long workflowId)
    {
        synchronized (LOCK)
        {
            WorkflowExporting w = getWorkflowExporting(workflowId);
            if (w == null)
            {
                return;
            }
            HibernateUtil.getSession().evict(w);
            w = getWorkflowExporting(workflowId);

            if (w != null)
            {
                try
                {
                    HibernateUtil.delete(w);
                }
                catch (Exception e)
                {
                    // logger.error(e);
                }
            }
        }
    }

    public static void setPageAsNotExporting(long targetPageId)
    {
        TargetPage targetPage = HibernateUtil.get(TargetPage.class,
                targetPageId);
        if (targetPage != null)
        {
            setAsNotExporting(targetPage.getWorkflowInstance().getId());
        }
    }
    
    //For secondary target file exporting
    public static void setStfAsNotExporting(long stfId)
    {
    	SecondaryTargetFile stf = HibernateUtil.get(SecondaryTargetFile.class,
        		stfId);
        if (stf != null)
        {
            setAsNotExporting(stf.getWorkflow().getId());
        }
    }

    public static void cleanTable()
    {
        String hql = "delete WorkflowExporting";
        HibernateUtil.excute(hql);
    }
    
    public static int getExportingWorkflowNumber(Boolean isSuperAdmin, long companyId)
    {
        StringBuffer hql = new StringBuffer("select count(id) from WorkflowExporting ");
        if(!isSuperAdmin)
        {       	
        	hql.append(" w,WorkflowImpl wf  where w.workflowId = wf.id ")
        			.append("and wf.companyId = ").append(companyId);
        }
        return HibernateUtil.count(hql.toString());
    }
}
