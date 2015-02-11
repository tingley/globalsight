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
package com.globalsight.everest.projecthandler;

import com.globalsight.everest.util.system.RemoteServer;

import java.rmi.RemoteException;

/**
 * This class represents the remote implementation of an observer of
 * events that affect Projects.
 */
public class ProjectEventObserverWLRMIImpl
    extends RemoteServer
    implements ProjectEventObserverWLRemote
{
    // an instance of the ProjectEventObserverLocal
    private ProjectEventObserver m_localInstance = null;

    //
    // Constructor
    //

    /**
     * Construct a remote Project event observer.
     *
     * @param p_localInstance The local instance of the project event observer
     * @exception java.rmi.RemoteException Network related exception.
     */
    public ProjectEventObserverWLRMIImpl()
        throws RemoteException
    {
        super(ProjectEventObserver.SERVICE_NAME);

        m_localInstance = new ProjectEventObserverLocal();
    }

    //
    // ProjectEventObserver Implementation
    //

    /**
     * @see ProjectEventObserver#notifyTermbaseRenamed(String,String)
     */
    public void notifyTermbaseRenamed(String p_oldName, String p_newName)
        throws RemoteException
    {
        m_localInstance.notifyTermbaseRenamed(p_oldName, p_newName);
    }

    /**
     * @see ProjectEventObserver#notifyTermbaseDeleted(String,String)
     */
    public void notifyTermbaseDeleted(String p_name)
        throws RemoteException
    {
        m_localInstance.notifyTermbaseDeleted(p_name);
    }
}
