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
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import com.globalsight.everest.page.ExtractedFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.tuv.LeverageGroupImpl;
import com.globalsight.persistence.PersistenceCommand;
import com.globalsight.persistence.SequenceStore;

/**
 * @deprecated
 */
public class LeverageGroupPersistenceCommand
    extends PersistenceCommand
{
    public static final String LEVERAGE_GROUP_SEQ_NAME = "LEVERAGE_GROUP_SEQ";

    private String m_insertLeverageGroup =
        "insert into leverage_group values(?)";

    private String m_insertSourcePageLeverageGroup =
        "insert into source_page_leverage_group values(?,?)";

    private PreparedStatement m_ps1;
    private PreparedStatement m_ps2;
    private SourcePage m_sourcePage;
    private List m_lgList;
    private HashMap m_sequenceMap;

    public LeverageGroupPersistenceCommand(SourcePage p_sourcePage,
        HashMap p_sequenceMap)
    {
        m_sourcePage = p_sourcePage;
        //assumes this source page has an extracted file
        m_lgList = ((ExtractedFile)p_sourcePage.getPrimaryFile())
                                .getLeverageGroups();
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
        catch (Exception sqle)
        {
            throw new PersistenceException(sqle);
        }
        finally
        {
            try
            {
                if (m_ps1 != null) m_ps1.close();
                if (m_ps2 != null) m_ps2.close();
            }
            catch(Exception ignore)
            { /* ignore */ }
        }
    }


    public void createPreparedStatement(Connection p_connection)
        throws Exception
    {
        m_ps1 = p_connection.prepareStatement(m_insertLeverageGroup);
        m_ps2 = p_connection.prepareStatement(m_insertSourcePageLeverageGroup);
    }


    public void setData()
        throws Exception
    {
        SequenceStore seqStore =
            (SequenceStore)m_sequenceMap.get(LEVERAGE_GROUP_SEQ_NAME);
        long primaryKey = allocateSequenceNumberRange(seqStore);

        ListIterator li = m_lgList.listIterator();
        while (li.hasNext())
        {
            LeverageGroupImpl lgi = (LeverageGroupImpl)li.next();

            m_ps1.setLong(1,primaryKey);
            m_ps1.addBatch();
            m_ps2.setLong(1,primaryKey);
            m_ps2.setLong(2,m_sourcePage.getId());

            m_ps2.addBatch();

            lgi.setId(primaryKey);

            primaryKey++;
        }
    }


    public void batchStatements()
        throws Exception
    {
        m_ps1.executeBatch();
        m_ps2.executeBatch();
    }
}
