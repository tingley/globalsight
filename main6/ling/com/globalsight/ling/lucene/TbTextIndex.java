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

package com.globalsight.ling.lucene;

import com.globalsight.ling.lucene.Index;
import com.globalsight.ling.lucene.IndexDocument;
import com.globalsight.ling.tm2.lucene.LuceneUtil;

import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;

import java.io.*;
import java.util.*;

/**
 * TbTextIndex is a termbase full-text index that uses stemming.
 *
 * The original text is stored for returning it with the result for
 * interactive searches.
 */
public class TbTextIndex
    extends Index
{
    public TbTextIndex(String p_dbname, String p_name, String p_locale) throws IOException
    {
        super(CATEGORY_TB, p_dbname, p_name, p_locale, TYPE_TEXT, TOKENIZE_STEM);
    }

    protected Document getDocument(long p_mainId, long p_subId, String p_text)
        throws IOException
    {
        return IndexDocument.DataDocument(p_mainId, p_subId, p_text);
    }

    protected Query getQuery(String p_text)
        throws IOException
    {
        PhraseQuery result = new PhraseQuery();

        TokenStream tokens = m_analyzer.tokenStream(
            IndexDocument.TEXT, new StringReader(p_text));
        tokens.reset();

        Token t;
        while ((t = LuceneUtil.getNextToken(tokens)) != null)
        {
            result.add(new Term(IndexDocument.TEXT, t.toString()));
        }

        return result;
    }

}
