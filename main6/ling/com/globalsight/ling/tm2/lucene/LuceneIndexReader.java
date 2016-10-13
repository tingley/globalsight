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
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import com.globalsight.ling.tm2.indexer.Token;
import com.globalsight.util.GlobalSightLocale;


/**
 * LuceneIndexReader is responsible for reading TM index for TM leveraging.
 */

public class LuceneIndexReader
{
    private static final Logger c_logger =
        Logger.getLogger(
            LuceneIndexReader.class);

    private Analyzer m_analyzer;
    private LuceneCache luceneCache;
    
    
    /**
     * The constructor opens specified Lucene index(es). The Lucene
     * index is created per tm per locale. If multiple TM ids are
     * specified, MultiReader is created. If TM index for the
     * specified TM ids and locale doesn't exist, the constructor
     * doesn't throw exception, but the subsequent method call on this
     * object will be silently ignored (meaning returns null or does
     * nothing).
     * 
     * @param p_tmIds Collection of TM id (Long)
     * @param p_locale locale of the index
     */
    public LuceneIndexReader(Collection<Long> p_tmIds, GlobalSightLocale p_locale)
        throws Exception
    {
        ArrayList<LuceneCache> readers = new ArrayList<LuceneCache>();
        m_analyzer = new GsAnalyzer(p_locale);
        ArrayList<Long> tmIds = new ArrayList<Long>();
        
        for(Iterator<Long> it = p_tmIds.iterator(); it.hasNext();)
        {
            Long tmId = it.next();
            File indexDir = LuceneUtil.getGoldTmIndexDirectory(
                tmId.longValue(), p_locale, true);
            if (indexDir == null || !indexDir.exists())
            {
                continue;
            }
            
            LuceneCache lc = LuceneCache.getLuceneCache(indexDir);
            if (lc != null)
            {
                readers.add(lc);
                tmIds.add(tmId);
            }
            else
            {
                c_logger.debug("No GoldTmIndex directory found. Maybe this is a bug, worth paying attension to");
            }
        }
        
        if(readers.size() == 0)
        {
            luceneCache = null;
        }
        else if(readers.size() == 1)
        {
            luceneCache = (LuceneCache)readers.get(0);
        }
        else
        {
            IndexReader[] ireaderArray = new IndexReader[readers.size()];
            
            for (int i = 0; i < ireaderArray.length; i++)
            {
                ireaderArray[i] = readers.get(i).getIndexReader();
            }
            
            luceneCache = LuceneCache.getLuceneCache(tmIds, ireaderArray);
        }
    }
    

    /**
     * Returns List of c.g.l.tm2.index.Token objects matching a
     * specified word. If TM index doesn't exist, null is returned.
     */
    public List getGsTokensByTerm(String p_term, boolean lookupTarget)
            throws Exception
    {
        if(luceneCache == null)
        {
            return null;
        }
        
        ArrayList tokenList = new ArrayList();
        
        QueryParser parser = new QueryParser(LuceneUtil.VERSION,
                TuvDocument.TEXT_FIELD, m_analyzer);
        String qqq = QueryParser.escape(p_term);
        Query q = parser.parse(qqq);
        TopDocs results = luceneCache.getIndexSearcher().search(q, 200);

        // fix a issue for lucene which cannot find : enterpris
        if (results.totalHits == 0 && p_term.endsWith("is"))
        {
            q = parser.parse(p_term + "e");
            results = luceneCache.getIndexSearcher().search(q, 200);
        }
        
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        Term term = new Term(TuvDocument.TEXT_FIELD, p_term);
        int docFreq = luceneCache.getIndexReader().docFreq(term);
        
        if (hits != null)
        {
            for (int i = 0; i < hits.length; i++)
            {
                Document doc = luceneCache.getIndexSearcher().doc(hits[i].doc);

                TuvDocument tuvDocument = new TuvDocument(doc);

                Long tmIdAsLong = tuvDocument.getTmIdAsLong();
                boolean isSourceLocale = tuvDocument.isSourceLocale();
                int freq = docFreq;

                if (isSourceLocale || lookupTarget)
                {
                    Token token = new Token(p_term, tuvDocument.getTuvId(),
                            tuvDocument.getTuId(), tmIdAsLong.longValue(),
                            freq, tuvDocument.getTotalTokenCount(),
                            isSourceLocale);

                    tokenList.add(token);
                }
            }
        }
        
//        Term term = new Term(TuvDocument.TEXT_FIELD, p_term);
//        //DocsEnum termDocs = m_indexReader.getContext().
//        int allcount = m_indexReader.numDocs();
//        int docFreq = m_indexReader.docFreq(term);
//        int docC = m_indexReader.getDocCount(TuvDocument.TEXT_FIELD);
//        long lll = m_indexReader.totalTermFreq(term);
//        
//        for(int i = 0; i < allcount; i++)
//        {
//            Document document = m_indexReader.document(i);
//            TuvDocument tuvDocument = new TuvDocument(document);
//
//            Long tmIdAsLong = tuvDocument.getTmIdAsLong();
//            boolean isSourceLocale = tuvDocument.isSourceLocale();
//            int freq = m_indexReader.docFreq(term);
//
//            if (isSourceLocale || lookupTarget)
//            {
//                Token token = new Token(
//                    p_term, tuvDocument.getTuvId(),
//                    tuvDocument.getTuId(), tmIdAsLong.longValue(),
//                    freq, tuvDocument.getTotalTokenCount(),
//                    isSourceLocale);
//                
//                tokenList.add(token);
//            }
//        }
            
        if(tokenList.size() == 0)
        {
            tokenList = null;
        }
        
        return tokenList;
    }
    
    public LuceneCache getLuceneCache()
    {
        return luceneCache;
    }

    public void close() throws Exception
    {
        // do not close, for cache
    }
    
}
