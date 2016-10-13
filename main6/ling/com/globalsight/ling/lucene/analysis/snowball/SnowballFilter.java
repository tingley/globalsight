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
package com.globalsight.ling.lucene.analysis.snowball;

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

import java.io.IOException;

import java.lang.reflect.Method;

import com.globalsight.ling.lucene.analysis.GSTokenFilter;
import com.globalsight.ling.lucene.analysis.snowball.snowball.SnowballProgram;
import com.globalsight.ling.lucene.analysis.snowball.snowball.ext.*;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * A filter that stems words using a Snowball-generated stemmer.
 *
 * Available stemmers are listed in {@link net.sf.snowball.ext}.  The
 * name of a stemmer is the part of the class name before "Stemmer",
 * e.g., the stemmer in {@link EnglishStemmer} is named "English".
 */

public class SnowballFilter
    extends GSTokenFilter
{
    private SnowballProgram stemmer;

    /**
     * Construct the named stemming filter.
     *
     * @param in the input tokens to stem
     * @param name the name of a stemmer
     */
    public SnowballFilter(TokenStream in, String name)
    {
        super(in);

        try
        {
            Class stemClass = Class.forName(
                "com.globalsight.ling.lucene.analysis.snowball.snowball.ext." +
                name + "Stemmer");
            stemmer = (SnowballProgram)stemClass.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * Returns the next input Token, after being stemmed.
     */
    public final Token next()
        throws IOException
    {
        Token token = getNextToken();

        if (token == null)
        {
            return null;
        }

        stemmer.setCurrent(token.toString());
        stemmer.stem();

        return new Token(stemmer.getCurrent(),
            token.startOffset(), token.endOffset(), token.type());
    }
}
