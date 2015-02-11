package com.globalsight.ling.tm3.core;

import java.util.Map;

/**
 * Subinterface for TMs created with {@link TM3Manager#createBilingualTM(long, TM3Locale, TM3Locale)}.
 * This exposes access to the fixed source and target locales, as well as 
 * provides simplified methods for some common operations.
 */
public interface TM3BilingualTm<T extends TM3Data> extends TM3Tm<T> {

    /**
     * Returns the TM's source locale.
     * @return source locale
     */
    public TM3Locale getSrcLocale();
    
    /**
     * Returns the TM's target locale.
     * @return target locale
     */
    public TM3Locale getTgtLocale();
    
    /**
     * Save a simple TUV pair to the segment.  This is simplified by the
     * fixed locale information in a bilingual TM.
     * 
     * @param source Source segment data
     * @param attributes segment attributes, if any
     * @param target Target segment data
     * @param mode
     * @param event
     * @return the TM3Tu to which the TUV data was saved
     * @throws TM3Exception
     */
    public TM3Tu<T> save(T source, Map<TM3Attribute, Object> attributes,
                         T target, TM3SaveMode mode, TM3Event event)
                         throws TM3Exception;

    /**
     * Leverage a single segment.  This is simplified form enabled by the fixed
     * locale information in a bilingual TM.
     * 
     * @param matchKey
     * @param attributes
     * @param matchType
     * @param maxResults
     * @return
     * @throws TM3Exception
     */
    public TM3LeverageResults<T> findMatches(T matchKey, 
            Map<TM3Attribute, Object> attributes, TM3MatchType matchType, 
            boolean lookupTarget, int maxResults, int threshold)
            throws TM3Exception;
    
    /**
     * Leverage a single segment.  This is simplified form enabled by the fixed
     * locale information in a bilingual TM.
     * 
     * @param matchKey
     * @param attributes
     * @param matchType
     * @param maxResults
     * @return
     * @throws TM3Exception
     */
    public TM3LeverageResults<T> findMatches(T matchKey, 
            Map<TM3Attribute, Object> attributes, TM3MatchType matchType, boolean lookupTarget);
}
