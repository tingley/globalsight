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

import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.TermbaseException;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.*;

/**
 * <p>The RMI interface for the Terminology Database Manager, which is
 * responsible for creating, deleting, and accessing terminology
 * databases.</p>
 */
public interface ITermbaseManager
    extends Remote
{
    /**
     * Retrieves the server name and version info.
     *
     * @return a string like "GlobalSight Termbase Version 1.0".
     */
    String getVersion()
        throws RemoteException;

    /**
     * Retrieves a list of termbase names and descriptions known to
     * the server.
     *
     * @param p_uiLocale -- the UI locale to use for sorting
     * @return an ArrayList
     */
    ArrayList getTermbaseList(Locale p_uiLocale)
        throws RemoteException;

    /**
     * Retrieves a sorted list of termbase names and descriptions known to
     * the server.
     *
     * @param p_uiLocale -- the UI locale to use for sorting
     * @return an XML string:
     * <termbases>
     *   <termbase>
     *     <name>NAME</name>
     *     <description>DESC</description>
     *   </termbase>
     * </termbases>
     */
    String getTermbases(Locale p_uiLocale, String p_userId)
        throws RemoteException;
    
    /**
     * Retrieves a sorted list of termbase names and descriptions known to
     * the server.
     *
     * @param p_uiLocale -- the UI locale to use for sorting
     * @param p_userId
     * @param p_companyId
     * @return an XML string:
     * <termbases>
     *   <termbase>
     *     <name>NAME</name>
     *     <description>DESC</description>
     *   </termbase>
     * </termbases>
     */
    String getTermbases(Locale p_uiLocale, String p_userId, String p_companyId)
        throws RemoteException;

    /**
     * Retrieves the name of the termbase with the given id.
     *
     * @return String if termbase was found, else null.
     */
    String getTermbaseName(long id)
        throws RemoteException;

    /**
     * Retrieves the id of the termbase with the given name.
     *
     * @return long >= 0 if termbase was found, else -1.
     */
    long getTermbaseId(String name)
        throws RemoteException;
    
    /**
     * Retrieves the id of the termbase with the given name and company id.
     * 
     * @return long >= 0 if termbase was found, else -1.
     */
    long getTermbaseId(String p_name, String p_companyId)
        throws RemoteException;

    /**
     * Connects to a termbase by name.
     *
     * @return an ITermbase interface pointer.
     *
     * @throws TermbaseException when the name does not exist or the
     * termbase is locked by a manager.
     */
    ITermbase connect(String termbaseName, String user, String password)
        throws TermbaseException, RemoteException;

    /**
     * Connects to a termbase by name and company id.
     *
     * @return an ITermbase interface pointer.
     *
     * @throws TermbaseException when the name does not exist or the
     * termbase is locked by a manager.
     */
    ITermbase connect(String p_termbaseName, String p_user, String p_password,
            String p_companyId) throws TermbaseException, RemoteException;
    
    /**
     * Connects to a termbase by id.
     *
     * @return an ITermbase interface pointer.
     *
     * @throws TermbaseException when the name does not exist or the
     * termbase is locked by a manager.
     */
    ITermbase connect(long p_id, String p_user, String p_password)
            throws TermbaseException, RemoteException;

    /**
     * Creates a new termbase. A termbase name and termbase definition
     * have to be specified.
     *
     * @param definition: an XML object for the database definition.
     *
     * @throws TermbaseException when the name exists, the definition
     * is incorrect, or the termbase cannot be created physically.
     */
    ITermbase create(String user, String password, String definition, String companyId)
        throws TermbaseException, RemoteException;

    /**
     * Creates a new termbase. A termbase name and termbase definition
     * have to be specified.
     *
     * @param definition: an XML object for the database definition.
     *
     * @throws TermbaseException when the name exists, the definition
     * is incorrect, or the termbase cannot be created physically.
     */
    ITermbase create(String user, String password, String definition)
        throws TermbaseException, RemoteException;
    
    /**
     * Deletes a Termbase from the system.
     *
     * @throws TermbaseException when the termbase does not exist, is
     * in use by another reader or manager, or a database error occurs
     * during deletion.
     */
    void delete(String termbaseName, String user, String password)
        throws TermbaseException, RemoteException;

    /**
     * Renames a Termbase.
     *
     * @throws TermbaseException when the termbase does not exist, the
     * new name already exists, or the termbase is in use by another
     * reader or manager, or a database error occurs during renaming.
     */
    void rename(String termbaseName, String newName,
        String user, String password)
        throws TermbaseException, RemoteException;

    /**
     * Updates a Termbase Definition. Since the definition includes
     * the termbase name, it can be changed as well.
     *
     * @throws TermbaseException when the termbase does not exist, the
     * the definition is incorrect or cannot be saved in the database,
     * the new name already exists (if changed in the definition), the
     * termbase is in use by another reader or manager, or a database
     * error occurs during renaming.
     */
    void updateDefinition(String termbaseName, String definitionXml,
        String user, String password)
        throws TermbaseException, RemoteException;

    /**
     * Returns the termbase definition of the given termbase. If the
     * argument <code>clone</code> is true, a copy of the definition
     * is returned.
     */
    String getDefinition(String termbaseName, boolean clone)
        throws TermbaseException, RemoteException;

    /**
     * Returns a default termbase definition that can be modified
     * according to taste.
     */
    String getDefaultDefinition()
        throws RemoteException;

    /**
     * Returns the termbase statistics of the given termbase.
     */
    String getStatistics(String termbaseName)
        throws TermbaseException, RemoteException;
    
    /**
     * only get the term count info.
     */
    public String getStatisticsNoIndexInfo(String termbaseName)
        throws TermbaseException, RemoteException;

}
