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
package com.globalsight.ling.lucene.analysis.da;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;

import com.globalsight.ling.lucene.analysis.snowball.SnowballAnalyzer;

import java.io.Reader;
import java.util.Set;

/**
 * Filters {@link StandardTokenizer} with {@link StandardFilter},
 * {@link LowerCaseFilter}, {@link StopFilter} and {@link SnowballFilter}.
 *
 * Available stemmers are listed in {@link net.sf.snowball.ext}.  The
 * name of a stemmer is the part of the class name before "Stemmer",
 * e.g., the stemmer in {@link EnglishStemmer} is named "English".
 */
public class DanishAnalyzer
    extends SnowballAnalyzer
{
    static private String[] s_stopwords = {
    "ad", "af", "alle", "alt", "anden", "at", "blev", "blive", "bliver",
    "da", "de", "dem", "den", "denne", "der", "deres", "det", "dette",
    "dig", "din", "disse", "dog", "du", "efter", "eller", "en", "end",
    "er", "et", "for", "fra", "ham", "han", "hans", "har", "havde",
    "have", "hende", "hendes", "her", "hos", "hun", "hvad", "hvis", "hvor",
    "i", "ikke", "ind", "jeg", "jer", "jo", "kunne", "man", "mange", "med",
    "meget", "men", "mig", "min", "mine", "mit", "mod", "ned", "noget",
    "nogle", "nu", "når", "og", "også", "om", "op", "os", "over", "på",
    "selv", "sig", "sin", "sine", "sit", "skal", "skulle", "som", "sådan",
    "thi", "til", "ud", "under", "var", "vi", "vil", "ville", "vor",
    "være", "været",
    };

    public DanishAnalyzer()
    {
        super("Danish", s_stopwords);
    }
}
