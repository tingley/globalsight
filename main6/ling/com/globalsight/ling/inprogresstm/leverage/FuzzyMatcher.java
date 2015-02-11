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
package com.globalsight.ling.inprogresstm.leverage;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.ling.inprogresstm.persistence.IndexPersistence;
import com.globalsight.ling.inprogresstm.persistence.TmPersistence;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.indexer.Token;
import com.globalsight.ling.tm2.indexer.Tokenizer;
import com.globalsight.ling.tm2.indexer.WordTokenizer;
import com.globalsight.ling.tm2.leverage.MatchState;
import com.globalsight.ling.tm2.leverage.TokenMatch;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

/**
 * FuzzyMatcher is responsible for calculating fuzzy match score and find
 * matches for a segment.
 */
class FuzzyMatcher
{
    private static final Logger c_logger = Logger.getLogger(FuzzyMatcher.class);

    // tokens of the original text
    // key: token string
    // value: Token object
    private HashMap m_originalTokens;

    private int m_totalOriginalTokenCount;
    private GlobalSightLocale m_sourceLocale;
    private Connection connection = null;

    // constructor
    public FuzzyMatcher(BaseTmTuv p_sourceTuv, Connection p_connection)
            throws Exception
    {
        connection = p_connection;
        populateOriginalTokenMap(p_sourceTuv);
        m_sourceLocale = p_sourceTuv.getLocale();
    }

    /**
     * Leverages a translatable segment from in-progress TM.
     * 
     * @param p_jobId
     *            job id to leverage from
     * @param p_tmIds
     *            TM ids to leverage from. This parameter can be null.
     * @param p_targetLocale
     *            target locale
     * @param p_matchThreshold
     *            match threshold
     * @param p_maxMatchCount
     *            max match count
     * @return Collection of LeveragedInProgressTu objects
     */
    public Collection leverage(long p_jobId, Set p_tmIds,
            GlobalSightLocale p_targetLocale, int p_matchThreshold,
            int p_maxMatchCount) throws Exception
    {
        HashSet jobIds = new HashSet();
        jobIds.add(p_jobId);

        return leverage(jobIds, p_tmIds, p_targetLocale, p_matchThreshold,
                p_maxMatchCount);
    }

    /**
     * Leverages a translatable segment from in-progress TM.
     * 
     * @param p_jobIds
     *            job ids to leverage from
     * @param p_tmIds
     *            TM ids to leverage from. This parameter can be null.
     * @param p_targetLocale
     *            target locale
     * @param p_matchThreshold
     *            match threshold
     * @param p_maxMatchCount
     *            max match count
     * @return Collection of LeveragedInProgressTu objects
     */
    public Collection leverage(Set<Long> p_jobIds, Set p_tmIds,
            GlobalSightLocale p_targetLocale, int p_matchThreshold,
            int p_maxMatchCount) throws Exception
    {
        // GSDEF00012790: segments consisting only of stop-words may
        // have 0 tokens and SQL queries with IN statements fail.
        if (m_totalOriginalTokenCount == 0)
        {
            return new ArrayList();
        }

        int maxTotalTokenCount = getMaxTotalTokenCount(
                m_totalOriginalTokenCount, p_matchThreshold);
        int minTotalTokenCount = getMinTotalTokenCount(
                m_totalOriginalTokenCount, p_matchThreshold);

        Collection tokens = queryIndexes(p_jobIds, p_tmIds);
        Collection candidateTokens = makeCandidateTokens(tokens,
                maxTotalTokenCount, minTotalTokenCount);
        TokenMatchHolder tokenMatchHolder = findMatchedSource(candidateTokens,
                p_matchThreshold, minTotalTokenCount);
        return findMatchedTarget(tokenMatchHolder, p_targetLocale,
                p_maxMatchCount);
    }

    /**
     * Retrieves indexes and store them in m_candidates.
     * 
     * @param p_jobId
     *            job id to leverage from
     * @param p_tmIds
     *            TM ids to leverage from. This parameter can be null.
     * @return Collection of Token objects
     */
    private Collection queryIndexes(Set<Long> p_jobIds, Set p_tmIds)
            throws Exception
    {
        Collection tokens;

        IndexPersistence indexPersistence = new IndexPersistence(connection);
        tokens = indexPersistence.getIndexes(m_originalTokens.keySet(),
                m_sourceLocale, p_jobIds, p_tmIds);

        return tokens;
    }

    /**
     * Sorts Token objects into CandidateTokens objects.
     * 
     * @param p_tokens
     *            Collection of all Tokens retrieved from the index table for a
     *            segment
     * @param p_maxTotalTokenCount
     *            max total token count
     * @param p_minTotalTokenCount
     *            min total token count
     * @return Collection of CandidateTokens objects
     */
    private Collection makeCandidateTokens(Collection p_tokens,
            int p_maxTotalTokenCount, int p_minTotalTokenCount)
    {
        HashMap tuvIdTokenMap = new HashMap();

        Iterator itTokens = p_tokens.iterator();
        while (itTokens.hasNext())
        {
            Token token = (Token) itTokens.next();

            // see if the TM segment this token belongs to has
            // adequate number of totalTokenCount for making
            // this segment be a match candidate
            int totalTokenCount = token.getTotalTokenCount();
            if (totalTokenCount >= p_minTotalTokenCount
                    && totalTokenCount <= p_maxTotalTokenCount)
            {
                Long tuvIdAsLong = new Long(token.getTuvId());

                CandidateTokens tokensByTuvId = (CandidateTokens) tuvIdTokenMap
                        .get(tuvIdAsLong);
                if (tokensByTuvId == null)
                {
                    tokensByTuvId = new CandidateTokens(token.getTuvId());
                    tuvIdTokenMap.put(tuvIdAsLong, tokensByTuvId);
                }

                tokensByTuvId.addToken(token);
            }
        }

        return tuvIdTokenMap.values();
    }

    /**
     * Finds matched source segments.
     * 
     * @param p_candidateTokens
     *            Collection of CandidateTokens objects
     * @param p_matchThreshold
     *            match threshold
     * @param p_minTotalTokenCount
     *            min total token count
     * @return TokenMatchHolder object
     */
    private TokenMatchHolder findMatchedSource(Collection p_candidateTokens,
            int p_matchThreshold, int p_minTotalTokenCount)
    {
        TokenMatchHolder tokenMatchHolder = new TokenMatchHolder();

        Iterator it = p_candidateTokens.iterator();
        while (it.hasNext())
        {
            CandidateTokens candidateTokens = (CandidateTokens) it.next();

            // if the candidate token count is less than min total
            // token count, don't bother to calculate the match
            // score. On the other hand, if the candidate token count
            // is greater than max total token count, we still need to
            // calculate the score because the shared token count can
            // be less than the candidate token count.
            int candidateTokenCount = candidateTokens.getTokenCount();
            if (candidateTokenCount >= p_minTotalTokenCount)
            {
                int score = getMatchScore(candidateTokens);

                if (score >= p_matchThreshold)
                {
                    tokenMatchHolder.addMatch(
                            candidateTokens.getSrcSegmentId(), score);
                }
            }
        }

        return tokenMatchHolder;
    }

    /**
     * Retrieves leverage matches from in-progress TM.
     * 
     * @param p_tokenMatchHolder
     *            TokenMatchHolder objext
     * @param p_targetLocale
     *            target locale
     * @param p_maxMatchCount
     *            max match count
     * @return Collection of LeveragedInProgressTu objects
     */
    private Collection findMatchedTarget(TokenMatchHolder p_tokenMatchHolder,
            GlobalSightLocale p_targetLocale, int p_maxMatchCount)
            throws Exception
    {
        Collection matches = new ArrayList();

        TmPersistence tmPersistence = new TmPersistence(connection);

        // retrieve 100% matches
        HashSet srcIds = new HashSet();
        Iterator itExacts = p_tokenMatchHolder.exactMatchIterator();
        while (itExacts.hasNext())
        {
            TokenMatch tokenMatch = (TokenMatch) itExacts.next();
            srcIds.add(new Long(tokenMatch.getMatchedTuId()));
        }

        if (srcIds.size() > 0)
        {
            matches = tmPersistence.getTranslatableSegment(m_sourceLocale,
                    p_targetLocale, srcIds);
        }

        itExacts = matches.iterator();
        while (itExacts.hasNext())
        {
            LeveragedInProgressTu tu = (LeveragedInProgressTu) itExacts.next();
            tu.setScore(100);
            tu.setMatchState(MatchState.IN_PROGRESS_TM_EXACT_MATCH);
        }

        // retrieve fuzzies
        int matchCount = 0;
        Iterator itFuzzies = p_tokenMatchHolder.fuzzyMatchIterator();
        while (itFuzzies.hasNext() && matchCount < p_maxMatchCount)
        {
            TokenMatch tokenMatch = (TokenMatch) itFuzzies.next();
            LeveragedInProgressTu tu = tmPersistence
                    .getTranslatableSegment(m_sourceLocale, p_targetLocale,
                            tokenMatch.getMatchedTuId());

            if (tu != null)
            {
                matchCount++;

                tu.setScore(tokenMatch.getScore());
                tu.setMatchState(MatchState.FUZZY_MATCH);
                matches.add(tu);
            }
        }

        return matches;
    }

    private void populateOriginalTokenMap(BaseTmTuv p_sourceTuv)
            throws Exception
    {
        // Word tokenizer is used in in-progress TM regardless of the
        // user setting
        Tokenizer tokenizer = new WordTokenizer();

        // tokenize segment. 2, 3, 4 and 6 th parameters to
        // tokenize() method are dummy. They don't matter for
        // getting token strings.
        List tokens = tokenizer.tokenize(p_sourceTuv.getFuzzyIndexFormat(), 0,
                0, 0, p_sourceTuv.getLocale(), true);

        m_originalTokens = new HashMap(tokens.size());

        if (tokens.size() > 0)
        {
            m_totalOriginalTokenCount = ((Token) tokens.get(0))
                    .getTotalTokenCount();

            Iterator it = tokens.iterator();
            while (it.hasNext())
            {
                Token token = (Token) it.next();
                m_originalTokens.put(token.getTokenString(), token);
            }
        }
        else
        {
            m_totalOriginalTokenCount = 0;
        }
    }

    /**
     * Gets match score by applying Dice's coefficient.
     */
    private int getMatchScore(CandidateTokens p_candidateTokens)
    {
        int candidateTokenCount = p_candidateTokens.getTotalTokenCount();
        int sharedTokenCount = 0;

        Iterator it = p_candidateTokens.getTokenList().iterator();
        while (it.hasNext())
        {
            Token candidateToken = (Token) it.next();
            Token orgToken = (Token) m_originalTokens.get(candidateToken
                    .getTokenString());
            if (candidateToken != null && orgToken != null)
            {
                sharedTokenCount += Math.min(candidateToken.getRepetition(),
                        orgToken.getRepetition());
            }
        }

        int result = (int) Math.round(((double) (2 * sharedTokenCount)
                / (m_totalOriginalTokenCount + candidateTokenCount) * 100));

        return result;
    }

    /**
     * Gets the highest possible candidate total token count assuming the
     * candidate has all tokens the original segment has. The formula is as
     * follows.
     * 
     * threshold = (2 * orgTotalCount) / (orgTotalCount + candidateTotalCount) *
     * 100
     * 
     * The integer division effectively performs as Math.floor.
     */
    private int getMaxTotalTokenCount(int p_orgTotalTokenCount,
            int p_matchThreshold)
    {
        return (200 - p_matchThreshold) * p_orgTotalTokenCount
                / p_matchThreshold;
    }

    /**
     * Get the lowest possible candidate total token count assuming the
     * candidate has all tokens the original segment has. The formula is as
     * follows.
     * 
     * threshold = (2 * candidateTotalCount) / (orgTotalCount +
     * candidateTotalCount) * 100
     * 
     * Math.ceil is called to get an integer that is not less than the minimal
     * token count.
     */
    private int getMinTotalTokenCount(int p_orgTotalTokenCount,
            int p_matchThreshold)
    {
        return (int) Math.ceil(p_matchThreshold * p_orgTotalTokenCount
                / (double) (200 - p_matchThreshold));
    }

    /**
     * This inner class holds a list of tokens of a candidate match. This class
     * also tracks of the total number of tokens (including repetition count)
     * found for this candidate. The number can be used to filter the candidate
     * out for the match score calculation.
     */
    private class CandidateTokens
    {
        private long m_srcSegmentId;
        private ArrayList m_tokenList = new ArrayList();
        private int m_tokenCount = 0;

        private CandidateTokens(long p_srcSegmentId)
        {
            m_srcSegmentId = p_srcSegmentId;
        }

        private long getSrcSegmentId()
        {
            return m_srcSegmentId;
        }

        private void addToken(Token p_token)
        {
            m_tokenList.add(p_token);
            m_tokenCount += p_token.getRepetition();
        }

        private int getTokenCount()
        {
            return m_tokenCount;
        }

        private List getTokenList()
        {
            return m_tokenList;
        }

        private int getTotalTokenCount()
        {
            return ((Token) m_tokenList.get(0)).getTotalTokenCount();
        }
    }

    /**
     * A holder of leverage matches found in the in-progress TM.
     * TokenMatchHolder object holds 100% and fuzzy matches separately.
     */
    private class TokenMatchHolder
    {
        private ArrayList m_exacts = new ArrayList();
        private ArrayList m_fuzzies = new ArrayList();

        private void addMatch(long p_matchTuId, int p_score)
        {
            TokenMatch tokenMatch = new TokenMatch(0, SegmentTmTu.ROOT,
                    p_matchTuId, p_score);

            if (p_score == 100)
            {
                m_exacts.add(tokenMatch);
            }
            else
            {
                m_fuzzies.add(tokenMatch);
            }
        }

        private Iterator exactMatchIterator()
        {
            return m_exacts.iterator();
        }

        private Iterator fuzzyMatchIterator()
        {
            SortUtil.sort(m_fuzzies);
            return m_fuzzies.iterator();
        }
    }
}
