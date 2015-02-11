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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.globalsight.everest.page.ExtractedFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.persistence.PersistenceCommand;
import com.globalsight.persistence.SequenceStore;
import com.globalsight.util.database.PreparedStatementBatch;

public class TuPersistenceCommand extends PersistenceCommand
{
    private static final String TU_SEQ = "TU_SEQ";

    private static final String m_insertCommand =
        "insert into translation_unit values(?,?,?,?,?,?,?,?,?)";

    private PreparedStatementBatch m_psBatch = null;
    private List m_lgList;
    private HashMap m_sequenceMap;

    public TuPersistenceCommand(SourcePage p_sourcePage, HashMap p_sequenceMap)
    {
        m_lgList = ((ExtractedFile)p_sourcePage.getPrimaryFile()).
            getLeverageGroups();
        m_sequenceMap = p_sequenceMap;
    }

    public String getInsertCommand()
    {
        return m_insertCommand;
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
            m_insertCommand,
            true);
    }

    public void setData()
        throws Exception
    {
        ListIterator li = m_lgList.listIterator();
        SequenceStore seqStore = (SequenceStore)m_sequenceMap.get(TU_SEQ);
        long primaryKey = allocateSequenceNumberRange(seqStore);

        while (li.hasNext())
        {
            LeverageGroup lg = (LeverageGroup)li.next();
            Collection cTus  = lg.getTus();
            Iterator iTus = cTus.iterator();
            while (iTus.hasNext())
            {
                TuImpl tu = (TuImpl)iTus.next();

                PreparedStatement ps = m_psBatch.getNextPreparedStatement();

                ps.setLong(1, primaryKey);
                ps.setLong(2, tu.getOrder());
                ps.setLong(3, tu.getTmId());
                ps.setString(4, tu.getDataType());
                ps.setString(5, tu.getTuType());
                String locType = String.valueOf(tu.getLocalizableType());
                ps.setString(6, locType);
                ps.setLong(7, lg.getId());
                ps.setLong(8, tu.getPid());
                
                ps.setString(9, tu.getSourceTmName());
                
                ps.addBatch();

                tu.setId(primaryKey);

                primaryKey++;
            }
        }
    }

    public void batchStatements()
        throws Exception
    {
        m_psBatch.executeBatches();
    }
}
