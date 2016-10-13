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
package com.globalsight.ling.lucene.analysis.pt_br;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Lucene" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Lucene", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

import com.globalsight.ling.lucene.analysis.GSTokenFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * Brazilian stem filter based on (copied from) the GermanStemFilter.
 *
 * A filter that stemms german words. It supports a table of words
 * that should not be stemmed at all.
 *
 * @author João Kramer
 * @author Gerhard Schwarz
 */
public final class BrazilianStemFilter
    extends GSTokenFilter
{
    /**
     * The actual token in the input stream.
     */
    private Token token = null;
    private BrazilianStemmer stemmer = null;
    private Set exclusions = null;

    public BrazilianStemFilter(TokenStream in)
    {
        super(in);
        stemmer = new BrazilianStemmer();
    }

    /**
     * Builds a BrazilianStemFilter that uses an exclusiontable.
     *
     * @deprecated
     */
    public BrazilianStemFilter(TokenStream in, Hashtable exclusiontable)
    {
        this(in);
        this.exclusions = new HashSet(exclusiontable.keySet());
    }

    public BrazilianStemFilter(TokenStream in, Set exclusiontable)
    {
        this(in);
        this.exclusions = exclusiontable;
    }

    /**
     * @return Returns the next token in the stream, or null at EOS.
     */
    public final Token next()
        throws IOException
    {
        token = getNextToken();
        
        if (token == null)
        {
            return null;
        }
        // Check the exclusiontable.
        else if (exclusions != null && exclusions.contains(token.toString()))
        {
            return token;
        }
        else
        {
            String s = stemmer.stem(token.toString());

            // If not stemmed, dont waste the time creating a new token.
            if (s != null && !s.equals(token.toString()))
            {
                return new Token(s, 0, s.length(), token.type());
            }

            return token;
        }
    }
}


