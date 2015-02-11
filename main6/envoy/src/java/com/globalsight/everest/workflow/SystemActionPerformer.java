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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageexport.ExportBatchEvent;
import com.globalsight.everest.page.pageexport.ExportEventObserverHelper;
import com.globalsight.everest.page.pageexport.ExportParameters;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowExportingHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Performs the activity according to the system action configured in workflow
 * activity ("System Action Before Activity").
 * 
 * @since GBS-3002
 */
public class SystemActionPerformer
{
    private static final Logger logger = Logger
            .getLogger(SystemActionPerformer.class);

    /**
     * Performs system actions before an activity is reached as workflow goes
     * forward.
     */
    public static void perform(String actionType, long taskId, String userId)
    {
        if (actionType != null && SystemAction.ETF.equals(actionType))
        {
            Task task = HibernateUtil.get(TaskImpl.class, taskId);
            Workflow wf = task.getWorkflow();
            List<TargetPage> targetPages = wf.getTargetPages();
            List<Long> ids = new ArrayList<Long>();
            for (TargetPage tp : targetPages)
            {
                ids.add(tp.getIdAsLong());
            }
            logger.info("Performing system action 'Export Target Files' for activity "
                    + task.getTaskName());
            try
            {
                long exportBatchId = createExportBatchId(wf, userId, ids,
                        taskId);
                ServerProxy.getPageManager().exportPage(
                        new ExportParameters(wf), ids, true, exportBatchId);
            }
            catch (Exception e)
            {
                logger.error(
                        "Error performing system action 'Export Target Files'",
                        e);
            }
        }
    }

    private static long createExportBatchId(Workflow wf, String userId,
            List<Long> ids, Long taskId) throws Exception
    {
        ArrayList<Long> workflowIds = new ArrayList<Long>();
        workflowIds.add(wf.getIdAsLong());
        WorkflowExportingHelper.setAsExporting(workflowIds);

        long exportBatchId = ExportEventObserverHelper
                .notifyBeginExportTargetBatch(wf.getJob(),
                        UserUtil.getUserById(userId), ids, workflowIds, taskId,
                        ExportBatchEvent.INTERIM_PRIMARY);

        return exportBatchId;
    }
}
