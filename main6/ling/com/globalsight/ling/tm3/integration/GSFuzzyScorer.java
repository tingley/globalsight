package com.globalsight.ling.tm3.integration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.ling.tm2.lucene.LuceneUtil;
import com.globalsight.ling.tm2.indexer.Token;
import com.globalsight.ling.tm3.core.TM3FuzzyMatchScorer;
import com.globalsight.ling.tm3.core.TM3Locale;

/**
 * Legacy segment tm fuzzy scoring algorithm from tm2/leverage/FuzzyMatcher.java. 
 */
public class GSFuzzyScorer implements TM3FuzzyMatchScorer<GSTuvData> {
    private static final Logger LOGGER =
        Logger.getLogger(
                GSFuzzyScorer.class);
    
    @Override
    public float score(GSTuvData matchKey, GSTuvData candidate, TM3Locale locale) {
        long start = System.currentTimeMillis();
        // This can ignore the locale because it should already be embedded 
        // in the keys
        List<Token> matchTokens = 
                    LuceneUtil.buildTokenList(matchKey.getTokens());
        List<Token> candidateTokens = 
                    LuceneUtil.buildTokenList(candidate.getTokens());

        // XXX: further filter by min/max tokens?
        
        float f = getScore(matchTokens, candidateTokens);
        LOGGER.debug("Score(" + matchKey + ", " + candidate + ") = " + 
            (int)(f * 100) +
            " in " + (System.currentTimeMillis() - start) + "ms");
        return f;
    }
    
    protected float getScore(List<Token> keyTokens, List<Token> candidateTokens) {
        int orgSegTokenCount = keyTokens.size();
        int candidateTokenCount = candidateTokens.size();

        // XXX This could be cached
        Map<String, Integer> keyTokenMap = getTokenMap(keyTokens);

        int count = 0;
        
        for (Token t : candidateTokens) {
            Integer keyReps = keyTokenMap.get(t.getTokenString());
            if (keyReps != null) { // Token in common
                count += Math.min(t.getRepetition(), keyReps);
            }
        }
        
        // Apply Dice's coefficient
        float numer = 2f * count;
        float denom = orgSegTokenCount + candidateTokenCount;
        return numer / denom;
    }
    
    private Map<String, Integer> getTokenMap(List<Token> tokens) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (Token t : tokens) {
            map.put(t.getTokenString(), t.getRepetition());
        }
        return map;
    }
}
