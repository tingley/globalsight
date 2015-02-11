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
package com.globalsight.terminology.searchreplace;

import com.globalsight.terminology.searchreplace.SearchReplaceParams;

import java.rmi.RemoteException;
import com.globalsight.terminology.TermbaseException;

// Should be renamed to a generic UI listener.
import com.globalsight.util.progress.IProcessStatusListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

public interface ISearchReplaceManager
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
     * Searches using the provided parameters, which specify the
     * field(s) to search on, and the search text.
     *
     * Returns a closure encapsulating the entries fulfilling the
     * search request and a window of N entries to be shown to the
     * user.
     */
    void search(SearchReplaceParams params)
        throws RemoteException, TermbaseException;

    /**
     * After a search, retrieves the next N results.
     */
    SearchResults getNextResults()
        throws RemoteException, TermbaseException;

    /**
     * After a search, retrieves the previous N results.
     */
    SearchResults getPreviousResults()
        throws RemoteException, TermbaseException;

    /**
     * Replaces strings in a search when the entries are shown in a UI
     * and only a few of them are selected; ReplaceParams contains the
     * IDs of entries to replace in.
     */
    void replace(SearchReplaceParams params)
        throws RemoteException, TermbaseException;

    /**
     * Replaces strings in all search results and outputs progress
     * status through the listener.
     */
    void replaceAll(SearchReplaceParams params)
        throws RemoteException, TermbaseException;

}
