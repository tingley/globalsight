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
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;

import com.globalsight.ling.lucene.analysis.WordlistLoader;

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
    private Set stopSet = new HashSet();

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
    public BasicThaiAnalyzer(Hashtable stopwords)
    {
        stopSet = new HashSet(stopwords.keySet());
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

    /* (non-Javadoc)
     * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String, java.io.Reader)
     */
    public TokenStream tokenStream(String fieldName, Reader reader)
    {
        TokenStream result = new ThaiTokenizer(reader);
        result = new BreakIteratorTokenTokenizer(
            result, BreakIterator.getWordInstance(LOCALE), TYPE);
        result = new LowerCaseFilter(result);

        if (stopSet != null)
        {
            result = new StopFilter(result, stopSet);
        }

        return result;
    }
}
