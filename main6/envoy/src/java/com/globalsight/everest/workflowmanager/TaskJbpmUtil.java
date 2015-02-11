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

import java.util.Date;
import java.util.Vector;

import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowJbpmUtil;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.util.date.DateHelper;

public class TaskJbpmUtil
{
    public static String getTaskDisplayName(String displayName)
    {
        try
        {
            displayName = WorkflowJbpmUtil.getActivityName(displayName);
            Activity act = (Activity) ServerProxy.getJobHandler().getActivity(
                    displayName);
            displayName = act.getDisplayName();
        }
        catch (Exception e)
        {

        }
        return displayName;
    }

    public static String[] getActivityRole(WorkflowInstance wfi, TaskInstance t)
    {
        String taskName = WorkflowJbpmUtil.getActivityName(t.getName());
        Vector<WorkflowTaskInstance> wTasks = wfi.getWorkflowInstanceTasks();
        for (WorkflowTaskInstance wTask : wTasks)
        {
            String name = wTask.getActivityName();
            if (name.equals(taskName))
            {
                return UserUtil.convertUserIdsToUserNamesInRoles(wTask
                        .getRoles());
            }
        }

        return null;
    }

    public static String getActualDuration(TaskInstance t)
    {
        if (t.getStart() == null)
        {
            return TaskImpl.EMPTY_DATE;
        }

        long accetpDate = t.getStart().getTime();
        long duration = t.getEnd() == null ? new Date().getTime() - accetpDate
                : t.getEnd().getTime() - accetpDate;

        return DateHelper.daysHoursMinutes(duration, "d", "h", "m");
    }
}
