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
package com.globalsight.ling.lucene.analysis.th;

import java.io.*;
import java.util.*;
import java.text.BreakIterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.TokenStream;

import com.globalsight.ling.lucene.analysis.WordlistLoader;
import com.globalsight.ling.tm2.lucene.LuceneUtil;

/**
 * A basic analyzer for Thai/English text. Use JDK BreakIterator to
 * break Thai words.
 * Use StopFilter to filter out Thai/English stop words.
 * Use LowerCaseFilter to convert English text to lowercase.
 *
 * @author Pichai Ongvasith
 *
 */
public class BasicThaiAnalyzer
    extends Analyzer
{
    static public final String TYPE = "<THAI>";
    static private final Locale LOCALE = new Locale("th");
    private CharArraySet stopSet = LuceneUtil.newCharArraySet();

    /**
     * Create an instance of this class with no stop word filter.
     */
    public BasicThaiAnalyzer()
    {
    }

    /**
     * @param stopWords Stop Words Hashtable.
     * @see org.apache.lucence.analysis.TokenFilter TokenFilter.
     */
    public BasicThaiAnalyzer(CharArraySet stopwords)
    {
        stopSet = stopwords;
    }

    /**
     * @param stopwords The file name of the stop word file.
     * The file must contains one word per line in standard encoding (UTF-8).
     */
    public BasicThaiAnalyzer(File stopwords)
        throws IOException
    {
        stopSet = WordlistLoader.getWordSet(stopwords, "UTF-8");
    }

    protected TokenStreamComponents createComponents(String fieldName,
            Reader reader)
    {
        Tokenizer t = new ThaiTokenizer(reader);

        BreakIteratorTokenTokenizer f = new BreakIteratorTokenTokenizer(t,
                BreakIterator.getWordInstance(LOCALE), TYPE);
        LowerCaseFilter lf = new LowerCaseFilter(LuceneUtil.VERSION, f);
        StopFilter sf = null;
        if (stopSet != null)
        {
            sf = new StopFilter(LuceneUtil.VERSION, lf, stopSet);
        }
        if (sf == null)
        {
            return new TokenStreamComponents(t, lf);
        }
        else
        {
            return new TokenStreamComponents(t, sf);
        }
    }
}
