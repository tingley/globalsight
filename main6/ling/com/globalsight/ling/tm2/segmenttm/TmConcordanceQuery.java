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
package com.globalsight.ling.tm2.segmenttm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.projecthandler.ProjectTmTuT;
import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.tm2.lucene.LuceneSearcher;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;


/**
 * This class makes a query for TM concordance.
 * 
 * This code is specific to the TM2 Segment TM.
 */
public class TmConcordanceQuery
{
    static private Logger c_category =
        Logger.getLogger(
            TmConcordanceQuery.class);

    static private String CHECK_TARGET_SEGMENT
        = "SELECT tu_id FROM project_tm_tuv_t "
        + "WHERE locale_id = ? AND tu_id = ?";
    
    static private String GET_ALL_TU_ID
        = "SELECT tu_id, tm_id FROM project_tm_tuv_t tuv, project_tm_tu_t tu "
        + "WHERE tuv.locale_id = ? AND tu.id = tuv.tu_id "
        + "AND tu.tm_id IN ";

    private Connection m_connection;

    //
    // Constructor
    //

    public TmConcordanceQuery(Connection p_connection)
    {
        m_connection = p_connection;
    }


    //
    // Public Methods
    //

    /**
     * Returns TU id list of matched segments. 
     *
     * @param p_tmIds List of TM ids (Long)
     * @param p_searchPattern search pattern a user entered
     * @param p_sourceLocale Source locale
     * @param p_targetLocale Target locale
     * @return List of TU ids from project_tm_tuv_t table (Long)
     */
    public List<TMidTUid> query(List<Tm> p_tms, String p_searchPattern,
        GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale)
        throws Exception
    {
        List<TMidTUid> result = null;

        List<Long> tmIds = new ArrayList<Long>();
        for (Tm tm : p_tms)
        {
            tmIds.add(tm.getId());
        }
        
        result = LuceneSearcher.search(
                tmIds, p_searchPattern, p_sourceLocale, null);
        // filter out tus that don't have target locale segment
        return getTuListByTargetLocale(result, p_targetLocale, tmIds);
    }

    private List<TMidTUid> getAllTuId(
        GlobalSightLocale p_sourceLocale, List p_tmIds)
        throws Exception
    {
        List<TMidTUid> resultTuList = new ArrayList<TMidTUid>();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try
        {
            String inClause = DbUtil.createIntegerInClause(p_tmIds);

            stmt = m_connection.prepareStatement(GET_ALL_TU_ID + inClause);
            stmt.setLong(1, p_sourceLocale.getId());
            rs = stmt.executeQuery();

            while(rs.next())
            {
                resultTuList.add(new TMidTUid(
                            rs.getLong("tm_id"), rs.getLong("tu_id")));
            }
        }
        finally
        {
            if(rs != null)
            {
                rs.close();
            }
            
            if(stmt != null)
            {
                stmt.close();
            }
        }
        
        return resultTuList;
    }
    

    private List<TMidTUid> getTuListByTargetLocale(
        List<TMidTUid> p_tuIdList, GlobalSightLocale p_targetLocale, List<Long> tmIds)
        throws Exception
    {
        List<TMidTUid> resultTuList = new ArrayList<TMidTUid>();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try
        {
            stmt = m_connection.prepareStatement(CHECK_TARGET_SEGMENT);
            stmt.setLong(1, p_targetLocale.getId());

            for(Iterator<TMidTUid> it = p_tuIdList.iterator(); it.hasNext(); )
            {
                TMidTUid tt = it.next();
                long tuId = tt.getTuId();
                float score = tt.getMatchScore();
                stmt.setLong(2, tuId);
                rs = stmt.executeQuery();
                
                if(rs.next())
                {
                	ProjectTmTuT tu = HibernateUtil.get(ProjectTmTuT.class, tuId);
                	if(tmIds.contains(tu.getProjectTm().getId()))
                	{
                        resultTuList.add(new TMidTUid(
                                tu.getProjectTm().getId(), tuId, score));
                	}
                }
            }
        }
        finally
        {
            if(rs != null)
            {
                rs.close();
            }
            
            if(stmt != null)
            {
                stmt.close();
            }
        }
        
        return resultTuList;
    }
}
