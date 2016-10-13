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
package com.globalsight.ling.tm2.indexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.ling.tm2.lucene.LuceneIndexWriter;
import com.globalsight.ling.tm2.population.SegmentsForSave;
import com.globalsight.util.GlobalSightLocale;

/**
 * TmSegmentIndexer class is responsible for saving index tokens to
 * index tables.
 */

public class TmSegmentIndexer
{
    // Flag to determine if target segments are indexed
    private boolean indexTarget = false;

    private static final boolean TRANSLATABLE = true;
    private static final boolean LOCALIZABLE = false;

    public static final boolean SOURCE = true;
    public static final boolean NOT_SOURCE = false;

    public TmSegmentIndexer(boolean p_indexTarget)
    {
        indexTarget = p_indexTarget;
    }

    /**
     * Index saved segments in Segment Tm index table
     *
     * @param p_segmentsForSave SegmentsForSave object. This object is
     * created for saving translated segments.
     */
    public void indexSegmentTmSegments(SegmentsForSave p_segmentsForSave)
        throws Exception
    {
        indexSavedSegments(p_segmentsForSave);
    }
    
    // Sort the saved segments and call indexTuvs()
    // XXX only adds to the index, doesn't re-index anything
    private void indexSavedSegments(
        SegmentsForSave p_segmentsForSave)
        throws Exception
    {
        // locale - tuv map
        // key:  GlobalSightLocale
        // value: Collection of BaseTmTuv
        Map translatableTuvs = new HashMap();

        sortTuvsInTus(translatableTuvs,
            p_segmentsForSave.getTusForCreate(TRANSLATABLE));

        sortTuvs(translatableTuvs,
            p_segmentsForSave.getTuvsForAdd(TRANSLATABLE));
        
        callIndexTuvs(translatableTuvs,
            p_segmentsForSave.getTmId(),
            p_segmentsForSave.getSourceLocale());
    }

    /**
     * Sort Tuvs by locale and put them in a given Map. All tuvs in a
     * given Tu list are added to the map.
     *
     * @param p_localeTuvMap Tuvs are sorted and added to the map. Its
     * key is GlobalSightLocale and its value is a Collection of
     * SegmentsForSave.AddTuv
     * @param p_createTus Collection of SegmentsForSave.CreateTu
     */
    private void sortTuvsInTus(Map p_localeTuvMap, Collection p_createTus)
    {
        Iterator itCreateTu = p_createTus.iterator();
        while(itCreateTu.hasNext())
        {
            SegmentsForSave.CreateTu createTu
                = (SegmentsForSave.CreateTu)itCreateTu.next();
            
            Iterator itAddTuv = createTu.getAddTuvIterator();
            while(itAddTuv.hasNext())
            {
                SegmentsForSave.AddTuv addTuv
                    = (SegmentsForSave.AddTuv)itAddTuv.next();
                
                GlobalSightLocale locale = addTuv.getTuv().getLocale();

                List tuvList = (List)p_localeTuvMap.get(locale);
                if(tuvList == null)
                {
                    tuvList = new ArrayList();
                    p_localeTuvMap.put(locale, tuvList);
                }
                tuvList.add(addTuv);
            }
        }
    }
    

    /**
     * Sort Tuvs by locale and put them in a given Map.
     *
     * @param p_localeTuvMap Tuvs are sorted and added to the map. Its
     * key is GlobalSightLocale and its value is a Collection of
     * SegmentsForSave.AddTuv
     * @param p_addTuvs Collection of SegmentsForSave.AddTuv
     */
    private void sortTuvs(Map p_localeTuvMap, Collection p_addTuvs)
    {
        Iterator itAddTuv = p_addTuvs.iterator();
        while(itAddTuv.hasNext())
        {
            SegmentsForSave.AddTuv addTuv
                = (SegmentsForSave.AddTuv)itAddTuv.next();
            
            GlobalSightLocale locale = addTuv.getTuv().getLocale();
            List tuvList = (List)p_localeTuvMap.get(locale);
            if(tuvList == null)
            {
                tuvList = new ArrayList();
                p_localeTuvMap.put(locale, tuvList);
            }
            tuvList.add(addTuv);
        }
    }
    

    /**
     * Iterate a given locale - tuv collection map and call
     * indexTuvs() method
     *
     * @param p_localeTuvMap map of locale and addTuv collection
     * @param p_translatable indicates whether the tuvs are
     * translatable or localizable
     * @param p_tmId Tm id
     * @param p_sourceLocale source locale
     */
    private void callIndexTuvs(Map p_localeTuvMap,
        long p_tmId, GlobalSightLocale p_sourceLocale)
        throws Exception
    {
        // index source segments
        List addTuvs = (List)p_localeTuvMap.get(p_sourceLocale);
        if(addTuvs != null)
        {
            indexTuvsForPopulation(addTuvs, p_sourceLocale, p_tmId, true);
        }
        
        // index target segments
        if(indexTarget)
        {
            Iterator itLocale = p_localeTuvMap.keySet().iterator();
            while(itLocale.hasNext())
            {
                GlobalSightLocale locale = (GlobalSightLocale)itLocale.next();
                if(! locale.equals(p_sourceLocale))
                {
                    List addTrgTuvs = (List)p_localeTuvMap.get(locale);
            
                    indexTuvsForPopulation(addTrgTuvs, locale, p_tmId, false);
                }
            }
         }
    }
    

    /**
     * index a collection of tuvs to a designated index table. The
     * collection of tuvs must have the same locale, the same localize
     * type (translatable or localizable).
     *
     * @param p_addTuvs Collection of SegmentsForSave.AddTuv
     * @param p_locale locale of the Tuvs
     * @param p_tmId Tm id of the Tuvs
     * @param p_sourceLocale indicates whether the Tuvs are source locale
     */
    private void indexTuvsForPopulation(
        List p_addTuvs, GlobalSightLocale p_locale,
        long p_tmId, boolean p_sourceLocale)
        throws Exception
    {
        LuceneIndexWriter indexWriter
            = new LuceneIndexWriter(p_tmId, p_locale);
        try
        {
            indexWriter.index(p_addTuvs, p_sourceLocale, false);
        }
        finally
        {
            indexWriter.close();
        }
    }
    
    public static boolean indexesTargetSegments(long projectTmId)
    {
        boolean indexTarget = false;
        try
        {
            indexTarget = ServerProxy.getProjectHandler()
                    .getProjectTMById(projectTmId, false)
                    .isIndexTarget();
        }
        catch (Exception e)
        {
        }

        return indexTarget;
    }
}
