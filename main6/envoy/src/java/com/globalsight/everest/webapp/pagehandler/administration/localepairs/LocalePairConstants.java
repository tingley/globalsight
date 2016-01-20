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
package com.globalsight.everest.webapp.pagehandler.administration.localepairs;


public interface LocalePairConstants
{
    // Constant for saving activity in session
    public static final String LP = "lp";

    // Locales
    public static final String LOCALES = "locales";

    public static final String LANGUAGE = "lang";
    public static final String COUNTRIES = "countries";
    public static final String LANGUAGECOUNTRIES = "langCountries";
    // For tags
    public static final String LP_LIST = "lps";
    public static final String LP_KEY = "lp";
    
    // Actions
    public static final String CANCEL = "cancel";
    public static final String CREATE = "create";
    public static final String CREATE_LOCALE = "createLocale";
    public static final String DEPENDENCIES = "dependencies";
    public static final String REMOVE = "remove";
    public static final String EXPORT = "export";
    public static final String IMPORT = "import";
    public static final String FILTER = "filter";

    // Filter Names
    public static final String FILTER_COMPANY = "lpCompanyFilter";
    public static final String FILTER_SOURCELOCALE = "lpSourceFilter";
    public static final String FILTER_TARGETLOCALE = "lpTargetFilter";
}
