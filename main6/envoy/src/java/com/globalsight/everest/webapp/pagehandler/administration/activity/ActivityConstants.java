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
package com.globalsight.everest.webapp.pagehandler.administration.activity;


public interface ActivityConstants
{
    // Constant for saving activity in session
    public static final String ACTIVITY = "activity";

    // For checking dup names
    public static final String NAMES = "names";

    // For tags
    public static final String ACTIVITY_LIST = "activities";
    public static final String ACTIVITY_KEY = "activity";
    
    // Actions
    public static final String CANCEL = "cancel";
    public static final String CREATE = "create";
    public static final String DEPENDENCIES = "dependencies";
    public static final String EDIT = "edit";
    public static final String REMOVE = "remove";

    // fields
    public static final String NAME = "nameField";
    public static final String DESC = "descField";
    public static final String REVIEW = "review";
    
    // For sla report issue
    public static final String TYPE = "type";
    public static final String IS_DITA_QA_CHECK_ACTIVITY = "isDitaActivity";
    public static final String TRANSLATE = "translate";
    public static final String REVIEW_EDITABLE = "reviewEditable";
    public static final String REVIEW_NOT_EDITABLE = "reviewNotEditable";

    // Filter
    public static final String FILTER_ACTIVITY_NAME = "activityNameFilter";
    public static final String FILTER_COMPANY_NAME = "activityCompanyNameFilter";
}
