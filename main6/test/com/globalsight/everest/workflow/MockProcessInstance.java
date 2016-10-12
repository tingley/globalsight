package com.globalsight.everest.workflow;

import java.util.Date;
import java.util.Set;

import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

@SuppressWarnings("serial")
public class MockProcessInstance extends ProcessInstance
{
    public void addTaskInstance(String p_tiName, String p_tiDes)
    {
        addTaskInstance(p_tiName, p_tiDes, false);
    }
    
    public void addTaskInstance(String p_tiName, String p_tiDes, boolean p_isStart)
    {
        addTaskInstance(p_tiName, p_tiDes, new Date(), p_isStart);
    }
    
    protected void addTaskInstance(String p_tiName, String p_tiDes,
            Date p_tiCreate, boolean isStart)
    {
        TaskInstance ti = new TaskInstance();
        ti.setName(p_tiName);
        ti.setActorId("JUnitTester");
        ti.setDescription(p_tiDes);
        ti.setCreate(p_tiCreate);
        if (isStart)
        {
            ti.start();
        }

        getTaskMgmtInstance().addTaskInstance(ti);
    }
    
    public void addTaskInstance(Set<TaskInstance> p_tis)
    {
        for (TaskInstance ti : p_tis)
        {
            getTaskMgmtInstance().addTaskInstance(ti);
        }
    }
    
    public void saveOrUpdateTaskInstance(TaskInstance p_ti, String p_type)
    {
        Set<TaskInstance> tis = (Set<TaskInstance>) getTaskMgmtInstance().getTaskInstances();
        boolean isUpdate = false;
        
        if (tis != null)
        {
            for (TaskInstance ti : tis)
            {
                if (equals(ti, p_ti))
                {
                    isUpdate = true;
                    if (WorkflowConstants.TASK_TYPE_ACC.equals(p_type)
                            && ti.getStart() == null)
                    {
                        ti.start();
                    }
                    else if (WorkflowConstants.TASK_TYPE_COM.equals(p_type)
                            && ti.getEnd() == null)
                    {
                        ti.end();
                    }
                    break;
                }
            }
        }
        
        if (isUpdate)
        {
            addTaskInstance(tis);
        }
        else
        {
            getTaskMgmtInstance().addTaskInstance(p_ti);
        }
    }
    
    public boolean equals(TaskInstance p_ti1, TaskInstance p_ti2)
    {
        if (p_ti1 != null && p_ti2 != null
                && p_ti1.getName().equals(p_ti2.getName()))
        {
            if ("Start".equals(p_ti1.getName()))
            {
                return true;
            }
            else if (p_ti1.getDescription().equals(p_ti2.getDescription()))
            {
                return true;
            }
        }

        return false;
    }
    
    public MockProcessInstance clone()
    {
        MockProcessInstance pi = new MockProcessInstance();
        Set<TaskInstance> tis = (Set<TaskInstance>) getTaskMgmtInstance().getTaskInstances();
        pi.addTaskInstance(tis);
        return pi;
    }
}
