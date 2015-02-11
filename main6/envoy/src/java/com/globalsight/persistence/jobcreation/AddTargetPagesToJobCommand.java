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

package com.globalsight.persistence.jobcreation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.persistence.PersistenceCommand;

public class AddTargetPagesToJobCommand extends PersistenceCommand
{
    private String m_updateTargetPage = "update target_page set workflow_iflow_instance_id = "
            + " (select distinct iflow_instance_id from workflow "
            + " where job_id = ? and target_locale_id = ?) "
            + " ,timestamp = ? where id = ? ";
    private HashMap m_targetPages;
    private JobImpl m_job;
    private PreparedStatement m_ps;

    public AddTargetPagesToJobCommand(HashMap p_targetPages, JobImpl p_job)
    {
        m_targetPages = p_targetPages;
        m_job = p_job;
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
                if (m_ps != null)
                    m_ps.close();
            }
            catch (Exception e)
            {

            }
        }
    }

    public void createPreparedStatement(Connection p_connection)
            throws Exception
    {
        m_ps = p_connection.prepareStatement(m_updateTargetPage);
    }

    public void setData() throws Exception
    {
        Collection c = m_targetPages.values();
        Iterator it = c.iterator();
        while (it.hasNext())
        {
            TargetPage tp = (TargetPage) it.next();
            m_ps.setLong(1, m_job.getId());
            m_ps.setLong(2, tp.getLocaleId());
            m_ps.setDate(3, new Date(System.currentTimeMillis()));
            m_ps.setLong(4, tp.getId());
            m_ps.addBatch();
        }
    }

    public void batchStatements() throws Exception
    {
        m_ps.executeBatch();
    }
}
