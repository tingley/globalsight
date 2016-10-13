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
// http://issues.apache.org/bugzilla/show_bug.cgi?id=32580
package com.globalsight.ling.lucene.analysis.ts;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;

import com.globalsight.ling.lucene.analysis.WordlistLoader;
import com.globalsight.ling.tm2.lucene.LuceneUtil;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * Analyzer for Tswana language. Supports an external list of
 * stopwords (words that will not be indexed at all) and an external
 * list of exclusions (word that will not be stemmed, but indexed).  A
 * default set of stopwords is used unless an alternative list is
 * specified, the exclusion list is empty by default.
 */
public class TswanaAnalyzer
    extends Analyzer
{
    /**
     * List of typical tswana stopwords.
     * These words will not be indexed
     */
    private String[] TSWANA_STOP_WORDS = {
    //
    // testing by peter
    //
    "wa", "ne", "go", "na","a","tse","go","re","a","tle","ke","ng","pa","o"
    };

    /**
     * Contains the stopwords used with the StopFilter.
     */
    private CharArraySet stopSet = LuceneUtil.newCharArraySet();

    /**
     * Contains words that should be indexed but not stemmed.
     */
    private CharArraySet exclusionSet = LuceneUtil.newCharArraySet();

    /**
     * Builds an analyzer.
     */
    public TswanaAnalyzer()
    {
        stopSet = StopFilter.makeStopSet(LuceneUtil.VERSION, TSWANA_STOP_WORDS);
        // stopSet = WordlistLoader.getWordSet("file.txt");
    }

    /**
     * Builds an analyzer with the given stop words.
     */
    public TswanaAnalyzer(String[] stopwords)
    {
        stopSet = StopFilter.makeStopSet(LuceneUtil.VERSION, stopwords);
    }

    /**
     * Builds an analyzer with the given stop words.
     */
    public TswanaAnalyzer(CharArraySet stopwords)
    {
        stopSet = stopwords;
    }

    /**
     * Builds an analyzer with the given stop words.
     */
    public TswanaAnalyzer(File stopwords)
        throws IOException
    {
        stopSet = WordlistLoader.getWordSet(stopwords);
    }

    /**
     * Builds an exclusionlist from an array of Strings.
     */
    public void setStemExclusionTable(String[] exclusionlist)
    {
        exclusionSet = StopFilter.makeStopSet(LuceneUtil.VERSION, exclusionlist);
    }

    /**
     * Builds an exclusionlist from a Hashtable.
     */
    public void setStemExclusionTable(CharArraySet exclusionlist)
    {
        exclusionSet = exclusionlist;
    }

    /**
     * Builds an exclusionlist from the words contained in the given file.
     */
    public void setStemExclusionTable(File exclusionlist)
        throws IOException
    {
        exclusionSet = WordlistLoader.getWordSet(exclusionlist);
    }

    /**
     * Creates a TokenStream which tokenizes all the text in the provided Reader.
     *
     * @return A TokenStream build from a StandardTokenizer filtered with
     *         StandardFilter, LowerCaseFilter, StopFilter, TswanaStemFilter
     */
    protected TokenStreamComponents createComponents(String fieldName,
            Reader reader)
    {
        Tokenizer t = new TswanaTokenizer(reader);
        
        StandardFilter f = new StandardFilter(LuceneUtil.VERSION, t);
        LowerCaseFilter lf = new LowerCaseFilter(LuceneUtil.VERSION, f);
        StopFilter ts = new StopFilter(LuceneUtil.VERSION, lf, stopSet);
        TswanaStemFilter gf = new TswanaStemFilter(ts, exclusionSet);
        
        return new TokenStreamComponents(t, gf);
    }
}
