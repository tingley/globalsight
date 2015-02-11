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

import com.globalsight.persistence.PersistenceCommand;

import com.globalsight.everest.persistence.PersistenceException;

import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.PageWordCounts;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;

import java.util.List;
import java.util.Iterator;

public class StatisticsPersistenceCommand extends PersistenceCommand
{
    private List m_targetPages;
    private String m_updateTargetPage = 
        "update target_page set fuzzy_low_word_count = ? " +
        ", fuzzy_med_word_count = ? , fuzzy_med_hi_word_count = ? " +
        ", fuzzy_hi_word_count = ? , exact_context_word_count = ? " +
        ", exact_segment_tm_word_count = ? , no_match_word_count = ? " +
        ", repetition_word_count = ? , total_word_count = ? " + 
        ", sub_lev_match_word_count = ?, sub_lev_repetition_word_count = ? "+
        ", timestamp = ? , in_context_match_word_count = ? " +
        ", no_use_ic_match_word_count = ?, total_exact_match_word_count = ?, is_default_context_match = ? " +
        " where id = ? ";

    private PreparedStatement m_ps;

    public StatisticsPersistenceCommand(List p_targetPages)
    {
        m_targetPages = p_targetPages;
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
                if (m_ps != null) m_ps.close();
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
        Iterator it = m_targetPages.iterator();
        while (it.hasNext())
        {
            TargetPage tp = (TargetPage)it.next();
            PageWordCounts pwc = tp.getWordCount();
            m_ps.setLong(1,pwc.getLowFuzzyWordCount());
            m_ps.setLong(2,pwc.getMedFuzzyWordCount());
            m_ps.setLong(3,pwc.getMedHiFuzzyWordCount());
            m_ps.setLong(4,pwc.getHiFuzzyWordCount());
            m_ps.setLong(5,pwc.getContextMatchWordCount());
            m_ps.setLong(6,pwc.getSegmentTmWordCount());
            m_ps.setLong(7,pwc.getUnmatchedWordCount());
            m_ps.setLong(8,pwc.getRepetitionWordCount());            
            m_ps.setLong(9,pwc.getTotalWordCount());
            m_ps.setLong(10,pwc.getSubLevMatchWordCount());
            m_ps.setLong(11,pwc.getSubLevRepetitionWordCount());
            m_ps.setDate(12, new Date(System.currentTimeMillis()));
            m_ps.setLong(13, pwc.getInContextWordCount());
            m_ps.setLong(14, pwc.getNoUseInContextMatchWordCount());
            m_ps.setLong(15, pwc.getTotalExactMatchWordCount());
            m_ps.setBoolean(16, tp.getIsDefaultContextMatch());
            m_ps.setLong(17, tp.getId());

            m_ps.addBatch();
        }
    }
    public void batchStatements() throws Exception
    {
        m_ps.executeBatch();
    }

}
