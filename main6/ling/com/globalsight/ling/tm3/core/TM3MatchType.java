package com.globalsight.ling.tm3.core;

/**
 * Flag passed to {@link TM3Tm#findMatches} to specify what kind of leveraging
 * should be performed.
 */
public enum TM3MatchType {
    /**
     * Return only exact matches.
     */
    EXACT,
    /**
     * Return both exact and fuzzy matches.
     */
    ALL,
    /**
     * Return exact matches; if none are found, also return fuzzy matches.
     */
    FALLBACK;
}
