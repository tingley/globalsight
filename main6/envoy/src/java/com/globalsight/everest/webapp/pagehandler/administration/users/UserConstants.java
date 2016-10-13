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

public interface UserConstants
{
    // Constant for saving activity in session
    public static final String USER = "user";

    // For tags
    public static final String USER_LIST = "users";
    public static final String USER_KEY = "user";
    public static final String ROLE_LIST = "roles";
    public static final String ROLE_KEY = "role";
    public static final String PROJECT_LIST = "projects";
    public static final String PROJECT_KEY = "project";

    // Actions
    public static final String CANCEL = "cancel";
    public static final String CREATE = "create";
    public static final String DEPENDENCIES = "dependencies";
    public static final String EDIT = "edit";
    public static final String REMOVE = "remove";

    // moved from UserSearchHandler (2/11/05)
    public static final String CREATE_USER_WRAPPER = "createUserWrapper";
    public static final String MODIFY_USER_WRAPPER = "modifyUserWrapper";
    public static final String ADD_ANOTHER = "addAnother";
    public static final String SEARCH_PARAMS = "searchParams";

}
