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

package com.globalsight.terminology;

import com.globalsight.terminology.TermbaseException;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.*;

/**
 * <p>The RMI interface for the Terminology User Data Manager that
 * manages system-wide and user-specific objects like Input Models,
 * Layouts, Filters, Import/Export Descriptions and so on.</p>
 *
 * <p>Data is managed as a collection of named objects. Objects are
 * identified by TB Id (implicit), Type Id, User Name, and finally
 * Object Name. If the user name is null, the object becomes a default
 * object shared by all users.</p>
 */
public interface IUserdataManager
    extends Remote
{
    static final int TYPE_INPUTMODEL = 1;
    static final int TYPE_FILTER = 2;
    static final int TYPE_LAYOUT = 3;

    /**
     * Returns the termbase name this manager is working with.
     */
    public String getTermbaseName()
        throws TermbaseException, RemoteException;

    /**
     * Retrieves all object names for objects of the specified
     * type. Administrators can read object names for all users,
     * normal users can read only their own objects' names.
     *
     * @return an XML object
     * &lt;names&gt;&lt;name type="system|user"&gt;...&lt;/name&gt;&lt;/names&gt;
     */
    String getObjectNames(int p_type, String p_user)
        throws TermbaseException, RemoteException;

    /**
     * <p>Retrieves a single typed and named object. Administrators
     * can retrieve objects for all users, normal users can only see
     * their own objects.</p>
     */
    String getObject(int p_type, String p_user, String p_name)
        throws TermbaseException, RemoteException;

    /**
     * <p>Retrieves a system-wide default object of the given type.</p>
     */
    String getDefaultObject(int p_type)
        throws TermbaseException, RemoteException;

    /**
     * Creates an object with name p_name and value p_value for the
     * given user. If the user name is null, a system object is
     * created for all users.
     */
    void createObject(int p_type, String p_user, String p_name, String p_value)
        throws TermbaseException, RemoteException;

    /**
     * Updates an object with name p_name and value p_value for the
     * given user.
     */
    void modifyObject(int p_type, String p_user, String p_name, String p_value)
        throws TermbaseException, RemoteException;

    /**
     * Deletes an objects for the given user. If the caller is
     * Administrator and p_user is null, the system object is
     * deleted.
     */
    void deleteObject(int p_type, String p_user, String p_name)
        throws TermbaseException, RemoteException;

    /**
     * Deletes all objects for the given user. If the caller is
     * Administrator and p_user is null, all system objects are
     * deleted.
     */
    void deleteObjects(int p_type, String p_user)
        throws TermbaseException, RemoteException;

    /**
     * Makes a system-wide object the default object.
     */
    void makeDefaultObject(int p_type, String p_user, String p_name)
        throws TermbaseException, RemoteException;

    /**
     * Unsets a system-wide default object.
     */
    public void unsetDefaultObject(int p_type, String p_user)
        throws TermbaseException, RemoteException;
}
