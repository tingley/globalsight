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
/*
 * Copyright (c) 2002 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF 
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

/**
 * This exception is thrown for any exception that is related to the
 * internal working of the Locale manager component.
 */
public class LocaleManagerException 
    extends GeneralException
{
    // message keys

    //   *** code sets ***

    public static final String MSG_FAILED_TO_GET_CODE_SETS = "FailedToGetCodeSets";
    //Arg: 1 - locale id
    public static final String MSG_FAILED_TO_GET_CODE_SETS_BY_LOCALE = "FailedToGetCodeSetsByLocale";

    //   ****  locales ***
    public static final String MSG_FAILED_TO_GET_LOCALES = "FailedToRetrieveLocales";
    public static final String MSG_FAILED_TO_GET_SOURCE_LOCALES = "FailedToRetrieveSourceLocales";
    public static final String MSG_FAILED_TO_GET_TARGET_LOCALES = "FailedToRetrieveTargetLocales";
    // Arg: 1 - Locale to add ("toString")
    public static final String MSG_FAILED_TO_ADD_LOCALE = "FailedToAddLocale"; 
    // Arg: 1 - Locale id  or Locale name
    public static final String MSG_FAILED_TO_GET_LOCALE = "FailedToRetrieveLocale";
    //   ***  sourcefomat sets ***
    // Arg: 1 sourcefomat id 
    public static final String MSG_FAILED_TO_GET_SOURCE_FILE_FORMAT = "FailedToRetrieveSourceFileFormat";
    

    // *** locale pairs ****

    public static final String MSG_FAILED_TO_GET_LOCALE_PAIRS = "FailedToRetrieveLocalePairs";
    //Arg: 1 - locale pair id
    public static final String MSG_FAILED_TO_GET_LOCALE_PAIR_BY_ID = "FailedToGetLocalePairById";
    //Arg: 1 - source locale id
    //     2 - target locale id
    public static final String MSG_FAILED_TO_GET_LOCALE_PAIR_BY_SRC_TRGT_IDs = 
        "FailedToGetLocalePairBySrcTrgtId";
    // Arg: 1 - source locale string
    //      2 - target locale string
    public static final String MSG_FAILED_TO_GET_LOCALE_PAIR_BY_SRC_TRGT_STRINGS =
        "FailedToGetLocalePairBySrcTrgtStrings";
    // Arg: 1 - source locale ("toString")
    public static final String MSG_FAILED_TO_GET_TARGET_LOCALES_BY_SOURCE = 
        "FailedToGetTargetLocalesBySource";
    // Arg: 1 - source locale ("toString")
    //      2 - target locale ("toString")
    public static final String MSG_FAILED_TO_ADD_LOCALE_PAIR = "FailedToAddLocalePair";
    // Arg: 1 - source locale ("toString")
    //      2 - target locale ("toString")
    public static final String MSG_FAILED_TO_REMOVE_LOCALE_PAIR = "FailedToRemoveLocalePair"; 
    
    // *** roles ***  created/removed with locale pairs
    public static final String MSG_FAILED_TO_GET_ACTIVITES = "FailedToRetrieveActivities";
    // Arg: 1 - role name
    public static final String MSG_FAILED_TO_ADD_ROLE = "FailedToAddRole";
    // Arg: 1 - role name
    public static final String MSG_FAILED_TO_REMOVE_ROLE = "FailedToRemoveRole";
    // Arg: 1 - role name that the rates are associated with
    public static final String MSG_FAILED_TO_DELETE_RATES = "FailedToRemoteRatesByRole";


    public static final String MSG_FAILED_TO_BIND_TO_COMPONENT = "FailedToBindToUserManagerOrJobHandler";

       
    // message file name
    private static final String PROPERTY_FILE_NAME = "LocaleManagerException";



    /*
     *  Constructor to create an exception that pertains to LocaleManager
     */
    public LocaleManagerException(String p_messageKey, String[] p_messageArguments, 
                                  Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException, PROPERTY_FILE_NAME);
    }                                                                      
}
