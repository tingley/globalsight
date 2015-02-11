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

package com.globalsight.everest.edit.offline;


import com.globalsight.everest.foundation.User;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.download.DownloadParams;
import com.globalsight.everest.edit.offline.OfflineEditManager;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.util.system.SystemStartupException;
import com.globalsight.everest.util.system.SystemShutdownException;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.util.progress.IProcessStatusListener;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import java.rmi.RemoteException;
import java.io.IOException;
import java.io.File;

/**
 * OfflineEditManagerWLRMIImpl is the remote implementation of
 * OfflineEditManagerLocal.
 */
public final class OfflineEditManagerWLImpl
    extends RemoteServer
    implements OfflineEditManagerWLRemote
{
    private OfflineEditManager m_localInstance = null;

    /** Constructor. */
    public OfflineEditManagerWLImpl()
        throws RemoteException, OfflineEditorManagerException
    {
        super(OfflineEditManager.SERVICE_NAME);
        m_localInstance = new OfflineEditManagerLocal();
    }

    public OfflineEditManagerWLImpl(OfflineEditManager p_lm)
        throws RemoteException
    {
        super();
        m_localInstance = p_lm;
    }

    /**
     * Bind the remote server to the ServerRegistry.
     * @throws SystemStartupException when a NamingException or other
     * Exception occurs.
     */
    public void init() throws SystemStartupException
    {
        super.init();
    }

    /**
     * Creates download files based on the download parameters.
     * The type of download (package vs. pages) and the file format (Rtf,
     * RtfForTrados, Text ) is determined by the combination of download
     * parameters.
     *
     * A task oriented (or packaged) download includes the leveraged file(s) to
     * be translated and the associated resource pages. A single page download
     * creates a single leveraged file to be translated in TXT format.
     *
     * The location of the output files is read from DownloadParams.
     * @param DownloadParams - all the parameters need to execute the download.
     *
     * @exception OfflineEditManagerException - Component related exception.
     * @exception java.rmi.RemoteException Network related exception.
     */
    public void processDownloadRequest(DownloadParams p_params)
        throws OfflineEditorManagerException, RemoteException
    {
        m_localInstance.processDownloadRequest(p_params);
    }

    /**
     * Error checks and otherwise processes an extracted file uploaded
     * in Offline text format (list view).
     *
     * @param p_inputStream a stream opened on the upload file
     * @param p_user the user object used for getting email and locale info.
     * Error messages will be reported in the user's default locale.
     * @param p_task - the task.
     * @param p_fileName - The name of the file to be uploaded.  Used
     * only for email notification.
     *
     * @exception AmbassadorDwUpException - Component related exception.
     * @exception java.rmi.RemoteException Network related exception.
     */
    public void processUploadPage(File p_tmpFile, String p_sessionId,
        User p_user, Task p_task, String p_fileName)
        throws AmbassadorDwUpException, RemoteException
    {
        m_localInstance.processUploadPage( p_tmpFile, p_sessionId,
            p_user, p_task, p_fileName);
    }
    
    /**
     * Error checks and otherwise processes an extracted file uploaded
     * in Offline text format (list view).
     * 
     * @param p_tmpFile
     * @param p_user
     * @param p_task
     * @param p_fileName
     * @param p_excludedTus
     * 
     * @throws AmbassadorDwUpException
     * @throws RemoteException
     */
    public void processUploadPage(final File p_tmpFile,
            final User p_user, final Task p_task,
            final String p_fileName)
        throws AmbassadorDwUpException, RemoteException
    {
    	 m_localInstance.processUploadPage(p_tmpFile, p_user, p_task, p_fileName);
    }
    
    public void processUploadReportPage(File p_tmpFile, String p_sessionId,
            User p_user, Task p_task, String p_fileName, String p_reportName)
            throws AmbassadorDwUpException, RemoteException
    {
        m_localInstance.processUploadReportPage( p_tmpFile, p_sessionId,
            p_user, p_task, p_fileName, p_reportName);
    }

    /**
     * Get the reference to the local implementation of the server.
     *
     * @return The reference to the local implementation of the server.
     */
    public Object getLocalReference()
    {
        return m_localInstance;
    }

    /**
     * Attaches a ProcessStatus event listener to this object for
     * asynchronously updating a UI.
     */
    public void attachListener(IProcessStatusListener p_listener)
    {
        m_localInstance.attachListener(p_listener);
    }

    /**
     * Detaches the UI's event listener.
     */
    public void detachListener()
    {
        m_localInstance.detachListener();
    }

    public OfflineEditManager newInstance()
        throws OfflineEditorManagerException, RemoteException
    {
        return m_localInstance.newInstance();
    }

	@Override
	public void runProcessDownloadRequest(DownloadParams downloadParams)
			throws OfflineEditorManagerException, RemoteException {
		m_localInstance.runProcessDownloadRequest(downloadParams);
	}
}
