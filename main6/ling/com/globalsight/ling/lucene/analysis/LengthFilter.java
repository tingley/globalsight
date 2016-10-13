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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;

/**
 * Removes words that are too long and too short from the stream.
 *
 * @author David Spencer
 * @version $Id: LengthFilter.java,v 1.2 2013/09/13 06:22:16 wayne Exp $
 */
public final class LengthFilter
    extends GSTokenFilter
{
    final int min;
    final int max;

    /**
     * Build a filter that removes words that are too long or too
     * short from the text.
     */
    public LengthFilter(TokenStream in, int min, int max)
    {
        super(in);
        this.min = min;
        this.max =max;
    }

    /**
     * Returns the next input Token whose termText() is the right len
     */
    public final Token next()
        throws IOException
    {
        // return the first non-stop word found
        for (Token token = getNextToken(); token != null; token = getNextToken())
        {
            int len = token.toString().length();

            if ( len >= min && len <= max)
            {
                return token;
            }
            // note: else we ignore it but should we index each part of it?
        }

        // reached EOS -- return null
        return null;
    }
}
