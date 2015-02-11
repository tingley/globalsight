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

import com.plug.Version_8_5_2.gs.ling.lucene.analysis.GSTokenFilter;
import com.plug.Version_8_5_2.gs.util.GlobalSightLocale;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Token;

/**
 * Stems words from a token stream.
 */

class GsStemFilter
    extends GSTokenFilter
{
    private GsStemmer m_stemmer;
    
    /// Constructor
    public GsStemFilter(
        TokenStream p_tokenStream, GlobalSightLocale p_locale)
    {
        super(p_tokenStream);
        m_stemmer = new GsStemmer(p_locale);
    }
    

    /**
     * Stems the next input Token and returns it.
     */
    public final Token next()
      throws IOException
    {
        Token token = getNextToken();
        if(token != null)
        {
            String stemmed = m_stemmer.stem(token.toString());
            if(! stemmed.equals(token.toString()))
            {
                token = new Token(stemmed, token.startOffset(),
                    token.endOffset(), token.type());
            }
        }

        return token;
    }
    
}
