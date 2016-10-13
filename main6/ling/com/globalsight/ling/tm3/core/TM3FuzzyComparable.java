package com.globalsight.ling.tm3.core;

/**
 * Interface to compare individual words, tokens, or objects from
 * a {@link TM3Scorable} instance.  TM3Scorable implementations are expected
 * to provide a list of TM3FuzzyComparable objects representing the data 
 * units that should be evaluated by the scorer.
 * <p>
 * Although it is recommended that a fuzzyCompare() score of 1.0 be 
 * consistent with equals(), it is not required by TM3.
 */
public interface TM3FuzzyComparable<T> {
    /**
     * Compare this object to another and return a similarity score between
     * 0 (completely different) and 1 (identical).  
     * @param target object to compare
     * @return similarity from 0 to 1
     */
    public float fuzzyCompare(T target);
}
