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

package com.globalsight.util.progress;

import java.io.IOException;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * <p>The RMI interface for a Process Status Listener with 2 progress
 * bars.</p>
 *
 * The first bar is for top-level objects (file to be processed,
 * indexes to be created), and the second for progress within one of
 * the objects (a single file, a single index).
 *
 * <p>This listener provides methods for the second progress bar.</p>
 *
 * @see IProcessStatusListener.
 */
public interface IProcessStatusListener2
    extends IProcessStatusListener
{
    /**
     * Updates the second progress bar with entry count,
     * percentage-complete and error/log message.
     *
     * @param p_description a description of the top-level object
     * @param p_message a message string or null for no message.
     *
     * @throws IOException when the background process should be
     * interrupted.
     */
    void listen2(String description, int percentage, String message)
        throws RemoteException, IOException;

    void setDone()
        throws RemoteException;
}
