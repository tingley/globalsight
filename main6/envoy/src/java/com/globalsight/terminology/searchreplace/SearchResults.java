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

import org.dom4j.Document;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class SearchResults
    implements Serializable
{
    // Parameters guiding the search
    private SearchReplaceParams m_params;

    // Result is a list of concept IDs (to show in the editor) and a
    // parallel list of IDs of conceptGrps, languageGrps or termGrps
    // in which the search string was found (to speed up replace).
    private ArrayList m_entryIds = new ArrayList();
    private ArrayList m_levelIds = new ArrayList();
    // A cache of the first 3*windowSize results
    private ArrayList m_levelDom = new ArrayList();

    // The real result data to send to the user, contains a max of
    // windowSize elements.
    private ArrayList m_windowDom = new ArrayList();

    // Show 10 results at a time
    private int m_windowSize = 10;
    private int m_windowStart = 0;
    private int m_windowEnd = 0;

    //
    // Constructor
    //

    public SearchResults(SearchReplaceParams p_params)
    {
        m_params = p_params;
    }

    //
    // Public Methods
    //

    /** Showing X-Y of Z results */
    public int getWindowStart()
    {
        return m_windowStart;
    }

    /** Showing X-Y of Z results */
    public int getWindowEnd()
    {
        return m_windowEnd;
    }

    /** Showing X-Y of Z results */
    public int getWindowSize()
    {
        return m_windowSize;
    }

    /** Showing X-Y of Z results */
    public int getNumResults()
    {
        return m_entryIds.size();
    }

    public List getWindowEntryIds()
    {
        return m_entryIds.subList(m_windowStart, m_windowEnd);
    }

    public List getWindowLevelIds()
    {
        return m_levelIds.subList(m_windowStart, m_windowEnd);
    }

    public ArrayList getWindowDom()
    {
        return m_windowDom;
    }

    //
    // Package-private Methods
    //

    void setWindowStart(int p_int)
    {
        m_windowStart = p_int;
    }

    void setWindowEnd(int p_int)
    {
        m_windowEnd = p_int;
    }

    void setWindowDom(List p_array)
    {
        m_windowDom.clear();
        m_windowDom.addAll(p_array);
    }

    void addWindowDom(List p_array)
    {
        m_windowDom.addAll(p_array);
    }

    /**
     * Returns the list of entry IDs (CIDs) in which the search string
     * was found.
     */
    ArrayList getEntryIds()
    {
        return m_entryIds;
    }

    /**
     * Returns the list of level-specific IDs (CIDs, LIDs, TIDs) in
     * which the search string was found.
     */
    ArrayList getLevelIds()
    {
        return m_levelIds;
    }

    /**
     * Returns the list of DOM fragments or entries in which the
     * search string was found.
     */
    ArrayList getLevelDom()
    {
        return m_levelDom;
    }

    void addEntryId(Long p_id)
    {
        m_entryIds.add(p_id);
    }

    void addLevelId(Long p_id)
    {
        m_levelIds.add(p_id);
    }

    int getCacheSize()
    {
        return 3 * m_windowSize;
    }

    /**
     * Maintains a cache of the first 3*windowSize results.
     * This method allows callers to avoid building an expensive string
     * (like logger.isDebugEnabled()).
     */
    boolean canAddLevelDom()
    {
        return m_levelDom.size() < getCacheSize();
    }

    /**
     * Adds a DOM fragment to the cache.
     */
    void addLevelDom(Document p_dom)
    {
        if (canAddLevelDom())
        {
            m_levelDom.add(p_dom);
        }
    }
}
