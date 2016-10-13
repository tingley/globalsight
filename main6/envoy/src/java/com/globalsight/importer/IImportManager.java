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

package com.globalsight.importer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import com.globalsight.util.progress.IProcessStatusListener;


/**
 * <p>The RMI interface for a basic importer.</p>
 */
public interface IImportManager
    extends Remote
{
    /**
     * Attaches an import event listener.  Currently, only a single
     * listener is supported.
     */
    public void attachListener(IProcessStatusListener p_listener)
        throws RemoteException;

    /**
     * Detaches an import event listener.  Currently, only a single
     * listener is supported.
     */
    public void detachListener(IProcessStatusListener p_listener)
        throws RemoteException;

    /**
     * Sets the import options that guide the import process.
     * @param options an XML string.
     */
    public void setImportOptions(String options)
        throws ImporterException, RemoteException;

    /**
     * Returns ImportOptions as XML string.
     */
    public String getImportOptions()
        throws ImporterException, RemoteException;

    /**
     * Sets the name of the file to be imported.
     */
    public void setImportFile(String filename, boolean deleteAfterImport)
        throws ImporterException, RemoteException;

    /**
     * Validates the file format sent in.
     */
    public String analyzeFile()
        throws ImporterException, RemoteException;

    /**
     * With all ImportOptions set, run a test import.  During test
     * import, a registered IProcessStatusListener receives status events and
     * error messages.
     */
    public void doTestImport()
        throws ImporterException, RemoteException;

    /**
     * With all ImportOptions set, start the actual import.  During
     * import, a registered IProcessStatusListener receives status events and
     * error messages.
     */
    public void doImport()
        throws ImporterException, RemoteException;

}

