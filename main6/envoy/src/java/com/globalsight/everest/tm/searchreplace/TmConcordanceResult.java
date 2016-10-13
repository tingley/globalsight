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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.TuQueryResult;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

public class TmConcordanceResult implements Serializable
{
    private GlobalSightLocale m_sourceLocale;
    private ArrayList<GlobalSightLocale> m_targetLocales;
    private ArrayList<Long> m_allLocaleIds = new ArrayList<Long>();
    private Map<Long, String> tmIdName = null;
    private TuQueryResult m_result;

    public TmConcordanceResult(GlobalSightLocale p_sourceLocale,
            ArrayList p_targetLocales, TuQueryResult p_result)
    {
        m_sourceLocale = p_sourceLocale;
        m_targetLocales = p_targetLocales;
        m_result = p_result;

        setAllLocaleIds();
    }

    //
    // Public Methods
    //

    public int getTotal()
    {
        return m_result.getCount();
    }

    public int getMin()
    {
        return m_result.getFirst();
    }

    public int getMax()
    {
        return m_result.getLast();
    }

    public void readNextPage()
    {
        m_result.loadNextPage();
    }

    public void readPreviousPage()
    {
        m_result.loadPreviousPage();
    }

    /**
     * Returns a list of SegmentTmTus.
     */
    public List<SegmentTmTu> getTus()
    {
        return m_result.getPageResults();
    }

    /**
     * Returns a list of SegmentTmTus, sorted using the provided comparator.
     */
    public List<SegmentTmTu> getTus(Comparator<SegmentTmTu> comparator)
    {
        List<SegmentTmTu> l = getTus();
        SortUtil.sort(l, comparator);
        return l;
    }

    public GlobalSightLocale getSourceLocale()
    {
        return m_sourceLocale;
    }

    public ArrayList<GlobalSightLocale> getTargetLocales()
    {
        return m_targetLocales;
    }

    public ArrayList<Long> getAllLocaleIds()
    {
        return m_allLocaleIds;
    }

    //
    // Private Methods
    //

    private void setAllLocaleIds()
    {
        m_allLocaleIds.clear();

        m_allLocaleIds.add(m_sourceLocale.getIdAsLong());

        for (int i = 0, max = m_targetLocales.size(); i < max; i++)
        {
            GlobalSightLocale loc = m_targetLocales.get(i);
            m_allLocaleIds.add(loc.getIdAsLong());
        }
    }

    public void setMapIdName(Map<Long, String> tmIdName)
    {
        this.tmIdName = tmIdName;
    }

    public Map<Long, String> getMapIdName()
    {
        return this.tmIdName;
    }
}
