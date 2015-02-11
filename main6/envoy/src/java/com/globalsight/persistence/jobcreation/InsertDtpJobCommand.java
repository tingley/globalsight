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

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.everest.workflowmanager.WorkflowOwner;
import com.globalsight.persistence.PersistenceCommand;

public class InsertDtpJobCommand extends PersistenceCommand
{
    private String m_insertWorkflow = "insert into workflow(iflow_instance_id,state ,target_locale_id,job_id,fraction, duration, timestamp, type, company_id)"
            + "values (?,?,?,?,?,?,?,?,?)";
    private String m_insertWorkflowOwners = "insert into workflow_owner(id, workflow_id, owner_id, owner_type)"
            + "values (null,?,?,?)";
    private String m_insertTask = "insert into task_info(task_id, workflow_id, name, state, expense_rate_id, revenue_rate_id, rate_selection_criteria, type, task_type, company_id) values(?,?,?,?,?,?,?,?,?,?)";
    private PreparedStatement m_ps2; // workflows
    private PreparedStatement m_ps3; // tasks
    private PreparedStatement m_ps4; // workflow owners
    private Job m_job;
    private List m_listOfWorkflows;

    public InsertDtpJobCommand(Job p_job, List p_listOfWorkflows)
    {
        m_job = p_job;
        m_listOfWorkflows = p_listOfWorkflows;
    }

    public void persistObjects(Connection p_connection)
            throws PersistenceException
    {
        try
        {
            createPreparedStatement(p_connection);
            setData();
            batchStatements();
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
        finally
        {
            try
            {
                if (m_ps2 != null)
                    m_ps2.close();
                if (m_ps3 != null)
                    m_ps3.close();
                if (m_ps4 != null)
                    m_ps4.close();
            }
            catch (Exception e)
            {

            }

        }
    }

    public void createPreparedStatement(Connection p_connection)
            throws Exception
    {
        m_ps2 = p_connection.prepareStatement(m_insertWorkflow);
        m_ps3 = p_connection.prepareStatement(m_insertTask);
        m_ps4 = p_connection.prepareStatement(m_insertWorkflowOwners);
    }

    public void setData() throws Exception
    {
        Iterator it = m_listOfWorkflows.iterator();
        while (it.hasNext())
        {
            WorkflowImpl workflow = (WorkflowImpl) it.next();
            m_ps2.setLong(1, workflow.getId());
            m_ps2.setString(2, workflow.getState());
            m_ps2.setLong(3, workflow.getTargetLocale().getId());
            m_ps2.setLong(4, m_job.getId());
            m_ps2.setString(5, workflow.getCompletionFraction());
            m_ps2.setLong(6, workflow.getDuration());
            m_ps2.setDate(7, new Date(System.currentTimeMillis()));
            m_ps2.setString(8, workflow.getWorkflowType());
            m_ps2.setLong(9, workflow.getCompanyId());
            m_ps2.addBatch();

            // create the workflow owners for each workflow
            List wfOwners = workflow.getWorkflowOwners();

            for (int i = 0; i < wfOwners.size(); i++)
            {
                // add
                WorkflowOwner wfo = (WorkflowOwner) wfOwners.get(i);
                m_ps4.setLong(1, workflow.getId());
                m_ps4.setString(2, wfo.getOwnerId());
                m_ps4.setString(3, wfo.getOwnerType());
                m_ps4.addBatch();
                // increment key
            }

            // go through all tasks for the workflow
            Collection tasks = workflow.getTasks().values();
            for (Iterator i = tasks.iterator(); i.hasNext();)
            {
                TaskImpl t = (TaskImpl) i.next();
                m_ps3.setLong(1, t.getId());
                m_ps3.setLong(2, t.getWorkflow().getId());
                m_ps3.setString(3, t.getName());
                m_ps3.setString(4, "DEACTIVE"); // default state for new task
                if (t.getExpenseRate() != null)
                {
                    m_ps3.setLong(5, t.getExpenseRate().getId());
                }
                else
                {
                    m_ps3.setNull(5, Types.NUMERIC);
                }
                if (t.getRevenueRate() != null)
                {
                    m_ps3.setLong(6, t.getRevenueRate().getId());
                }
                else
                {
                    m_ps3.setNull(6, Types.NUMERIC);
                }
                if (t.getRateSelectionCriteria() != 0)
                {
                    m_ps3.setInt(7, t.getRateSelectionCriteria());
                }
                else
                {
                    m_ps3.setNull(7, Types.NUMERIC);
                }
                // set the type of TaskImpl
                String typeString = "TRANSLATE";
                if (t.getType() == TaskImpl.TYPE_REVIEW)
                {
                    typeString = "REVIEW";
                }
                m_ps3.setString(8, typeString);
                m_ps3.setString(9, t.getTaskType());
                m_ps3.setLong(10, t.getCompanyId());
                m_ps3.addBatch();
            }
        }
    }

    public void batchStatements() throws Exception
    {
        m_ps2.executeBatch();
        m_ps3.executeBatch();
        m_ps4.executeBatch();
    }

}
