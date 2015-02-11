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
package com.plug.Version_8_5_2.gs.ling.tm2.lucene;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import com.plug.Version_8_5_2.gs.util.GlobalSightLocale;


/**
 * Wrapper of Lucene Document object for Tm segment
 */
public class TuvDocument
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
    

    public TuvDocument(String text, long tuvId, long tuId,
        long tmId, boolean isSourceLocale,
        Set<String> targetLocales, Analyzer analyzer)
        throws Exception
    {
        m_text = text;
        m_tuvId = new Long(tuvId);
        m_tuId = new Long(tuId);
        m_tmId = new Long(tmId);
        m_isSourceLocale = new Boolean(isSourceLocale);
        m_targetLocales = targetLocales;
        m_totalTokenCount
            = new Integer(getTotalTokenCount(text, analyzer));

        m_document = createDocument();
    }


    public TuvDocument(Document document)
    {
        m_document = document;
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
    
    private int getTotalTokenCount(String text, Analyzer analyzer)
        throws Exception
    {
        TokenStream tokenStream = analyzer.tokenStream("blah",
                new StringReader(text));
        tokenStream.reset();
        
        int tokenCount = 0;
        while (tokenStream.incrementToken())
        {
            tokenCount++;
        }

        return tokenCount;
    }
    

    private Document createDocument()
    {
        Document doc = new Document();
        FieldType ft;
        Field field;
        
        // text field. not stored, indexed, tokenized.
        ft = new FieldType();
        ft.setStored(false);
        ft.setIndexed(true);
        ft.setTokenized(true);
        field = new Field(TEXT_FIELD, m_text, ft);// false, true, true);
        doc.add(field);
        
        // Tuv id field. stored, indexed, not tokenized.
        ft = new FieldType();
        ft.setStored(true);
        ft.setIndexed(true);
        ft.setTokenized(false);
        field = new Field(
            TUV_ID_FIELD, m_tuvId.toString(), ft);// true, true, false);
        doc.add(field);
        
        // Tu id field. stored, not indexed, not tokenized.
        ft = new FieldType();
        ft.setStored(true);
        ft.setIndexed(false);
        ft.setTokenized(false);
        field = new Field(
            TU_ID_FIELD, m_tuId.toString(), ft);//true, false, false);
        doc.add(field);
        
        // TM id field. stored, not indexed, not tokenized.
        ft = new FieldType();
        ft.setStored(true);
        ft.setIndexed(false);
        ft.setTokenized(false);
        field = new Field(
            TM_ID_FIELD, m_tmId.toString(), ft);//true, false, false);
        doc.add(field);
        
        // Token count field. stored, not indexed, not tokenized.
        ft = new FieldType();
        ft.setStored(true);
        ft.setIndexed(false);
        ft.setTokenized(false);
        field = new Field(TOKEN_COUNT_FIELD,
            m_totalTokenCount.toString(), ft);//true, false, false);
        doc.add(field);
        
        // Is source field. stored, not indexed, not tokenized.
        ft = new FieldType();
        ft.setStored(true);
        ft.setIndexed(false);
        ft.setTokenized(false);
        field = new Field(IS_SOURCE_FIELD,
            m_isSourceLocale.toString(), ft);// true, false, false);
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
            ft = new FieldType();
            ft.setStored(false);
            ft.setIndexed(true);
            ft.setTokenized(true);
            field = new Field(TARGET_LOCALES_FIELD, locs.toString(),
                              ft);//false, true, true);
            doc.add(field);
        }
        
        return doc;
    }

    /**
     * Create a query for TuvDocuments, optionally filtered by target locale.
     *
     * @param analyzer An analyzer contructed by makeAnalyzer
     * @param query A Lucene query for the text
     * @param targetLocale targetLocale filter on target locale (TM3) (null
     * for TM2)
     */
    public static Query makeQuery(Analyzer analyzer, String query,
        GlobalSightLocale targetLocale)
        throws ParseException
    {
        //escape reserved word of Lucene, // + - & | ! ( ) { } [ ] ^ ~ * ? : \
        query = replaceReservedWordForLucenne(query);
        query = QueryParser.escape(query);
        query = TEXT_FIELD + ":\"" + query + "\"";
        if (targetLocale != null) {
            // from TM3
            if ("".equals(query.trim()))
            {
                return null;
            }
            query = query + " AND ";
            query = query + TARGET_LOCALES_FIELD + ":"
                    + targetLocale.toString();
        }
        else
        {
            // From TM2
            if ("".equals(query.trim()))
            {
                return null;
            }
        }
        QueryParser qp = new QueryParser(Version.LUCENE_44, TEXT_FIELD, analyzer);
        Query qq = qp.parse(query);
        
        return qq;
    }

    /**
     * Fix the reserved word for Lucene by leon
     * 
     * @param pattern
     * @return
     */
    private static String replaceReservedWordForLucenne(String pattern)
    {
        //For AND, OR, NOT
        pattern = replace(pattern, "AND");
        pattern = replace(pattern, "OR");
        pattern = replace(pattern, "NOT");

        return pattern.trim();
    }

    /**
     * Replace AND OR NOT
     * 
     * @param pattern
     * @param replaceStr
     * @return
     */
    private static String replace(String pattern, String replaceStr)
    {
        while (pattern.indexOf(" " + replaceStr + " ") > 0)
        {
            pattern = pattern.replace(" " + replaceStr + " ", " ");
        }
        if (pattern.startsWith(replaceStr + " "))
        {
            pattern = pattern.substring(replaceStr.length(), pattern.length());
        }

        if (pattern.endsWith(" " + replaceStr))
        {
            pattern = pattern.substring(0,
                    pattern.length() - replaceStr.length());
        }

        if (pattern.trim().equals(replaceStr))
        {
            pattern = "";
        }
        return pattern;
    }
}
