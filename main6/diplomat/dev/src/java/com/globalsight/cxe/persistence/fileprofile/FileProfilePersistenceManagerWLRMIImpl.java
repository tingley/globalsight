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

import com.globalsight.cxe.entity.fileextension.FileExtensionImpl;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.everest.util.system.RemoteServer;

public class FileProfilePersistenceManagerWLRMIImpl 
    extends RemoteServer implements FileProfilePersistenceManagerWLRemote
{
    FileProfilePersistenceManager m_localReference;

    public FileProfilePersistenceManagerWLRMIImpl() 
        throws java.rmi.RemoteException, FileProfileEntityException
    {
        super(FileProfilePersistenceManager.SERVICE_NAME);
        m_localReference = new FileProfilePersistenceManagerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    //////////////////////////////////////////////////////////////////////////////
    //  BEGIN: File Profiles  ////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    public  com.globalsight.cxe.entity.fileprofile.FileProfile createFileProfile(com.globalsight.cxe.entity.fileprofile.FileProfile param1) throws com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException,java.rmi.RemoteException
    {
        return m_localReference.createFileProfile(param1);
    }

    public  void deleteFileProfile(com.globalsight.cxe.entity.fileprofile.FileProfile param1) throws com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException,java.rmi.RemoteException
    {
        m_localReference.deleteFileProfile(param1);
    }

    public Collection getAllFileProfiles() throws com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException, java.rmi.RemoteException
    {
        return m_localReference.getAllFileProfiles();
    }

    public Collection getFileProfilesByExtension(List p_extensionNames)
        throws com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException, java.rmi.RemoteException
    {
        return m_localReference.getFileProfilesByExtension(p_extensionNames);
    }
    
    public Collection getFileProfilesByExtension(List p_extensionNames,
            long p_companyId)
            throws com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException,
            java.rmi.RemoteException
    {
        return m_localReference.getFileProfilesByExtension(p_extensionNames,
                p_companyId);
    }

    public  com.globalsight.cxe.entity.fileprofile.FileProfile getFileProfileById(long p_id, boolean p_editable) throws com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException,java.rmi.RemoteException
    {
        return m_localReference.getFileProfileById(p_id,p_editable);
    }

    public  com.globalsight.cxe.entity.fileprofile.FileProfile readFileProfile(long param1) throws com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException,java.rmi.RemoteException
    {
        return m_localReference.readFileProfile(param1);
    }

    public  com.globalsight.cxe.entity.fileprofile.FileProfile updateFileProfile(com.globalsight.cxe.entity.fileprofile.FileProfile param1) throws com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException,java.rmi.RemoteException
    {
        return m_localReference.updateFileProfile(param1);
    }

    /**
     * Return the file profile id for the given name from the database
     * The returned object is in a state that does not allow editing.
     *
     * @param p_name - The name of the file profile.
     * @return the file profile id for the given name.
     */
    public long getFileProfileIdByName(String p_name)
        throws FileProfileEntityException, java.rmi.RemoteException
    {
        return m_localReference.getFileProfileIdByName(p_name);
    }

    /**
     * Return the file profile for the given name from the database
     * The returned object is in a state that does not allow editing.
     *
     * @param p_name - The name of the file profile.
     * @return the file profile for the given name.
     */
    public FileProfile getFileProfileByName(String p_name)
        throws FileProfileEntityException, java.rmi.RemoteException
    {
        return m_localReference.getFileProfileByName(p_name);
    }

    public FileProfile getFileProfileByName(String p_name, boolean p_isActive)
            throws FileProfileEntityException, java.rmi.RemoteException
    {
        return m_localReference.getFileProfileByName(p_name, p_isActive);
    }
    
    public boolean isXlzReferenceXlfFileProfile(String p_fileProfileName)
            throws FileProfileEntityException, java.rmi.RemoteException
    {
        return m_localReference.isXlzReferenceXlfFileProfile(p_fileProfileName);
    }

    //////////////////////////////////////////////////////////////////////////////
    //  END: File Profiles  //////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////////
    //  BEGIN: File Extensions  //////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    public com.globalsight.cxe.entity.fileextension.FileExtension createFileExtension(com.globalsight.cxe.entity.fileextension.FileExtension param1) throws com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException,java.rmi.RemoteException
    {
        return m_localReference.createFileExtension(param1);
    }
    
    /**
     * Creates default file extensions for each company.
     * @param p_companyId Id of the company with to be created.
     */
    public void createDefaultFileExtension(String p_companyId) throws FileProfileEntityException, RemoteException
    {
        m_localReference.createDefaultFileExtension(p_companyId);
    }

    public  void deleteFileExtension(com.globalsight.cxe.entity.fileextension.FileExtension param1) throws com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException,java.rmi.RemoteException
    {
        m_localReference.deleteFileExtension(param1);
    }

    public Collection getAllFileExtensions() throws com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException, java.rmi.RemoteException
    {
        return m_localReference.getAllFileExtensions();
    }

    public  com.globalsight.cxe.entity.fileextension.FileExtension readFileExtension(long param1) throws com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException,java.rmi.RemoteException
    {
        return m_localReference.readFileExtension(param1);
    }

    public com.globalsight.cxe.entity.fileextension.FileExtension updateFileExtension(com.globalsight.cxe.entity.fileextension.FileExtension param1) throws com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException,java.rmi.RemoteException
    {
        return m_localReference.updateFileExtension(param1);
    }

    public java.util.Collection getFileExtensionsByFileProfile(com.globalsight.cxe.entity.fileprofile.FileProfile param1) throws com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException, java.rmi.RemoteException
    {
        return m_localReference.getFileExtensionsByFileProfile(param1);
    }
    //////////////////////////////////////////////////////////////////////////////
    //  END: File Extensions  ////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

        //////////////////////////////////////////////////////////////////////////////
    //  BEGIN: KnownFormatTypes  /////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    public Collection getAllKnownFormatTypes() throws com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException, java.rmi.RemoteException
    {
        return m_localReference.getAllKnownFormatTypes();
    }

    public KnownFormatType queryKnownFormatType(long p_id) throws com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException, java.rmi.RemoteException
     {
	 return m_localReference.queryKnownFormatType(p_id);
     }

    public KnownFormatType getKnownFormatTypeById(long p_id,boolean p_editable) throws com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException, java.rmi.RemoteException
     {
	 return m_localReference.getKnownFormatTypeById(p_id,p_editable);
     }

	public FileExtensionImpl getFileExtension(long id)
			throws FileProfileEntityException, RemoteException
	{
		return m_localReference.getFileExtension(id);
	}
	public HashMap<Long, String> getIdViewFileExtensions()
            throws FileProfileEntityException, RemoteException{
        return m_localReference.getIdViewFileExtensions();
        
    }

    @Override
    public Collection getAllFileProfilesByCondition(String condtion)
            throws FileProfileEntityException, RemoteException
    {
        // TODO Auto-generated method stub
        return m_localReference.getAllFileProfilesByCondition(condtion);
    }

    //////////////////////////////////////////////////////////////////////////////
    //  END: KnownFormatTypes  ///////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
}
