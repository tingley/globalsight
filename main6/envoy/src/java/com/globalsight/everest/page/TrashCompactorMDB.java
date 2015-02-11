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

import java.util.ArrayList;
import java.util.HashMap;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adaptermdb.EventTopicMap;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.workflowmanager.TaskTuvDeleter;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Helper class for asynchronously deleting garbage in the database, for
 * instance TUVs, TaskTuvs, and other objects that can be deleted in the
 * background.
 * 
 * The data should be inaccessible and deletable in small chunks in order not to
 * overflow database's rollback segments.
 */
@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig =
{
        @ActivationConfigProperty(propertyName = "destination", propertyValue = EventTopicMap.QUEUE_PREFIX_JBOSS
                + JmsHelper.JMS_TRASH_COMPACTION_QUEUE),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = JmsHelper.JMS_TYPE_QUEUE) })
@TransactionManagement(value = TransactionManagementType.BEAN)
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

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message p_message)
    {
        try
        {
            HashMap map = (HashMap) ((ObjectMessage) p_message).getObject();

            CompanyThreadLocal.getInstance().setIdValue(
                    (String) map.get(CompanyWrapper.CURRENT_COMPANY_ID));

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
        }
        catch (Exception e)
        {
            // tbd - more specific?
            CATEGORY.error("An error occurred when processing a message "
                    + "for the TrashCompactor.", e);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }
}
