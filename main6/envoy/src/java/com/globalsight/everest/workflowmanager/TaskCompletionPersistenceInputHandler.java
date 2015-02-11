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

import java.util.List;

/**
 * @deprecated
 */
public class TaskCompletionPersistenceInputHandler
{
    public TaskCompletionPersistenceInputHandler()
    {

    }

    public void persistObjects(List p_tuvs, List p_taskTuvs)
            throws WorkflowManagerException
    {
        // HashMap map = new HashMap(3);
        // try
        // {
        // long taskTuvTotal = determineNumberOfTaskTuvs(p_taskTuvs);
        // long sequenceTaskTuv = getPersistenceService().
        // getSequenceNumber(taskTuvTotal, taskTuvSequenceName);
        //
        // map.put(taskTuvSequenceName,new SequenceStore(sequenceTaskTuv,
        // taskTuvTotal));
        // long tuvTotal = determineNumberOfTuvs(p_tuvs);
        // long sequenceTuv = getPersistenceService().
        // getSequenceNumber(tuvTotal, tuvSequenceName);
        // map.put(tuvSequenceName, new SequenceStore(sequenceTuv, tuvTotal));
        // } catch (Exception e)
        // {
        // s_workflowLogger.error("The exception is " + e);
        // throw new WorkflowManagerException(e);
        //
        // }
        // Connection connection = null;
        // try
        // {
        // connection = getPersistenceService().getConnection();
        // connection.setAutoCommit(false);
        // TuvPersistenceCommand tuvpc = createTuvPersistenceCommand(p_tuvs,
        // map);
        // tuvpc.persistObjects(connection);
        // InsertTaskTuvPersistenceCommand ittpc =
        // new InsertTaskTuvPersistenceCommand(p_taskTuvs,
        // map);
        // ittpc.persistObjects(connection);
        // connection.commit();
        // } catch (Exception sqle)
        // {
        // try
        // {
        // s_workflowLogger.error("The value of the exception is" + sqle);
        // connection.rollback();
        // throw new WorkflowManagerException(sqle);
        // } catch (Exception se)
        // {
        // s_workflowLogger.error("The exception is " + se);
        // throw new WorkflowManagerException(se);
        // }
        // } finally
        // {
        // // return connection to connection pool
        // try
        // {
        // getPersistenceService().returnConnection(connection);
        // } catch (Exception pe)
        // {
        // s_workflowLogger.error("Unable to return to connection pool" + pe);
        // }
        // }

    }

    // private TuvPersistenceCommand
    // createTuvPersistenceCommand(List p_tuvs,
    // HashMap p_sequenceMap)
    // {
    // ArrayList nonClobTuvList = new ArrayList();
    // ArrayList clobTuvList = new ArrayList();
    // Iterator iTuvs = p_tuvs.iterator();
    // while (iTuvs.hasNext())
    // {
    // Tuv tuv = (Tuv)iTuvs.next();
    // if (isClob(tuv.getGxml()))
    // {
    // clobTuvList.add(tuv);
    // } else
    // {
    // nonClobTuvList.add(tuv);
    // }
    // }
    // boolean p_deleteableTuv = true;
    // return new
    // TuvPersistenceCommand(nonClobTuvList,
    // clobTuvList,
    // p_sequenceMap,
    // p_deleteableTuv);
    //
    // }
    //
    // private boolean isClob(String p_segment)
    // {
    // boolean isClobString = false;
    // if (EditUtil.getUTF8Len(p_segment) > CLOB_THRESHOLD)
    // {
    // isClobString = true;
    // }
    // return isClobString;
    // }
    // private long determineNumberOfTaskTuvs(List p_taskTuvs)
    // {
    // int totalNumberOfTaskTuvs = p_taskTuvs.size();
    // return new Long(totalNumberOfTaskTuvs).longValue();
    // }
    // private long determineNumberOfTuvs(List p_tuvs)
    // {
    // int totalNumberOfTuvs = p_tuvs.size();
    // return new Long(totalNumberOfTuvs).longValue();
    // }
    // private PersistenceService getPersistenceService() throws Exception
    // {
    // if (m_persistenceService == null)
    // {
    // m_persistenceService = PersistenceService.getInstance();
    // }
    // return m_persistenceService;
    // }
}
