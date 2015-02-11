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

package com.globalsight.persistence.jobcreation;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.everest.workflowmanager.WorkflowOwner;

public class InsertJobCommand
{
    private JobImpl m_job;

    private RequestImpl m_request;

    private List m_listOfWorkflows;

    public InsertJobCommand(JobImpl p_job, RequestImpl p_request,
            List p_listOfWorkflows)
    {
        m_job = p_job;
        m_request = p_request;
        m_listOfWorkflows = p_listOfWorkflows;
    }

    public JobImpl persistObjects(Session session) throws PersistenceException
    {
        try
        {
            long companyId = m_request.getCompanyId();

            m_job.setCreateDate(new Timestamp(System.currentTimeMillis()));
            m_job.setPriority(m_request.getL10nProfile().getPriority());
            m_job.setIsWordCountReached(false);
            m_job.setTimestamp(new Timestamp(System.currentTimeMillis()));
            m_job.setPageCount(1);
            m_job.setCompanyId(companyId);
            m_request.setJob(m_job);
            boolean isDefaultContextMatch = PageHandler
                    .isDefaultContextMatch(m_request);
            boolean isInContextMatch = PageHandler.isInContextMatch(m_request);
            if (isDefaultContextMatch)
            {
                m_job.setLeverageOption(Job.DEFAULT_CONTEXT);
            }
            else if (isInContextMatch)
            {
                m_job.setLeverageOption(Job.IN_CONTEXT);
            }
            else
            {
                m_job.setLeverageOption(Job.EXACT_ONLY);
            }

            m_request.setTimestamp(new Timestamp(System.currentTimeMillis()));
            List requtests = new ArrayList();
            requtests.add(m_request);
            m_job.setRequestList(requtests);

            Iterator it = m_listOfWorkflows.iterator();
            while (it.hasNext())
            {
                WorkflowImpl workflow = (WorkflowImpl) it.next();
                workflow.setJob(m_job);
                workflow.setTimestamp(new Timestamp(System.currentTimeMillis()));
                workflow.setCompanyId(companyId);
                List workflows = new ArrayList();
                workflows.add(workflow);
                m_job.setWorkflowInstances(workflows);

                // create the workflow owners for each workflow
                List wfOwners = workflow.getWorkflowOwners();

                for (int i = 0; i < wfOwners.size(); i++)
                {
                    WorkflowOwner wfo = (WorkflowOwner) wfOwners.get(i);
                    wfo.setWorkflow(workflow);
                }

                // go through all tasks for the workflow
                Collection tasks = workflow.getTasks().values();
                for (Iterator i = tasks.iterator(); i.hasNext();)
                {
                    TaskImpl t = (TaskImpl) i.next();
                    t.setStateStr("DEACTIVE");
                    t.setCompanyId(companyId);
                }
            }

            session.save(m_job);
            return m_job;
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
    }
}
