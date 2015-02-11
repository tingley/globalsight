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
package com.globalsight.everest.costing;

/* Copyright (c) 2002, GlobalSight Corporation.  All rights reserved.
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

//globalsight
import com.globalsight.util.GeneralException;

/**
 * An exception handling object for the Costing component.
 */
public class CostingException extends GeneralException
{

   //message keys - for new GeneralException

    // ---- general
    public final static String MSG_FAILED_TO_FIND_USER_MANAGER = "FailedToFindUserManager";
    public final static String MSG_FAILED_TO_FIND_LOCALE_MANAGER = "FailedToFindLocaleManager";
    public final static String MSG_FAILED_TO_FIND_WORKFLOW_SERVER = "FailedToFindWorkflowServer";
    public final static String MSG_FAILED_TO_FIND_TASKMANAGER = "FailedToFindTaskManager";
    public final static String MSG_FAILED_TO_SCHEDULE_CURRENCY_UPDATE = "FailedToScheduleCurrencyUpdate";

    // --- iso currencies ---
    public final static String MSG_FAILED_TO_RETRIEVE_ISO_CURRENCIES = "FailedToGetIsoCurrencies";
    // Args: 1 - iso currency code
    public final static String MSG_FAILED_TO_RETRIEVE_ISO_CURRENCY = "FailedToRetrieveIsoCurrency";

    // --- currencies ---
    public final static String MSG_FAILED_TO_RETRIEVE_CURRENCIES = "FailedToGetAllCurrencies";
    public final static String MSG_FAILED_TO_ADDORMODIFY_CURRENCIES = "FailedToAddOrModifyCurrencies";
    // Args: 1 - currency code
    public final static String MSG_FAILED_TO_ADDORMODIFY_CURRENCY = "FailedToAddOrModifyCurrency";
    // Args: 1 - currency code
    public final static String MSG_FAILED_TO_RETRIEVE_CURRENCY = "FailedToRetrieveCurrency";
    public final static String MSG_FAILED_TO_GET_PIVOT_CURRENCY = "FailedToRetrievePivotCurrency";
    // Args: 1 - name of new pivot currency trying to change to
    public final static String MSG_FAILED_TO_CHANGE_PIVOT_CURRENCY = "FailedToChangePivotCurrency";

    // locales for rates
    // Args: 1 - source locale
    public final static String MSG_FAILED_TO_GET_TARGET_LOCALES_TO_FIND_RATES = 
        "FailedToRetrieveTargetLocalesToGetRates";
    // ---- rates
    // Args: 1 - name of rate
    public final static String MSG_FAILED_TO_ADD_RATE = "FailedToAddRate";
    //Args: 1 - name of rate, 2 - name of activity, 3 - source locale, 4 - target locale
    public final static String MSG_FAILED_TO_ADD_RATE_TO_ROLE = "FailedToAddRateToRole";
    // Args: 1 - rate id
    public final static String MSG_FAILED_TO_MODIFY_RATE = "FailedToModifyRate";
    public final static String MSG_FAILED_TO_RETRIEVE_RATES = "FailedToRetrieveRates";
    // Args: 1 - rate id
    public final static String MSG_FAILED_TO_RETRIEVE_RATE = "FailedToRetrieveRate";
    // Args: 1 - activity name, 2 - source locale, 3 - target locale
    public final static String MSG_FAILED_TO_RETRIEVE_RATES_ON_ROLE = "FailedToRetrieveRatesOnRole";
    // Args: 1 - source locale, 2 - target locale
    public final static String MSG_FAILED_TO_RETRIEVE_RATES_ON_LOCALES = "FailedToRetrieveRatesOnLocales";
    // Args: 1 - the role the rates are associated with
    public final static String MSG_FAILED_TO_DELETE_RATES = "FailedToDeleteRatesOnRole";


    // costing
    // Args: 1 - workflow id the tasks are for
    public final static String MSG_FAILED_TO_GET_TASKS_FOR_COSTING = 
        "FailedToGetTasksForCosting";
    // Args: 1 - cost to override with
    public final static String MSG_FAILED_TO_OVERRIDE_COST =  
        "FailedToOverrideCost";                               
    public final static String MSG_FAILED_TO_CLEAR_OVERRIDE_COST = 
        "FailedToClearOverrideCost";
    // Args: 1 - sucharge name
    //       2 - cost id surcharge is being added to
    public final static String MSG_FAILED_TO_ADD_SURCHARGE = 
        "FailedToAddSurcharge";
    // Args: 1 - surcharge name
    //       2 - cost id that is being removed from
    public final static String MSG_FAILED_TO_REMOVE_SURCHARGE = 
        "FailedToRemoveSurcharge";
    // Args: 1 - estimated amount
    //       2 - amount of work type (rate type)
    //       3 - job id
    public final static String MSG_FAILED_TO_SET_AOW_IN_JOB = 
        "FailedToSetEstimatedAmountOfWorkInJob";
    // Args: 1 - amount of work type (rate type)
    //       2 - job id
    public final static String MSG_FAILED_TO_GET_ESTIMATED_AOW_ON_JOB = 
        "FailedToGetEstimatedAmountOfWorkOnJob";
    // Args: 1 - estimated amount
    //       2 - amount of work type (rate type)
    //       3 - task id
    public final static String MSG_FAILED_TO_SET_ESTIMATED_WORK = 
        "FailedToSetEstimatedAmountOnTask";
    // Args: 1 - invalid type
    public final static String MSG_INVALID_ESTIMATED_UNIT_OF_WORK = 
        "FailedToSetEstimatedAmountBecauseInvalidUow";
    // Args: 1 - actual amount
    //       2 - amount of work type (rate type)
    //       3 - task id
    public final static String MSG_FAILED_TO_SET_ACTUAL_WORK = 
        "FailedToSetActualAmountOfWorkInTask";
    // Args: 1 - job id
    public final static String MSG_FAILED_TO_COST_A_JOB =
        "FailedToCostJob";
    // Args: 1 - workflow id
    public final static String MSG_FAILED_TO_COST_A_WORKFLOW = 
        "FailedToCostWorkflow";
    // Args: 1 - task id
    public final static String MSG_FAILED_TO_COST_A_TASK = 
        "FailedToCostTask";
    // Args: 1 - cost id
    public final static String MSG_FAILED_TO_RETRIEVE_COST = 
        "FailedToRetrieveCost";
    // Args: 1 - some string that identifies the object
    //       2 - id of object
    public final static String MSG_FAILED_TO_RETRIEVE_COST_ON_OBJ = 
        "FailedToRetrieveCostOnObj";

    public final static String MSG_INVALID_OBJ_TYPE = 
        "FailedToRetrieveCostInvalidObjectType";

    // message file name
    private static final String PROPERTY_FILE_NAME = "CostingException";

    /**
     *
     */
    public CostingException(String p_messageKey, String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException, PROPERTY_FILE_NAME);
    }
}
