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

/**
Defines the accepted leverage types. Note that the values are ordered.
Setting a leverager type assumes we want all of the types above it.
For example, seeting a FUZZY match type assumes we will return:
TEXT_ONLY, EXACT and LEVERAGED_GROUP_EXACT match types as well.
*/
public interface LeverageType
{
    /**
    Return only Guaranteed Exact matches.
    */
    public static final int GUARANTEED_EXACT = 0;

    /**
    Return only LEVERAGED_GROUP_EXACT matches.
    */
    public static final int LEVERAGE_GROUP_EXACT = 0;

    /**
    Return only LEVERAGED_GROUP_EXACT and EXACT matches.
    */
    public static final int EXACT = 1;

//  JEH - not for System 4.0
//  /**
//  */
//  public static final int TEXT_ONLY = 2;

    /**
    Return only LEVERAGED_GROUP_EXACT, EXACT and FUZZY matches.
    */
    public static final int FUZZY = 3;

    /**
    The de-facto fuzzy threshold.
    */
    public static final short DEFAULT_FUZZY_THRESHOLD = 70;

    /**
    Non project TM penatly for fuzzies.
    */
    public static final short NON_PROJECT_TM_PENALTY = 1;
}
