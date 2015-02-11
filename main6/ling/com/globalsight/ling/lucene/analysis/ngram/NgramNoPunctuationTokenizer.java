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

package com.globalsight.ling.lucene.analysis.ngram;

import com.globalsight.ling.lucene.analysis.ngram.NgramTokenizer;
import java.io.Reader;

/**
 * Produces ngrams out of the input string. This tokenizer filters
 * punctuation out of the original string, leaving only letters,
 * digits, and whitespace.
 */
public class NgramNoPunctuationTokenizer
    extends NgramTokenizer
{
    public NgramNoPunctuationTokenizer(Reader p_input, int p_ngram)
    {
        super(p_input, p_ngram);
    }

    /**
     * Returns true iff a character should be included in a token.
     * This tokenizer discards all characters that do not satisfy this
     * predicate. The default implementation allows all characters.
     */
    /*abstract*/protected boolean isTokenChar(char c)
    {
        return Character.isLetterOrDigit(c) || Character.isSpaceChar(c);
    }
}
