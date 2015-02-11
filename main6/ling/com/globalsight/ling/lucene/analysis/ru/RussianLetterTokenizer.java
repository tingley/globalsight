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
package com.globalsight.ling.lucene.analysis.ru;

/**
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Reader;

import org.apache.lucene.analysis.util.CharTokenizer;

import com.globalsight.ling.tm2.lucene.LuceneUtil;

/**
 * A RussianLetterTokenizer is a tokenizer that extends
 * LetterTokenizer by additionally looking up letters in a given
 * "russian charset". The problem with LeterTokenizer is that it uses
 * Character.isLetter() method, which doesn't know how to detect
 * letters in encodings like CP1252 and KOI8 (well-known problems with
 * 0xD7 and 0xF7 chars)
 *
 * @author  Boris Okner, b.okner@rogers.com
 * @version $Id: RussianLetterTokenizer.java,v 1.2 2013/09/13 06:22:17 wayne Exp $
 */
public class RussianLetterTokenizer
    extends CharTokenizer
{
    /** Construct a new LetterTokenizer. */
    private char[] charset;

    public RussianLetterTokenizer(Reader in, char[] charset)
    {
        super(LuceneUtil.VERSION, in);
        this.charset = charset;
    }

    /**
     * Collects only characters which satisfy
     * {@link Character#isLetter(char)}.
     */
    @Override
    protected boolean isTokenChar(int c)
    {
        if (Character.isLetter(c))
        {
            return true;
        }

        for (int i = 0; i < charset.length; i++)
        {
            if (c == charset[i])
            {
                return true;
            }
        }

        return false;
    }
}
