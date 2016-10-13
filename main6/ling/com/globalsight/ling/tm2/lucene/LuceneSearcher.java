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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import com.globalsight.ling.tm2.segmenttm.TMidTUid;
import com.globalsight.util.GlobalSightLocale;


/**
 * LuceneSearcher searches TM segments by means of Lucene indexes
 */

public class LuceneSearcher
{
    private static final Logger c_logger =
        Logger.getLogger(
            LuceneSearcher.class);

    private GlobalSightLocale m_targetLocale;
    private LuceneCache luceneCache;
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
        ArrayList<LuceneCache> searchers = new ArrayList<LuceneCache>();
        ArrayList<Long> tmIds = new ArrayList<Long>();
        
        for(Iterator it = p_tmIds.iterator(); it.hasNext();)
        {
            Long tmId = (Long)it.next();
            File indexDir = LuceneUtil.getGoldTmIndexDirectory(
                tmId.longValue(), p_locale, false);
            if (indexDir == null || !indexDir.exists())
            {
                continue;
            }
            
            LuceneCache lc = LuceneCache.getLuceneCache(indexDir);
            if(lc != null)
            {
                searchers.add(lc);
                tmIds.add(tmId);
            }
        }

        if(searchers.size() == 0)
        {
            luceneCache = null;
        }
        else if(searchers.size() == 1)
        {
            luceneCache = searchers.get(0);
        }
        else
        {
            IndexReader[] ireaderArray = new IndexReader[searchers.size()];
            
            for (int i = 0; i < ireaderArray.length; i++)
            {
                ireaderArray[i] = searchers.get(i).getIndexReader();
            }
            
            luceneCache = LuceneCache.getLuceneCache(tmIds, ireaderArray);
        }

        // create analyzer
        m_analyzer = new GsPerFieldAnalyzer(p_locale);
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
        if(luceneCache == null)
        {
            return Collections.emptyList();
        }
        
        // due to indexing anomalies, it's possible for the same TUV to be
        // indexed twice.  Add the hits to a list in the order we 
        // find them, but use a temporary set to eliminate duplicates.
        List<TMidTUid> result = new ArrayList<TMidTUid>();
        Set<TMidTUid> duplicateCheck = new HashSet<TMidTUid>();
        Query query = TuvDocument.makeQuery(m_analyzer, p_searchPattern,
            m_targetLocale);
        if (query == null)
        {
            return Collections.emptyList();
        }
        // For special search, only with reserved word and stop words
        String queryStr = query.toString();
        if (queryStr.startsWith("+target_locales:") || "".equals(queryStr))
        {
            return Collections.emptyList();
        }

        if(c_logger.isDebugEnabled())
        {
            c_logger.debug("Searching for: "
                + query.toString(TuvDocument.TEXT_FIELD));
        }
        
        TopDocs topdocs = luceneCache.getIndexSearcher().search(query, 100);
        ScoreDoc[] hits = topdocs.scoreDocs;
        if (hits != null)
        {
            for(int i = 0; i < hits.length; i++)
            {
                Document doc = luceneCache.getIndexSearcher().doc(hits[i].doc);
                float score = hits[i].score;
                TuvDocument tuvDoc = new TuvDocument(doc);
                TMidTUid tt = new TMidTUid(tuvDoc.getTmId(), tuvDoc.getTuId(),
                        score);
                if (!duplicateCheck.contains(tt))
                {
                    result.add(tt);
                    duplicateCheck.add(tt);
                }
            }
        }

        return result;
    }

    public void close() throws Exception
    {
    }
}
