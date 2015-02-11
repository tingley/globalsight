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
package com.globalsight.everest.webapp.pagehandler.administration.users;

/**
* This interface contains the constants associated with the state of
* user handlers.
*/
public interface UserStateConstants
{
    public static final String USER_NAME = "userName";
    public static final String PASSWORD = "password";
    public static final String PASSWORD_CONFIRM = "passwordConfirm";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String ADDRESS = "address";
    public static final String PHONE = "phone";
    public static final String EMAIL = "email";
    public static final String USER_GROUPS = "userGroups";
    public static final String UI_LOCALE = "uiLocale";
    public static final String GROUPS = "groups";

    // For projects table
    public static final String PROJ_PAGE_NUM = "projectPageNum";
    public static final String PROJ_LIST_SIZE = "projectSize";
    public static final String PROJ_REVERSE_SORT = "projectReverseSort";
    public static final String PROJ_LAST_PAGE_NUM = "projectLastPageNum";
    public static final String PROJ_NUM_PAGES = "projectNumPages";
    public static final String PROJ_LIST = "projects";
    public static final String PROJ_NUM_PER_PAGE_STR = "projectNumPerPage";
    public static final String PROJ_SORTING = "projectSorting";
}

