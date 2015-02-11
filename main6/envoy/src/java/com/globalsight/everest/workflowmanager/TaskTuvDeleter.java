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
import org.apache.log4j.Logger;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.persistence.PersistenceException;
import java.sql.Connection;
import java.util.List;

import com.globalsight.persistence.pageexport.PageExportQuery;
import com.globalsight.persistence.pageexport.DeleteTaskTuvPersistenceCommand;

public class TaskTuvDeleter
{
    static private final Logger s_logger =
        Logger.getLogger(
            TaskTuvDeleter.class);

    /**
     * Delete the task tuvs for the given workflow
     * 
     * @param p_wfId wf id
     * @exception PersistenceException
     */
    public static void deleteTaskTuvs(long p_wfId)
        throws PersistenceException
    {
        Connection connection = null;

        try
        {
            PageExportQuery peq = new PageExportQuery();
            List list = peq.getTaskTuvsForWorkflow(p_wfId);
            connection = PersistenceService.getInstance().getConnection();
            connection.setAutoCommit(false);
            DeleteTaskTuvPersistenceCommand dttpc =
                new DeleteTaskTuvPersistenceCommand(list);
            dttpc.persistObjects(connection);
            connection.commit();
        }
        catch (Exception e)
        {
            try
            {
                connection.rollback();
            }
            catch (Exception sqle)
            {
                throw new PersistenceException(e);
            }
        }
        finally
        {
            try
            {
                if (connection != null)
                {
                    PersistenceService.getInstance().returnConnection(connection);
                }
            }
            catch (Exception e)
            {
                s_logger.error("Unable to return connection to pool", e);
            }
        }
    }

}
