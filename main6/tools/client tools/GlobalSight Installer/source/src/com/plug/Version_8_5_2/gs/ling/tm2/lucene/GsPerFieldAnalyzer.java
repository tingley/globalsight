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

import java.io.Reader;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;

import com.plug.Version_8_5_2.gs.util.GlobalSightLocale;

/**
 * Lucene analyzer for GlobalSight. It tokenizes a string using Java's word
 * break iterator. It also filters out the stopwords.
 */

public class GsPerFieldAnalyzer extends Analyzer
{
    private static final Logger c_logger = Logger.getLogger(GsAnalyzer.class);

    private GlobalSightLocale m_locale;

    // / Constructor
    public GsPerFieldAnalyzer(GlobalSightLocale p_locale)
    {
        super(new ReuseStrategyNo());
        m_locale = p_locale;
    }

    public GlobalSightLocale getLocale()
    {
        return m_locale;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader)
    {
        TokenStreamComponents result = null;

        if (TuvDocument.TARGET_LOCALES_FIELD.equalsIgnoreCase(fieldName))
        {
            result = new TokenStreamComponents(new WhitespaceTokenizer(LuceneUtil.VERSION, reader));
        }
        else
        {
            try
            {
                Tokenizer t = new GsTokenizer(reader, m_locale);
                TokenStream tok = new GsStopFilter(t, m_locale);
                tok = new GsStemFilter(tok, m_locale);

                result = new TokenStreamComponents(t, tok);
            }
            catch (Exception e)
            {
                // can't throw checked exception
                c_logger.error("An error occured in tokenStream", e);

                throw new RuntimeException(e);
            }
        }

        return result;
    }
}
