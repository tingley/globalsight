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

import org.apache.log4j.Logger;

import com.plug.Version_8_5_2.gs.ling.lucene.analysis.GSTokenizer;
import com.plug.Version_8_5_2.gs.util.GlobalSightLocale;

import java.io.Reader;
import java.io.IOException;
import java.text.BreakIterator;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Token;

/**
 * Lucene analyzer for GlobalSight. It tokenizes a string using Java's
 * word break iterator. It also filters out the stopwords.
 */

class GsTokenizer
    extends GSTokenizer
{
    private static final Logger c_logger =
        Logger.getLogger(
            GsTokenizer.class);

    private BreakIterator m_wordIterator;
    private String m_text;
    
    /// Constructor
    public GsTokenizer(Reader p_input, GlobalSightLocale p_locale)
        throws Exception
    {
        super(p_input);
        
        m_text = getText(p_input);
        m_wordIterator = getWordBreakIterator(m_text, p_locale);
    }
    

    /** Returns the next token in the stream, or null at EOS.
     */
    final public Token next()
    {
        Token token = null;
        
        int start = m_wordIterator.current();
        int end = m_wordIterator.next();
        if(end != BreakIterator.DONE)
        {
            String tokenString
                = m_text.substring(start, end).toLowerCase();
            token = new Token(tokenString, start, end);
        }
        
        return token;
    }
    

    private BreakIterator getWordBreakIterator(
        String p_text, GlobalSightLocale p_locale)
    {
        BreakIterator wordIterator
            = BreakIterator.getWordInstance(p_locale.getLocale());
        wordIterator.setText(p_text);
        
        // first() must be called for getting ready to a subsequnce
        // next() call
        wordIterator.first();
        
        return wordIterator;
    }


    private String getText(Reader p_reader)
        throws Exception
    {
        StringBuffer sb = new StringBuffer();
        char[] cbuf = new char[4096];
        
        int cnt = 0;
        while((cnt = p_reader.read(cbuf)) != -1)
        {
            sb.append(cbuf, 0, cnt);
        }
        
        return sb.toString();
    }
    
        
}
