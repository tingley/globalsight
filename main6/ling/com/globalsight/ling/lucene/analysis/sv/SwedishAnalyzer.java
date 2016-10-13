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
package com.globalsight.ling.lucene.analysis.sv;

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
public class SwedishAnalyzer
    extends SnowballAnalyzer
{
    static private String[] s_stopwords = {
    "och", "det", "att", "i", "en", "jag", "hon", "som", "han",
    "på", "den", "med", "var", "sig", "för", "så", "till", "är",
    "men", "ett", "om", "hade", "de", "av", "icke", "mig", "du",
    "henne", "då", "sin", "nu", "har", "inte", "hans", "honom",
    "skulle", "hennes", "där", "min", "man", "ej", "vid", "kunde",
    "något", "från", "ut", "när", "efter", "upp", "vi", "dem",
    "vara", "vad", "över", "än", "dig", "kan", "sina", "här", "ha",
    "mot", "alla", "under", "någon", "eller", "allt", "mycket",
    "sedan", "ju", "denna", "själv", "detta", "åt", "utan", "varit",
    "hur", "ingen", "mitt", "ni", "bli", "blev", "oss", "din",
    "dessa", "några", "deras", "blir", "mina", "samma", "vilken",
    "er", "sådan", "vår", "blivit", "dess", "inom", "mellan",
    "sådant", "varför", "varje", "vilka", "ditt", "vem", "vilket",
    "sitta", "sådana", "vart", "dina", "vars", "vårt", "våra",
    "ert", "era", "vilkas",
    };

    public SwedishAnalyzer()
    {
        super("Swedish", s_stopwords);
    }
}
