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
package com.globalsight.persistence.upload;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.tuv.Tuv;

public class UpdateOfflineTuvPersistenceCommand
{
    private static final int MAX_BATCH_UPDATE = 5000;

    private PreparedStatement m_psUpdateTuv = null;
    private PreparedStatement m_psSelectClobTuv = null;
    private Collection tuvs = new ArrayList();

    private static final String m_updateTuvSql = "update translation_unit_variant "
            + "set exact_match_key = ?, "
            + "segment_string = ?, state = ?, merge_state = ?, "
            + "last_modified = SYSDATE, modify_user = ?, "
            + "timestamp = SYSDATE, segment_clob = null where id = ? ";

    public static void saveTuvs(Collection p_tuvs) throws Exception
    {
        Connection conn = null;
        try
        {
            conn = PersistenceService.getInstance().getConnection();
            conn.setAutoCommit(false);
            UpdateOfflineTuvPersistenceCommand cmd = new UpdateOfflineTuvPersistenceCommand(
                    p_tuvs);
            cmd.persistObjects(conn);
            conn.commit();
        }
        finally
        {
            if (conn != null)
            {
                PersistenceService.getInstance().returnConnection(conn);
            }
        }
    }

    public UpdateOfflineTuvPersistenceCommand(Collection p_tuvs)
    {
        tuvs.addAll(p_tuvs);
    }

    public void persistObjects(Connection p_connection)
            throws PersistenceException
    {
        try
        {
            m_psUpdateTuv = p_connection.prepareStatement(m_updateTuvSql);

            if (tuvs.size() > 0)
            {
                saveTuvs();
            }
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
        finally
        {
            try
            {
                if (m_psUpdateTuv != null)
                    m_psUpdateTuv.close();
                if (m_psSelectClobTuv != null)
                    m_psSelectClobTuv.close();
            }
            catch (Throwable ignore)
            { /* ignore */
            }
        }

    }

    private void saveTuvs() throws Exception
    {
        int cnt = 0;
        Iterator nonClobIterator = tuvs.iterator();
        while (nonClobIterator.hasNext())
        {
            Tuv tuv = (Tuv) nonClobIterator.next();
            m_psUpdateTuv.setLong(1, tuv.getExactMatchKey());
            m_psUpdateTuv.setString(2, tuv.getGxml());
            m_psUpdateTuv.setString(3, tuv.getState().getName());
            m_psUpdateTuv.setString(4, tuv.getMergeState());
            m_psUpdateTuv.setString(5, tuv.getLastModifiedUser());
            m_psUpdateTuv.setLong(6, tuv.getId());
            m_psUpdateTuv.addBatch();
            cnt++;

            if (cnt > MAX_BATCH_UPDATE)
            {
                m_psUpdateTuv.executeBatch();
                cnt = 0;
            }
        }
        if (cnt > 0)
        {
            m_psUpdateTuv.executeBatch();
        }
    }
}
