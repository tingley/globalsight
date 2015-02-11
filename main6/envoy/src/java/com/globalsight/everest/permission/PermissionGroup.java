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
package com.globalsight.everest.permission;

/**
 * Provides methods for a flexible user permission PermissionGroup
 */
public interface PermissionGroup
{
    /**
     ** Return the id of the PermissionGroup
     ** 
     * @return id as a long
     **/
    public long getId();

    /**
     * Return the name of the permission group
     * 
     * @return name
     */
    public String getName();

    /**
     * Return the description of the permissiongroup
     * 
     * @return description
     */
    public String getDescription();

    /**
     * Sets the name of the permission group
     * 
     * @param p_newName
     */
    public void setName(String p_newName);

    public long getCompanyId();

    public void setCompanyId(long companyId);

    /**
     * Sets the description of the permissiongroup
     * 
     * @param p_newDesc
     */
    public void setDescription(String p_newDesc);

    /** Gets the PermissionSet for this PermissionGroup */
    public PermissionSet getPermissionSet();

    /**
     * Sets the PermissionSet for this PermissionGroup The string should be of
     * the format "|1|2|3|" where 1,2,and 3 are permissions that the use has.
     */
    public void setPermissionSet(String p_permissionSetString);
}
