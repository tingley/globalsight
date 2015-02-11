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
package com.globalsight.everest.workflow;

import java.util.Iterator;
import java.util.List;

import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.taskmgmt.def.AssignmentHandler;
import org.jbpm.taskmgmt.exe.Assignable;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.everest.qachecks.DITAQAChecker;
import com.globalsight.everest.qachecks.DITAQACheckerHelper;
import com.globalsight.everest.qachecks.QAChecker;
import com.globalsight.everest.taskmanager.TaskInterimPersistenceAccessor;

public class WorkflowAssignment implements AssignmentHandler
{
    private static final long serialVersionUID = 2049391654171720021L;

    private String workflow_pm;

    private String workflow_manager;

    private String activity;

    private String roles;

    private String accepted_time;

    private String completed_time;

    private String overdueToPM_time;

    private String overdueToUser_time;

    private String role_type;

    private String sequence;

    private String structural_state;

    private String rate_selection_criteria;

    private String expense_rate_id;

    private String revenue_rate_id;

    private String role_name;

    private String role_id;

    private String action_type;

    private String role_preference;

    private String point;

    private String report_upload_check;

    public void assign(Assignable arg0, ExecutionContext arg1) throws Exception
    {
        arg0.setPooledActors(role_id.split(","));
        TaskInstance taskInstance = arg1.getTaskInstance();
        taskInstance.setDescription(workflow_pm);
        cleanRejectedTaskInstances(arg1);
        // for GBS-1302, add activity to TASK_INTERIM table in 'ACTIVE' state
        // when assigning it to the assignees.
        TaskInterimPersistenceAccessor.dispatchInterimActivity(taskInstance);
        // entrance of GBS-3697
        QAChecker qaChecker = new QAChecker();
        qaChecker.runQAChecksAndGenerateReport(taskInstance);

        if (DITAQACheckerHelper.isDitaQaActivity(taskInstance))
        {
            DITAQAChecker ditaQaChecker = new DITAQAChecker();
            ditaQaChecker.runQAChecksAndGenerateReport(taskInstance);
        }
    }

    /**
     * Sets the isRejected value to null in all the old task instances with the
     * specified task node id.
     */
    private void cleanRejectedTaskInstances(ExecutionContext arg1)
    {
        JbpmContext ctx = arg1.getJbpmContext();
        long taskNodeId = arg1.getTaskInstance().getTask().getTaskNode()
                .getId();
        List taskInstances = WorkflowJbpmPersistenceHandler
                .getTaskInstancesById(taskNodeId, ctx);
        for (Iterator it = taskInstances.iterator(); it.hasNext();)
        {
            TaskInstance ti = (TaskInstance) it.next();
            ti.setVariable(WorkflowConstants.VARIABLE_IS_REJECTED, null);
        }
    }

    public String getAccepted_time()
    {
        return accepted_time;
    }

    public void setAccepted_time(String accepted_time)
    {
        this.accepted_time = accepted_time;
    }

    public String getAction_type()
    {
        return action_type;
    }

    public void setAction_type(String action_type)
    {
        this.action_type = action_type;
    }

    public String getActivity()
    {
        return activity;
    }

    public void setActivity(String activity)
    {
        this.activity = activity;
    }

    public String getCompleted_time()
    {
        return completed_time;
    }

    public void setCompleted_time(String completed_time)
    {
        this.completed_time = completed_time;
    }

    public String getPoint()
    {
        return point;
    }

    public void setPoint(String point)
    {
        this.point = point;
    }

    public String getRole_id()
    {
        return role_id;
    }

    public void setRole_id(String role_id)
    {
        this.role_id = role_id;
    }

    public String getRole_name()
    {
        return role_name;
    }

    public void setRole_name(String role_name)
    {
        this.role_name = role_name;
    }

    public String getRole_preference()
    {
        return role_preference;
    }

    public void setRole_preference(String role_preference)
    {
        this.role_preference = role_preference;
    }

    public String getRole_type()
    {
        return role_type;
    }

    public void setRole_type(String role_type)
    {
        this.role_type = role_type;
    }

    public String getRoles()
    {
        return roles;
    }

    public void setRoles(String roles)
    {
        this.roles = roles;
    }

    public String getSequence()
    {
        return sequence;
    }

    public void setSequence(String sequence)
    {
        this.sequence = sequence;
    }

    public String getStructural_state()
    {
        return structural_state;
    }

    public void setStructural_state(String structural_state)
    {
        this.structural_state = structural_state;
    }

    public String getWorkflow_manager()
    {
        return workflow_manager;
    }

    public void setWorkflow_manager(String workflow_manager)
    {
        this.workflow_manager = workflow_manager;
    }

    public String getWorkflow_pm()
    {
        return workflow_pm;
    }

    public void setWorkflow_pm(String workflow_pm)
    {
        this.workflow_pm = workflow_pm;
    }

    public String getExpense_rate_id()
    {
        return expense_rate_id;
    }

    public void setExpense_rate_id(String expense_rate_id)
    {
        this.expense_rate_id = expense_rate_id;
    }

    public String getRate_selection_criteria()
    {
        return rate_selection_criteria;
    }

    public void setRate_selection_criteria(String rate_selection_criteria)
    {
        this.rate_selection_criteria = rate_selection_criteria;
    }

    public String getRevenue_rate_id()
    {
        return revenue_rate_id;
    }

    public void setRevenue_rate_id(String revenue_rate_id)
    {
        this.revenue_rate_id = revenue_rate_id;
    }

    public String getOverdueToPM_time()
    {
        return overdueToPM_time;
    }

    public void setOverdueToPM_time(String voerdutpm_time)
    {
        this.overdueToPM_time = voerdutpm_time;
    }

    public String getOverdueToUser_time()
    {
        return overdueToUser_time;
    }

    public void setOverdueToUser_time(String voerdutpm_time)
    {
        this.overdueToUser_time = voerdutpm_time;
    }

    public void setReport_upload_check(String report_upload_check)
    {
        this.report_upload_check = report_upload_check;
    }

    public String getReport_upload_check()
    {
        return report_upload_check;
    }
}
