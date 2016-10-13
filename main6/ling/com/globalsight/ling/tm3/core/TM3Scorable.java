package com.globalsight.ling.tm3.core;

import java.util.List;

/**
 * TM3Data implementations that wish to take advantage of built-in 
 * scoring code such as {@link EditDistanceScorer} should implement this
 * interface.
 * <p>
 * Alternately, custom scorers may simply have direct knowledge of TM3Data
 * implementation internals, in which case this interface is not required.
 */
public interface TM3Scorable<V extends TM3FuzzyComparable<V>> extends TM3Data {
    
    /**
     * Return objects for scoring.  These should generally correspond to 
     * words or individual markup pieces such as placeholders.
     * @return
     */
    public List<V> getScoringObjects();
}
