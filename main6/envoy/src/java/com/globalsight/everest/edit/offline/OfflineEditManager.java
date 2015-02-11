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
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.util.progress.IProcessStatusListener;

import java.rmi.RemoteException;
import java.io.File;

/**
 * The OfflineEditManager interface is intended to manage the download
 * and upload of offline files for translation.  Management includes
 * the creation of download packages and/or indivudual files, parsing
 * and error-checking of uploaded files.
 */
public interface OfflineEditManager
{
    /** The remote service name used to retrieve this object. */
    static public final String SERVICE_NAME = "OfflineEditManager";

    /** Creates a separate new instance. */
    public OfflineEditManager newInstance()
        throws OfflineEditorManagerException, RemoteException;

    /**
     * Creates download files driven by the download parameters.
     *
     * For instance, the type of download (package vs. pages) and the
     * file format (Text, Rtf, or Rtf for Trados ) are all determined
     * by the download parameters.
     *
     * A package download includes the leveraged file(s) to be
     * translated and the associated resource pages. A page download
     * creates a single leveraged file to be translated in TXT format.
     *
     * The location of the output files is read from DownloadParams.
     *
     * @param DownloadParams - all parameters needed to execute the download.
     *
     * @exception OfflineEditorManagerException - Component related exception.
     * @exception java.rmi.RemoteException Network related exception.
     */
    public void processDownloadRequest(DownloadParams p_params)
        throws OfflineEditorManagerException, RemoteException;

    /**
     * Error checks and otherwise processes an extracted file uploaded
     * in Offline text format (list view).
     *
     * @param p_tempFile the upload file in a temporary location (on
     * the server now).
     * @param p_user the user object used for getting email and locale info.
     * Error messages will be reported in the user's default locale.
     * @param p_task - the task we are uploading into.
     * @param p_fileName - The name of the file to be uploaded.  Used
     * only for email notification.
     *
     * @exception AmbassadorDwUpException - Component related exception.
     * @exception java.rmi.RemoteException Network related exception.
     */
    public void processUploadPage(File p_tempFile,
        User p_user, Task p_task, String p_fileName)
        throws AmbassadorDwUpException, RemoteException;
    
    /**
     * Error checks and otherwise processes offline Report .
     * report should be Translation Edit Report or Language
     * Sign Off report and in excel format.
     * @param p_tempFile the upload file in a temporary location (on
     * the server now).
     * @param p_user the user object used for getting email and locale info.
     * Error messages will be reported in the user's default locale.
     * @param p_task - the task we are uploading into.
     * @param p_fileName - The name of the file to be uploaded.  Used
     * only for email notification.
     * @param p_reportName - the report name to be uploaded
     * @exception AmbassadorDwUpException - Component related exception.
     * @exception java.rmi.RemoteException Network related exception.
     */
    public void processUploadReportPage(File p_tempFile,
        User p_user, Task p_task, String p_fileName, String p_reportName)
        throws AmbassadorDwUpException, RemoteException;

    /**
     * Attaches a ProcessStatus event listener to this object for
     * asynchronously updating a UI.
     */
    public void attachListener(IProcessStatusListener p_listener);

    /**
     * Detaches the UI's event listener.
     */
    public void detachListener();

    public void runProcessDownloadRequest(DownloadParams downloadParams)
            throws OfflineEditorManagerException, RemoteException;

    public String runProcessUploadReportPage(File p_tmpFile, User p_user,
            Task p_task, String p_fileName, String p_reportName)
            throws AmbassadorDwUpException;

    public String runProcessUploadPage(File p_tmpFile, User p_user, Task p_task,
            String p_fileName) throws AmbassadorDwUpException;

    /**
     * Get user's default download options ("My Account" >> "Download Options").
     * 
     * @param p_userId
     * @param p_task
     * @return DownloadParams
     * @throws OfflineEditorManagerException
     */
    public DownloadParams getDownloadParamsByUser(String p_userId, Task p_task)
            throws OfflineEditorManagerException;

    /**
     * Get offline translation kit file in ZIP according to download params.
     * 
     * @param p_userId
     * @param p_taskId
     * @param p_downloadParams
     * @return File
     * @throws OfflineEditorManagerException
     */
    public File getDownloadOfflineFiles(String p_userId, Long p_taskId,
            DownloadParams p_downloadParams)
            throws OfflineEditorManagerException;
}
