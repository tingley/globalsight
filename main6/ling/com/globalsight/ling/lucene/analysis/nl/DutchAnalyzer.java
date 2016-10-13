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
package com.globalsight.ling.lucene.analysis.nl;

/**
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;

import com.globalsight.ling.lucene.analysis.WordlistLoader;
import com.globalsight.ling.tm2.lucene.LuceneUtil;

import java.io.File;
import java.io.Reader;
import java.io.IOException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

/**
 * @author Edwin de Jonge
 *
 *         Analyzer for Dutch language. Supports an external list of
 *         stopwords (words that will not be indexed at all), an
 *         external list of exclusions (word that will not be stemmed,
 *         but indexed) and an external list of word-stem pairs that
 *         overrule the algorithm (dictionary stemming).  A default
 *         set of stopwords is used unless an alternative list is
 *         specified, the exclusion list is empty by default.  As
 *         start for the Analyzer the German Analyzer was used. The
 *         stemming algorithm implemented can be found at .....
 */
public class DutchAnalyzer
    extends Analyzer
{
    /**
     * List of typical Dutch stopwords.
     */
    private String[] DUTCH_STOP_WORDS = {
    "de", "en", "van", "ik", "te", "dat", "die", "in", "een",
    "hij", "het", "niet", "zijn", "is", "was", "op", "aan", "met", "als", "voor", "had",
    "er", "maar", "om", "hem", "dan", "zou", "of", "wat", "mijn", "men", "dit", "zo",
    "door", "over", "ze", "zich", "bij", "ook", "tot", "je", "mij", "uit", "der", "daar",
    "haar", "naar", "heb", "hoe", "heeft", "hebben", "deze", "u", "want", "nog", "zal",
    "me", "zij", "nu", "ge", "geen", "omdat", "iets", "worden", "toch", "al", "waren",
    "veel", "meer", "doen", "toen", "moet", "ben", "zonder", "kan", "hun", "dus",
    "alles", "onder", "ja", "eens", "hier", "wie", "werd", "altijd", "doch", "wordt",
    "wezen", "kunnen", "ons", "zelf", "tegen", "na", "reeds", "wil", "kon", "niets",
    "uw", "iemand", "geweest", "andere"
    };


    /**
     * Contains the stopwords used with the StopFilter.
     */
    private CharArraySet stoptable = LuceneUtil.newCharArraySet();

    /**
     * Contains words that should be indexed but not stemmed.
     */
    private CharArraySet excltable = LuceneUtil.newCharArraySet();

    private Map _stemdict = new HashMap();


    /**
     * Builds an analyzer.
     */
    public DutchAnalyzer()
    {
        stoptable = StopFilter.makeStopSet(LuceneUtil.VERSION, DUTCH_STOP_WORDS);

        _stemdict.put("fiets", "fiets"); //otherwise fiet
        _stemdict.put("bromfiets", "bromfiets"); //otherwise bromfiet
        _stemdict.put("ei", "eier");
        _stemdict.put("kind", "kinder");
    }

    /**
     * Builds an analyzer with the given stop words.
     *
     * @param stopwords
     */
    public DutchAnalyzer(String[] stopwords)
    {
        stoptable = StopFilter.makeStopSet(LuceneUtil.VERSION, stopwords);
    }

    /**
     * Builds an analyzer with the given stop words.
     *
     * @param stopwords
     */
    public DutchAnalyzer(CharArraySet stopwords)
    {
        stoptable = stopwords;
    }

    /**
     * Builds an analyzer with the given stop words.
     *
     * @param stopwords
     */
    public DutchAnalyzer(File stopwords)
        throws IOException
    {
        stoptable = WordlistLoader.getWordSet(stopwords);
    }

    /**
     * Builds an exclusionlist from an array of Strings.
     *
     * @param exclusionlist
     */
    public void setStemExclusionTable(String[] exclusionlist)
    {
        excltable = StopFilter.makeStopSet(LuceneUtil.VERSION, exclusionlist);
    }

    /**
     * Builds an exclusionlist from a Hashtable.
     */
    public void setStemExclusionTable(CharArraySet exclusionlist)
    {
        excltable = exclusionlist;
    }

    /**
     * Builds an exclusionlist from the words contained in the given file.
     */
    public void setStemExclusionTable(File exclusionlist)
        throws IOException
    {
        excltable = WordlistLoader.getWordSet(exclusionlist);
    }

    /**
     * Reads a stemdictionary file that overrules the stemming algorithm.
     * This is a textfile that contains per line
     * word \t stem
     * i.e: tab seperated.
     */
    public void setStemDictionary(File stemdict)
    {
        _stemdict = WordlistLoader.getStemDict(stemdict);
    }

    /**
     * Creates a TokenStream which tokenizes all the text in the
     * provided TextReader.
     *
     * @return A TokenStream build from a StandardTokenizer filtered
     * with StandardFilter, StopFilter, GermanStemFilter
     */
    protected TokenStreamComponents createComponents(String fieldName,
            Reader reader)
    {
        Tokenizer t = new StandardTokenizer(LuceneUtil.VERSION, reader);
        
        StandardFilter f = new StandardFilter(LuceneUtil.VERSION, t);
        StopFilter ts = new StopFilter(LuceneUtil.VERSION, f, stoptable);
        DutchStemFilter gf = new DutchStemFilter(ts, excltable, _stemdict);
        
        return new TokenStreamComponents(t, gf);
    }
}
