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

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.population.UniqueSegmentRepository;

/**
 * SegmentTmRetriever class is responsible for retrieving data from Segment TM
 */

public class SegmentTmRetriever
    implements TuRetriever
{
    private Connection m_connection = null;
    private long m_tmId;
    private UniqueSegmentRepository m_dataToSave;
    private Set m_targetLocales;
    
    
    /**
     * Constructor
     *
     * @param p_tmId Segment Tm id to get data from 
     * @param p_dataToSave segment data to be saved in
     * UniqueSegmentRepository object
     * @param p_targetLocales a Set of target locales (GlobalSightLocale)
     */
    public SegmentTmRetriever(Connection p_connection, long p_tmId,
        UniqueSegmentRepository p_dataToSave, Set p_targetLocales)
        throws Exception
    {
        m_connection = p_connection;
        m_tmId = p_tmId;
        m_dataToSave = p_dataToSave;
        m_targetLocales = p_targetLocales;
    }
    

    /**
     * Get source segments in a specified Page Tm
     */
    public SegmentQueryResult query()
        throws Exception
    {
        // make lists of translatable and localizable source Tuvs
        Collection sourceSegmentsTr = new ArrayList();
        Collection sourceSegmentsLo = new ArrayList();
        Iterator it = m_dataToSave.sourceTuvIterator();
        while(it.hasNext())
        {
            BaseTmTuv tuv = (BaseTmTuv)it.next();
            if(tuv.isTranslatable())
            {
                sourceSegmentsTr.add(tuv);
            }
            else
            {
                sourceSegmentsLo.add(tuv);
            }
        }
        
        // get translatable segments
        SegmentTmQueryProcCaller callerTr =
            new SegmentTmQueryProcCaller(m_connection, m_tmId,
                m_dataToSave.getSourceLocale().getId(), true,
                m_targetLocales, sourceSegmentsTr);

        ResultSet rsTr = callerTr.getNextResult();

        // get localizable segments
        SegmentTmQueryProcCaller callerLo =
            new SegmentTmQueryProcCaller(m_connection, m_tmId,
                m_dataToSave.getSourceLocale().getId(), false,
                m_targetLocales, sourceSegmentsLo);

        ResultSet rsLo = callerLo.getNextResult();

        return new SegmentTmQueryResult(rsTr, rsLo,
            m_dataToSave.getSourceLocale(), m_tmId);
    }
    
    public void close()
    {
    }
}
