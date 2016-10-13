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

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import com.globalsight.util.GlobalSightLocale;

/**
 * This class holds a result of LGEM match search.  If matches are
 * found for a segment for all target locales, the segment can be
 * excluded from the subsequent fuzzy search. If matches are found for
 * a target locale for all segments, the locale can be excluded from
 * the subsequent fuzzy search.
 *
 * An example of the result matrix.
 *       fr_FR  de_DE  ja_JP
 * 1001    x      x      x
 * 1002    x             x
 * 1003           x      x
 *
 * A segment 1001 and ja_JP locale can be excluded from the subsequent
 * fuzzy search. Fuzzy matches found for a segment 1002 of fr_FR will
 * be discarded.
 *
 * This class also keeps track of duplicated LGEM matches.
 *
 */

class LgemHitsResult
{
    private boolean[][] m_resultMatrix;
    private Map m_tuvIdIndexMap;
    private Map m_localeIndexMap;
    
    // Key:   tuv id (Long)
    // Value: LeverageMatchesPerTuv
    private Map m_duplicatedLgemMatches;
    
    /**
     * Only constructor.
     *
     * @param p_tuvIds a collection of all TUV ids (Long) to search
     * for matches of.
     * @param p_locales a collection of all target locales (GlobalSightLocale).
     */
    public LgemHitsResult(Collection p_tuvIds, Collection p_locales)
    {
        int tuvIdSize = p_tuvIds.size();
        int localeSize = p_locales.size();
        
        // initialize the matrix with false
        m_resultMatrix = new boolean[tuvIdSize][localeSize];
        for(int i = 0; i < tuvIdSize; i++)
        {
            for(int j = 0; j < localeSize; j++)
            {
                m_resultMatrix[i][j] = false;
            }
        }
        
        // initialize tuv id to index of the matrix map
        m_tuvIdIndexMap = new HashMap(p_tuvIds.size());

        int idx = 0;
        Iterator it = p_tuvIds.iterator();
        while(it.hasNext())
        {
            m_tuvIdIndexMap.put(it.next(), new Integer(idx++));
        }
        
        // initialize locale to index of the matrix map
        m_localeIndexMap = new HashMap(p_locales.size());

        idx = 0;
        it = p_locales.iterator();
        while(it.hasNext())
        {
            m_localeIndexMap.put(it.next(), new Integer(idx++));
        }

        // initialize duplicated LGEM match holder
        m_duplicatedLgemMatches = new HashMap();
    }

    /**
     * Set true to the result of match search for a given tuv id for a
     * given target locale.
     *
     * @param p_tuvId Tuv id
     * @param p_locale target locale
     */
    public void foundMatch(Long p_tuvId, GlobalSightLocale p_locale)
    {
        if(m_tuvIdIndexMap.containsKey(p_tuvId)
            && m_localeIndexMap.containsKey(p_locale))
        {
            m_resultMatrix[getTuvIdIndex(p_tuvId)]
                [getLocaleIndex(p_locale)] = true;
        }
    }


    /**
     * Check if a match for a given tuv id for a given target locale
     * are found.
     *
     * @param p_tuvId Tuv id
     * @param p_locale target locale
     * @return true if the match is found, false otherwise
     */
    public boolean hasMatch(Long p_tuvId, GlobalSightLocale p_locale)
    {
        if(m_tuvIdIndexMap.containsKey(p_tuvId)
            && m_localeIndexMap.containsKey(p_locale))
        {
            return m_resultMatrix[getTuvIdIndex(p_tuvId)]
                [getLocaleIndex(p_locale)];
        }
        else
        {
            return false;
        }
    }
    

    /**
     * Return a list of locales of which matches for all segments are
     * found.
     *
     * @return a collection of GlobalSightLocale objects
     */
    public Collection getCompletedLocales()
    {
        Collection result = new ArrayList();
        Collection locales = m_localeIndexMap.keySet();
        Iterator it = locales.iterator();
        while(it.hasNext())
        {
            GlobalSightLocale locale = (GlobalSightLocale)it.next();
            int localeIdx = getLocaleIndex(locale);
            
            boolean found = true;
            for(int i = 0; i < m_tuvIdIndexMap.size(); i++)
            {
                if(m_resultMatrix[i][localeIdx] == false)
                {
                    found = false;
                    break;
                }
            }

            if(found)
            {
                result.add(locale);
            }
        }
        return result;
    }
    
    /**
     * Return a list of TUV ids for which matches of all locales are
     * found.
     *
     * @return a collection of TUV ids (Long)
     */
    public Collection getCompletedTuvs()
    {
        Collection result = new ArrayList();
        Collection tuvs = m_tuvIdIndexMap.keySet();
        Iterator it = tuvs.iterator();
        while(it.hasNext())
        {
            Long tuvId = (Long)it.next();
            int tuvIdx = getTuvIdIndex(tuvId);
            
            boolean found = true;
            for(int i = 0; i < m_localeIndexMap.size(); i++)
            {
                if(m_resultMatrix[tuvIdx][i] == false)
                {
                    found = false;
                    break;
                }
            }

            if(found)
            {
                result.add(tuvId);
            }
        }
        return result;
    }
    


    public void setDuplicatedLgemMatch(Long p_tuvId,
        LeverageMatchesPerTuv p_lmPerTuv)
    {
        m_duplicatedLgemMatches.put(p_tuvId, p_lmPerTuv);
    }

    public LeverageMatchesPerTuv getDuplicatedLgemMatch(Long p_tuvId)
    {
        return (LeverageMatchesPerTuv)m_duplicatedLgemMatches.get(p_tuvId);
    }


    public void merge(LgemHitsResult p_other)
        throws LingManagerException
    {
        // check if two objects have the same Tuv set
        if(!m_tuvIdIndexMap.equals(p_other.m_tuvIdIndexMap))
        {
            throw new LingManagerException("LgemHitsMergeBadTuvs", null, null);
        }
        
        // check if two objects don't have the same locale
        Iterator it = p_other.m_localeIndexMap.keySet().iterator();
        while(it.hasNext())
        {
            Set thisLocales = m_localeIndexMap.keySet();
            if(thisLocales.contains(it.next()))
            {
                throw new LingManagerException(
                    "LgemHitsMergeBadLocales", null, null);
            }
        }
            
        int tuvIdSize = m_tuvIdIndexMap.size();
        int localeSize = m_localeIndexMap.size()
            + p_other.m_localeIndexMap.size();
        
        // merge the matrix and locale index map at the same time
        boolean[][] matrix = new boolean[tuvIdSize][localeSize];
        Map localeIdxMap = new HashMap();

        // move this matrix to a tmp matrix
        int idx = 0;
        for(it = m_localeIndexMap.keySet().iterator(); it.hasNext(); idx++)
        {
            GlobalSightLocale locale = (GlobalSightLocale)it.next();
            localeIdxMap.put(locale, new Integer(idx)); // new index for locale

            int oldIdx = getLocaleIndex(locale); // old index
            for(int i = 0; i < tuvIdSize; i++)
            {
                matrix[i][idx] = m_resultMatrix[i][oldIdx];
            }

        }
        
        // move the other's matrix to a tmp matrix
        for(it = p_other.m_localeIndexMap.keySet().iterator();
            it.hasNext(); idx++)
        {
            GlobalSightLocale locale = (GlobalSightLocale)it.next();
            localeIdxMap.put(locale, new Integer(idx)); // new index for locale

            int oldIdx = p_other.getLocaleIndex(locale); // old index
            for(int i = 0; i < tuvIdSize; i++)
            {
                matrix[i][idx] = p_other.m_resultMatrix[i][oldIdx];
            }

        }

        m_resultMatrix = matrix;
        m_localeIndexMap = localeIdxMap;
        
        // merge m_duplicatedLgemMatches
        it = p_other.m_duplicatedLgemMatches.keySet().iterator();
        while(it.hasNext())
        {
            Long tuvId = (Long)it.next();
            if(m_duplicatedLgemMatches.containsKey(tuvId))
            {
                LeverageMatchesPerTuv lmPerTuv
                    = (LeverageMatchesPerTuv)m_duplicatedLgemMatches
                    .get(tuvId);
                lmPerTuv.merge((LeverageMatchesPerTuv)p_other
                    .m_duplicatedLgemMatches.get(tuvId));
            }
            else
            {
                m_duplicatedLgemMatches.put(tuvId,
                    p_other.m_duplicatedLgemMatches.get(tuvId));
            }
        }
        
        
    }
    

    private int getTuvIdIndex(Long p_tuvId)
    {
        return ((Integer)m_tuvIdIndexMap.get(p_tuvId)).intValue();
    }
    
    private int getLocaleIndex(GlobalSightLocale p_locale)
    {
        return ((Integer)m_localeIndexMap.get(p_locale)).intValue();
    }
    
}
