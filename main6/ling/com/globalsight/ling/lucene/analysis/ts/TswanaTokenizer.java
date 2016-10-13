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

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import com.globalsight.ling.lucene.analysis.GSTokenizer;
import com.globalsight.ling.tm2.lucene.LuceneUtil;

import java.io.IOException;
import java.io.Reader;

public class TswanaTokenizer extends GSTokenizer {

    StandardTokenizer standardTokenizer = null;

    public TswanaTokenizer(Reader input) {
        super(input);
        System.out.println("input is " + input);
        standardTokenizer = new StandardTokenizer(LuceneUtil.VERSION, input);
    }

    public Token next() throws IOException {
        Token token = null;

        token = LuceneUtil.getNextToken(standardTokenizer);

        if ( token != null ) {
            System.out.println("my next: " + token.toString());
        }
        else
            System.out.println("my next: null");
        return token;
    }

    public void close() throws IOException {
        System.out.println("myclose");
        standardTokenizer.close();
    }

}
