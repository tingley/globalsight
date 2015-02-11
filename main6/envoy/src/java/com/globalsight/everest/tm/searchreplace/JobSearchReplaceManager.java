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

import java.rmi.RemoteException;
import java.util.Collection;

import com.globalsight.everest.tm.TmManagerException;

public interface JobSearchReplaceManager
{
    public JobSearchReportQueryResult searchForJobSegments(
            boolean p_caseSensitiveSearch, String p_queryString,
            Collection<String> p_targetLocales, Collection<String> p_jobIds)
            throws TmManagerException, RemoteException;

    public ActivitySearchReportQueryResult searchForActivitySegments(
            boolean p_caseSensitiveSearch, String p_queryString,
            Collection<String> p_targetLocales, Collection<String> p_jobId)
            throws TmManagerException, RemoteException;

    public Collection<JobInfo> replaceForPreview(String p_old, String p_new,
            Collection<JobInfo> p_jobInfos, boolean p_caseSensitiveSearch)
            throws TmManagerException, RemoteException;

    public void replace(Collection<TuvInfo> p_tuvs) throws TmManagerException,
            RemoteException;
}
