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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.StoredProcCallerProxy;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.StoredProcCaller;
import com.globalsight.ling.tm2.leverage.TokenMatch;
import com.globalsight.util.GlobalSightLocale;

/**
 * SegmentTmMatchRetrieveProcCaller calls stored procedures to retrieve matches
 * from Segment Tm.
 */

public class SegmentTmMatchRetrieveProcCaller implements StoredProcCallerProxy
{
    private static final Logger c_logger = Logger
            .getLogger(SegmentTmMatchRetrieveProcCaller.class.getName());

    private Connection m_connection;
    private Collection m_locales;

    private Iterator m_orgTuvIdIt;
    private Iterator m_orgSubIdIt;
    private Iterator m_matchedTuIdIt;
    private Iterator m_scoreIt;

    /**
     * Constructor
     * 
     * @param p_connection
     *            Connection
     * @param p_locales
     *            Collection of locales (GlobalSightLocale) of the segments to
     *            retrieve
     * @param p_tokenMatches
     *            Collection of TokenMatch objects
     */
    public SegmentTmMatchRetrieveProcCaller(Connection p_connection,
            Collection p_locales, Collection p_tokenMatches) throws Exception
    {
        m_connection = p_connection;
        m_locales = p_locales;
        m_locales = new ArrayList();
        Iterator iterator = p_locales.iterator();
        while (iterator.hasNext())
        {
            GlobalSightLocale locale = (GlobalSightLocale) iterator.next();
            m_locales.add(locale.getIdAsLong());
        }

        splitTokenMatchParam(p_tokenMatches);
    }

    public ResultSet getNextResult() throws PersistenceException
    {
        ResultSet resultSet = null;

        try
        {
            // number of iterations of all iterators are always the
            // same
            while (m_orgTuvIdIt.hasNext())
            {
                resultSet = callProc(m_locales, (List) m_orgTuvIdIt.next(),
                        (List) m_orgSubIdIt.next(), (List) m_matchedTuIdIt
                                .next(), (List) m_scoreIt.next());

                if (resultSet != null)
                {
                    break;
                }
            }
        }
        catch (PersistenceException pe)
        {
            throw pe;
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }

        return resultSet;
    }

    /**
     * Split the list of token match info.
     * 
     * @param p_tokenMatches
     *            Collection of TokenMatch objects
     */
    private void splitTokenMatchParam(Collection p_tokenMatches)
            throws Exception
    {
        // List of Long (original tuv id)
        List orgTuvIds = new ArrayList(p_tokenMatches.size());
        // List of String (original sub id)
        List orgSubIds = new ArrayList(p_tokenMatches.size());
        // List of Long (matched tu id)
        List matchedTuIds = new ArrayList(p_tokenMatches.size());
        // List of Long (score)
        List scores = new ArrayList(p_tokenMatches.size());

        Iterator it = p_tokenMatches.iterator();
        while (it.hasNext())
        {
            TokenMatch tokenMatch = (TokenMatch) it.next();

            orgTuvIds.add(new Long(tokenMatch.getOriginalTuvId()));
            orgSubIds.add(tokenMatch.getOriginalSubId());
            matchedTuIds.add(new Long(tokenMatch.getMatchedTuId()));
            scores.add(new Float(tokenMatch.getScore()));
        }

        Collection orgTuvIdARRAYs = new ArrayList();
        Collection orgSubIdARRAYs = new ArrayList();
        Collection matchedTuIdARRAYs = new ArrayList();
        Collection scoreARRAYs = new ArrayList();

        int arraySize = p_tokenMatches.size() / DbUtil.MAX_ELEM;
        for (int i = 0; i < arraySize; i++)
        {
            orgTuvIdARRAYs.add(orgTuvIds.subList(i * DbUtil.MAX_ELEM, (i + 1)
                    * DbUtil.MAX_ELEM));

            orgSubIdARRAYs.add(orgSubIds.subList(i * DbUtil.MAX_ELEM, (i + 1)
                    * DbUtil.MAX_ELEM));

            matchedTuIdARRAYs.add(matchedTuIds.subList(i * DbUtil.MAX_ELEM,
                    (i + 1) * DbUtil.MAX_ELEM));

            scoreARRAYs.add(scores.subList(i * DbUtil.MAX_ELEM, (i + 1)
                    * DbUtil.MAX_ELEM));
        }

        if (p_tokenMatches.size() % DbUtil.MAX_ELEM > 0)
        {
            orgTuvIdARRAYs.add(orgTuvIds.subList(arraySize * DbUtil.MAX_ELEM,
                    orgTuvIds.size()));

            orgSubIdARRAYs.add(orgSubIds.subList(arraySize * DbUtil.MAX_ELEM,
                    orgSubIds.size()));

            matchedTuIdARRAYs.add(matchedTuIds.subList(arraySize
                    * DbUtil.MAX_ELEM, matchedTuIds.size()));

            scoreARRAYs.add(scores.subList(arraySize * DbUtil.MAX_ELEM, scores
                    .size()));
        }

        m_orgTuvIdIt = orgTuvIdARRAYs.iterator();
        m_orgSubIdIt = orgSubIdARRAYs.iterator();
        m_matchedTuIdIt = matchedTuIdARRAYs.iterator();
        m_scoreIt = scoreARRAYs.iterator();
    }

    private ResultSet callProc(Collection p_locales, List p_orgTuvIds,
            List p_orgSubIds, List p_matchedTuIds, List p_scores)
            throws Exception
    {
        Statement statement = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;

        if (p_locales == null || p_orgTuvIds == null || p_orgSubIds == null
                || p_matchedTuIds == null || p_scores == null)
        {
            throw new PersistenceException("Fuzzy token list is null.");
        }

        if (p_orgTuvIds.size() != p_orgSubIds.size() 
                || p_orgSubIds.size() != p_matchedTuIds.size()
                || p_matchedTuIds.size() != p_scores.size())
        {
            throw new PersistenceException(
                    "Number of items in some of the parameter are inconsistence.");
        }

        statement = m_connection.createStatement();
        startProc(statement);

        String preparedSql = "INSERT INTO tmp_ids VALUES (?, ?, ?, ?)";
        preparedStatement = m_connection.prepareStatement(preparedSql);
        for (int i = 0, length = p_matchedTuIds.size(); i < length; i++)
        {
            preparedStatement.setLong(1, ((Long) p_orgTuvIds.get(i))
                    .longValue());
            preparedStatement.setString(2, (String) p_orgSubIds.get(i));
            preparedStatement.setLong(3, ((Long) p_matchedTuIds.get(i))
                    .longValue());
            preparedStatement.setFloat(4, ((Float) p_scores.get(i)).floatValue()); 

            preparedStatement.execute();
        }

        String query = " SELECT tmp.org_tuv_id org_tuv_id, tmp.org_sub_id org_sub_id, "
                + "        tu.id tu_id, tu.tm_id tm_id, tu.format format, "
                + "        tu.type type, tuv.id tuv_id, tuv.segment_string segment_string, "
                + "        tuv.segment_clob segment_clob, tuv.creation_user creation_user, "
                + "        tuv.exact_match_key exact_match_key, tuv.locale_id locale_id, "
                + "        tuv.modify_date modify_date, tmp.score score, tuv.sid, tu.FROM_WORLD_SERVER fromWorldServer, "
                + "        tuv.creation_date creation_date, tuv.modify_user modify_user "
                + " FROM project_tm_tu_t tu, project_tm_tuv_t tuv, tmp_ids tmp "
                + " WHERE tmp.tu_id = tu.id "
                + " AND tu.id = tuv.tu_id "
                + " AND tmp.tu_id = tuv.tu_id "
                + " AND tuv.locale_id IN "
                + " (:locale_id_list) "
                + " ORDER BY tmp.org_tuv_id, tmp.org_sub_id, tu.id ";

        query = query.replaceAll(":locale_id_list", StoredProcCaller
                .convertCollectionToSql(p_locales, null));

        rs = statement.executeQuery(query);

        return rs;
    }

    private void startProc(Statement statement) throws SQLException
    {
        dropTempTable(statement);
        statement
                .execute("create temporary table tmp_ids (org_tuv_id bigint, org_sub_id VARCHAR(40), tu_id bigint, score decimal(8, 3))");
    }

    private void dropTempTable(Statement statement) throws SQLException
    {
        statement.execute("drop temporary table if exists tmp_ids");
    }
}
