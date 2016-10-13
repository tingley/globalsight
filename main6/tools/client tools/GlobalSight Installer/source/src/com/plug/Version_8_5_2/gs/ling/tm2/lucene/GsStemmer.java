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
package com.plug.Version_8_5_2.gs.ling.tm2.lucene;

import com.plug.Version_8_5_2.gs.util.GlobalSightLocale;
import com.plug.Version_8_5_2.gs.ling.lucene.analysis.snowball.snowball.SnowballProgram;
import com.plug.Version_8_5_2.gs.ling.lucene.analysis.snowball.snowball.ext.*;

/**
 * Locale dependent stemmer factory. Snowball stemmers are used.
 */

class GsStemmer
{
    private SnowballProgram m_stemmer;
    
    /// Constructor
    public GsStemmer(GlobalSightLocale p_locale)
    {
        m_stemmer = getStemmer(p_locale);
    }
    

    /**
     * Stems the specified term.
     */
    public String stem(String p_term)
    {
        m_stemmer.setCurrent(p_term);
        m_stemmer.stem();
        return m_stemmer.getCurrent();
    }
    
        
    /**
     * returns a locale dependent stemmer. If a stemmer for a
     * specified locale cannot be found, it returns a stemmer that
     * doesn't stem.
     */
    private SnowballProgram getStemmer(GlobalSightLocale p_locale)
    {
        SnowballProgram stemmer = null;
        String langCode = p_locale.getLanguageCode().toLowerCase();
        
        if(langCode.equals("en"))
        {
            stemmer = new EnglishStemmer();
        }
        else if(langCode.equals("da"))
        {
            stemmer = new DanishStemmer();
        }
        else if(langCode.equals("nl"))
        {
            stemmer = new DutchStemmer();
        }
        else if(langCode.equals("fi"))
        {
            stemmer = new FinnishStemmer();
        }
        else if(langCode.equals("fr"))
        {
            stemmer = new FrenchStemmer();
        }
        else if(langCode.equals("de"))
        {
            stemmer = new GermanStemmer();
        }
        else if(langCode.equals("it"))
        {
            stemmer = new ItalianStemmer();
        }
        else if(langCode.equals("no"))
        {
            stemmer = new NorwegianStemmer();
        }
        else if(langCode.equals("pt"))
        {
            stemmer = new PortugueseStemmer();
        }
        else if(langCode.equals("ru"))
        {
            stemmer = new RussianStemmer();
        }
        else if(langCode.equals("es"))
        {
            stemmer = new SpanishStemmer();
        }
        else if(langCode.equals("sv"))
        {
            stemmer = new SwedishStemmer();
        }
        else
        {
            stemmer = new SnowballProgram()
                {
                    public boolean stem()
                    {
                        return true;
                    }
                };
        }
        
        return stemmer;
    }
    
}
