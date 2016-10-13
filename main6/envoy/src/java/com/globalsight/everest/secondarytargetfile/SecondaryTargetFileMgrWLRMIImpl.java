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
package com.globalsight.everest.secondarytargetfile;

import com.globalsight.everest.util.system.RemoteServer;


import java.rmi.RemoteException;


/**
 * This class represents the remote implementation of a stf manager.  
 * Note that all of the methods of this class throw the following exceptions:
 *  1. SecondaryTargetFileException - For page related errors.
 *  2. RemoteException - For network related exception.
 *
 */
public class SecondaryTargetFileMgrWLRMIImpl
    extends RemoteServer
    implements SecondaryTargetFileMgrWLRemote
{
    SecondaryTargetFileMgr m_localInstance = null;;

    //
    //  Begin: Constructor
    //

    /**
     * Construct a remote Page Manager.
     *
     * @exception java.rmi.RemoteException Network related exception.
     */
    public SecondaryTargetFileMgrWLRMIImpl() 
        throws RemoteException, SecondaryTargetFileException
    {
        super(SecondaryTargetFileMgr.SERVICE_NAME);
        m_localInstance = new SecondaryTargetFileMgrLocal();
    }

    //
    //  Begin: SecondaryTargetFileMgr Implementation
    //

    /**
     * @see SecondaryTargetFileMgr.createSecondaryTargetFile(SecondaryTargetFile)
     */
    public void createSecondaryTargetFile(String p_absolutePath, 
                                          String p_relativePath,
                                          int p_sourcePageBomType,
                                          String p_eventFlowXml,
                                          long p_exportBatchId)
        throws SecondaryTargetFileException, RemoteException
    {
        m_localInstance.createSecondaryTargetFile(p_absolutePath,
                                                  p_relativePath,
                                                  p_sourcePageBomType,
                                                  p_eventFlowXml,
                                                  p_exportBatchId);
    }

    /**
     * @see SecondaryTargetFileMgr.failedToCreateSecondaryTargetFile(long)
     */
    public void failedToCreateSecondaryTargetFile(long p_exportBatchId)
        throws SecondaryTargetFileException, RemoteException
    {
        m_localInstance.failedToCreateSecondaryTargetFile(p_exportBatchId);
    }

    /**
     * @see SecondaryTargetFileMgr.getSecondaryTargetFile(long)
     */
    public SecondaryTargetFile getSecondaryTargetFile(long p_stfId)
        throws SecondaryTargetFileException, RemoteException
    {
        return m_localInstance.getSecondaryTargetFile(p_stfId);
    }

    /**
     * @see SecondaryTargetFileMgr.notifyExportFailEvent(p_stfId);
     */
    public void notifyExportFailEvent(Long p_stfId)
        throws SecondaryTargetFileException, RemoteException
    {
        m_localInstance.notifyExportFailEvent(p_stfId);
    }

    /**
     * @see SecondaryTargetFile.notifyExportSuccessEvent(Long)
     */
    public void notifyExportSuccessEvent(Long p_stfId)
        throws SecondaryTargetFileException, RemoteException
    {
        m_localInstance.notifyExportSuccessEvent(p_stfId);
    }

    /**
     * @see SecondaryTargetFileMgr.removeSecondaryTargetFile(SecondaryTargetFile)
     */
    public void removeSecondaryTargetFile(SecondaryTargetFile p_stf)
        throws SecondaryTargetFileException, RemoteException
    {        
        m_localInstance.removeSecondaryTargetFile(p_stf);
    }

    /**
     * @see SecondaryTargetFileMgr.updateSecondaryTargetFile(SecondaryTargetFile)
     */
    public void updateSecondaryTargetFile(SecondaryTargetFile p_stf)
        throws SecondaryTargetFileException, RemoteException
    {
        m_localInstance.updateSecondaryTargetFile(p_stf);
    }    

    /**
     * @see SecondaryTargetFileMgr.updateState(SecondaryTargetFile, String)
     */
    public SecondaryTargetFile updateState(Long p_stfId, String p_state)
        throws SecondaryTargetFileException, RemoteException
    {
        return m_localInstance.updateState(p_stfId, p_state);
    }
}
