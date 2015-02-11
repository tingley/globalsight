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

import com.globalsight.ling.lucene.analysis.GSTokenTokenizer;

/**
 * This class uses the standard JDK BreakIterator Interface to further
 * tokenize a token.
 *
 * This class is not thread-safe as the BreakIterator interface allows
 * setting the input text at any time.  For multi-thread, use a unique
 * instance of BreakIterator in the constructor of this class.
 *
 * @author Pichai Ongvasith
 *
 */
public class BreakIteratorTokenTokenizer
    extends GSTokenTokenizer
{
    private final String type;
    private final BreakIterator bi;

    /**
     *
     * @param input TokenStream from other tokenizer
     * @param BreakIterator that will be used to tokenize input tokens
     * @param type is the type of token to be returned by this class.
     */
    public BreakIteratorTokenTokenizer(TokenStream input, BreakIterator bi,
        String type)
    {
        super(input);
        this.bi = bi;
        this.type = type;
    }

    /* (non-Javadoc)
     * @see com.po.lucene.parser.SubTokenStream#createSubStream(org.apache.lucene.analysis.Token)
     */
    protected TokenStream createSubStream(Token t)
    {
        if (t.type().equals(type))
        {
            bi.setText(t.toString());
            return new BreakIteratorAdaptor(t.toString(), bi,
                t.type(), t.startOffset());
        }

        return null;
    }

    @Override
    public Token next() throws IOException
    {
        Token t = getNextToken();
        return t;
    }
}
