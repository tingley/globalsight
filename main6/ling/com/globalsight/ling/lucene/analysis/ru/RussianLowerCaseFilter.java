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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import com.globalsight.ling.lucene.analysis.GSTokenFilter;

/**
 * Normalizes token text to lower case, analyzing given ("russian") charset.
 *
 * @author  Boris Okner, b.okner@rogers.com
 * @version $Id: RussianLowerCaseFilter.java,v 1.2 2013/09/13 06:22:17 wayne Exp $
 */
public final class RussianLowerCaseFilter
    extends GSTokenFilter
{
    char[] charset;

    public RussianLowerCaseFilter(TokenStream in, char[] charset)
    {
        super(in);
        this.charset = charset;
    }

    public final Token next() throws java.io.IOException
    {
        Token t = getNextToken();

        if (t == null)
            return null;

        String txt = t.toString();

        char[] chArray = txt.toCharArray();
        for (int i = 0; i < chArray.length; i++)
        {
            chArray[i] = RussianCharsets.toLowerCase(chArray[i], charset);
        }

        String newTxt = new String(chArray);
        // create new token
        Token newToken = new Token(newTxt, t.startOffset(), t.endOffset());

        return newToken;
    }
}
