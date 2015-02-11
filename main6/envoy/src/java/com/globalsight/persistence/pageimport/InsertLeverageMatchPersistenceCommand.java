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

package com.globalsight.persistence.pageimport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Iterator;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.ling.tm.CandidateMatch;
import com.globalsight.ling.tm.LeverageMatches;
import com.globalsight.persistence.PersistenceCommand;
import com.globalsight.util.database.PreparedStatementBatch;

public class InsertLeverageMatchPersistenceCommand
    extends PersistenceCommand
{
    private static final String m_insertLeverageMatch =
        "insert into leverage_match(source_page_id, original_source_tuv_id, source_match_tuv_id, " +
        "leveraged_target_tuv_id, target_locale_id, match_type, order_num, score_num) values(?, ?, ? , ? , ? , ?, ?, ?)";

    private PreparedStatementBatch m_psBatch;
    private Collection m_leverageMatches;
    private long m_sourcePageId;

    public InsertLeverageMatchPersistenceCommand(long p_sourcePageId,
        Collection p_leverageMatches)
    {
        m_sourcePageId = p_sourcePageId;
        m_leverageMatches = p_leverageMatches;
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
                if (m_psBatch != null) m_psBatch.closeAll();
                m_psBatch = null;
            }
            catch (Throwable ignore)
            { /* ignore */ }
        }
    }

    public void createPreparedStatement(Connection p_connection)
        throws Exception
    {
        m_psBatch = new PreparedStatementBatch(
            PreparedStatementBatch.DEFAULT_BATCH_SIZE,
            p_connection,
            m_insertLeverageMatch,
            true);
    }

    public void setData()
        throws Exception
    {
        for (Iterator lmIt = m_leverageMatches.iterator(); lmIt.hasNext(); )
        {
            LeverageMatches lm = (LeverageMatches)lmIt.next();

            for (Iterator cmIt = lm.getLeverageMatches().iterator(); cmIt.hasNext(); )
            {
                CandidateMatch cm = (CandidateMatch)cmIt.next();

                PreparedStatement ps = m_psBatch.getNextPreparedStatement();

                ps.setLong(1, m_sourcePageId);
                ps.setLong(2, cm.getOriginalSourceId());
                ps.setLong(3, cm.getMatchedSourceId());
                ps.setLong(4, cm.getMatchedTargetId());
                ps.setLong(5, lm.getGlobalSightLocale().getId());
                ps.setString(6, cm.getMatchTypeString());
                ps.setLong(7, cm.getOrderNum());
                ps.setLong(8, cm.getScoreNum());
                ps.addBatch();
            }
        }
    }

    public void batchStatements()
        throws Exception
    {
        m_psBatch.executeBatches();
    }
}
