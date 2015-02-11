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
package com.globalsight.ling.tm;

import java.util.List;
import java.util.Collection;

import java.rmi.RemoteException;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm.fuzzy.FuzzyIndexManagerException;

/**
 * The Indexer is responsible for taking a Leveraged Group and
 * generating exact match keys and fuzzy index.<p>
 *
 * Uses Cases:
 * 1.    Index an entire page on import.
 * 2.    Index translated content after completion of a workflow.
 */
public interface Indexer
{
    /**
     * Index an entire page for a single locale.
     *
     * @param p_leverageGroupIds - The group id (page) or group ids
     * (db records) that we need to index.
     * @param p_locale - locale of the Tuv(s) we need to index.
     * @param p_tmId - Id of the TM we add the indexed content to.
     * @param p_services - used to get reference to the TuvLingManager
     * and TmFuzzyIndexManager.
     */
    /*
    void index(List p_leverageGroupIds, GlobalSightLocale p_locale,
        long p_tmId, Collection p_excludeTypes)
        throws RemoteException,
               FuzzyIndexManagerException,
               LingManagerException;
    */
    /**
     * Index a single Tuv.
     *
     * @param p_tuv - the TUV we wish to index.
    */
    public void index(TuvLing p_tuv)
        throws RemoteException,
               FuzzyIndexManagerException,
               LingManagerException;


    public void index(List p_tuvs)
        throws RemoteException,
               FuzzyIndexManagerException,
               LingManagerException;
    
}
