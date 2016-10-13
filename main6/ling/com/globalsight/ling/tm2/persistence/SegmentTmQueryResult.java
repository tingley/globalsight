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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.globalsight.everest.projecthandler.ProjectTmTuTProp;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.GlobalSightLocalePool;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.util.GlobalSightLocale;

/**
 * SegmentTmQueryResult is a wrapper of a ResultSet object that is
 * returned by SegmentTmRetriever#query() method.
 */

public class SegmentTmQueryResult
    extends SegmentQueryResult
{
    private GlobalSightLocale m_sourceLocale;
    private long m_tmId;
    private ResultSet m_rsTr;
    private ResultSet m_rsLo;
    
    // This result set is set to the current result set. It's either
    // m_rsTr or m_rsLo. We start reading from m_rsTr and then m_rsLo.
    private ResultSet m_current;
    

    /**
     * constructor
     *
     * @param p_rsTr ResultSet object for translatables
     * @param p_rsLo ResultSet object for localizables
     * @param p_sourceLocale source locale
     * @param p_tmId Tm id
     */
    public SegmentTmQueryResult(ResultSet p_rsTr,
        ResultSet p_rsLo, GlobalSightLocale p_sourceLocale, long p_tmId)
    {
        m_sourceLocale = p_sourceLocale;
        m_tmId = p_tmId;
        m_rsTr = p_rsTr;
        m_rsLo = p_rsLo;
        m_current = p_rsTr;
    }
    
    

    /**
     * Advances a row ahead just like ResultSet does.
     */
    protected boolean next()
        throws Exception
    {
        if (m_current==null)
            return false;

        boolean hasNext = false;
        if(m_current.next() == true)
        {
            hasNext = true;
        }
        else if(m_current == m_rsTr)
        {
            m_current = m_rsLo;
            hasNext = m_current == null ? false : m_current.next();
        }

        if (hasNext==false)
        {
        	closeStatement(m_rsTr);
        	closeStatement(m_rsLo);
            DbUtil.silentClose(m_rsTr);
            DbUtil.silentClose(m_rsLo);
            m_current=null;
            m_rsTr=null;
            m_rsLo=null;
        }
        
        return hasNext;
    }
    
    private void closeStatement(ResultSet rs) {
    	if (rs != null) {
    		try {
	    		Statement statement = rs.getStatement();
	    		if (statement != null) {
	    		    statement.close();
	    		}
    		} catch (SQLException e) {
    			// Do nothing
    		}
    	}
    }

    /**
     * Create PageTmTu object
     */
    protected BaseTmTu createTu()
        throws Exception
    {
        long tuid = getTuId();
        SegmentTmTu tu = new SegmentTmTu(tuid, m_tmId, getTuFormat(),
                getTuType(), isTranslatable(), m_sourceLocale);
        tu.setProps(ProjectTmTuTProp.getTuProps(tuid));

        return tu;
    }


    /**
     * Create PageTmTuv object
     */
    protected BaseTmTuv createTuv()
        throws Exception
    {
        BaseTmTuv segmentTmTuv = new SegmentTmTuv(getTuvId(), getTuvSegment(),
            getTuvGlobalSightLocale());
        segmentTmTuv.setExactMatchKey(getTuvExactMatchKey());
        segmentTmTuv.setModifyDate(getModifyDate());
        segmentTmTuv.setSid(getSid());

        return segmentTmTuv;
    }


    // Returned result set is defined as follows.
    // 'SELECT tu.id tu_id, format, type, tuv.id tuv_id, segment_string, '
    // '       segment_clob, exact_match_key, locale_id, modify_date '

    protected long getTuId()
        throws Exception
    {
        return m_current.getLong(1);
    }
    
    private String getTuFormat()
        throws Exception
    {
        return m_current.getString(2);
    }
    
    private String getTuType()
        throws Exception
    {
        return m_current.getString(3);
    }
    
    private boolean isTranslatable()
        throws Exception
    {
        return m_current == m_rsTr;
    }
    
    private long getTuvId()
        throws Exception
    {
        return m_current.getLong(4);
    }
    
    private String getTuvSegment()
        throws Exception
    {
        String segment = m_current.getString(5);
        if(segment == null)
        {
            segment = DbUtil.readClob(m_current, 6);
        }
        return segment;
    }
    
    private long getTuvExactMatchKey()
        throws Exception
    {
        return m_current.getLong(7);
    }

    private GlobalSightLocale getTuvGlobalSightLocale()
        throws Exception
    {
        long locale_id = m_current.getLong(8);
        return GlobalSightLocalePool.getLocaleById(locale_id);
    }
    
    private Timestamp getModifyDate()
        throws Exception
    {
        return m_current.getTimestamp(9);
    }
    
    private String getSid() throws SQLException
    {
        String sid = null;
        ResultSetMetaData data = m_current.getMetaData();
        if (data.getColumnCount() >= 10)
        {
            sid = m_current.getString(10);
        }
        
        return sid;
    }
}
