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
package com.globalsight.ling.lucene.analysis.it;

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
public class ItalianAnalyzer
    extends SnowballAnalyzer
{
    static private String[] s_stopwords = {
    "ad", "al", "allo", "ai", "agli", "all", "agl", "alla", "alle",
    "con", "col", "coi", "da", "dal", "dallo", "dai", "dagli", "dall",
    "dagl", "dalla", "dalle", "di", "del", "dello", "dei", "degli",
    "dell", "degl", "della", "delle", "in", "nel", "nello", "nei",
    "negli", "nell", "negl", "nella", "nelle", "su", "sul", "sullo",
    "sui", "sugli", "sull", "sugl", "sulla", "sulle", "per", "tra",
    "contro", "io", "tu", "lui", "lei", "noi", "voi", "loro", "mio",
    "mia", "miei", "mie", "tuo", "tua", "tuoi", "tue", "suo", "sua",
    "suoi", "sue", "nostro", "nostra", "nostri", "nostre", "vostro",
    "vostra", "vostri", "vostre", "mi", "ti", "ci", "vi", "lo", "la",
    "li", "le", "gli", "ne", "il", "un", "uno", "una", "ma", "ed", "se",
    "perché", "anche", "come", "dov", "dove", "che", "chi", "cui", "non",
    "più", "quale", "quanto", "quanti", "quanta", "quante", "quello",
    "quelli", "quella", "quelle", "questo", "questi", "questa", "queste",
    "si", "tutto", "tutti",

    // single letter forms:
    "a", "c", "e", "i", "l", "o",

    // forms of avere, to have (not including the infinitive):
    "ho", "hai", "ha", "abbiamo", "avete", "hanno", "abbia", "abbiate",
    "abbiano", "avrò", "avrai", "avrà", "avremo", "avrete", "avranno",
    "avrei", "avresti", "avrebbe", "avremmo", "avreste", "avrebbero",
    "avevo", "avevi", "aveva", "avevamo", "avevate", "avevano", "ebbi",
    "avesti", "ebbe", "avemmo", "aveste", "ebbero", "avessi", "avesse",
    "avessimo", "avessero", "avendo", "avuto", "avuta", "avuti", "avute",

    // forms of essere, to be (not including the infinitive):
    "sono", "sei", "è", "siamo", "siete", "sia", "siate", "siano",
    "sarò", "sarai", "sarà", "saremo", "sarete", "saranno", "sarei",
    "saresti", "sarebbe", "saremmo", "sareste", "sarebbero",
    "ero", "eri", "era", "eravamo", "eravate", "erano",
    "fui", "fosti", "fu", "fummo", "foste", "furono",
    "fossi", "fosse", "fossimo", "fossero", "essendo",

    // forms of fare, to do (not including the infinitive, fa, fat-):
    "faccio", "fai", "facciamo", "fanno", "faccia", "facciate", "facciano",
    "farò", "farai", "farà", "faremo", "farete", "faranno", "farei", "faresti",
    "farebbe", "faremmo", "fareste", "farebbero", "facevo", "facevi",
    "faceva", "facevamo", "facevate", "facevano", "feci", "facesti", "fece",
    "facemmo", "faceste", "fecero", "facessi", "facesse", "facessimo",
    "facessero", "facendo",

    // forms of stare, to be (not including the infinitive):
    "sto", "stai", "sta", "stiamo", "stanno", "stia", "stiate", "stiano",
    "starò", "starai", "starà", "staremo", "starete", "staranno", "starei",
    "staresti", "starebbe", "staremmo", "stareste", "starebbero", "stavo",
    "stavi", "stava", "stavamo", "stavate", "stavano", "stetti", "stesti",
    "stette", "stemmo", "steste", "stettero", "stessi", "stesse", "stessimo",
    "stessero", "stando",
    };

    public ItalianAnalyzer()
    {
        super("Italian", s_stopwords);
    }
}
