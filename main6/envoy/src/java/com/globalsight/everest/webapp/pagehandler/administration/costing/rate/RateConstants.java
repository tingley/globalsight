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
package com.globalsight.everest.webapp.pagehandler.administration.costing.rate;


public interface RateConstants
{
    // Constant for saving currency in session
    public static final String RATE = "rate";

    // For combo boxes
    public static final String ACTIVITIES = "activities";
    public static final String CURRENCIES = "currencies";
    public static final String LPS = "localePairs";
    public static final String RATES = "rates";
    public static final String RATE_NAMES = "rate_names";

    // For tags
    public static final String RATE_LIST = "rates";
    public static final String RATE_KEY = "rate";
    
    // Actions
    public static final String CANCEL = "cancel";
    public static final String CREATE = "create";
    public static final String EDIT = "edit";
    public static final String REMOVE = "remove";

    // For multiply create rate
    public static final String RATE_NAME = "rate_name";
    public static final String RATE_LOCALEPAIR = "rate_localePair";
    
    public static final String RATE_ID = "rateID";
    
    public static final String FILTER_RATE_NAME = "rateNameFilter";
    public static final String FILTER_RATE_COMPANY = "rateCompanyFilter";
    public static final String FILTER_RATE_SOURCE_LOCALE = "rateSourceLocaleFilter";
    public static final String FILTER_RATE_TARGET_LOCALE = "rateTargetLocaleFilter";
}
