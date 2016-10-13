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

package com.globalsight.ling.lucene.analysis;

import com.globalsight.ling.lucene.analysis.cjk.CJKAnalyzer;
import com.globalsight.ling.lucene.analysis.cn.ChineseAnalyzer;
import com.globalsight.ling.lucene.analysis.cz.CzechAnalyzer;
import com.globalsight.ling.lucene.analysis.da.DanishAnalyzer;
import com.globalsight.ling.lucene.analysis.de.GermanAnalyzer;
import com.globalsight.ling.lucene.analysis.es.SpanishAnalyzer;
import com.globalsight.ling.lucene.analysis.fi.FinnishAnalyzer;
import com.globalsight.ling.lucene.analysis.fr.FrenchAnalyzer;
import com.globalsight.ling.lucene.analysis.it.ItalianAnalyzer;
import com.globalsight.ling.lucene.analysis.nl.DutchAnalyzer;
import com.globalsight.ling.lucene.analysis.no.NorwegianAnalyzer;
import com.globalsight.ling.lucene.analysis.pl.PolishAnalyzer;
import com.globalsight.ling.lucene.analysis.pt.PortugueseAnalyzer;
import com.globalsight.ling.lucene.analysis.pt_br.BrazilianAnalyzer;
import com.globalsight.ling.lucene.analysis.ru.RussianAnalyzer;
import com.globalsight.ling.lucene.analysis.sv.SwedishAnalyzer;

import com.globalsight.ling.lucene.analysis.ngram.NgramAnalyzer;
import com.globalsight.ling.lucene.analysis.ngram.NgramNoPunctuationAnalyzer;
import com.globalsight.ling.tm2.lucene.LuceneUtil;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

import java.util.*;

public class AnalyzerFactory
{
    /** Token creation extracts lower-case words. */
    static public final int TOKENIZE_NONE = 1;
    /** Token creation extracts stemmed words. */
    static public final int TOKENIZE_STEM = 2;
    /** Token creation extracts lower-cased trigrams. */
    static public final int TOKENIZE_3GRAM = 3;

    static HashMap s_map = new HashMap();

    static
    {
        s_map.put("ja", new CJKAnalyzer());
        s_map.put("ko", new CJKAnalyzer());
        s_map.put("cn", new ChineseAnalyzer());

        s_map.put("cz", new CzechAnalyzer());
        s_map.put("da", new DanishAnalyzer());
        s_map.put("de", new GermanAnalyzer());
        s_map.put("es", new SpanishAnalyzer());
        s_map.put("fi", new FinnishAnalyzer());
        s_map.put("fr", new FrenchAnalyzer());
        s_map.put("it", new ItalianAnalyzer());
        s_map.put("nl", new DutchAnalyzer());
        s_map.put("no", new NorwegianAnalyzer());
        s_map.put("pl", new PolishAnalyzer());
        s_map.put("pt", new PortugueseAnalyzer());
        s_map.put("pt_br", new BrazilianAnalyzer());
        s_map.put("ru", new RussianAnalyzer());
        s_map.put("sv", new SwedishAnalyzer());
    }

    private AnalyzerFactory(){}

    static public Analyzer getInstance(String p_locale, int p_type)
    {
        Analyzer result;

        if (p_type == TOKENIZE_STEM)
        {
            // Try the full locale.
            result = (Analyzer)s_map.get(p_locale);

            // Try the language part of locale.
            if (result == null)
            {
                p_locale = p_locale.substring(0, 2);

                result = (Analyzer)s_map.get(p_locale);
            }

            // No known stemmer, return default word-based analyzer.
            if (result == null)
            {
                result = new StandardAnalyzer(LuceneUtil.VERSION);
            }
        }
        else if (p_type == TOKENIZE_3GRAM)
        {
            //result = new NgramNoPunctuationAnalyzer(3);
            result = new NgramAnalyzer(3);
        }
        else
        {
            result = new StandardAnalyzer(LuceneUtil.VERSION);
        }

        return result;
    }
}
