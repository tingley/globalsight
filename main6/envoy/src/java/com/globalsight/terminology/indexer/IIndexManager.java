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

package com.globalsight.terminology.indexer;

import com.globalsight.util.progress.IProcessStatusListener2;
import com.globalsight.terminology.Termbase;
import com.globalsight.util.SessionInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.*;
import java.io.IOException;

/**
 * <p>The RMI interface implementation for the Terminology Indexer.</p>
 *
 * <p>Re-indexing a termbase is implemented as a reader that reads all
 * entries, and a index writer that updates all indexes.</p>
 */
public interface IIndexManager
    extends Remote
{
    /**
     * Attaches an event listener for 2 progress bars.
     */
    public void attachListener(IProcessStatusListener2 p_listener)
        throws RemoteException;

    /**
     * Detaches an event listener.
     */
    public void detachListener(IProcessStatusListener2 p_listener)
        throws RemoteException;

    /**
     * Indexes all entries in a termbase based on the indexes defined
     * in the termbase's definition.
     */
    public void doIndex()
        throws RemoteException;
}
