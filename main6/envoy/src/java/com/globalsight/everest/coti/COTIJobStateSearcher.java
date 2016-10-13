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

package com.globalsight.everest.coti;

import com.globalsight.everest.permission.Permission;

/**
 * Searching COTI jobs by its status
 * @author Wayzou
 *
 */
public abstract class COTIJobStateSearcher extends COTIJobSearcher
{

    abstract String getStateSql();

    @Override
    protected String getSpecialFrom()
    {
        return "";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected String getSpecialWhere()
    {
        StringBuffer sql = new StringBuffer();
        sql.append(getStateSql());

        return sql.toString();
    }

    private boolean isProjectManger()
    {
        return userPerms.getPermissionFor(Permission.PROJECTS_MANAGE);
    }

    private boolean isWorkflowManger()
    {
        return userPerms.getPermissionFor(Permission.PROJECTS_MANAGE_WORKFLOWS);
    }

    private boolean isScopeMyProject()
    {
        return userPerms.getPermissionFor(Permission.JOB_SCOPE_MYPROJECTS);
    }
}
