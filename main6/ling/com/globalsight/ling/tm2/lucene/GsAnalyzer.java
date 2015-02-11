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
package com.globalsight.ling.tm2.lucene;

import org.apache.log4j.Logger;

import com.globalsight.util.GlobalSightLocale;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

/**
 * Lucene analyzer for GlobalSight. It tokenizes a string using Java's
 * word break iterator. It also filters out the stopwords.
 */

public class GsAnalyzer
    extends Analyzer
{
    private static final Logger c_logger =
        Logger.getLogger(
            GsAnalyzer.class);

    private GlobalSightLocale m_locale;
    
    /// Constructor
    public GsAnalyzer(GlobalSightLocale p_locale)
    {
        m_locale = p_locale;
    }
    

    /**
     * Overridden method from Analyzer.
     */
    public TokenStream tokenStream(String p_fieldName, Reader p_reader)
    {
        TokenStream result = null;
        
        try
        {
            result = new GsTokenizer(p_reader, m_locale);
            result = new GsStopFilter(result, m_locale);
            result = new GsStemFilter(result, m_locale);
        }
        catch(Exception e)
        {
            // can't throw checked exception
            c_logger.error("An error occured in tokenStream", e);
            
            throw new RuntimeException(e);
        }
        
        return result;
    }
}
