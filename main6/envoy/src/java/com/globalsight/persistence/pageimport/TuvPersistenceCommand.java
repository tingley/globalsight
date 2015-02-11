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
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.persistence.PersistenceCommand;
import com.globalsight.persistence.SequenceStore;
import com.globalsight.util.database.PreparedStatementBatch;

/**
 * @deprecated
 */
public class TuvPersistenceCommand
    extends PersistenceCommand
{
    static public final String TUV_SEQ_NAME = "TUV_SEQ";

    static private final String m_insertNonClobCommand =
        "insert into Translation_Unit_Variant " +
        "(id, order_num, locale_id, tu_id, is_indexed, segment_string, " +
        " word_count, exact_match_key, state, merge_state, timestamp, " +
        " last_modified, modify_user, creation_date, creation_user," +
        " updated_by_project) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    static private final String m_insertClobCommand =
        "insert into Translation_Unit_Variant " +
        "(id, order_num, locale_id, tu_id, is_indexed, word_count, " +
        " exact_match_key, state, merge_state, timestamp, last_modified, " +
        " segment_clob, modify_user, creation_date, creation_user, " +
        " updated_by_project) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private List m_nonClobTuvs;
    private List m_clobTuvs;
    private HashMap m_sequenceMap;

    private PreparedStatementBatch m_ps1Batch=null;
    private PreparedStatementBatch m_ps2Batch=null;
    private PreparedStatement m_ps3=null;

    //
    // Constructor
    //
    public TuvPersistenceCommand(List p_nonClobTuvs, List p_clobTuvs,
        HashMap p_sequenceMap, boolean p_deleteableTuv)
    {
        m_nonClobTuvs = p_nonClobTuvs;
        m_clobTuvs =    p_clobTuvs;
        m_sequenceMap = p_sequenceMap;
    }

    //
    // Public Methods
    //
    public String getInsertNonClobCommand()
    {
        return m_insertNonClobCommand;
    }


    public String getInsertClobCommand()
    {
        return m_insertClobCommand;
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
            close(m_ps1Batch);
            close(m_ps2Batch);
            close(m_ps3);
        }
    }


    public void createPreparedStatement(Connection p_connection)
        throws Exception
    {
        if (m_nonClobTuvs != null && m_nonClobTuvs.size() > 0)
        {
            m_ps1Batch = new PreparedStatementBatch(
                PreparedStatementBatch.DEFAULT_BATCH_SIZE,
                p_connection,
                m_insertNonClobCommand,
                true);
        }

        if (m_clobTuvs != null && m_clobTuvs.size() > 0)
        {
            m_ps2Batch = new PreparedStatementBatch(
                PreparedStatementBatch.DEFAULT_BATCH_SIZE,
                p_connection,
                m_insertClobCommand,
                true);

        }
    }


    public void setData()
        throws Exception
    {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        Iterator nonClobIterator = m_nonClobTuvs.iterator();

        SequenceStore seqStore =
            (SequenceStore)m_sequenceMap.get(TUV_SEQ_NAME);

        long primaryKey = allocateSequenceNumberRange(seqStore);
        if (m_nonClobTuvs != null && m_nonClobTuvs.size() > 0)
        {
            while (nonClobIterator.hasNext())
            {
                TuvImpl tuv = (TuvImpl)nonClobIterator.next();

                PreparedStatement ps1 = m_ps1Batch.getNextPreparedStatement();
                ps1.setLong  (1, primaryKey);
                ps1.setLong  (2, tuv.getOrder());
                ps1.setLong  (3, tuv.getLocaleId());
                ps1.setLong  (4, tuv.getTu().getId());
                ps1.setString(5, "N");
                ps1.setString(6, tuv.getGxml());
                ps1.setLong  (7, tuv.getWordCount());
                ps1.setLong  (8, tuv.getExactMatchKey());
                ps1.setString(9, tuv.getState().getName());
                ps1.setString(10, tuv.getMergeState());
                ps1.setTimestamp(11, now);
                ps1.setTimestamp(12, now);
                ps1.setString(13, tuv.getLastModifiedUser());
                ps1.setTimestamp(14, now);
                ps1.setString(15, tuv.getCreatedUser());
                ps1.setString(16, tuv.getUpdatedProject());
                ps1.addBatch();

                tuv.setId(primaryKey);
                tuv.setLastModified(now);

                primaryKey++;
            }
        }

        if (m_clobTuvs != null && m_clobTuvs.size() > 0)
        {
            Iterator clobIterator = m_clobTuvs.iterator();
            while (clobIterator.hasNext())
            {
                TuvImpl tuv = (TuvImpl)clobIterator.next();
                PreparedStatement ps2 = m_ps2Batch.getNextPreparedStatement();
                ps2.setLong  (1, primaryKey);
                ps2.setLong  (2, tuv.getOrder());
                ps2.setLong  (3, tuv.getLocaleId());
                ps2.setLong  (4, tuv.getTu().getId());
                ps2.setString(5, "N");
                ps2.setLong  (6, tuv.getWordCount());
                ps2.setLong  (7, tuv.getExactMatchKey());
                ps2.setString(8, tuv.getState().getName());
                ps2.setString(9, tuv.getMergeState());
                ps2.setTimestamp(10, now);
                ps2.setTimestamp(11, now);
                ps2.setString(12, tuv.getSegmentClob());
                ps2.setString(13, tuv.getLastModifiedUser());
                ps2.setTimestamp(14, now);
                ps2.setString(15, tuv.getCreatedUser());
                ps2.setString(16, tuv.getUpdatedProject());
                ps2.addBatch();

                tuv.setId(primaryKey);
                tuv.setLastModified(now);

                primaryKey++;
            }
        }
    }


    public void batchStatements()
        throws Exception
    {
        if (m_nonClobTuvs != null && m_nonClobTuvs.size() > 0)
        {
            m_ps1Batch.executeBatches();
        }

        if (m_clobTuvs != null && m_clobTuvs.size() > 0)
        {
            m_ps2Batch.executeBatches();
        }
    }
}
