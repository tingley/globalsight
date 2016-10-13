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
package com.globalsight.ling.lucene.analysis.fi;

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
public class FinnishAnalyzer
    extends SnowballAnalyzer
{
    static private String[] s_stopwords = {
    // This is a slightly illegal list.  Another, much longer list is
    // available from http://www.unine.ch/info/clef/finnishST.txt.

    "aikana", "aikaisin", "aikasemmin", "aina", "ainakin", "ainoastaan",
    "‰l‰", "alas", "‰lk‰‰", "alla", "alle", "edess‰", "ehk‰", "ei",
    "eik‰", "eikˆ", "eiv‰t", "eli", "ellen", "ellet", "emme", "en",
    "en‰‰", "ennen", "esimerkiksi", "et", "ett‰", "ette", "etten", "ettet",
    "halki", "haluaisi", "haluaisimme", "haluaisin", "haluaisit",
    "haluaisitte", "haluaisivat", "h‰n", "h‰nell‰", "h‰nelle",
    "h‰nelt‰", "h‰nen", "h‰ness‰", "h‰nest‰", "h‰net", "h‰nt‰", "harvoin",
    "he", "heid‰n", "heid‰t", "heill‰", "heille", "heilt‰", "heiss‰",
    "heist‰", "heit‰", "heti", "hetkinen", "hyvin", "ihan", "ilman",
    "ja", "j‰lkeen", "j‰lleen", "jo", "johon", "johonkin", "johonkuhun",
    "joiden", "joihin", "joilla", "joille", "joilta", "joita", "joista",
    "joka", "jokin", "joko", "joksikin", "joku", "joissa", "jolla",
    "jollakin", "jollakulla", "jolle", "jollekin", "jollekulle", "jollen",
    "jollet", "jolta", "joltakin", "jonakuna", "jonka", "jonkin", "jonkun",
    "jonnekin", "joo", "jopa", "jos", "joskus", "josta", "jossa", "jossakin",
    "jostakin", "jostakusta", "jota", "jotakin", "jotakuta", "jotka", "jotta",
    "jotten", "jottet", "kaikki", "kaikin", "kaiken", "kaikkiaan", "kaikkea",
    "kaikesta", "kaikkein", "kaikkien", "kaikissa", "kanssa", "kautta",
    "kenell‰", "kenelle", "kenelt‰", "kenen", "kenest‰", "kenet", "keskell‰",
    "ket‰", "kiitos", "kohtaan", "kohti", "koska", "koskaan", "kovin", "kuin",
    "kuinka", "kuka", "kumpi", "kumpikaan", "kun", "kunhan", "l‰hell‰",
    "liika", "liian", "luo", "luona", "l‰pi", "me", "meid‰n", "meid‰t",
    "meill‰", "meille", "meilt‰", "meiss‰", "meist‰", "meit‰", "mihin",
    "mik‰", "mik‰li", "miksi", "mill‰", "millainen", "millaiseksi",
    "millaisella", "millaiselle", "millaiselta", "millaisen", "millaisena",
    "millaisessa", "millaisesta", "millaiset", "millaisi", "millaisiin",
    "millaisiksi", "millaisilla", "millaisille", "millaisilta", "millaisina",
    "millaisissa", "millaista", "millaisten", "mille", "milloin", "milt‰",
    "min‰", "mink‰", "minne", "minua", "minulla", "minulle", "minulta",
    "minun", "minussa", "minusta", "minut", "miss‰", "mist‰", "mit‰",
    "mit‰‰n", "miten", "mukaan", "mutta", "muu", "muun", "muut", "muuta",
    "muutama", "muuten", "myˆhemmin", "myˆs", "myˆsk‰‰n", "n‰ihin", "n‰in",
    "n‰ill‰", "n‰ille", "n‰ilt‰", "n‰in‰", "n‰ist‰", "n‰m‰", "ne", "niiden",
    "niihin", "niill‰", "niille", "niilt‰", "niin", "niinkuin", "niit‰", "niin‰",
    "niiss‰", "niist‰", "no", "noiden", "noihin", "noilla", "noille",
    "noilta", "noin", "noina", "noissa", "noista", "noita", "nuo", "nyt",
    "ohi", "ole", "olemme", "olen", "olen", "olet", "olette", "oli", "olisi",
    "olisin", "olisivat", "olimme", "olin", "olit", "olitte", "olivat", "olkaa",
    "olla", "ollut", "olleet", "on", "ovat", "p‰‰st‰", "p‰‰ll‰", "pakko",
    "paljon", "pian", "pit‰‰", "pit‰isi", "pitkin", "poikki", "puhdas",
    "sangen", "se", "sek‰", "sellainen", "sen", "siell‰", "sielt‰", "siihen",
    "siin‰", "siis", "siit‰", "siksi", "sill‰", "sille", "silt‰", "sin‰",
    "sinne", "sinulla", "sinulle", "sinulta", "sinun", "sinussa", "sinusta",
    "sis‰ll‰", "sit‰", "sitten", "siten", "t‰‰ll‰", "t‰‰lt‰", "taas",
    "t‰h‰n", "t‰h‰n", "tahansa", "tahtoisi", "tahtoisimme", "tahtoisin",
    "tahtoisit", "tahtoisitte", "tahtoisivat", "tai", "takana", "takia",
    "t‰llainen", "t‰m‰", "t‰m‰n", "t‰n‰", "t‰n‰‰n", "t‰nne", "t‰ss‰", "t‰st‰",
    "t‰t‰", "t‰ytyy", "te", "teid‰n", "teid‰t", "teill‰", "teille", "teilt‰",
    "teiss‰", "teist‰", "teit‰", "toinen", "toisen", "toisin", "toiseksi",
    "toisaalla", "toisella", "toisensa", "toista", "toisaalta", "tokko",
    "tulla", "tuo", "tuohon", "tuolla", "tuollainen", "tuolle", "tuolta",
    "tuon", "tuona", "tuonne", "tuossa", "tuosta", "tuota", "ulkopuolella",
    "usein", "vaan", "v‰h‰n", "vai", "vaikka", "vain", "v‰liss‰", "v‰lill‰",
    "varten", "vastaan", "vastap‰‰t‰", "vasten", "viel‰", "vieress‰", "voi",
    "voida", "voin", "voinut", "voisimme", "voisin", "voisit", "voisitte",
    "voisivat", "voit", "voitte", "vosin", "yh‰", "yht‰", "yli", "yll‰",
    "yl‰puolella", "ylh‰‰ll‰", "ylˆs", "ymp‰rill‰",
    };

    public FinnishAnalyzer()
    {
        super("Finnish", s_stopwords);
    }
}
