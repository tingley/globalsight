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
package com.globalsight.ling.tm2.persistence;

import java.sql.ResultSet;
import java.sql.Timestamp;

import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.GlobalSightLocalePool;
import com.globalsight.ling.tm2.PageTmTu;
import com.globalsight.ling.tm2.PageTmTuv;
import com.globalsight.machineTranslation.MTHelper;
import com.globalsight.util.GlobalSightLocale;

/**
 * PageJobDataQueryResult is a wrapper of a ResultSet object that is returned by
 * PageJobDataRetriever#query() method.
 */

public class PageJobDataQueryResult extends SegmentQueryResult
{
    private ResultSet m_resultSet = null;
    // GBS-3722
    private boolean m_isExport = false;

    /**
     * constructor
     * 
     * @param p_resultSet
     *            ResultSet object
     */
    public PageJobDataQueryResult(ResultSet p_resultSet)
    {
        m_resultSet = p_resultSet;
    }

    public PageJobDataQueryResult(ResultSet p_resultSet, boolean p_isExport)
    {
        m_resultSet = p_resultSet;
        m_isExport = p_isExport;
    }

    /**
     * Advances a row ahead just like ResultSet does.
     */
    protected boolean next() throws Exception
    {
        if (m_resultSet == null)
            return false;

        boolean hasNext = m_resultSet.next();
        if (hasNext == false)
        {
            DbUtil.silentClose(m_resultSet);
            m_resultSet = null;
        }
        return hasNext;
    }

    /**
     * Create PageTmTu object
     */
    protected BaseTmTu createTu() throws Exception
    {
        BaseTmTu tmTu = new PageTmTu(getTuId(), getTmId(), getTuFormat(),
                getTuType(), isTranslatable());
        tmTu.setSourceTmName(getSourceTmName());
        return tmTu;
    }

    /**
     * Create PageTmTuv object
     */
    protected BaseTmTuv createTuv() throws Exception
    {
        long crc = getTuvExactMatchKey();
        PageTmTuv jobTuv = new PageTmTuv(getTuvId(), getTuvSegment(),
                getTuvGlobalSightLocale());
        jobTuv.setState(getTuvState());
        jobTuv.setMergeState(getTuvMergeState());
        jobTuv.setModifyDate(getTuvModifyTime());

        jobTuv.setModifyUser(getTuvMofifyUser());
        jobTuv.setCreationDate(getTuvCreationTime());
        jobTuv.setCreationUser(getTuvCreationUser());
        jobTuv.setUpdatedProject(getTuvUpdatedProject());

        jobTuv.setSid(getSid());

        // Not all segments have exact_match_key set in
        // translation_unit_variant table
        if (crc == 0)
        {
            jobTuv.setExactMatchKey();
        }
        else
        {
            jobTuv.setExactMatchKey(crc);
        }

        return jobTuv;
    }

    protected long getTuId() throws Exception
    {
        return m_resultSet.getLong(1);
    }

    private long getTmId() throws Exception
    {
        return m_resultSet.getLong(2);
    }

    private String getTuFormat() throws Exception
    {
        return m_resultSet.getString(3);
    }

    private String getTuType() throws Exception
    {
        return m_resultSet.getString(4);
    }

    private boolean isTranslatable() throws Exception
    {
        String s = m_resultSet.getString(5);
        return s.equals("T");
    }

    private long getTuvId() throws Exception
    {
        return m_resultSet.getLong(6);
    }

    private String getTuvSegment() throws Exception
    {
        String segment = m_resultSet.getString(7);
        if (segment == null)
        {
            segment = DbUtil.readClob(m_resultSet, 8);
        }
        if (m_isExport)
        {
            // GBS-3722, remove MT tags before going to TM
            segment = MTHelper.cleanMTTagsForTMStorage(segment);
        }
        return segment;
    }

    private GlobalSightLocale getTuvGlobalSightLocale() throws Exception
    {
        long locale_id = m_resultSet.getLong(9);
        return GlobalSightLocalePool.getLocaleById(locale_id);
    }

    private String getTuvState() throws Exception
    {
        return m_resultSet.getString(10);
    }

    private String getTuvMergeState() throws Exception
    {
        return m_resultSet.getString(11);
    }

    private Timestamp getTuvModifyTime() throws Exception
    {
        return m_resultSet.getTimestamp(12);
    }

    private long getTuvExactMatchKey() throws Exception
    {
        return m_resultSet.getLong(13);
    }

    private String getSourceTmName() throws Exception
    {
        return m_resultSet.getString(14);
    }

    private String getTuvMofifyUser() throws Exception
    {
        return m_resultSet.getString(15);
    }

    private Timestamp getTuvCreationTime() throws Exception
    {
        return m_resultSet.getTimestamp(16);
    }

    private String getTuvCreationUser() throws Exception
    {
        return m_resultSet.getString(17);
    }

    private String getTuvUpdatedProject() throws Exception
    {
        return m_resultSet.getString(18);
    }

    private String getSid() throws Exception
    {
        return m_resultSet.getString(19);
    }
}
