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
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.tuv.TaskTuv;
import com.globalsight.ling.tm2.persistence.DbUtil;

public class PageExportQuery
{
    private static final String m_selectTaskTuv = "select tt.* from TASK_TUV tt, TASK_INFO ti where "
            + "tt.task_id = ti.task_id and ti.workflow_id = ?";
    private static Logger c_logger = Logger
            .getLogger(PageExportQuery.class.getName());
    private PreparedStatement m_ps;

    public PageExportQuery()
    {
    }

    public List getTaskTuvsForWorkflow(long p_workflowId)
            throws PersistenceException
    {
        Connection connection = null;
        ResultSet rs = null;
        List taskTuvList = null;
        try
        {
            connection = DbUtil.getConnection();
            m_ps = connection.prepareStatement(m_selectTaskTuv);
            m_ps.setLong(1, p_workflowId);
            rs = m_ps.executeQuery();
            taskTuvList = processResultSet(rs);
        }
        catch (Exception e)
        {
            c_logger.error("The exception in pagexportquery is " + e);
            throw new PersistenceException(e);
        }
        finally
        {
        	DbUtil.silentClose(rs);
        	DbUtil.silentClose(m_ps);
        	DbUtil.silentReturnConnection(connection);
        }

        return taskTuvList;
    }

    private List processResultSet(ResultSet p_rs) throws Exception
    {
        ResultSet rs = p_rs;
        List list = new ArrayList();
        while (rs.next())
        {
            TaskTuv taskTuv = new TaskTuv();
            taskTuv.setId(rs.getLong(1));
            taskTuv.setCurrentTuvId(rs.getLong(2));
            taskTuv.setTaskId(rs.getLong(3));
            taskTuv.setVersion(rs.getInt(4));
            taskTuv.setPreviousTuvId(rs.getLong(5));
            list.add(taskTuv);
        }
        return list;
    }
}
