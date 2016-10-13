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

package com.globalsight.ling.lucene.search;

import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.IndexSearcher;

import java.util.*;

/**
 * This similarity class ignores the inverse document frequency (idf)
 * factor, which is relevant for terms in few long documents, but not
 * for dictionaries where every term is listed regardless of any
 * actual occurences in real texts.
 */
public class DictionarySimilarity
    extends DefaultSimilarity
{
    final public float idf(Collection terms, IndexSearcher searcher)
    {
        return 1.0f;
    }

    final public float idf(long docFreq, long numDocs)
    {
        return 1.0f;
    }
}
