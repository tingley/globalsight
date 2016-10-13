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
package com.globalsight.ling.inprogresstm.persistence;

import org.apache.log4j.Logger;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm2.indexer.Token;
import com.globalsight.ling.tm2.persistence.DbUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;

/**
 * IndexPersistence class is responsible for accessing index tables in
 * in-progress TM.
 */

public class IndexPersistence
{
    private static final Logger c_logger = 
        Logger
            .getLogger(IndexPersistence.class);

    // index table name
    public static final String INDEX_TABLE = "IP_TM_INDEX";
    
    // select indexes
    private static final String SELECT_INDEXES
        = "SELECT TOKEN, SRC_ID, JOB_ID, REPETITION, TOTAL_TOKEN_COUNT FROM IP_TM_INDEX";
    
    private static final String SELECT_INDEXES_CONDITION_A = " WHERE TOKEN IN ";
    
    private static final String SELECT_INDEXES_CONDITION_B = " AND LOCALE_ID = ? ";
    
    private static final String SELECT_INDEXES_CONDITION_C = " AND JOB_ID IN ";
    
    private static final String TM_CONDITION_UNION = " UNION ";
    
    private static final String TM_CONDITION_A = " WHERE TOKEN IN ";
    
    private static final String TM_CONDITION_B = " AND LOCALE_ID = ? AND POPULATION_TM_ID IN ";

    // delete indexes
    private static final String DELETE_INDEXES
        = "DELETE from ip_tm_index WHERE job_id = ?";
    

    // insert indexes
    private static final String INSERT_INDEX
        = "INSERT INTO ip_tm_index (token, src_id, job_id, "
        + "population_tm_id, locale_id, repetition, total_token_count) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?)";


    private Connection m_connection;

    public IndexPersistence(Connection p_connection)
    {
        m_connection = p_connection;
    }

    /**
     * Retrieves indexes for leveraging a text.
     * 
     * @param p_tokenStrings
     *            token strings of original text
     * @param p_sourceLocale
     *            source locale
     * @param p_jobId
     *            job id to leverage from
     * @param p_tmIds
     *            TM ids to leverage from. This parameter can be null.
     * @return Collection of Token objects
     */
    public Collection getIndexes(
        Set p_tokenStrings, GlobalSightLocale p_sourceLocale,
        long p_jobId, Set p_tmIds)
        throws Exception
    {
        HashSet<Long> p_jobIds = new HashSet();
        p_jobIds.add(p_jobId);
        Collection tokens = getIndexes(p_tokenStrings, p_sourceLocale,
                p_jobIds, p_tmIds);
        return tokens;
    }

    /**
     * Retrieves indexes for leveraging a text. This method allows to leverage
     * from specified multiple jobs.
     * 
     * @param p_tokenStrings
     *            token strings of original text
     * @param p_sourceLocale
     *            source locale
     * @param p_jobIds
     *            job ids to leverage from
     * @param p_tmIds
     *            TM ids to leverage from. This parameter can be null.
     * @return Collection of Token objects
     */
    public Collection getIndexes(Set p_tokenStrings,
            GlobalSightLocale p_sourceLocale, Set<Long> p_jobIds,
            Set<Long> p_tmIds) throws Exception
    {
        // In general, jobIds can't be empty.If empty, return null regardless
        // what p_tmIds are.
        if (p_jobIds == null || p_jobIds.size() == 0 ) {
            return null;
        }
        
        String querySQL = getQuerySQL(p_tokenStrings,  p_jobIds, p_tmIds);
        
        Collection tokens = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ps = m_connection.prepareStatement(querySQL);
            ps.setLong(1, p_sourceLocale.getId());
            if (p_tmIds != null && p_tmIds.size() > 0) {
                ps.setLong(2, p_sourceLocale.getId());
            }
            rs = ps.executeQuery();
            tokens = composeTokens(rs);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
        }

        return tokens;
    }
    
    private String getQuerySQL(Set p_tokenStrings, Set<Long> p_jobIds, Set<Long> p_tmIds)
    {
        StringBuffer query = new StringBuffer();
        
        query = query.append(SELECT_INDEXES);
        query = query.append(SELECT_INDEXES_CONDITION_A);
        query = query.append(DbUtil.createStringInClause(p_tokenStrings));
        query = query.append(SELECT_INDEXES_CONDITION_B);
        query = query.append(SELECT_INDEXES_CONDITION_C);
        query = query.append(DbUtil.createIntegerInClause(p_jobIds));
        
        if(p_tmIds != null && p_tmIds.size() > 0)
        {
            query = query.append(TM_CONDITION_UNION);
            query = query.append(SELECT_INDEXES);
            query = query.append(TM_CONDITION_A);
            query = query.append(DbUtil.createStringInClause(p_tokenStrings));
            query = query.append(TM_CONDITION_B);
            query = query.append(DbUtil.createIntegerInClause(p_tmIds));
        }
        
        return query.toString();
    }

    /**
     * delete indexes for a given job id
     *
     * @param p_jobId job id
     */
    public void delete(long p_jobId)
        throws Exception
    {
        PreparedStatement ps = null;
        try
        {
            ps = m_connection.prepareStatement(DELETE_INDEXES);
            ps.setLong(1, p_jobId);
            ps.executeUpdate();
        }
        finally
        {
            if(ps != null)
            {
            	DbUtil.silentClose(ps);
            }
        }
    }
    

    /**
     * insert tokens
     *
     * @param p_toknes Collection of Token objects
     * @param p_populationTmId Segment TM id this segment is to be saved
     * @param p_sourceLocale source locale
     */
    public void insertTokens(Collection p_toknes,
        long p_populationTmId, GlobalSightLocale p_sourceLocale)
        throws Exception
    {
        PreparedStatement ps = null;
        try
        {
            ps = m_connection.prepareStatement(INSERT_INDEX);

            for(Iterator it = p_toknes.iterator(); it.hasNext();)
            {
                Token token = (Token)it.next();
                
                ps.setString(1, token.getTokenString());
                ps.setLong(2, token.getTuId());
                ps.setLong(3, token.getTmId());
                ps.setLong(4, p_populationTmId);
                ps.setLong(5, p_sourceLocale.getId());
                ps.setInt(6, token.getRepetition());
                ps.setInt(7, token.getTotalTokenCount());

                ps.addBatch();
            }

            ps.executeBatch();
        }
        finally
        {
            if(ps != null)
            {
                ps.close();
            }
        }
    }
        


    // create Collection of Token objects
    private Collection composeTokens(ResultSet p_rs)
        throws Exception
    {
        ArrayList tokens = new ArrayList();
        
        while(p_rs.next())
        {
            Token token = new Token(p_rs.getString("token"),
                p_rs.getLong("src_id"), p_rs.getLong("src_id"),
                p_rs.getLong("job_id"), p_rs.getInt("repetition"),
                p_rs.getInt("total_token_count"), true);
            tokens.add(token);
        }
        
        return tokens;
    }
}
