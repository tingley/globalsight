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

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;


/**
 * Wrapper of Lucene Document object for Tm segment
 */
class TuvDocument
{
    // field names
    public static final String TEXT_FIELD = "text";
    public static final String TUV_ID_FIELD = "tuv_id";
    public static final String TU_ID_FIELD = "tu_id";
    public static final String TM_ID_FIELD = "tm_id";
    public static final String IS_SOURCE_FIELD = "is_source";
    public static final String TOKEN_COUNT_FIELD = "token_count";
    public static final String TARGET_LOCALES_FIELD = "target_locales";
    
    private Document m_document = null;

    // cache values of each Field in m_document
    private String m_text = null;
    private Long m_tuvId = null;
    private Long m_tuId = null;
    private Long m_tmId = null;
    private Boolean m_isSourceLocale = null;
    // used by TM3, null for TM2
    private Set<String> m_targetLocales = null;
    private Integer m_totalTokenCount = null;
    

    public TuvDocument(String p_text, long p_tuvId, long p_tuId,
        long p_tmId, boolean p_isSourceLocale,
        Set<String> p_targetLocales, Analyzer p_analyzer)
        throws Exception
    {
        m_text = p_text;
        m_tuvId = new Long(p_tuvId);
        m_tuId = new Long(p_tuId);
        m_tmId = new Long(p_tmId);
        m_isSourceLocale = new Boolean(p_isSourceLocale);
        m_targetLocales = p_targetLocales;
        m_totalTokenCount
            = new Integer(getTotalTokenCount(p_text, p_analyzer));

        m_document = createDocument();
    }


    public TuvDocument(Document p_document)
    {
        m_document = p_document;
    }
    

    public Document getDocument()
    {
        return m_document;
    }


    public long getTuvId()
        throws Exception
    {
        if(m_tuvId == null)
        {
            String idStr = m_document.get(TUV_ID_FIELD);
            m_tuvId = new Long(idStr);
        }
        
        return m_tuvId.longValue();
    }
    
    public Long getTuIdAsLong()
        throws Exception
    {
        if(m_tuId == null)
        {
            String idStr = m_document.get(TU_ID_FIELD);
            m_tuId = new Long(idStr);
        }
        
        return m_tuId;
    }
    
    public long getTuId()
        throws Exception
    {
        return getTuIdAsLong().longValue();
    }
    
    public long getTmId()
        throws Exception
    {
        if(m_tmId == null)
        {
            String idStr = m_document.get(TM_ID_FIELD);
            m_tmId = new Long(idStr);
        }
        
        return m_tmId.longValue();
    }
    
    public Long getTmIdAsLong()
        throws Exception
    {
        if(m_tmId == null)
        {
            getTmId();
        }
        
        return m_tmId;
    }
    
    public int getTotalTokenCount()
        throws Exception
    {
        if(m_totalTokenCount == null)
        {
            String idStr = m_document.get(TOKEN_COUNT_FIELD);
            m_totalTokenCount = new Integer(idStr);
        }
        
        return m_totalTokenCount.intValue();
    }
    
    public boolean isSourceLocale()
        throws Exception
    {
        if(m_isSourceLocale == null)
        {
            String idStr = m_document.get(IS_SOURCE_FIELD);
            m_isSourceLocale = new Boolean(idStr);
        }
        
        return m_isSourceLocale.booleanValue();
    }
    
    private int getTotalTokenCount(String p_text, Analyzer p_analyzer)
        throws Exception
    {
        TokenStream tokenStream = p_analyzer.tokenStream(
            "blah", new StringReader(p_text));
        
        int tokenCount = 0;
        while(tokenStream.next() != null)
        {
            tokenCount++;
        }
        
        return tokenCount;
    }
    

    private Document createDocument()
    {
        Document doc = new Document();

        // text field. not stored, indexed, tokenized.
        Field field = new Field(TEXT_FIELD, m_text, false, true, true);
        doc.add(field);
        
        // Tuv id field. stored, indexed, not tokenized.
        field = new Field(
            TUV_ID_FIELD, m_tuvId.toString(), true, true, false);
        doc.add(field);
        
        // Tu id field. stored, not indexed, not tokenized.
        field = new Field(
            TU_ID_FIELD, m_tuId.toString(), true, false, false);
        doc.add(field);
        
        // TM id field. stored, not indexed, not tokenized.
        field = new Field(
            TM_ID_FIELD, m_tmId.toString(), true, false, false);
        doc.add(field);
        
        // Token count field. stored, not indexed, not tokenized.
        field = new Field(TOKEN_COUNT_FIELD,
            m_totalTokenCount.toString(), true, false, false);
        doc.add(field);
        
        // Is source field. stored, not indexed, not tokenized.
        field = new Field(IS_SOURCE_FIELD,
            m_isSourceLocale.toString(), true, false, false);
        doc.add(field);

        // target locales field. not stored, indexed, tokenized.
        if (m_targetLocales != null)
        {
            StringBuilder locs = new StringBuilder();
            for (String locale: m_targetLocales)
            {
                locs.append(locale);
                locs.append(' ');
            }
            field = new Field(TARGET_LOCALES_FIELD, locs.toString(),
                              false, true, true);
            doc.add(field);
        }
        
        return doc;
    }

    /**
     * Create an Analyzer for TuvDocuments, using the given analyzer for the
     * text, that will also analyze the target locales correctly.
     */
    public static Analyzer makeAnalyzer(Analyzer p_analyzer)
    {
        PerFieldAnalyzerWrapper r = new PerFieldAnalyzerWrapper(p_analyzer);
        r.addAnalyzer(TARGET_LOCALES_FIELD, new WhitespaceAnalyzer());
        return r;
    }

    /**
     * Create a query for TuvDocuments, optionally filtered by target locale.
     *
     * @param p_analyzer An analyzer contructed by makeAnalyzer
     * @param p_query A Lucene query for the text
     * @param p_targetLocale p_targetLocale filter on target locale (TM3) (null
     * for TM2)
     */
    public static Query makeQuery(Analyzer p_analyzer, String p_query,
        GlobalSightLocale p_targetLocale)
        throws ParseException
    {
        if (p_targetLocale != null) {
            // this case is handled at a higher level by TM2
            p_query =
                (p_query.trim().equals("*") ? "" : p_query + " AND ") +
                TARGET_LOCALES_FIELD + ":" + p_targetLocale.toString();
        }
        return QueryParser.parse(p_query, TEXT_FIELD, p_analyzer);
    }
}
