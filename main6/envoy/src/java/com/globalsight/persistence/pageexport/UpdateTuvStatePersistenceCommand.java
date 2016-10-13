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
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.persistence.tuv.TuvQueryConstants;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.persistence.PersistenceCommand;

public class UpdateTuvStatePersistenceCommand extends PersistenceCommand
{
    private static final String m_updateStateTuv = "update "
            + TuvQueryConstants.TUV_TABLE_PLACEHOLDER
            + " set state = ? , timestamp = ? " + " where id = ? ";

    private static final String m_updateStateAndCrcTuv = "update "
            + TuvQueryConstants.TUV_TABLE_PLACEHOLDER
            + " set state = ? , timestamp = ? , exact_match_key = ? "
            + " where id = ? ";

    private long jobId = -1L;
    private PreparedStatement m_psUpdateStateTuv = null;
    private PreparedStatement m_psUpdateStateAndCrcTuv = null;

    private Collection m_tuvsForState = null;
    private Collection m_tuvsForStateAndCrc = null;

    public void setJobId(long p_jobId)
    {
        jobId = p_jobId;
    }

    public void setTuvsForStateUpdate(Collection p_tuvs)
    {
        m_tuvsForState = p_tuvs;
    }

    public void setTuvsForStateAndCrcUpdate(Collection p_tuvs)
    {
        m_tuvsForStateAndCrc = p_tuvs;
    }

    public void persistObjects(Connection p_connection)
            throws PersistenceException
    {
        try
        {
            createPreparedStatement(p_connection);
            setData();
            batchStatements();

            // clear the TUVs Collections
            m_tuvsForState = null;
            m_tuvsForStateAndCrc = null;
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
        finally
        {
            try
            {
                if (m_psUpdateStateTuv != null)
                {
                    m_psUpdateStateTuv.close();
                    m_psUpdateStateTuv = null;
                }

                if (m_psUpdateStateAndCrcTuv != null)
                {
                    m_psUpdateStateAndCrcTuv.close();
                    m_psUpdateStateAndCrcTuv = null;
                }
            }
            catch (Throwable ignore)
            { /* ignore */
            }
        }

    }

    public void createPreparedStatement(Connection p_connection)
            throws Exception
    {
        // default
        String tuvTableName = "translation_unit_variant";
        if (jobId > 0)
        {
            tuvTableName = BigTableUtil.getTuvTableJobDataInByJobId(jobId);
        }

        if (m_tuvsForState != null)
        {
            String sql1 = m_updateStateTuv.replace(
                    TuvQueryConstants.TUV_TABLE_PLACEHOLDER, tuvTableName);
            m_psUpdateStateTuv = p_connection.prepareStatement(sql1);
        }

        if (m_tuvsForStateAndCrc != null)
        {
            String sql2 = m_updateStateAndCrcTuv.replace(
                    TuvQueryConstants.TUV_TABLE_PLACEHOLDER, tuvTableName);
            m_psUpdateStateAndCrcTuv = p_connection.prepareStatement(sql2);
        }
    }

    public void setData() throws Exception
    {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        if (m_tuvsForState != null)
        {
            Iterator it = m_tuvsForState.iterator();
            while (it.hasNext())
            {
                TuvImpl tuv = (TuvImpl) it.next();

                m_psUpdateStateTuv.setString(1, tuv.getState().getName());
                m_psUpdateStateTuv.setTimestamp(2, now);
                m_psUpdateStateTuv.setLong(3, tuv.getId());

                m_psUpdateStateTuv.addBatch();
            }
        }

        if (m_tuvsForStateAndCrc != null)
        {
            Iterator it = m_tuvsForStateAndCrc.iterator();
            while (it.hasNext())
            {
                TuvImpl tuv = (TuvImpl) it.next();

                m_psUpdateStateAndCrcTuv.setString(1, tuv.getState().getName());
                m_psUpdateStateAndCrcTuv.setTimestamp(2, now);
                m_psUpdateStateAndCrcTuv.setLong(3, tuv.getExactMatchKey());
                m_psUpdateStateAndCrcTuv.setLong(4, tuv.getId());

                m_psUpdateStateAndCrcTuv.addBatch();
            }
        }
    }

    public void batchStatements() throws Exception
    {
        if (m_psUpdateStateTuv != null)
        {
            m_psUpdateStateTuv.executeBatch();
        }

        if (m_psUpdateStateAndCrcTuv != null)
        {
            m_psUpdateStateAndCrcTuv.executeBatch();
        }
    }
}
