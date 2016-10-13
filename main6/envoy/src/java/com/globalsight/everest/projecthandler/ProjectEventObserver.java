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

import java.rmi.RemoteException;

/**
 * This class represents an observer of events that affect Page.
 * The callers notify the observer of an event that could
 * have an affect on the state of the page.
 */
public interface ProjectEventObserver
{
    /**
     * The name bound to the remote object.
     */
    public static final String SERVICE_NAME = "ProjectEventObserverServer";

    /**
     * Notification that a termbase has changed its name.
     *
     * @param p_oldName old name of the termbase
     * @param p_newName new name of the termbase
     */
    public void notifyTermbaseRenamed(String p_oldName, String p_newName)
        throws RemoteException;

    /**
     * Notification that a termbase was deleted.
     *
     * @param p_name name of the deleted termbase.
     */
    public void notifyTermbaseDeleted(String p_name)
        throws RemoteException;
}
