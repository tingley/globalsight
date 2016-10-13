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

package com.globalsight.ling.lucene.analysis.ngram;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;

import com.globalsight.ling.lucene.analysis.GSTokenizer;

import java.io.IOException;
import java.io.Reader;

/**
 * Produces ngrams out of the input string. ngrams have boundary
 * markers inserted at the beginning and end of the text: "_text_".
 * The spaces mark beginning and end of words.
 */
public class NgramTokenizer
    extends GSTokenizer
{
    /** n-gram count, 3 or 4. */
    private int m_ngram;

    private StringBuffer m_buffer;
    private int m_offset = 0;

    //
    // Constructor
    //
    public NgramTokenizer(Reader p_input, int p_ngram)
    {
        super(p_input);
        m_ngram = p_ngram;
    }
    
    @Override
    public void reset()
    {
        m_buffer = null;
        m_offset = 0;
    }

    /**
     * Returns the next token in the stream, or null at EOS.
     *
     * @return Token
     * @throws IOException - throw IOException when read error
     * happened in the InputStream
     */
    final public Token next()
        throws IOException
    {
        Token result;

        // First time around, read the entire input.
        if (m_buffer == null)
        {
            m_buffer = fillBuffer();

            // If input is too short for a full ngram, return null.
            if (m_buffer.length() < m_ngram)
            {
                m_offset = m_buffer.length();
                return null;
            }
        }

        if (m_offset + m_ngram <= m_buffer.length())
        {
            result = new Token(
                m_buffer.substring(m_offset, m_offset + m_ngram),
                m_offset, m_offset + m_ngram - 1, "ngram");

            ++m_offset;

            return result;
        }

        return null;
    }

    final private StringBuffer fillBuffer()
        throws IOException
    {
        StringBuffer result = new StringBuffer();
        int c;

        // append a begin of word character
        result.append(" ");

        while ((c = input.read()) != -1)
        {
            final char cc = (char)c;

            if (isTokenChar(cc))
            {
                result.append(normalize(cc));
            }
        }

        // append an end of word character
        result.append(" ");

        return result;
    }

    /**
     * Returns true iff a character should be included in a token.
     * This tokenizer discards all characters that do not satisfy this
     * predicate. The default implementation allows all characters.
     */
    /*abstract*/protected boolean isTokenChar(char c)
    {
        return true;
    }

    /**
     * Called on each token character to normalize it before it is
     * added to the token.  The default implementation lowercases the
     * character.
     */
    /*abstract*/protected char normalize(char c)
    {
        return Character.toLowerCase(c);
    }
}
