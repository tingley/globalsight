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
package com.globalsight.ling.tm2.leverage;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.GlobalSightLocalePool;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * LeverageIterator is a class that implements functions of iterating through
 * the leverage results and returning LeverageMatches objects.
 */

public class LeverageIterator implements Iterator
{
    private static final Logger c_logger = Logger
            .getLogger(LeverageIterator.class.getName());

    private ResultSet m_resultSet;
    private SegmentIdMap m_segmentIdMap;
    private GlobalSightLocale m_sourceLocale;
    private boolean m_isTranslatable;
    private int m_matchTableType;
    private LeverageOptions m_leverageOptions;

    // flags that tracks the availability of the next data
    private boolean m_nextCalled = false;
    private boolean m_hasNext = false;

    // flags that tracks the current status of the query retrieval
    private BaseTmTuv m_currentOriginalTuv = null;
    private long m_currentMatchedTuId = 0;
    private LeverageMatches m_currentLeverageMatches = null;
    private LeveragedTu m_currentMatchedTu = null;

    public LeverageIterator(ResultSet p_resultSet, SegmentIdMap p_segmentIdMap,
            GlobalSightLocale p_sourceLocale, boolean p_isTranslatable,
            int p_matchTableType, LeverageOptions p_leverageOptions)
    {
        m_resultSet = p_resultSet;
        m_segmentIdMap = p_segmentIdMap;
        m_sourceLocale = p_sourceLocale;
        m_isTranslatable = p_isTranslatable;
        m_matchTableType = p_matchTableType;
        m_leverageOptions = p_leverageOptions;
    }

    public boolean hasNext()
    {
        if (!m_nextCalled)
        {
            m_nextCalled = true;
            // can't throw Exception here
            try
            {
                m_hasNext = m_resultSet == null ? false : m_resultSet.next();
            }
            catch (Exception e)
            {
                DbUtil.closeAll(m_resultSet);
                m_resultSet = null;
                throw new RuntimeException(e.getMessage());
            }
        }
        if (m_hasNext == false)
        {
            DbUtil.closeAll(m_resultSet);
            m_resultSet = null;
        }
        return m_hasNext;
    }

    public Object next()
    {
        Object o = null;
        if (hasNext())
        {
            // can't throw Exception here
            try
            {
                o = createLeverageMatches();
            }
            catch (Exception e)
            {
                e.printStackTrace(System.out);

                throw new RuntimeException(e.getMessage());
            }

            m_nextCalled = false;
        }
        else
        {
            throw new NoSuchElementException();
        }
        return o;
    }

    public void remove()
    {
        // it shouldn't be called.
    }

    // Read one or more rows from the query result and returns
    // LeverageMatches object. When this method is called, ResultSet
    // is always pointing to a new row to be read.
    private LeverageMatches createLeverageMatches() throws Exception
    {
        // readCurrentRow() method returns LeverageMatches object when
        // it's ready. If more rows are needed to create
        // LeverageMatches, the method returns null.

        LeverageMatches returningObj = readCurrentRow();

        if (returningObj == null)
        {
            while (m_resultSet.next())
            {
                returningObj = readCurrentRow();
                if (returningObj != null)
                {
                    break;
                }
            }

            // When the loop breaks at the end of the query result,
            // returningObj is still null. Return the current one.
            if (returningObj == null)
            {
                returningObj = m_currentLeverageMatches;
            }
        }

        return returningObj;
    }

    // Read the current row of the query result and create necessary
    // objects. It returns LeverageMatches object when the current
    // result is the start of the data set for the different original
    // source Tuv.
    private LeverageMatches readCurrentRow() throws Exception
    {
        LeverageMatches returningObj = null;

        long orgTuvId = m_resultSet.getLong("org_tuv_id");
        String orgSubId = m_resultSet.getString("org_sub_id");
        BaseTmTuv orgTuv = m_segmentIdMap.get(orgTuvId, orgSubId);
        // String sid = m_resultSet.getString("sid");

        // create a new LeverageMatches when the original Tuv ids are
        // different from the previous one
        if (m_currentOriginalTuv != orgTuv)
        {
            returningObj = m_currentLeverageMatches;
            m_currentLeverageMatches = new LeverageMatches(orgTuv,
                    m_leverageOptions);

            // reset the flags
            m_currentOriginalTuv = orgTuv;
            m_currentMatchedTuId = 0;
        }

        long matchedTuId = m_resultSet.getLong("tu_id");

        // create a new LeveragedTu when matche Tu id is different
        // from the previous one
        if (m_currentMatchedTuId != matchedTuId)
        {
            m_currentMatchedTu = createTu();
            m_currentLeverageMatches.add(m_currentMatchedTu);
            m_currentMatchedTuId = matchedTuId;
        }

        // Tuv is always created.
        m_currentMatchedTu.addTuv(createTuv());

        return returningObj;
    }

    // create LeveragedTu object from the current query result
    private LeveragedTu createTu() throws Exception
    {
        long tuId = m_resultSet.getLong("tu_id");
        long tmId = m_resultSet.getLong("tm_id");
        String format = m_resultSet.getString("format");
        String type = m_resultSet.getString("type");
        float score = m_resultSet.getFloat("score");
        
        String fromWorldServer = null;
        ResultSetMetaData data = m_resultSet.getMetaData();
        int n = data.getColumnCount();
        for (int i = n; i > 0; i--)
        {
            if ("fromWorldServer".equalsIgnoreCase(data.getColumnLabel(i)))
            {
                fromWorldServer = m_resultSet.getString("fromWorldServer");
                break;
            }
        }

        LeveragedTu leveragedTu = null;
        if (m_matchTableType == LeveragedTu.PAGE_TM)
        {
            leveragedTu = new LeveragedPageTu(tuId, tmId, format, type,
                    m_isTranslatable, m_sourceLocale);
            leveragedTu.setMatchState(MatchState.PAGE_TM_EXACT_MATCH);
            leveragedTu.setScore(score);
        }
        else
        {
            leveragedTu = new LeveragedSegmentTu(tuId, tmId, format, type,
                    m_isTranslatable, m_sourceLocale);
            leveragedTu.setMatchState(getSegmentTmMatchState(score));
            leveragedTu.setScore(score);
        }

        leveragedTu.setFromWorldServer("y".equalsIgnoreCase(fromWorldServer));
        
        return leveragedTu;
    }

    // create LeveragedTuv object from the current query result
    private LeveragedTuv createTuv() throws Exception
    {
        long tuvId = m_resultSet.getLong("tuv_id");
        String segment = m_resultSet.getString("segment_string");
        if (segment == null)
        {
            segment = DbUtil.readClob(m_resultSet, "segment_clob");
        }
        long exactMatchKey = m_resultSet.getLong("exact_match_key");
        long localeId = m_resultSet.getLong("locale_id");
        GlobalSightLocale targetLocale = GlobalSightLocalePool
                .getLocaleById(localeId);
        String creationUser = m_resultSet.getString("creation_user");
        Timestamp creationDate = m_resultSet.getTimestamp("creation_date");
        String modifyUser = m_resultSet.getString("modify_user");
        Timestamp modifyDate = m_resultSet.getTimestamp("modify_date");
        String sid = null;
        try
        {
            sid = m_resultSet.getString("sid");
        }
        catch (Exception e)
        {
            // do nothing.
        }

        LeveragedTuv leveragedTuv = null;
        if (m_matchTableType == LeveragedTu.PAGE_TM)
        {
            leveragedTuv = new LeveragedPageTuv(tuvId, segment, targetLocale);           
        }
        else
        {
            leveragedTuv = new LeveragedSegmentTuv(tuvId, segment, targetLocale);
        }

        leveragedTuv.setExactMatchKey(exactMatchKey);
        leveragedTuv.setCreationUser(creationUser);
        leveragedTuv.setCreationDate(creationDate);
        leveragedTuv.setModifyUser(modifyUser);
        leveragedTuv.setModifyDate(modifyDate);
        leveragedTuv.setSid(sid);
        leveragedTuv.setOrgSid(this.m_currentOriginalTuv.getSid());

        return leveragedTuv;
    }

    private MatchState getSegmentTmMatchState(double p_score)
    {
        MatchState state = null;

        if (p_score == 100)
        {
            state = MatchState.SEGMENT_TM_EXACT_MATCH;
        }
        else if (p_score < m_leverageOptions.getMatchThreshold())
        {
            state = MatchState.STATISTICS_MATCH;
        }
        else
        {
            state = MatchState.FUZZY_MATCH;
        }

        return state;
    }

}
