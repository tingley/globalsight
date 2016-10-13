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
package com.globalsight.ling.lucene.analysis.th;

import java.io.IOException;
import java.text.BreakIterator;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import com.globalsight.ling.lucene.analysis.GSTokenStream;

/**
 * This class wraps the {@link java.txt.BreakIterator BreakIterator}
 * and makes it behave like a TokenStream.
 *
 * @author Pichai Ongvasith
 *
 */
public class BreakIteratorAdaptor
    extends GSTokenStream
{
    private final BreakIterator bi;
    private final String type;
    private final String text;
    private final int startOffSet;

    /**
     * @param text The text that the BreakIterator is breaking.  This
     * parameter is necessary because BreakIterator uses the interface
     * CharacterIterator to iterate the input. Converting
     * CharacterIterator to String is not efficient.
     * @param bi BreakIterator to be wrapped by this class.
     * @param type The type of token to be returned by next()
     * @param startOffSet The startOffSet of the first token to be
     * retured by next()
     */
    public BreakIteratorAdaptor(String text, BreakIterator bi,
        String type, int startOffSet)
    {
        this.bi = bi;
        this.type = type;
        this.text = text;
        bi.first();
        this.startOffSet = startOffSet;
    }

    public Token next()
        throws IOException
    {
        int start = bi.current();
        int next = bi.next();

        if (next != BreakIterator.DONE)
        {
            return new Token(text.substring(start, next),
                start + startOffSet, next + startOffSet, type);
        }

        return null;
    }
}
