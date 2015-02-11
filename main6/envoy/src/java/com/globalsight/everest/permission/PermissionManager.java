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
import java.util.Collection;
import java.util.List;
import java.rmi.RemoteException;

/** A service interface for performing CRUD operations for PermissionGroup objects **/
public interface PermissionManager
{
    public static final String SERVICE_NAME = "PermissionManager";

    /**
    ** Creates a new PermissionGroup object in the data store
    ** @return the created object
    **/
    public PermissionGroup createPermissionGroup(PermissionGroup p_permGroup)
    throws PermissionException, RemoteException;
    
    /**
    ** Reads the PermissionGroup object from the database
    ** @return the PermissionGroup
    **/
    public PermissionGroup readPermissionGroup(long p_id)
    throws PermissionException, RemoteException;

    /**
    ** Deletes a PermissionGroup from the database
    **/
    public void deletePermissionGroup(PermissionGroup p_permGroup)
    throws PermissionException, RemoteException;


    /**
    ** Update the XMLRuleFile object in the database
    ** @return the updated object
    **/
    public PermissionGroup updatePermissionGroup(PermissionGroup p_permGroup)
    throws PermissionException, RemoteException; 

    /**
    ** Get a list of all existing PermissionGroup objects in the database
    ** @return a Collection of the PermissionGroup objects
    **/
    public Collection<PermissionGroup> getAllPermissionGroups()
    throws PermissionException, RemoteException;
    public Collection getPermissionGroupsBycondition(String condition)
            throws PermissionException, RemoteException;
    /**
     ** Get a list of all existing PermissionGroup objects in the database with
     *  specified company id.
     ** @return a Collection of the PermissionGroup objects
     **/
     public Collection<PermissionGroup> getAllPermissionGroupsByCompanyId(String p_companyId)
     throws PermissionException, RemoteException;
     
    /**
     * Queries all permissiongroups for this user
     * 
     * @param p_userId user
     * @return Collection of PermissionGroups
     */
    public Collection<PermissionGroup> getAllPermissionGroupsForUser(String p_userId)
    throws PermissionException, RemoteException;
    
    /**
     * Queries all permissiongroup or project names for this user
     * 
     * @param tableName project or permission
     * @return Collection of PermissionGroup name or project name
     */
    
    public  Collection<Object[]> getAlltableNameForUser(String tableName)
    throws PermissionException, RemoteException;

    /**
     * Queries all permissiongroup names for this user.
     * This method only exists for backwards compatibility with
     * the old code that checked for group names. Try not to use it.
     * 
     * @deprecated
     * @param p_userId user
     * @return Collection of PermissionGroup names as Strings
     */
    public Collection<String> getAllPermissionGroupNamesForUser(String p_userId)
    throws PermissionException, RemoteException;

    /**
     * Queries out all the permissiongroups for the user,
     * and ORs together all the permissionsets to create
     * one permission set.
     * 
     * @param p_userId userid
     * @return PermissionSet
     */
    public PermissionSet getPermissionSetForUser(String p_userId)
    throws PermissionException, RemoteException;

    /**
     * Queries all usernames for a given permission group id
     * @param p_id -- the ID of the permission group
     * @return Collection of String
     */
    public Collection<String> getAllUsersForPermissionGroup(long p_id)
        throws PermissionException, RemoteException;


	/**
	 * Queries all usernames for a given permission group name
	 * @param permGroupName -- the name of the permission group
	 * @return Collection of String
	 */
	public Collection<String> getAllUsersForPermissionGroup(String permGroupName)
        throws PermissionException, RemoteException;

	/**
     * Queries for all users with a specific permission.
     * 
     * @param p_permission
     *               permission name
     *               @see Permission
     * @return Collection of String
     * @exception PermissionException
     * @exception RemoteException
     */
    public Collection<String> getAllUsersWithPermission(String p_permission)
        throws PermissionException, RemoteException;

    /**
     * Takes in a list of usernames and a permissiongroup.
     * And maps all the users to the permissiongroup.
     * 
     * @param p_users  list of usernames (list of String)
     * @param p_permGroup PermissionGroup
     * @exception PermissionException
     * @exception RemoteException
     */
    public void mapUsersToPermissionGroup(List<String> p_users, PermissionGroup p_permGroup)
    throws PermissionException, RemoteException;

    /**
     * Removes the given users from the PermissionGroup
     * 
     * @param p_users list of usernames (String)
     * @param p_permGroup
     */
    public void unMapUsersFromPermissionGroup(List<String> p_users, PermissionGroup p_permGroup)
    throws PermissionException, RemoteException;

}

