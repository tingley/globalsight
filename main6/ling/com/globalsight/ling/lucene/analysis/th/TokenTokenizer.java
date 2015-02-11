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

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

import com.globalsight.ling.lucene.analysis.GSTokenFilter;
import com.globalsight.ling.tm2.lucene.LuceneUtil;

/**
 * This abstract class provides a logic to tokenizes an input token
 * and return TokenStream of the subtokens. Concrete subclasses have
 * to implement the createSubStream method to tokenize the input
 * token. Subclasses then can be used in TokenFilter chain to handle
 * some types of text that require a special tokenization algorithm.
 *
 * @author Pichai Ongvasith
 *
 */
public abstract class TokenTokenizer
    extends GSTokenFilter
{
    // Token stream for the current token that is returned from the
    // wrapped token stream.
    private TokenStream subTokenStream;

    public TokenTokenizer(TokenStream input)
    {
        super(input);
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.analysis.TokenStream#next()
     */
    public Token next()
        throws IOException
    {
        Token ret = nextFromSubStream();

        if (ret == null)
        {
            Token mainToken = getNextToken();
            if (mainToken == null)
            {
                return null;
            }
            subTokenStream = createSubStream(mainToken);
            ret = subTokenStream == null ? mainToken : nextFromSubStream();
        }

        return ret;
    }

    private Token nextFromSubStream()
        throws IOException
    {
        Token ret = null;

        if (subTokenStream != null)
        {
            Token subToken = LuceneUtil.getNextToken(subTokenStream);
            if (subToken != null)
            {
                ret = subToken;
            }
            else
            {
                subTokenStream = null;
            }
        }

        return ret;
    }

    /**
     * Create a TokenStream from the input token.  Return null if the
     * subclass does not want to tokenize the input token.  The whole
     * input token will be returned by next() of this class.
     * @param t
     * @return
     */
    abstract protected TokenStream createSubStream(Token t);
}
