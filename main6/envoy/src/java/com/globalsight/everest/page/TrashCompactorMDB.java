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

package com.globalsight.everest.page;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.everest.workflowmanager.TaskTuvDeleter;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Helper class for asynchronously deleting garbage in the database, for
 * instance TUVs, TaskTuvs, and other objects that can be deleted in the
 * background.
 * 
 * The data should be inaccessible and deletable in small chunks in order not to
 * overflow database's rollback segments.
 */
public class TrashCompactorMDB extends GenericQueueMDB
{
    private static final Logger CATEGORY = Logger
            .getLogger(TrashCompactorMDB.class);

    // To send messages to this MDB, use the following code:
    // import com.globalsight.everest.util.jms.JmsHelper;
    // JmsHelper.sendMessageToQueue((Serializable)hashmap,
    // JmsHelper.JMS_TERMBASE_DELETION_QUEUE);

    public TrashCompactorMDB()
    {
        super(CATEGORY);
    }

    public void onMessage(Message p_message)
    {
        try
        {
            HashMap map = (HashMap) ((ObjectMessage) p_message).getObject();

            CompanyThreadLocal.getInstance().setIdValue(
                    (String) map.get(CompanyWrapper.CURRENT_COMPANY_ID));

            // TODO: check message parameters, delete stuff.
            // When done, ackowledge message.
            String command = (String) map.get("command");
            if ("DeleteTuvIds".equals(command))
            {
                // right now only issues from TUV - so don't check any TYPE
                // parameter
                ArrayList tuvIds = (ArrayList) map.get("deletedTuvIds");

                ServerProxy.getCommentManager().deleteIssues(
                        Issue.TYPE_SEGMENT, tuvIds);
            }
            else if ("DeleteTaskTuvs".equals(command))
            {
                Long wfId = (Long) map.get("workflowId");
                TaskTuvDeleter.deleteTaskTuvs(wfId.longValue());
            }

            p_message.acknowledge();
        }
        catch (JMSException ex)
        {
            // Unexpected, but do nothing. The message will come back.
        }
        catch (Exception ex)
        {
            // tbd - more specific?
            CATEGORY.error("An error occurred when processing a message "
                    + "for the TrashCompactor.", ex);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }
}
