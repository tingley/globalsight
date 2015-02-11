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
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;

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

    private IndexReader m_indexReader;
    
    
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
        ArrayList readers = new ArrayList();
        
        for(Iterator<Long> it = p_tmIds.iterator(); it.hasNext();)
        {
            Long tmId = it.next();
            File indexDir = LuceneUtil.getGoldTmIndexDirectory(
                tmId.longValue(), p_locale, false);

            if(indexDir != null && IndexReader.indexExists(indexDir))
            {
                IndexReader reader = IndexReader.open(indexDir);
                readers.add(reader);
            } else {
                c_logger.debug("No GoldTmIndex directory found. Maybe this is a bug, worth paying attension to");
            }
        }
        
        if(readers.size() == 0)
        {
            m_indexReader = null;
        }
        else if(readers.size() == 1)
        {
            m_indexReader = (IndexReader)readers.get(0);
        }
        else
        {
            IndexReader[] readerArray = new IndexReader[readers.size()];
            readerArray = (IndexReader[])readers.toArray(readerArray);
            m_indexReader = new MultiReader(readerArray);
        }

    }
    

    /**
     * Returns List of c.g.l.tm2.index.Token objects matching a
     * specified word. If TM index doesn't exist, null is returned.
     */
    public List getGsTokensByTerm(String p_term, boolean lookupTarget)
            throws Exception
    {
        if(m_indexReader == null)
        {
            return null;
        }
        
        ArrayList tokenList = new ArrayList();
            
        Term term = new Term(TuvDocument.TEXT_FIELD, p_term);
        TermDocs termDocs = m_indexReader.termDocs(term);
            
        while(termDocs.next())
        {
            Document document = m_indexReader.document(termDocs.doc());
            TuvDocument tuvDocument = new TuvDocument(document);

            Long tmIdAsLong = tuvDocument.getTmIdAsLong();
            boolean isSourceLocale = tuvDocument.isSourceLocale();
            int freq = termDocs.freq();

            if (isSourceLocale || lookupTarget)
            {
                Token token = new Token(
                    p_term, tuvDocument.getTuvId(),
                    tuvDocument.getTuId(), tmIdAsLong.longValue(),
                    freq, tuvDocument.getTotalTokenCount(),
                    isSourceLocale);
                
                tokenList.add(token);
            }
        }

        termDocs.close();
            
        if(tokenList.size() == 0)
        {
            tokenList = null;
        }
        
        return tokenList;
    }


    public void close()
        throws Exception
    {
        if(m_indexReader != null)
        {
            m_indexReader.close();
        }
    }
    
}
