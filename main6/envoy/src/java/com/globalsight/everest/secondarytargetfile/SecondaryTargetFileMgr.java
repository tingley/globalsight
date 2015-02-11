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



import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import java.rmi.RemoteException;

/**
 * The SecondaryTargetFileMgr interface is intended to provide management of 
 * SecondaryTargetFile objects.  Management include create, read, update, and delete.
 */
public interface SecondaryTargetFileMgr
{
    /**
     * The name bound to the remote object.
     */
    public static final String SERVICE_NAME = "StfServer";


    /**
     * Create a new secondary target file.
     * @param p_absolutePath - The absolute path of the file to be read for
     * the creation of STF.
     * @param p_relativePath - The file name including relative path.
     * @param p_eventFlowXml - The event flow xml for this file.
     * @param p_exportBatchId - The export batch id that determines which pages
     * were exported for the creation of this STF.
     *
     * @exception SecondaryTargetFileException when an error occurs.
     */
    void createSecondaryTargetFile(String p_absolutePath, 
                                   String p_relativePath,
                                   int p_sourcePageBomType,
                                   String p_eventFlowXml,
                                   long p_exportBatchId)
        throws SecondaryTargetFileException, RemoteException;

    /**
     * Failed to create the secondary target files for the given
     * export batch id.
     *
     * @param p_exportBatchId - The export batch id that determines which 
     * task caused the creation of this STF.  This is needed to update the
     * creation state of stf for a task.
     *
     * @exception SecondaryTargetFileException when an error occurs.
     */
    void failedToCreateSecondaryTargetFile(long p_exportBatchId)
        throws SecondaryTargetFileException, RemoteException;

    /**
     * Get the secondary target file based on the given id.
     * @param p_stfId - The secondary target file id.
     *
     * @return The secondary target file for the given id, or null
     * if the file does not exist.
     *
     * @exception SecondaryTargetFileException when an error occurs.
     */
    SecondaryTargetFile getSecondaryTargetFile(long p_stfId)
        throws SecondaryTargetFileException, RemoteException;

    /**
     * Notify the interested parties about the export failure.  Basically,
     * the state of the secondary target file will be updated to
     * 'EXPORT_FAIL' along with the workflow and job that it belongs to.
     *
     * @param p_stfId - The id of the secondary target file to be 
     * updated to 'EXPORT_FAIL' state.
     *
     * @exception SecondaryTargetFileException when an error occurs.
     */
    void notifyExportFailEvent(Long p_stfId)
        throws SecondaryTargetFileException, RemoteException;

    /**
     * Notify the interested parties about the export success.  Basically,
     * the state of the secondary target file and the workflow (if all 
     * secondary target files have been exported) will be updated to
     * 'EXPORTED'.
     *
     * @param p_stfId - The id of the secondary target file to be 
     * updated to 'EXPORTED' state.
     *
     * @exception SecondaryTargetFileException when an error occurs.
     */
    void notifyExportSuccessEvent(Long p_stfId)
        throws SecondaryTargetFileException, RemoteException;

    /**
     * Remove the given secondary target file.  Note that this is not
     * a physical remove.
     * param p_stf - The secondary target file to be removed.
     *
     * @exception SecondaryTargetFileException when an error occurs.
     */
    void removeSecondaryTargetFile(SecondaryTargetFile p_stf)
        throws SecondaryTargetFileException, RemoteException;

    /**
     * Update the given secondary target file.
     * @param p_stf - The secondary target file to be updated.
     *
     * @exception SecondaryTargetFileException when an error occurs.
     */
    void updateSecondaryTargetFile(SecondaryTargetFile p_stf)
        throws SecondaryTargetFileException, RemoteException;

    /**
     * Update the state of the secondary target file to the given state.
     *
     * @param p_stfId - The id of the secondary target file to be 
     * updated with the new state.
     * @param p_state - The new state to be set.  Note that only 
     * 'ACTIVE_JOB', 'LOCALIZED', 'OUT_OF_DATE', 'EXPORTED', 
     * 'EXPORT_IN_PROGRESS', and 'EXPORT_FAIL' are valid states.
     *
     * @return The updated secondary target file.
     *
     * @exception SecondaryTargetFileException when an error occurs.
     */
    SecondaryTargetFile updateState(Long p_stfId, String p_state)
        throws SecondaryTargetFileException, RemoteException;
}
