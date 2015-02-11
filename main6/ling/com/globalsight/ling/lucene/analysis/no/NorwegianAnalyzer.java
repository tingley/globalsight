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
package com.globalsight.ling.lucene.analysis.no;

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
public class NorwegianAnalyzer
    extends SnowballAnalyzer
{
    static private String[] s_stopwords = {
    "og","i","jeg","det","at","en","den","til","er","som","på",
    "de","med","han","av","ikke","inte","der","så","var","meg",
    "seg","men","ett","har","om","vi","min","mitt","ha","hade",
    "hu","hun","nå","over","da","ved","fra","du","ut","sin",
    "dem","oss","opp","man","kan","hans","hvor","eller","hva",
    "skal","selv","sjøl","her","alle","vil","bli","ble","blei",
    "blitt","kunne","inn","når","være","kom","noen","noe","ville",
    "dere","de","som","deres","kun","ja","etter","ned","skulle",
    "denne","for","deg","si","sine","sitt","mot","å","meget",
    "hvorfor","sia","sidan","dette","desse","disse","uden","hvordan",
    "ingen","inga","din","ditt","blir","samme","hvilken","hvilke",
    "sånn","inni","mellom","vår","hver","hvem","vors","dere",
    "deres","hvis","både","båe","begge","siden","dykk","dykkar",
    "dei","deira","deires","deim","di","då","eg","ein","ei","eit",
    "eitt","elles","honom","hjå","ho","hoe","henne","hennar","hennes",
    "hoss","hossen","ikkje","ingi","inkje","korleis","korso","kva",
    "kvar","kvarhelst","kven","kvi","kvifor","me","medan","mi","mine",
    "mykje","no","nokon","noka","nokor","noko","nokre","si","sia","sidan",
    "so","somt","somme","um","upp","vere","er","var","vore","verte",
    "vort","varte","vart","er","være","var","å",
    };

    public NorwegianAnalyzer()
    {
        super("Norwegian", s_stopwords);
    }
}
