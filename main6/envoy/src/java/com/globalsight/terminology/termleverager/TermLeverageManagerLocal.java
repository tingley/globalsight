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

package com.globalsight.terminology.termleverager;

import org.apache.log4j.Logger;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.java.TbTerm;
import com.globalsight.terminology.termleverager.TermLeverageMatchDbAccessor;
import com.globalsight.terminology.termleverager.TermLeverageMatchResultSet;

import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.terminology.util.SqlUtil;

import java.util.*;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.sql.Connection;

/**
 * Performs terminology leveraging for TUVs and persists results in
 * the database.
 */
public final class TermLeverageManagerLocal
    implements TermLeverageManager
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            TermLeverageManagerLocal.class);

    private class TermListGenerator
    {
        private ArrayList<TermLeverageMatchResult> m_termList = new ArrayList<TermLeverageMatchResult>();
        private long m_prevSourceTermId = -1;
        private TermLeverageMatchResult m_matchResult = null;

        void init()
        {
            m_termList = new ArrayList<TermLeverageMatchResult>();
            m_prevSourceTermId = -1;
            m_matchResult = null;
        }

        void addTerm(String p_sourceTerm, long p_sourceTermId,
            long p_sourceConceptId, String p_targetTerm,
            long p_targetTermId, long p_targetConceptId, int p_score, String p_xml)
        {
            // accumulate target terms whose corresponding source term
            // is the same into a single TermLeverageMatchResult
            if (p_sourceTermId != m_prevSourceTermId)
            {
                if (m_matchResult != null)
                {
                    m_termList.add(m_matchResult);
                }

                m_matchResult = new TermLeverageMatchResult();
                m_matchResult.setSource(p_sourceTerm, p_sourceConceptId,
                    p_sourceTermId, p_score, p_xml);
                m_prevSourceTermId = p_sourceTermId;
            }

            // add a target term
            m_matchResult.addTarget(p_targetTerm, p_targetConceptId,
                p_targetTermId, p_score, "");
        }

        ArrayList<TermLeverageMatchResult> getTermList()
        {
            // add the last TermLeverageMatchResult to the list
            if (m_matchResult != null)
            {
                m_termList.add(m_matchResult);
                m_matchResult = null;
            }

            return m_termList;
        }
    }

    //
    // TermLeverageManager interface methods
    //

    /**
     * Leverages terms and persists them in TERM_LEVERAGE_MATCH table.
     */
    public TermLeverageResult leverageTerms(Collection p_tuvs,
        TermLeverageOptions p_options)
        throws GeneralException, RemoteException
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        return leverageTerms(p_tuvs, p_options, companyId);
    }
    
    /**
     * Leverages terms and persists them in TERM_LEVERAGE_MATCH table.
     */
    public TermLeverageResult leverageTerms(Collection p_tuvs,
        TermLeverageOptions p_options, String p_companyId)
        throws GeneralException, RemoteException
    {
        TermLeverager leverager = new TermLeverager();

        TermLeverageResult result =
            leverager.leverageTerms(new ArrayList(p_tuvs), p_options, p_companyId);

        if (p_options.getSaveToDatabase())
        {
            TermLeverageMatchDbAccessor.saveLeveragedTerms(result, p_options);
        }

        return result;
    }

    /**
     * Retrieves all TermLeverageMatchResults for a given SourcePage.
     */
    public TermLeverageMatchResultSet getTermMatchesForPage(
        SourcePage p_sourcePage, GlobalSightLocale p_targetPageLocale)
        throws GeneralException, RemoteException
    {
        Connection conn = null;
        TermLeverageMatchDbAccessor.SelectResult selectResult = null;

        try
        {
            conn = SqlUtil.hireConnection();

            // query TERM_LEVERAGE_MATCH table
            selectResult = TermLeverageMatchDbAccessor.getTermMatchesForPage(
                p_sourcePage.getId(),
                p_sourcePage.getGlobalSightLocale().getId(),
                p_targetPageLocale.toString(),
                conn);

            TermLeverageMatchResultSet matchResultSet =
                new TermLeverageMatchResultSet();
            TermListGenerator termListGenerator =
                new TermListGenerator();
            long prevTuvId = -1;

            while (selectResult.next())
            {
                long tuvId = selectResult.getLong("tlm.source_tuv_id");

                if (tuvId != prevTuvId)
                {
                    ArrayList<TermLeverageMatchResult> termList = termListGenerator
                            .getTermList();

                    if (termList != null)
                    {
                        matchResultSet.setLeverageMatches(prevTuvId, 0,
                            termList);
                    }

                    termListGenerator.init();
                    prevTuvId = tuvId;
                }

                // put leveraged terms into TermLeverageMatchResult object
                long source_term_id = selectResult.getLong("tlm.source_term_id");
                TbTerm source_term = HibernateUtil.get(TbTerm.class, source_term_id);
                long target_term_id = selectResult.getLong("tlm.target_term_id");
                TbTerm target_term = HibernateUtil.get(TbTerm.class, target_term_id);
                termListGenerator.addTerm(
                    selectResult.getString("src_term.term"),
                    selectResult.getLong("tlm.source_term_id"),
                    source_term.getTbLanguage().getConcept().getId(),
                    selectResult.getString("tgt_term.term"),
                    selectResult.getLong("tlm.target_term_id"),
                    target_term.getTbLanguage().getConcept().getId(),
                    selectResult.getInt("tlm.score"),
                    selectResult.getString("src_term.xml"));
            }

            // finish the last tuv that the loop above didn't finish
            if (prevTuvId != -1)
            {
                ArrayList termList = termListGenerator.getTermList();

                if (termList != null)
                {
                    matchResultSet.setLeverageMatches(prevTuvId, 0, termList);
                }
            }

            return matchResultSet;
        }
        catch (SQLException e)
        {
            throw new GeneralException(e);
        }
        finally
        {
            if (selectResult != null)
            {
                try
                {
                    // close SelectResult which in turns closes its ResultSet
                    selectResult.close();
                }
                catch (Throwable ignore)
                {
                }
            }

            // return the connection
            SqlUtil.fireConnection(conn);
        }
    }

    /**
     * Retrieves Map<TuvId, Set<TermLeverageMatch>> for given SourcePages.
     * Added for get Term Match Result for report.
     */
    public Map<Long, Set<TermLeverageMatch>> getTermMatchesForPages(
            Collection<SourcePage> p_sourcePages,
            GlobalSightLocale p_targetPageLocale) throws GeneralException,
            RemoteException
    {
        Connection conn = null;
        TermLeverageMatchDbAccessor.SelectResult selectResult = null;
        Map<Long, Set<TermLeverageMatch>> result = new HashMap<Long, Set<TermLeverageMatch>>();

        try
        {
            conn = SqlUtil.hireConnection();
            
            for (SourcePage sp : p_sourcePages)
            {
                // query TERM_LEVERAGE_MATCH table
                selectResult = TermLeverageMatchDbAccessor.getTermMatchesForPage(
                        sp.getId(), sp.getGlobalSightLocale().getId(),
                        p_targetPageLocale.toString(), conn);

                while (selectResult.next())
                {
                    long tuvId = selectResult.getLong("tlm.source_tuv_id");
                    
                    TermLeverageMatch tlm = new TermLeverageMatch();
                    tlm.setSourceTuvId(tuvId);
                    tlm.setMatchedSourceTermId(selectResult.getLong("tlm.source_term_id"));
                    tlm.setMatchedSourceTerm(selectResult.getString("src_term.term"));
                    tlm.setMatchedTargetTermId(selectResult.getLong("tlm.target_term_id"));
                    tlm.setMatchedTargetTerm(selectResult.getString("tgt_term.term"));
                    tlm.setScore(selectResult.getInt("tlm.score"));
                    tlm.setConceptId(selectResult.getInt("src_term.cid"));
                    
                    Set<TermLeverageMatch> tlmSet = result.get(tuvId);
                    if (tlmSet == null)
                    {
                        tlmSet = new HashSet<TermLeverageMatch>();
                    }                    
                    tlmSet.add(tlm);                    
                    result.put(tuvId, tlmSet);
                }
            }

            return result;
        }
        catch (SQLException e)
        {
            throw new GeneralException(e);
        }
        finally
        {
            if (selectResult != null)
            {
                try
                {
                    // close SelectResult which in turns closes its ResultSet
                    selectResult.close();
                }
                catch (Throwable ignore)
                {
                }
            }

            // return the connection
            SqlUtil.fireConnection(conn);
        }
    }
    
    /**
     * Retrieves the TermLeverageMatchResult for a single source TuvId.
     */
    public ArrayList<TermLeverageMatchResult> getTermMatchesForSegment(
            long p_srcTuvId, long p_subId, GlobalSightLocale p_targetPageLocale)
            throws GeneralException, RemoteException
    {
        Connection conn = null;
        TermLeverageMatchDbAccessor.SelectResult selectResult = null;

        try
        {
            // p_subId is not used in this release (4.2)

            conn = SqlUtil.hireConnection();

            // query TERM_LEVERAGE_MATCH table
            selectResult = TermLeverageMatchDbAccessor.
                getTermMatchesForSegment(p_srcTuvId,
                    p_targetPageLocale.toString(), conn);

            // put leveraged terms into TermLeverageMatchResult object
            TermListGenerator termListGenerator = new TermListGenerator();

            while (selectResult.next())
            {
                termListGenerator.addTerm(
                    selectResult.getString("src_term.term"),
                    selectResult.getLong("tlm.source_term_id"),
                    selectResult.getLong("src_term.cid"),
                    selectResult.getString("tgt_term.term"),
                    selectResult.getLong("tlm.target_term_id"),
                    selectResult.getLong("tgt_term.cid"),
                    selectResult.getInt("tlm.score"),
                    selectResult.getString("src_term.xml"));
            }

            // return a collection of TermLeverageMatchResult
            return termListGenerator.getTermList();
        }
        catch (SQLException e)
        {
            throw new GeneralException(e);
        }
        finally
        {
            if (selectResult != null)
            {
                try
                {
                    // close SelectResult which in turns closes its ResultSet
                    selectResult.close();
                }
                catch (Throwable ignore)
                {
                }
            }

            // return the connection
            SqlUtil.fireConnection(conn);
        }
    }
}

