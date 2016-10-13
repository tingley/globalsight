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
package com.globalsight.everest.securitymgr;

import java.rmi.RemoteException;
import java.util.List;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.PermissionManager;
import com.globalsight.everest.usermgr.UserManagerException;

/**
 * SecurityManager is an interface used for handling security related processes.
 * It does not handle Permissions.
 * 
 * @see PermissionManager
 */
public interface SecurityManager
{
    // The name bound to the remote object.
    public static final String SERVICE_NAME = "SecurityManagerServer";

    /**
     * Authenticates the user by the provided credentials and returns the user
     * information upon a successful login.
     * 
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    User authenticateUser(String p_userId, String p_password)
            throws RemoteException, SecurityManagerException;

    User authenticateUserByName(String p_userName, String p_password)
            throws RemoteException, SecurityManagerException;

    /**
     * Log the user out.
     * 
     * @param p_userName
     *            The user name (aka user id) of the user to log out.
     * 
     * @exception SecurityManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    void logUserOut(String p_userName) throws RemoteException,
            SecurityManagerException;

    /**
     * Get the field security that the specified user has on the object passed
     * in.
     * 
     * @param p_requestingUser
     *            The user requesting to access fields on the object.
     * @param p_objectWithFields
     *            The object (vendor/user) that has security on its fields.
     * @param p_checkProjects
     *            Specifies if the projects should be checked or not. If 'false'
     *            then don't check if the user and object are in the same
     *            projects and just return the field security, if 'true' check
     *            if they are in the same project - if so return a field
     *            security that allows "sharing."
     * 
     * @return The field security associated with the object passed in according
     *         to the user making the request.
     */
    public FieldSecurity getFieldSecurity(User p_requesting,
            Object p_objectWithFields, boolean p_checkProjects)
            throws RemoteException, SecurityManagerException;

    /**
     * Get the field security object for each of the objects in the list. The
     * objects in the list should all be of the same type (User, Vendor)
     * 
     * @param p_requestingUser
     *            The user requesting to accee fields.
     * @param p_objectsWithFields
     *            The list of object that have security on their fields.
     * @param p_checkWithProjects
     *            Specifies if the projects should be checked or not. If 'false'
     *            then don't check if the user and objects are in the same
     *            projects and return the field security. If 'true' check if
     *            they are in the same project - if so return a a field security
     *            that allows "sharing."
     * 
     * @return A list of FieldSecurity objects for the specified
     *         Vendor/VendorInfo/User list.
     */
    public List getFieldSecurities(User p_requestingUser,
            List p_objectsWithFields, boolean p_checkProjects)
            throws RemoteException, SecurityManagerException;

    /**
     * Set the field security on the specified user. Verifies that that the user
     * requesting to set the fields has authority to do so.
     * 
     * @param p_requestingUser
     *            The user requesting to change the security fields.
     * @param p_objectWithFields
     *            The object (vendor/user) that has security on its fields.
     * @param p_fs
     *            The field security to set on the object.
     */
    public void setFieldSecurity(User p_requestingUser,
            Object p_objectWithFields, FieldSecurity p_fs)
            throws RemoteException, SecurityManagerException;

    /**
     * Removes the field security for the specified object. This is used when
     * the specified object is being removed from the system.
     * 
     * @param p_requestingUser
     *            The user requesting to remove the security fields.
     * @param p_objectWithFields
     *            The object (Vendor/User) whose security fields will be
     *            removed.
     */
    public void removeFieldSecurity(User p_requestingUser,
            Object p_objectWithFields) throws RemoteException,
            SecurityManagerException;
}
