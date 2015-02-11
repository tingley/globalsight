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
package com.globalsight.ling.tm3.core;

import org.hibernate.cfg.Configuration;

/**
 * Integration point that manages the creation of TM3TuvData objects, either
 * from scratch or from the database.
 * <p>
 * Client code must implement this interface so that TM3 can marshall TUV data
 * in and out of the database.
 */
public interface TM3DataFactory<T extends TM3Data>
{

    /**
     * Extend the Hibernate configuration with any additional mappings required
     * to make this factory operate.
     * <p>
     * This routine should be called exactly once during a given application's
     * lifetime, when Hibernate is initialized.
     * 
     * @param cfg
     *            Hibernate Configuration object
     * @return the same Configuration instance, optionally modified with
     *         additional calls
     */
    public Configuration extendConfiguration(Configuration cfg);

    /**
     * Create a new {@link TM3Data} instance that corresponds to the provided
     * serialized data.
     * 
     * @param value
     *            Serialized TUV data produced by a previous call to
     *            {@link TM3Data#getSerializedForm()}.
     * @return TM3TuvData instance
     */
    public T fromSerializedForm(TM3Locale locale, String value);

    /**
     * Return the fuzzy scorer to be used to compare these objects.
     * 
     * @return
     */
    public TM3FuzzyMatchScorer<T> getFuzzyMatchScorer();

    /**
     * Return a TM3Locale instance by ID.
     * 
     * @param session
     *            active Hibernate Session
     * @param id
     *            ID of the locale to fetch
     * @return TM3Locale
     */
    public TM3Locale getLocaleById(long id);

    /**
     * Return a TM3Locale instance by Locale code.
     * 
     * @param session
     *            active Hibernate Session
     * @param code
     *            locale code (xx_YY) of the locale to fetch.
     * @return TM3Locale
     */
    public TM3Locale getLocaleByCode(String code);
}
