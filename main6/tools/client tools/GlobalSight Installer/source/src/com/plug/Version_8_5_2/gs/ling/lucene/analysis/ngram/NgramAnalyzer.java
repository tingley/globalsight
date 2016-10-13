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

package com.plug.Version_8_5_2.gs.ling.lucene.analysis.ngram;

import org.apache.lucene.analysis.*;
import java.io.Reader;
import java.util.Set;

/**
 * Filters text like the StandardAnalyzer, and then produces ngrams
 * out of each token.
 */
public class NgramAnalyzer
    extends Analyzer
{
    private int m_ngram = 3;

    public NgramAnalyzer()
    {
    }

    public NgramAnalyzer(int p_ngram)
    {
        m_ngram = p_ngram;
    }
    
    protected TokenStreamComponents createComponents(String fieldName,
            Reader reader)
    {
        Tokenizer t = new NgramTokenizer(reader, m_ngram);
        
        return new TokenStreamComponents(t);
    }

    //
    // Test Code
    //
    static void test(String p_text)
        throws java.io.IOException
    {
        NgramAnalyzer x = new NgramAnalyzer(3);
        NgramTokenizer y = new NgramTokenizer(new java.io.StringReader(p_text), 3);

        System.out.println("Text = " + p_text);

        Token t;
        while ((t = y.next()) != null)
        {
            System.out.println(t.toString() +
                " (" + t.startOffset() + ":" + t.endOffset() + ")");
        }
    }

    static public void main(String[] args)
        throws java.io.IOException
    {
        test("");
        test("a");
        test("ab");
        test("abc");
        test("abcd");
        test("abd(2) @de^3");
        test("1234567890");
    }
}
