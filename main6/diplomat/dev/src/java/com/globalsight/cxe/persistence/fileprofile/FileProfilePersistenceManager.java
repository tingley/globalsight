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
package com.globalsight.cxe.persistence.fileprofile;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.globalsight.cxe.entity.fileextension.FileExtension;
import com.globalsight.cxe.entity.fileextension.FileExtensionImpl;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;

/** A service interface for performing CRUD operations for FileProfiles **/
public interface FileProfilePersistenceManager
{
    public static final String SERVICE_NAME = "FileProfilePersistenceManager";

    //////////////////////////////////////////////////////////////////////////////
    //  BEGIN: File Profiles  ////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    /**
    ** Creates a new FileProfile object in the data store
    ** @return the newly created object
    **/
    public FileProfile createFileProfile(FileProfile p_fileProfile)
    throws FileProfileEntityException, RemoteException;

    /**
    ** Reads the FileProfile object from the datastore
    ** @return FileProfile with the given id
    **/
    public FileProfile readFileProfile(long p_id)
    throws FileProfileEntityException, RemoteException;

    /**
    ** Deletes a FileProfile object from the datastore
    **/
    public void deleteFileProfile(FileProfile p_fileProfile)
    throws FileProfileEntityException, RemoteException;


    /**
    ** Update the FileProfile object in the datastore
    ** @return the newly updated object
    **/
    public FileProfile updateFileProfile(FileProfile p_fileProfile)
    throws FileProfileEntityException, RemoteException;

    /**
    ** Get a list of all existing FileProfile objects in the datastore
    ** @return a vector of the FileProfile objects
    **/
    public Collection getAllFileProfiles()
    throws FileProfileEntityException, RemoteException;
    public Collection getAllFileProfilesByCondition(String condtion)
            throws FileProfileEntityException, RemoteException;

    /**
     * Return the file profile id for the given name from the database
     * The returned object is in a state that does not allow editing.
     *
     * @param p_name - The name of the file profile.
     * @return the file profile id for the given name.
     */
    public long getFileProfileIdByName(String p_name)
        throws FileProfileEntityException, RemoteException;

    /**
     * Return the file profile for the given name from the database
     * The returned object is in a state that does not allow editing.
     *
     * @param p_name - The name of the file profile.
     * @return the file profile for the given name.
     */
    public FileProfile getFileProfileByName(String p_name)
        throws FileProfileEntityException, RemoteException;

    public FileProfile getFileProfileByName(String p_name, boolean p_isActive)
            throws FileProfileEntityException, RemoteException;

    /**
     * Return the file profile that has the given id.
     *
     * @param p_name - The id of the file profile.
     * @param p_editable -- whether the object should be editable
     * @return the file profile for the given name.
     */
    public FileProfile getFileProfileById(long p_id, boolean p_editable)
        throws FileProfileEntityException, RemoteException;

    /**
     * Validate if specified file profile is a XLZ reference file profile
     * @param p_fileProfileName
     * @return if specified file profile name is a xlz reference file profile, 
     *         return true
     */
    public boolean isXlzReferenceXlfFileProfile(String p_fileProfileName)
            throws FileProfileEntityException, RemoteException;

    //////////////////////////////////////////////////////////////////////////////
    //  END: File Profiles  //////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////////
    //  BEGIN: File Extensions  //////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    /**
    ** Creates a new FileExtension object in the data store
    ** @return the created object
    **/
    public FileExtension createFileExtension(FileExtension p_fileExtension)
    throws FileProfileEntityException, RemoteException;

    /**
     * Creates default file extensions for each company.
     * @param p_companyId Id of the company with to be created.
     */
    public void createDefaultFileExtension(String p_companyId)
        throws FileProfileEntityException, RemoteException;
    /**
    ** Reads the FileExtension object from the datastore
    ** @return the FileExtension
    **/
    public FileExtension readFileExtension(long p_id)
    throws FileProfileEntityException, RemoteException;

    /**
    ** Deletes a FileExtension from the datastore
    **/
    public void deleteFileExtension(FileExtension p_fileExtension)
    throws FileProfileEntityException, RemoteException;


    /**
    ** Update the FileExtension object in the datastore
    ** @return the updated object
    **/
    public FileExtension updateFileExtension(FileExtension p_fileExtension)
    throws FileProfileEntityException, RemoteException;

    /**
    ** Get a list of all existing FileExtension objects in the datastore
    ** @return a vector of the FileExtension objects
    **/
    public Collection getAllFileExtensions()
    throws FileProfileEntityException, RemoteException;
    
    public HashMap<Long, String> getIdViewFileExtensions()
            throws FileProfileEntityException, RemoteException;
    
    /**
     * Return the file extension that has the given id.
     * @throws FileProfileEntityException
     * @throws RemoteException
     */
    public FileExtensionImpl getFileExtension(long id)
    throws FileProfileEntityException, RemoteException;

    /**
     * Get a list of file profiles that contain at least one of the file
     * extensions named in the list.
     * The file extension ANY_EXTENSION can be passed to for file profiles
     * that can be used for any extension
     *
     * @param p_extensionNames  The file extension names as strings - not the objects.
     * @return A list of FileProfile objects or an empty list if none found.
     */
    public Collection getFileProfilesByExtension(List p_extensionNames)
        throws FileProfileEntityException, RemoteException;

    /**
     * Get a list of file profiles that contain at least one of the file
     * extensions named in the list.
     * The file extension ANY_EXTENSION can be passed to for file profiles
     * that can be used for any extension
     *
     * @param p_extensionNames  The file extension names as strings - not the objects.
     * @return A list of FileProfile objects or an empty list if none found.
     */
    public Collection getFileProfilesByExtension(List p_extensionNames, long p_companyId)
        throws FileProfileEntityException, RemoteException;

    /**
     ** Get a list of FileExtension objects in the datastore associated with
     ** the FileProfile object.
     ** @param p_fileProfile The FileProfile object.
     ** @return a vector of FileExtension objects.
     **/
    public Collection getFileExtensionsByFileProfile(FileProfile p_fileProfile)
    throws FileProfileEntityException, RemoteException;
    //////////////////////////////////////////////////////////////////////////////
    //  END: File Extensions  ////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////////
    //  BEGIN: KnownFormatTypes  /////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    /**
     ** Get a list of all KnownFormatType objects in the datastore
     ** @return a vector of KnownFormatType objects. These are immutable
     **/
    public Collection getAllKnownFormatTypes()
    throws FileProfileEntityException, RemoteException;

    /**
    ** Reads the KnownFormatType object from the datastore (not editable)
    **/
    public KnownFormatType queryKnownFormatType(long p_id)
    throws FileProfileEntityException, RemoteException;

    /**
    ** Gets the KnownFormatType object from the datastore
    **/
    public KnownFormatType getKnownFormatTypeById(long p_id, boolean p_editable)
    throws FileProfileEntityException, RemoteException;

    //////////////////////////////////////////////////////////////////////////////
    //  END: KnownFormatTypes  ///////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

}

