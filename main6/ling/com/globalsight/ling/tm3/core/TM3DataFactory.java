package com.globalsight.ling.tm3.core;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;

/**
 * Integration point that manages the creation of TM3TuvData objects,
 * either from scratch or from the database.
 * <p>
 * Client code must implement this interface so that TM3 can marshall
 * TUV data in and out of the database. 
 */
public interface TM3DataFactory<T extends TM3Data> {

    /**
     * Extend the Hibernate configuration with any additional mappings
     * required to make this factory operate.
     * <p>
     * This routine should be called exactly once during a given application's
     * lifetime, when Hibernate is initialized.
     * 
     * @param cfg Hibernate Configuration object
     * @return the same Configuration instance, optionally modified with 
     *      additional calls
     */
    public Configuration extendConfiguration(Configuration cfg);
    
    /**
     * Create a new {@link TM3Data} instance that corresponds to the
     * provided serialized data.
     * @param value Serialized TUV data produced by a previous call to
     * {@link TM3Data#getSerializedForm()}.
     * @return TM3TuvData instance
     */
    public T fromSerializedForm(TM3Locale locale, String value);
    

    /**
     * Return the fuzzy scorer to be used to compare these objects.
     * @return
     */
    public TM3FuzzyMatchScorer<T> getFuzzyMatchScorer();
    
    /**
     * Return a TM3Locale instance by ID.
     * @param session active Hibernate Session
     * @param id ID of the locale to fetch
     * @return TM3Locale
     */
    public TM3Locale getLocaleById(Session session, long id);
    
    /**
     * Return a TM3Locale instance by Locale code.
     * @param session active Hibernate Session
     * @param code locale code (xx_YY) of the locale to fetch.
     * @return TM3Locale
     */
    public TM3Locale getLocaleByCode(Session session, String code);
}
