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
package com.globalsight.everest.localemgr;

import java.rmi.RemoteException;
import java.util.Vector;

import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.util.GlobalSightLocale;

/**
 * Handles locale information for the application. Locale Pairs,
 * GlobalSightLocale, Locales for UI
 */
public interface LocaleManager
{
    /*
     * Constants
     */
    /**
     * Service name
     */
    public static final String SERVICE_NAME = "LocaleManager";

    /*
     * Methods
     */

    /**
     * Persist a GlobalSightLocale
     * 
     * @param GlobalSightLocale
     * @return GlobalSightLocale that is persisted
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    GlobalSightLocale addLocale(GlobalSightLocale p_locale)
            throws LocaleManagerException, RemoteException;

    /**
     * Add a source/target locales pair. After this, the source/target locale
     * pair becomes valid for a localization profile.
     * 
     * @param p_source
     *            The source locale
     * @param p_target
     *            The target locale
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public LocalePair addSourceTargetLocalePair(GlobalSightLocale p_source,
            GlobalSightLocale p_target, long companyId)
            throws LocaleManagerException, RemoteException;

    /**
     * Get all the locales which are the source locale in all the source/target
     * locale pairs with specified company id
     * <p>
     * 
     * @return A vector all the GlobalSightLocales that are specified as sources
     *         in LocalePairs.
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public Vector getAllSourceLocalesByCompanyId(String p_companyId)
            throws LocaleManagerException, RemoteException;

    /**
     * Get all the locales which are the source locale in all the source/target
     * locale pairs
     * 
     * @return A list of GlobalSightLocale's (the source locales)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    Vector getAllSourceLocales() throws LocaleManagerException, RemoteException;

    /**
     * Get all the distinct locales which are the targets locale in all the
     * source/target locale pairs
     * 
     * @return A list of GlobalSightLocale's (the target locales)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    Vector getAllTargetLocales() throws LocaleManagerException, RemoteException;

    /**
     * Get all the available locales (GlobalSightLocale) supported by the
     * system.
     * 
     * @return A vector of locales
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    Vector getAvailableLocales() throws LocaleManagerException, RemoteException;

    /**
     * Get all the available locales supported by the system.
     * <p>
     * 
     * @return Vector A vector of all the locales (GlobalSightLocales) that is
     *         supported by the UI.
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    Vector getSupportedLocalesForUi() throws LocaleManagerException,
            RemoteException;

    /**
     * Get a particular locale based on the id.
     * <p>
     * 
     * @return A locale based on the specified id.
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    GlobalSightLocale getLocaleById(long p_id) throws LocaleManagerException,
            RemoteException;

    /**
     * Get a particular locale based on the string
     * "language code"_"country code"
     * <p>
     * 
     * @return A locale based on the specified string
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    GlobalSightLocale getLocaleByString(String p_localeString)
            throws LocaleManagerException, RemoteException;

    /**
     * Get all the source/target locales pairs
     * 
     * @return A list of LocalePair's (the source/target locale pairs)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    Vector getSourceTargetLocalePairs() throws LocaleManagerException,
            RemoteException;

    /**
     * Get a source/target locales pair based on the given id.
     * 
     * @return A LocalePair object (the source/target locale pair)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    LocalePair getLocalePairById(long p_localePairId)
            throws LocaleManagerException, RemoteException;

    /**
     * Get a locale pair object based on the given source and target ids.
     * 
     * @return A LocalePair object (the source/target locale pair)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    LocalePair getLocalePairBySourceTargetIds(long p_sourceLocaleId,
            long p_targetLocaleId) throws LocaleManagerException,
            RemoteException;

    /**
     *
     */
    LocalePair getLocalePairBySourceTargetStrings(String p_sourceLocaleString,
            String p_targetLocaleString) throws LocaleManagerException,
            RemoteException;

    /**
     * Get a locale pair object based on the given source, target string (ie.
     * en_US, fr_FR_), and a company ID.
     * 
     * @return A LocalePair object (the source/target locale pair)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public LocalePair getLocalePairBySourceTargetAndCompanyStrings(
            String p_sourceLocaleString, String p_targetLocaleString,
            long companyId) throws LocaleManagerException, RemoteException;
            
    /**
     * For a given source locale, get the list of target locales that it is
     * paired with and specified company id.
     * <p>
     * 
     * @param GlobalSightLocale
     *            The source locale
     * @return A list of GlobalSightLocale's (the target locales)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public Vector getTargetLocalesByCompanyId(GlobalSightLocale p_sourceLocale,
            String p_companyId) throws LocaleManagerException, RemoteException;

    /**
     * For a given source locale, get the list of target locales that it is
     * paired with.
     * 
     * @param Locale
     *            The source locale
     * @return A list of Locale's (the target locales)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    Vector getTargetLocales(GlobalSightLocale p_sourceLocale)
            throws LocaleManagerException, RemoteException;

    /**
     * For a given source locale and project, get the list of target locales
     * that it is paired with.
     * 
     * @param Locale
     *            The source locale
     * @param String
     *            The project ID
     * @return A list of Locale's (the target locales)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    Vector getTargetLocalesByProject(GlobalSightLocale p_sourceLocale,
            String p_project) throws LocaleManagerException, RemoteException;

    /**
     * Remove a source/target locales pair
     * 
     * @param LocalePair
     *            The locales Pair
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    void removeSourceTargetLocalePair(LocalePair p_thePair)
            throws LocaleManagerException, RemoteException;

    // ////////////////////////////////////////////////////////////////////////////
    // BEGIN: Code Set /////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieve all codesets in the datastore
     * 
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public Vector getAllCodeSets() throws LocaleManagerException,
            RemoteException;

    /**
     * Retrieve all codesets associated with the specific locale id.
     * 
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public Vector getAllCodeSets(long p_localeId)
            throws LocaleManagerException, RemoteException;

}
