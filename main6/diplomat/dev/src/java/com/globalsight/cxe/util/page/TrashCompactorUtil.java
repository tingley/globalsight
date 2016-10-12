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
package com.globalsight.cxe.util.page;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflowmanager.TaskTuvDeleter;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Class {@code TrashCompactorUtil} is used for deleting garbage in database,
 * for instance TUVs, TaskTuvs, and other objects that can be deleted in the
 * background without using JMS.
 * 
 * @since GBS-4400
 */
public class TrashCompactorUtil
{
    static private final Logger logger = Logger.getLogger(TrashCompactorUtil.class);

    public static final String ACTION_DELETE_ISSUES = "deleteIssues";

    public static final String ACTION_DELETE_TASK_TUVS = "deleteTaskTuvs";

    /**
     * Compacts the trash asynchronously with thread instead of JMS.
     */
    static public void compactTrashWithThread(Map<String, Object> data)
    {
        TrashCompactorRunnable runnable = new TrashCompactorRunnable(data);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();
    }

    /**
     * Compacts the trash synchronously.
     */
    static public void compactTrash(Map<String, Object> p_data)
    {
        String action = (String) p_data.get("action");
        try
        {
            if (ACTION_DELETE_ISSUES.equals(action))
            {
                @SuppressWarnings("unchecked")
                List<Long> tuvIds = (List<Long>) p_data.get("deletedTuvIds");
                ServerProxy.getCommentManager().deleteIssues(Issue.TYPE_SEGMENT, tuvIds);
            }
            else if (ACTION_DELETE_TASK_TUVS.equals(action))
            {
                long wfId = (long) p_data.get("workflowId");
                TaskTuvDeleter.deleteTaskTuvs(wfId);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }
}
