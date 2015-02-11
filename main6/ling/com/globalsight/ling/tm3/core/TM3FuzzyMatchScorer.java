package com.globalsight.ling.tm3.core;

/**
 * Interface for pluggable fuzzy match scoring.
 */
public interface TM3FuzzyMatchScorer<T extends TM3Data> {
    /**
     * Score a fuzzy match candidate.  Scores should be between 0 (no similarity)
     * and 1.0 (exact match).
     * 
     * @param matchKey the match key that was used to find this candidate
     * @param candidate candidate match
     * @param locale locale of both the match key and the match candidate
     * @return score between 0-1.0
     */
    public float score(T matchKey, T candidate, TM3Locale locale);
}
