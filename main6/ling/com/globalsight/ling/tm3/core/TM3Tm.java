package com.globalsight.ling.tm3.core;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Top-level TM interface.
 */
public interface TM3Tm<T extends TM3Data> {

    /**
     * Get this TM's ID.
     * @return id
     */
    public Long getId();

    /**
     * Return this TM's type.
     * @return type
     */
    public TM3TmType getType();
    
    /**
     * Add a new event to this TM's event record.  This may be subsequently
     * used to tag save() and updateTuv() events.  The timestamp for this
     * event will be set to the current time.
     */
    public TM3Event addEvent(int type, String username, String arg);

    /**
     * Add a new event to this TM's event record, specifying a timestamp.  
     * This may be subsequently used to tag save() and updateTuv() events. 
     */
    public TM3Event addEvent(int type, String username, String arg, Date date);
    
    /**
     * Load an existing event by ID.
     * @param id ID of a previously saved event.
     * @return TM3Event
     * @throws TM3Exception
     */
    public TM3Event getEvent(long id) throws TM3Exception;
    
    /**
     * Lookup an existing TU by ID.
     * @param id ID of a TU in this TM
     * @return TM3Tu
     * @throws TM3Exception 
     */
    public TM3Tu<T> getTu(long id) throws TM3Exception;

    /**
     * Load existing TUs by IDs.
     * @param ids
     * @return TM3Tu list
     * @throws TM3Exception
     */
    public List<TM3Tu<T>> getTu(List<Long> ids) throws TM3Exception;

    /**
     * Lookup an existing TUV by ID.
     * @param id ID of a TUV in this TM
     * @return TM3Tuv
     * @throws TM3Exception
     */
    public TM3Tuv<T> getTuv(long id) throws TM3Exception;

    /**
     * Leverage a single segment, returning an unlimited number of exact and
     * fuzzy matches as specified by {@link MatchType}.  
     * 
     * @param matchKey segment data to match
     * @param sourceLocale source locale
     * @param targetLocales target locale
     * @param attributes source attributes to match, or null
     * @param matchType what type of leveraging to perform
     * @return {@link TM3LeverageResults}
     * @throws TM3Exception
     */
    public TM3LeverageResults<T> findMatches(T matchKey,
            TM3Locale sourceLocale, Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> attributes, TM3MatchType matchType,
            boolean lookupTarget) throws TM3Exception;

    /**
     * Leverage a single segment, returning some a finite number of exact and
     * fuzzy matches as specified by {@link MatchType}.  Exact matches are 
     * preferentially returned, followed by fuzzy matches in descending order
     * by score.
     * 
     * @param matchKey segment data to match
     * @param sourceLocale source locale
     * @param targetLocale target locale
     * @param attributes source attributes to match, or null
     * @param matchType what type of leveraging to perform
     * @param maxResults maximum number of results to return
     * @param threshold minimum score that must be assigned to a segment for 
     *        it to be returned as a result
     * @return {@link TM3LeverageResults}
     * @throws TM3Exception
     */
    public TM3LeverageResults<T> findMatches(T matchKey, 
            TM3Locale sourceLocale, Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> attributes, TM3MatchType matchType, 
            boolean lookupTarget, int maxResults, int threshold)
            throws TM3Exception;

    /**
     * Leverage a single segment across multiple TMs.Note that these TMs must be
     * from the same company.
     */
    public TM3LeverageResults<T> findMatches(T matchKey,
            TM3Locale sourceLocale, Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> attributes, TM3MatchType matchType,
            boolean lookupTarget, int maxResults, int threshold,
            List<Long> tm3TmIds) throws TM3Exception;

    /**
     * Save a simple TUV pair to the segment.  Is this signature really needed? 
     * 
     * @param sourceLocale locale of source segment
     * @param source Source segment data
     * @param attributes segment attributes, if any
     * @param targetLocale locale of target segment
     * @param target Target segment data
     * @param mode
     * @param event
     * @return the TM3Tu to which the TUV data was saved
     * @throws TM3Exception
     */
//    public TM3Tu<T> save(TM3Locale srcLocale, T source,
//            Map<TM3Attribute, Object> attributes,
//            TM3Locale tgtLocale, T target, 
//            TM3SaveMode mode, TM3Event event) throws TM3Exception;

    /**
     * Save a segment with an arbitrary number of target translations.
     * 
     * Note the mode parameter.  The behavior of this function varies depending
     * on its value:
     * <ul>
     * <li><tt>OVERWRITE_ALL</tt>: If a TU exists with this source information 
     * and attributes, it will be completely replaced.</li>
     * <li><tt>OVERWRITE_TARGET</tt>: If a TU exists with this source information
     * and attributes, the data in <tt>targets</tt> will replace any target
     * data in those locales.</li>
     * <li><tt>MERGE</tt>: If a TU exists with this source information and 
     * attributes, the data in <tt>targets</tt> will be added to existing 
     * target data.</li>
     * <li><tt>DISCARD</tt>: If a TU exists with this source information and 
     * attributes, the data in <tt>targets</tt> will be ignored and the 
     * TU will be left untouched.</li>
     * </ul>
     * 
     * Note that segment attributes must exactly match an existing entry in
     * order for their to be a conflict that requires examination of the save
     * mode.  This is different from how attributes are handled when finding
     * matches, where they may match a subset of the attributes of a given 
     * segment in the TM.
     * 
     * @param srcLocale source locale
     * @param source source segment data
     * @param attributes segment attributes, if any
     * @param targets map of target locales to corresponding target 
     *        translations
     * @param mode
     * @param event
     * @return
     * @throws TM3Exception
     */
//    public TM3Tu<T> save(TM3Locale srcLocale, T source, 
//                      Map<TM3Attribute, Object> attributes, 
//                      Map<TM3Locale, T> targets, 
//                      TM3SaveMode mode, TM3Event event) throws TM3Exception;

    /**
     * Create a {@link TM3Saver} instance that can be used to perform 
     * complex save operations.  See the {@link TM3Saver} docs for details.
     * @return saver instance
     */
    public TM3Saver<T> createSaver();
    
    /**
     * Update an existing TU.  This will update the persistent 
     * copy of a TU in the TM.  Any modifications made to the TU in-memory
     * will be persisted, including:
     * <ul>
     * <li>Changes to attribute values
     * <li>Additions of new attributes or the removal of old ones
     * <li>Changes to one or more TUs (or the addition or removal of TUs)
     * </ul>
     * The specified event, if present, will also be added to the TU's event
     * history.  
     * <p>
     * If the TU has not been modified, this call will have no effect (and the
     * event will not be added to the TU's event history).
     * <p>
     * Note: there is special behavior if the source TUV content has been
     * modified.  In this case, there is the chance that the TU now needs
     * to be merged with another (pre-existing) TU.  In this case, the 
     * current TU will be deleted and then recreated through the normal
     * save mechanism, so any necessary merging will be transparent.
     *
     * @param tu TU to be updated
     * @param event event to add to the TU's event history, or null
     * @param indexTarget
     * @return updated TM3Tu instance.  
     * @throws TM3Exception
     */
    public TM3Tu<T> modifyTu(TM3Tu<T> tu, TM3Event event, boolean indexTarget)
            throws TM3Exception;
    
    /**
     * Get a TU attribute defined on this TM, by name.
     * @param name attribute name
     * @return TM3Attribute, or null if no attribute is defined with that name
     */
    public TM3Attribute getAttributeByName(String name);
    
    public boolean doesAttributeExist(String name);
    
    /**
     * Get a list of all TU attributes that have been defined for this TM.
     * @return set of TM attributes
     */
    public Set<TM3Attribute> getAttributes();

    /**
     * Define a new TU attribute for this TM.  This method is idempotent --
     * if an attribute already exists with the specified name, it is returned. 
     * @param name attribute name
     * @return TU attribute with this name 
     * @throws TM3Exception
     */
    public TM3Attribute addAttribute(String name);

    /**
     * Remove a TU attribute 
     * @param name
     * @throws TM3Exception
     */
    public void removeAttribute(TM3Attribute name);

    /**
     * Access events that affect this TM.
     * @return
     */
    public TM3EventLog getEventLog();

    /**
     * Access the data factory used to deserialize TUV content in this TM.
     * @return
     */
    public TM3DataFactory<T> getDataFactory();
    
    /**
     * Return the locales for which this TM contains at least one TUV.
     * 
     * @return set of locales
     */
    public Set<TM3Locale> getTuvLocales() throws TM3Exception;
    
    /**
     * Return all the possible values for an attribute in this TM.
     * 
     * @return set of attribute values
     */
    public List<Object> getAllAttributeValues(TM3Attribute attr) throws TM3Exception;
    
    /**
     * Get a handle to all the TU data in this TM, optionally 
     * qualified by a date range.  If specified, the date range
     * will be compared against the modification time for the 
     * individual TUs.
     * @param start (optional) start point of data range
     * @param end (optional) end point of data range
     * @return handle to the requested TU data
     */
    public TM3Handle<T> getAllData(Date start, Date end);

	public TM3Handle<T> getAllDataByParamMap(Map<String, Object> paramMap);

    /**
     * Get a handle to all the TU data in this TM that includes 
     * a TUV with the specified locale, optionally
     * qualified by a date range.  If specified, the date range
     * will be compared against the modification time for the 
     * individual TUs.
     *
     * Note this TM3Handle does not support the purge method.
     * @param locale TUV locale with which to select TUs
     * @param start (optional) start point of data range
     * @param end (optional) end point of data range
     * @return handle to the requested TU data
     */
    public TM3Handle<T> getDataByLocales(List<TM3Locale> localeList, Date start, Date end);

    /**
     * Get a handle to all segments in this TM whose IDs are included
     * in the specified set of ids.
     * 
     * @param tuIds IDs of TUs to select
     * @return handle to the requested TU data
     */
    public TM3Handle<T> getDataById(List<Long> tuIds);
    
    /**
     * Get a handle to all segments in this TM that satisfy a given set of
     * attribute criteria.
     * @param attrs map of attribute/value pairs
     * @param start (optional) start point of the data range, or null
     * @param end (optional) start point of the data range, or null
     * @return handle to the requested TU data
     */
    public TM3Handle<T> getDataByAttributes(Map<TM3Attribute, Object> attrs,
                                            Date start, Date end);

    /**
     * Remove TUVs by locale.
     *
     * @param locale Remove all TUVs with this locale.
     */
    public void removeDataByLocale(TM3Locale locale);

    public void setConnection(Connection connection);
    
    public Connection getConnection();

    /**
     * Recreate fuzzy index data for specified tm3 tuvs.
     * @param tuvs -- List<TM3Tuv<T>>
     */
    public void recreateFuzzyIndex(List<TM3Tuv<T>> tuvs);
}
