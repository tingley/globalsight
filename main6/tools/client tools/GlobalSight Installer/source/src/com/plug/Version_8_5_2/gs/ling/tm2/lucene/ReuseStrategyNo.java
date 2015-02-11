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
package com.plug.Version_8_5_2.gs.ling.tm2.lucene;

import org.apache.lucene.analysis.Analyzer.ReuseStrategy;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;

/**
 * Lucene analyzer for GlobalSight. It tokenizes a string using Java's word
 * break iterator. It also filters out the stopwords.
 */

public class ReuseStrategyNo extends ReuseStrategy
{
    public TokenStreamComponents getReusableComponents(String fieldName)
    {
        return null;
    }

    @Override
    public void setReusableComponents(String fieldName,
            TokenStreamComponents components)
    {
        // do nothing
    }
}
