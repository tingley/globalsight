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
package com.globalsight.ling.tm2.lucene;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.ling.tm2.segmenttm.TmConcordanceQuery.TMidTUid;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Hits;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;


/**
 * LuceneSearcher searches TM segments by means of Lucene indexes
 */

public class LuceneSearcher
{
    private static final GlobalSightCategory c_logger =
        (GlobalSightCategory) GlobalSightCategory.getLogger(
            LuceneSearcher.class);

    private GlobalSightLocale m_targetLocale;
    private Searcher m_indexSearcher;
    private Analyzer m_analyzer;
    
    
    /**
     * Search static method.
     */
    static public List<TMidTUid> search(Collection<Long> p_tmIds,
        String p_searchPattern, GlobalSightLocale p_locale,
        GlobalSightLocale p_targetLocale)
        throws Exception
    {
        LuceneSearcher searcher = null;
        
        try
        {
            searcher = new LuceneSearcher(p_tmIds, p_locale, p_targetLocale);
            return searcher.search(p_searchPattern);
        }
        finally
        {
            searcher.close();
        }
    }
    

    /**
     * The constructor opens specified Lucene index searcher(s). The
     * Lucene index is created per tm per locale. If multiple TM ids
     * are specified, MultiReader is created.
     * 
     * @param p_tmIds Collection of TM id (Long)
     * @param p_locale locale of the index
     * @param p_targetLocale filter by target locale (TM3) (null for TM2)
     */
    public LuceneSearcher(Collection<Long> p_tmIds, GlobalSightLocale p_locale,
        GlobalSightLocale p_targetLocale)
        throws Exception
    {
        m_targetLocale = p_targetLocale;
        ArrayList searchers = new ArrayList();
        
        for(Iterator it = p_tmIds.iterator(); it.hasNext();)
        {
            Long tmId = (Long)it.next();
            File indexDir = LuceneUtil.getGoldTmIndexDirectory(
                tmId.longValue(), p_locale, false);

            if(indexDir != null && IndexReader.indexExists(indexDir))
            {
                IndexSearcher searcher
                    = new IndexSearcher(IndexReader.open(indexDir));
                searchers.add(searcher);
            }
        }

        if(searchers.size() == 0)
        {
            m_indexSearcher = null;
        }
        else if(searchers.size() == 1)
        {
            m_indexSearcher = (Searcher)searchers.get(0);
        }
        else
        {
            IndexSearcher[] searcherArray
                = new IndexSearcher[searchers.size()];
            searcherArray = (IndexSearcher[])searchers.toArray(searcherArray);
            m_indexSearcher = new MultiSearcher(searcherArray);
        }

        // create analyzer
        m_analyzer = TuvDocument.makeAnalyzer(new GsAnalyzer(p_locale));
    }
    

    /**
     * Searches indexes for a query pattern.
     *
     * @param p_searchPattern query pattern (legal Lucene query pattern)
     * @return List of TU ids
     */
    public List<TMidTUid> search(String p_searchPattern)
        throws Exception
    {
        // no index to search
        if(m_indexSearcher == null)
        {
            return Collections.EMPTY_LIST;
        }
        
        // due to indexing anomalies, it's possible for the same TUV to be
        // indexed twice, so eliminate dups with a Set
        Set<TMidTUid> result = new HashSet<TMidTUid>();

        Query query = TuvDocument.makeQuery(m_analyzer, p_searchPattern,
            m_targetLocale);
        if(c_logger.isDebugEnabled())
        {
            c_logger.debug("Searching for: "
                + query.toString(TuvDocument.TEXT_FIELD));
        }
        
        Hits hits = m_indexSearcher.search(query);
        for(int i = 0; i < hits.length(); i++)
        {
            TuvDocument tuvDoc = new TuvDocument(hits.doc(i));
            result.add(new TMidTUid(tuvDoc.getTmId(), tuvDoc.getTuId()));
        }

        return new ArrayList<TMidTUid>(result);
    }


    public void close()
        throws Exception
    {
        if(m_indexSearcher != null)
        {
            m_indexSearcher.close();
        }
    }


}
