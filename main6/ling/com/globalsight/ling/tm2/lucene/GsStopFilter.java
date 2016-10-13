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

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.lucene.analysis.GSTokenFilter;
import com.globalsight.ling.tm2.indexer.StopWord;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Token;

/**
 * Removes stop words from a token stream.
 */

class GsStopFilter
    extends GSTokenFilter
{
    private StopWord m_stopWord;
    
    /// Constructor
    public GsStopFilter(
        TokenStream p_tokenStream, GlobalSightLocale p_locale)
        throws Exception
    {
        super(p_tokenStream);
        m_stopWord = StopWord.getStopWord(p_locale);
    }

	public GsStopFilter(TokenStream p_tokenStream, GlobalSightLocale p_locale,
			boolean p_careStopWordFile) throws Exception
	{
		super(p_tokenStream);
		if (p_careStopWordFile)
		{
			m_stopWord = StopWord.getStopWord(p_locale);
		}
		else
		{
			m_stopWord = StopWord.getBaseStopWord(p_locale);
		}
	}

    /**
     * Returns the next input Token whose termText() is not a stop word.
     */
    public final Token next()
      throws IOException
    {
        // return the first non-stop word found
        for (Token token = getNextToken(); token != null; token = getNextToken())
            if(!m_stopWord.isStopWord(token.toString()))
                return token;

        // reached EOS -- return null
        return null;
    }
    
}
