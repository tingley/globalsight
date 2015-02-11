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
package com.globalsight.everest.persistence.locale;

/**
 * Specifies the names of all the named queries for Locale.
 */
public interface LocaleQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all available locales
     * <p>
     * Arguments: None.
     */
    public static String ALL_LOCALES = "getAllLocales";
 

    /**
     * A named query to return all locales supported for the UI
     * <p>
     * Arguments: None.
     */
    public static String LOCALES_SUPPORTED_FOR_UI = "getLocalesSupportedForUi";

    /**
     * A named query to return a locale based on its id
     * <p>
     * Arguments: 1: Locale Id.
     */
    public static String LOCALE_BY_ID = "getLocaleById"; 

    /**
     * A named query to return a locale based on the ISO language and country codes.
     * <p>
     * Arguments: 1: language ISO code.
     *            2: country ISO code.
     */
    public static String LOCALE_BY_LANGUAGE_AND_COUNTRY = "getLocaleByLanguageAndCountry";

    /**
     * A named query to return all source locales
     * <p>
     * Arguments: None.
     */
    public static String ALL_SOURCE_LOCALES = "getAllSourceLocales";

    /**
     * A named query to return all target locales
     * <p>
     * Arguments: None.
     */
    public static String ALL_TARGET_LOCALES = "getAllTargetLocales";

    /**
     * A named query to return all target locales associated with a specific
     * source locale id.
     * <p>
     * Arguments: 1: Locale Id.
     */
    public static String TARGET_LOCALES_BY_SOURCE =
        "getAllTargetLocalesAssociatedWithSource";
    
    /**
     * A named query to return fileprofile source id
     * <p>
     * Arguments: None.
     */
    public static String SOURCE_FILE_FORMAT_BY_ID = "getSourceFileFormatById";
    
}
