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

package com.globalsight.everest.tm;

import com.globalsight.everest.tm.TmManagerException;

import com.globalsight.everest.tm.searchreplace.SearchReplaceManager;
import com.globalsight.everest.tm.searchreplace.JobSearchReplaceManager;

import java.util.ArrayList;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The TmManager interface provides management functions of Tm
 * objects.
 */
public interface TmManager extends Remote
{
    /**
     * Returns a manager for basic search and replace TM maintenance.
     *
     * @param p_tmNames list of tm names (String)
     */
    SearchReplaceManager getSearchReplacer(ArrayList p_tmNames)
        throws RemoteException, TmManagerException;

    JobSearchReplaceManager getJobSearchReplaceManager()
        throws RemoteException, TmManagerException;
}
