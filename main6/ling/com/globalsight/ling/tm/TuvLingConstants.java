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
package com.globalsight.ling.tm;

public interface TuvLingConstants
{
    // localization types (in TU)
    public final static int TRANSLATABLE = 1;
    public final static int LOCALIZABLE = 2;

    // Tuv states
    public final static int UNKNOWN = 0;
    public final static int NOT_LOCALIZED = 1;
    public final static int LOCALIZED = 2;
    public final static int OUT_OF_DATE = 3;
    public final static int COMPLETE = 4;
    public final static int EXACT_MATCH_LOCALIZED = 5;
    public final static int LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED = 6;
    public final static int ALIGNMENT_LOCALIZED = 7;

    // Tuv state names
    public final static String UNKNOWN_NAME = "UNKNOWN";
    public final static String NOT_LOCALIZED_NAME = "NOT_LOCALIZED";
    public final static String LOCALIZED_NAME = "LOCALIZED";
    public final static String OUT_OF_DATE_NAME = "OUT_OF_DATE";
    public final static String COMPLETE_NAME = "COMPLETE";
    public final static String EXACT_MATCH_LOCALIZED_NAME 
        = "EXACT_MATCH_LOCALIZED";
    public final static String LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED_NAME
        = "LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED";
    public final static String ALIGNMENT_LOCALIZED_NAME
        = "ALIGNMENT_LOCALIZED";
    
}
