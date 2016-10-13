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

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.PageTmTu;
import com.globalsight.ling.tm2.PageTmTuv;

import java.sql.ResultSet;

/**
 * PageTmQueryResult is a wrapper of a ResultSet object that is
 * returned by PageTmRetriever#query() method.
 */

public class PageTmQueryResult
    extends SegmentQueryResult
{
    private GlobalSightLocale m_sourceLocale;
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
     */
    public PageTmQueryResult(ResultSet p_rsTr, ResultSet p_rsLo,
        GlobalSightLocale p_sourceLocale)
    {
        m_rsTr = p_rsTr;
        m_rsLo = p_rsLo;
        m_sourceLocale = p_sourceLocale;
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
            hasNext = m_current.next();
        }
        
        if (hasNext==false)
        {
            DbUtil.silentClose(m_rsTr);
            DbUtil.silentClose(m_rsLo);
            m_current=null;
            m_rsTr=null;
            m_rsLo=null;
        }

        return hasNext;
    }



    /**
     * Create PageTmTu object
     */
    protected BaseTmTu createTu()
        throws Exception
    {
        return new PageTmTu(getTuId(), 0, getTuFormat(),
            getTuType(), isTranslatable());
    }


    /**
     * Create PageTmTuv object
     */
    protected BaseTmTuv createTuv()
        throws Exception
    {
        BaseTmTuv pageTmTuv
            = new PageTmTuv(0, getTuvSegment(), m_sourceLocale);
        pageTmTuv.setExactMatchKey(getTuvExactMatchKey());

        return pageTmTuv;
    }

    
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
    
    private String getTuvSegment()
        throws Exception
    {
        String segment = m_current.getString(4);
        if(segment == null)
        {
            segment = DbUtil.readClob(m_current, 5);
        }
        return segment;
    }
    
    private long getTuvExactMatchKey()
        throws Exception
    {
        return m_current.getLong(6);
    }
}
