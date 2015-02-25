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

package com.globalsight.everest.edit;

import com.globalsight.everest.edit.SynchronizationStatus;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.*;

/**
 * <p>The RMI interface for allowing the offline and online editors to
 * synchronize offline uploads.</p>
 */
public interface SynchronizationManager
    extends Remote
{
    static final String UNKNOWN  = "UNKNOWN";
    static final String UPLOAD_STARTED  = "UPLOAD_STARTED";
    static final String UPLOAD_FINISHED = "UPLOAD_FINISHED";
    static final String GXMLUPDATE_STARTED = "GXMLUPDATE_STARTED";
    static final String GXMLUPDATE_FINISHED = "GXMLUPDATE_FINISHED";

    /**
     * Marks if a particular target page is going to be uploaded and
     * the database is about to be modified. This allows the online
     * editor to notify the user of pending changes.
     */
    void uploadStarted(Long p_pageId)
        throws RemoteException;

    /**
     * Marks if a particular target page has been fully uploaded and
     * the database update is finished. This allows the online editor
     * to notify the user to reload the editor.
     */
    void uploadFinished(Long p_pageId)
        throws RemoteException;

    /**
     * Marks if the GXML editing of a target page's source page has
     * started.
     */
    void gxmlUpdateStarted(Long p_pageId)
        throws RemoteException;

    /**
     * Marks if the GXML editing of a target page's source page has
     * been completed.
     */
    void gxmlUpdateFinished(Long p_pageId)
        throws RemoteException;

    /**
     * Marks if a particular target page has been fully uploaded and
     * the database update is finished. This allows the online editor
     * to notify the user to reload the editor.
     */
    SynchronizationStatus getStatus(Long p_pageId)
        throws RemoteException;

	boolean checkTempFileName(String pTempFileName);
	
	void setTempFileName(String p_tempFileName);
}
