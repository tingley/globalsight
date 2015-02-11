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

package com.globalsight.everest.nativefilestore;

// globalsight
import com.globalsight.everest.page.UnextractedFile;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.util.system.RemoteServer;


// java
import java.rmi.RemoteException;
import java.io.File;

public class NativeFileManagerWLRMIImpl extends RemoteServer 
    implements NativeFileManagerWLRemote
{
    private NativeFileManager m_localInstance;
      
    public NativeFileManagerWLRMIImpl() throws RemoteException, NativeFileManagerException
    {
        super(NativeFileManager.SERVICE_NAME);
        m_localInstance = new NativeFileManagerLocal();
    }

    // See interface for documentation 
    public void save(SecondaryTargetFile p_stf, File p_tmpFile, 
        User p_user, String p_newFilename) 
        throws NativeFileManagerException
    {
        m_localInstance.save(p_stf, p_tmpFile, p_user, p_newFilename);
    }

    /**
     * @see NativeFileManager.save(UnextractedFile, byte[])
     */
    public void save(UnextractedFile p_unextractedFile, File p_tmpFile, User p_user) 
        throws NativeFileManagerException
    {
        m_localInstance.save(p_unextractedFile, p_tmpFile, p_user);
    }



    public String save(String p_fileContent, String p_encoding, String p_relPath) throws NativeFileManagerException
    {
        return m_localInstance.save(p_fileContent, p_encoding, p_relPath);
    }

    public File getFile(String p_relPath)  throws NativeFileManagerException
    {
        return m_localInstance.getFile(p_relPath);
    }

    public byte[] getBytes(String p_relPath) throws NativeFileManagerException
    {
        return m_localInstance.getBytes(p_relPath);
    }
    
    public String getString(String p_relPath, String p_encoding) throws NativeFileManagerException
    {
        return m_localInstance.getString(p_relPath,p_encoding);
    }

    /**
     *@See NativeFileManager.getFile(SecondaryTargetFile)
     */
    public File getFile(SecondaryTargetFile p_stf) 
        throws NativeFileManagerException
    {
        return m_localInstance.getFile(p_stf);        
    }
    
    /**
     *@See NativeFileManager.getFile(SecondaryTargetFile)
     */
    public File getFile(SecondaryTargetFile p_stf, String companyId) 
        throws NativeFileManagerException
    {
        return m_localInstance.getFile(p_stf, companyId);        
    }
    
    /**
     *@See NativeFileManager.getFile(UnextractedFile)
     */
    public File getFile(UnextractedFile p_unextractedFile) 
        throws NativeFileManagerException
    {
        return m_localInstance.getFile(p_unextractedFile);
    }

    /** 
     * @see NativeFileManager.moveFileToStorage(String, SecondaryTargetFile )
     */ 
    public SecondaryTargetFile moveFileToStorage(String p_absolutePath, SecondaryTargetFile p_stf, int p_sourcePageBomType)
        throws NativeFileManagerException
    {
        return m_localInstance.moveFileToStorage(p_absolutePath, p_stf, p_sourcePageBomType);
    }
 
    /** 
     * @see NativeFileManager.copyFileToStorage(String, UnextractedFile, boolean)
     */
    public UnextractedFile copyFileToStorage(String p_absolutePath,
                                             UnextractedFile p_unextractedFile,
                                             boolean p_removeOriginal)
        throws NativeFileManagerException
    {
        return m_localInstance.copyFileToStorage(p_absolutePath, p_unextractedFile,
                                                 p_removeOriginal);
    }  

    /** 
     * @see NativeFileManager.copyFileToStorage(String, String, boolean)
     */  
    public String copyFileToStorage(String p_absolutePathToOriginalFile,
                                    String p_relPathToNewFile,
                                    boolean p_removeOriginal)
    throws NativeFileManagerException
    {
        return m_localInstance.copyFileToStorage(p_absolutePathToOriginalFile,
                                                 p_relPathToNewFile,
                                                 p_removeOriginal);
    }
}

