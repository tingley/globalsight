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
package com.globalsight.everest.tm.searchreplace;

import com.globalsight.everest.tm.TmManagerException;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.GeneralException;

import com.globalsight.util.progress.IProcessStatusListener;
import com.globalsight.util.progress.ProcessStatus;
import com.globalsight.ling.util.GlobalSightCrc;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;


public interface SearchReplaceManager
{
    public void search(String p_queryString,
        GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale,
        boolean p_caseSensitiveSearch, Map<Long, Integer> mapOfTmIdIndex)
        throws TmManagerException, RemoteException;

    public TmConcordanceResult searchIt(String p_queryString,
        GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale,
        boolean p_caseSensitiveSearch)
        throws TmManagerException, RemoteException;

    public ArrayList replace(String p_old, String p_new,
        ArrayList p_tuvs, boolean p_caseSensitiveSearch)
        throws GeneralException, RemoteException;
    
    public ArrayList replace(String p_old, String p_new,
        ArrayList p_tuvs, boolean p_caseSensitiveSearch, String userId)
        throws GeneralException, RemoteException;

    /** Attach Listener */
    public void attachListener(IProcessStatusListener p_listener);

    /** Detach Listener */
    public void detachListener(IProcessStatusListener p_listener);
}
