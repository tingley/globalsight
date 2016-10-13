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

package com.globalsight.persistence.pageexport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.List;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.tuv.TaskTuv;
import com.globalsight.persistence.PersistenceCommand;

public class DeleteTaskTuvPersistenceCommand extends PersistenceCommand
{
    static private final String s_deleteTaskTuv = "delete from task_tuv where id=?";
    static private final String s_deletePrevTuv = "delete from translation_unit_variant where id=?";

    private PreparedStatement m_psTaskTuv;
    private PreparedStatement m_psTuv;
    private List m_taskTuvList;

    public DeleteTaskTuvPersistenceCommand(List p_taskTuvList)
    {
        m_taskTuvList = p_taskTuvList;
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
            close(m_psTaskTuv);
            close(m_psTuv);
        }
    }

    public void createPreparedStatement(Connection p_connection)
            throws Exception
    {
        m_psTaskTuv = p_connection.prepareStatement(s_deleteTaskTuv);
        m_psTuv = p_connection.prepareStatement(s_deletePrevTuv);
    }

    public void setData() throws Exception
    {
        for (Iterator it = m_taskTuvList.iterator(); it.hasNext();)
        {
            TaskTuv taskTuv = (TaskTuv) it.next();

            long taskTuvId = taskTuv.getId();
            m_psTaskTuv.setLong(1, taskTuvId);
            m_psTaskTuv.addBatch();

            long prevTuvId = taskTuv.getPreviousTuvId();
            m_psTuv.setLong(1, prevTuvId);
            m_psTuv.addBatch();
        }
    }

    public void batchStatements() throws Exception
    {
        m_psTaskTuv.executeBatch();
        m_psTuv.executeBatch();
    }
}
