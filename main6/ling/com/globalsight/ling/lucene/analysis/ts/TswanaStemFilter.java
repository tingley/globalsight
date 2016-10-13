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
// http://issues.apache.org/bugzilla/show_bug.cgi?id=32580
package com.globalsight.ling.lucene.analysis.ts;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

import com.globalsight.ling.lucene.analysis.GSTokenFilter;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;

/**
 * A filter that stems Tswana words. It supports a table of words that
 * should not be stemmed at all. The stemmer used can be changed at
 * runtime after the filter object is created (as long as it is a
 * TswanaStemmer).
 *
 */
public final class TswanaStemFilter
    extends GSTokenFilter
{
    /**
     * The actual token in the input stream.
     */
    private Token token = null;
    private TswanaStemmer stemmer = null;
    private Set exclusionSet = null;

    private String stems[] = null;
    private int stemsPointer = -1;


    public TswanaStemFilter(TokenStream in)
    {
        super(in);
        stemmer = new TswanaStemmer();
    }

    /**
     * Builds a TswanaStemFilter that uses an exclusiontable.
     * @deprecated Use {@link #TswanaStemFilter(org.apache.lucene.analysis.TokenStream, java.util.Set)} instead.
     */
    public TswanaStemFilter(TokenStream in, Hashtable exclusiontable)
    {
        this(in);
        exclusionSet = new HashSet(exclusiontable.keySet());
    }

    /**
     * Builds a TswanaStemFilter that uses an exclusiontable.
     */
    public TswanaStemFilter(TokenStream in, Set exclusionSet)
    {
        this(in);
        this.exclusionSet = exclusionSet;
    }

    /**
     * @return  Returns the next token in the stream, or null at EOS
     public final Token next()
     throws IOException
     {
     System.out.,println("Insdie token next()");
     if ((token = input.next()) == null) {
     return null;
     }
     // Check the exclusiontable
     else if (exclusionSet != null && exclusionSet.contains(token.termText())) {
     return token;
     }
     else {
     String s = stemmer.stem(token.termText());
     // If not stemmed, dont waste the time creating a new token
     if (!s.equals(token.termText())) {
     return new Token(s, token.startOffset(),
     token.endOffset(), token.type());
     }
     return token;
     }
     }
    */

    /**
     * @return  Returns the next token in the stream, or null at EOS
     */
    public final Token next()
        throws IOException
    {
        String s = null;

        if (stems != null)
        {
            if (stemsPointer < stems.length)
            {
                token = new Token(stems[stemsPointer], 0,
                    stems[stemsPointer].length());

                stemsPointer++;

                if (stemsPointer == stems.length)
                {
                    stems = null;
                    stemsPointer = -1;
                }
            }
        }
        else
        {
            token = getNextToken();
        }

        if (token  == null)
        {
            return null;
        }
        // Check the exclusiontable
        else if (exclusionSet != null &&
            exclusionSet.contains(token.toString()))
        {
            return token;
        }
        else
        {
            if (stems == null)
            {
                stems = stemmer.multipleStems(token.toString());

                if (stems != null)
                {
                    stemsPointer = 0;
                    token = new Token(stems[stemsPointer], 0,
                        stems[stemsPointer].length());
                    stemsPointer++;
                }
            }

            s = stemmer.stem(token.toString());

            //s = stemmer.stem(token.termText());
            // If not stemmed, dont waste the time creating a new token
            if (!s.equals(token.toString()))
            {
                return new Token(s, token.startOffset(),
                    token.endOffset(), token.type());
            }

            return token;
        }
    }

    /**
     * Set a alternative/custom TswanaStemmer for this filter.
     */
    public void setStemmer(TswanaStemmer stemmer)
    {
        if (stemmer != null)
        {
            this.stemmer = stemmer;
        }
    }

    /**
     * Set an alternative exclusion list for this filter.
     * @deprecated Use {@link #setExclusionSet(java.util.Set)} instead.
     */
    public void setExclusionTable(Hashtable exclusiontable)
    {
        exclusionSet = new HashSet(exclusiontable.keySet());
    }

    /**
     * Set an alternative exclusion list for this filter.
     */
    public void setExclusionSet(Set exclusionSet)
    {
        this.exclusionSet = exclusionSet;
    }
}
