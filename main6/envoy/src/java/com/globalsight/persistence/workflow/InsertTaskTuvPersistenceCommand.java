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

package com.globalsight.persistence.workflow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.tuv.TaskTuv;
import com.globalsight.persistence.PersistenceCommand;
import com.globalsight.persistence.SequenceStore;

/**
 * @deprecated
 */
public class InsertTaskTuvPersistenceCommand
    extends PersistenceCommand
{
    public static final String TASK_TUV_SEQ_NAME = "TASK_TUV_SEQ";
    private static String m_insertTaskTuv =
        "insert into task_tuv(id, current_tuv_id, task_id, version, previous_tuv_id, task_name) " +
        "values(null, ?, ?, ?, ?, ?)";

    private PreparedStatement m_psInsertTaskTuv;
    private Collection m_listOfTaskTuvs;
    private HashMap m_sequenceMap;

    public InsertTaskTuvPersistenceCommand(Collection p_listOfTaskTuvs,
        HashMap p_sequenceMap)
    {

        m_listOfTaskTuvs = p_listOfTaskTuvs;
        m_sequenceMap = p_sequenceMap;
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
                if (m_psInsertTaskTuv != null) m_psInsertTaskTuv.close();
            }
            catch (Throwable ignore)
            { /* ignore */ }
        }
    }

    public void createPreparedStatement(Connection p_connection)
        throws Exception
    {
        m_psInsertTaskTuv = p_connection.prepareStatement(m_insertTaskTuv);
    }

    public void setData()
        throws Exception
    {
        Iterator it = m_listOfTaskTuvs.iterator();
        SequenceStore seqStore =
            (SequenceStore)m_sequenceMap.get(TASK_TUV_SEQ_NAME);
        long primaryKey = allocateSequenceNumberRange(seqStore);

        while (it.hasNext())
        {
            TaskTuv taskTuv = (TaskTuv)it.next();

            m_psInsertTaskTuv.setLong(1, taskTuv.getCurrentTuvId());
            m_psInsertTaskTuv.setLong(2, taskTuv.getTaskId());
            m_psInsertTaskTuv.setLong(3, taskTuv.getVersion());
            m_psInsertTaskTuv.setLong(4, taskTuv.getPreviousTuvId());
            m_psInsertTaskTuv.setString(5, taskTuv.getTaskName());

            m_psInsertTaskTuv.addBatch();

            primaryKey++;
        }
    }
    public void batchStatements() throws Exception
    {
        m_psInsertTaskTuv.executeBatch();
    }
}
