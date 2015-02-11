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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.tm2.indexer.Token;
import com.globalsight.ling.tm2.lucene.LuceneIndexReader;
import com.globalsight.util.CacheDataRetriever;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SoftReferenceCache;


/**
 * FuzzyMatcher is responsible for calculating fuzzy match score and
 * find matches for segments.
 */

class FuzzyMatcher
{
    private static final Logger c_logger =
        Logger.getLogger(
            FuzzyMatcher.class);

    // minimum match point to leverage. This is purely for statistics
    // purpose and irrelevant to the leverage threshold user option.
    public static final int MIN_MATCH_POINT = 50;
    
    private GlobalSightLocale m_sourceLocale;
    private LuceneIndexReader m_indexReader;
    private SoftReferenceCache m_tokenListCache;
    
    // constructor
    public FuzzyMatcher(GlobalSightLocale p_sourceLocale, Collection<Tm> p_tms,
            boolean lookupTarget) throws Exception
    {
        m_sourceLocale = p_sourceLocale;
        List<Long> tmIds = new ArrayList<Long>();
        for (Tm tm : p_tms)
        {
            tmIds.add(tm.getId());
        }
        m_indexReader = new LuceneIndexReader(tmIds, p_sourceLocale);

        m_tokenListCache = new SoftReferenceCache(new TokenListGetter(
                lookupTarget));
    }
    

    /**
     * find matched segments.
     *
     * @param p_segmentForFuzzyMatching a matching segment
     * @param p_matchThreshold match threshold
     * @return Collection of TokenMatch objects
     */
    public Collection findMatches(
        SegmentForFuzzyMatching p_segmentForFuzzyMatching,
        int p_matchThreshold, int p_maxMatchCount)
        throws Exception
    {
//         c_logger.debug("Original tuv id = "
//             + p_segmentForFuzzyMatching.getTuvId());
        
        TokenMatchHolder tokenMatchHolder
            = new TokenMatchHolder(p_maxMatchCount, p_matchThreshold);

        // match threshold for leverage. 
        int levThreshold = Math.min(p_matchThreshold, MIN_MATCH_POINT);
        
        int orgTotalTokenCount
            = p_segmentForFuzzyMatching.getTotalTokenCount();
        
        int maxTotalTokenCount
            = getMaxTotalTokenCount(orgTotalTokenCount, levThreshold);
        int minTotalTokenCount
            = getMinTotalTokenCount(orgTotalTokenCount, levThreshold);
        if (c_logger.isDebugEnabled())
        {
            c_logger.debug("Original segment token count = "
                    + orgTotalTokenCount + ", max token count = "
                    + maxTotalTokenCount + ", min token count = "
                    + minTotalTokenCount);            
        }
        
        Collection candidateTokens = getCandidateTokens(
            p_segmentForFuzzyMatching.getAllTokenStrings(),
            maxTotalTokenCount, minTotalTokenCount);

        Iterator itCandidateTokens = candidateTokens.iterator();
        while(itCandidateTokens.hasNext())
        {
            CandidateTokens tokensByTuvId
                = (CandidateTokens)itCandidateTokens.next();

            // if the candidate token count is less than min total
            // token count, don't bother to calculate the match
            // score. On the other hand, if the candidate token count
            // is greater than max total token count, we still need to
            // calculate the score because the shared token count can
            // be less than the candidate token count.
            int candidateTokenCount = tokensByTuvId.getTokenCount();
            if(candidateTokenCount >= minTotalTokenCount)
            {
                List tokenList = tokensByTuvId.getTokenList();
                
                float score
                    = getMatchScore(tokenList, p_segmentForFuzzyMatching);
            
//                 c_logger.debug("Score = " + score);

                if(score >= levThreshold)
                {
                    tokenMatchHolder.possiblyAddMatch(
                        p_segmentForFuzzyMatching.getTuvId(),
                        p_segmentForFuzzyMatching.getSubId(),
                        ((Token)tokenList.get(0)).getTuId(), score);
                }
            }
        }
        
        return tokenMatchHolder.getMatches();
    }
    

    /**
     * Close LuceneIndexReader
     */
    public void close()
        throws Exception
    {
        m_indexReader.close();
    }
    
            
    private Collection getCandidateTokens(Set p_tokenStrings,
        int p_maxTotalTokenCount, int p_minTotalTokenCount)
        throws Exception
    {
        // set initial capacity 512 for performance improvement
        // Key: Tuv id of matched source
        // Value: CandidateTokens object
        HashMap tuvIdTokenMap = new HashMap(512);
        
        Iterator itTokenString = p_tokenStrings.iterator();
        
        while(itTokenString.hasNext())
        {
            Object obj = itTokenString.next();
            List tokenList = (List)m_tokenListCache.get(obj);

            if(tokenList != null)
            {
                Iterator itTokens = tokenList.iterator();
                while(itTokens.hasNext())
                {
                    Token token = (Token)itTokens.next();

                    // see if the TM segment this token belongs to has
                    // adequate number of totalTokenCount for making
                    // this segment be a match candidate
                    int totalTokenCount = token.getTotalTokenCount();
                    if(totalTokenCount >= p_minTotalTokenCount
                        && totalTokenCount <= p_maxTotalTokenCount)
                    {
                        Long tuvIdAsLong = new Long(token.getTuvId());
                    
                        CandidateTokens tokensByTuvId
                            = (CandidateTokens)tuvIdTokenMap.get(tuvIdAsLong);
                        if(tokensByTuvId == null)
                        {
                            tokensByTuvId = new CandidateTokens();
                            tuvIdTokenMap.put(tuvIdAsLong, tokensByTuvId);
                        }
                    
                        tokensByTuvId.addToken(token);
                    }
                }
            }
        }

        return tuvIdTokenMap.values();
    }
    

    // get match score by applying Dice's coefficient
    private float getMatchScore(List p_tokensByTuvId,
        SegmentForFuzzyMatching p_segmentForFuzzyMatching)
    {
        int orgSegTokenCount = p_segmentForFuzzyMatching.getTotalTokenCount();
        int candidateTokenCount
            = ((Token)p_tokensByTuvId.get(0)).getTotalTokenCount();

//         c_logger.debug("Org token count = " + orgSegTokenCount);
//         c_logger.debug("Candidate token count = " + candidateTokenCount);

        int sharedTokenCount = 0;
        
        Iterator it = p_tokensByTuvId.iterator();
        while(it.hasNext())
        {
            Token candidateToken = (Token)it.next();
            Token orgToken = p_segmentForFuzzyMatching
                .getTokenByTokenString(candidateToken.getTokenString());
            
            int minR = Math.min(
                    candidateToken.getRepetition(), orgToken.getRepetition());

            sharedTokenCount += minR;

//             c_logger.debug("Token string = "
//                 + candidateToken.getTokenString());
//             c_logger.debug("Shared token count = " + sharedTokenCount);
        }
        
        float result = ((float)(2 * sharedTokenCount) /
            (orgSegTokenCount + candidateTokenCount) * 100);
        
        return result;
    }
    
    
    // get the highest possible candidate total token count assuming
    // the candidate has all tokens the original segment has. The
    // formula is as follows.
    //
    // threshold
    //    = (2 * orgTotalCount) / (orgTotalCount + candidateTotalCount) * 100
    //
    // The integer division effectively performs as Math.floor.
    private int getMaxTotalTokenCount(
        int p_orgTotalTokenCount, int p_matchThreshold)
    {
        return (200 - p_matchThreshold)
            * p_orgTotalTokenCount / p_matchThreshold;
    }
    

    // get the lowest possible candidate total token count assuming
    // the candidate has all tokens the original segment has. The
    // formula is as follows.
    //
    // threshold
    //    = (2 * candidateTotalCount) / (orgTotalCount + candidateTotalCount)
    //      * 100
    //
    // Math.ceil is called to get an integer that is not less than the
    // minimal token count
    private int getMinTotalTokenCount(
        int p_orgTotalTokenCount, int p_matchThreshold)
    {
        return (int)Math.ceil(p_matchThreshold
            * p_orgTotalTokenCount / (double)(200 - p_matchThreshold));
    }
    

//     private boolean qualifiesForScoreCompute(int p_orgTokenCount,
//         int p_candidateTokenCount, int p_matchThreshold)
//     {
//         int maxScore = (int)Math.round(
//             ((double)(2 * Math.min(p_orgTokenCount, p_candidateTokenCount))
//                 / (p_orgTokenCount + p_candidateTokenCount) * 100));
        
//         return maxScore >= p_matchThreshold;
//     }
            


    /**
     * This inner class holds a list of tokens of a candidate
     * match. This class also tracks of the total number of tokens
     * (including repetition count) found for this candidate. The
     * number can be used to filter the candidate out for the match
     * score calculation.
     */
    private class CandidateTokens
    {
        private ArrayList m_tokenList = new ArrayList();
        private int m_tokenCount = 0;

        
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
    }
    
        
        
    /**
     * A holder of leverage matches found in the Segment
     * TM. TokenMatchHolder object holds all matches whose score is
     * 100% and the limited number of other fuzzy matches (less than
     * 100%). The number of fuzzy matches are capped by
     * m_maxMatchCount. When possiblyAddMatch() method is called, a
     * leverage match with a lowest score held in this object is
     * replaced with the new leverage match if the match has higher
     * score.
     *
     * Also TokenMatchHolder holds only one leverage match whose score
     * is lower than m_matchThreshold if there is no higher
     * match. This is done for statistics purpose.
     */

    private class TokenMatchHolder
    {
        private int m_matchCount;
        private TokenMatch[] m_matches;
        private int m_maxMatchCount;
        private int m_matchThreshold;
        private List m_100percentFuzzies;
        private TokenMatch m_matchForStatistics;
        
        private TokenMatchHolder(int p_maxMatchCount, int p_matchThreshold)
        {
            m_matchCount = 0;
            m_maxMatchCount = p_maxMatchCount;
            m_matches = new TokenMatch[p_maxMatchCount];
            m_matchThreshold = p_matchThreshold;
            m_100percentFuzzies = new ArrayList();
            m_matchForStatistics = null;
        }
        

        private void possiblyAddMatch(long p_orgTuvId,
            String p_orgSubId, long p_matchTuId, float p_score)
        {
            if(p_score == 100)
            {
                TokenMatch tokenMatch = new TokenMatch(
                    p_orgTuvId, p_orgSubId, p_matchTuId, p_score);

                m_100percentFuzzies.add(tokenMatch);
            }
            else if(p_score < m_matchThreshold)
            {
                if(m_matchForStatistics == null
                    || m_matchForStatistics.getScore() < p_score)
                {
                    m_matchForStatistics = new TokenMatch(
                        p_orgTuvId, p_orgSubId, p_matchTuId, p_score);
                }
            }
            else if(m_matchCount < m_maxMatchCount)
            {
                TokenMatch tokenMatch = new TokenMatch(
                    p_orgTuvId, p_orgSubId, p_matchTuId, p_score);

                m_matches[m_matchCount] = tokenMatch;
                m_matchCount++;

                // When all elements in m_matches array are filled,
                // the elements are sorted by their score in ascending
                // order.
                if(m_matchCount == m_maxMatchCount)
                {
                    Arrays.sort(m_matches);
                }
            }
            else
            {
                TokenMatch lowestScoreMatch = m_matches[0];
                
                if(lowestScoreMatch.getScore() < p_score)
                {
                    TokenMatch tokenMatch = new TokenMatch(
                        p_orgTuvId, p_orgSubId, p_matchTuId, p_score);
                    
                    m_matches[0] = tokenMatch;

                    // When an element is replaced, the array is
                    // re-sorted.
                    Arrays.sort(m_matches);
                }
            }
        }
        

        private List getMatches()
        {
            List list = new ArrayList();
            list.addAll(m_100percentFuzzies);
            
            for(int i = 0; i < m_matchCount; i++)
            {
                list.add(m_matches[i]);
            }

            if(list.size() == 0 && m_matchForStatistics != null)
            {
                list.add(m_matchForStatistics);
            }

            return list;
        }
    }
    
                    
    private class TokenListGetter implements CacheDataRetriever
    {
        private boolean lookupTarget;

        private TokenListGetter(boolean lookupTarget)
        {
            this.lookupTarget = lookupTarget;
        }

        public Object getData(Object p_key) throws Exception
        {
            return m_indexReader
                    .getGsTokensByTerm((String) p_key, lookupTarget);
        }

    }
    
    
}
