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
package com.globalsight.ling.tm2.indexer;

import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.GlobalSightLocale;

import java.util.List;
import java.util.ResourceBundle;

/**
 * Tokenizer is an abstract class upon which various tokenizer can be built.
 */

public interface Tokenizer
{
    /**
     * tokenize a given string and return a collection of Token objects.
     *
     * @param p_segment string to be tokenized. This is usually a
     *        return value of BaseTmTuv#fuzzyIndexFormat() method.
     * @param p_tuvId Tuv id of the segment
     * @param p_tuId Tu id of the segment
     * @param p_tmId Tm id the Tuv belongs to
     * @param p_locale locale of the segment
     * @param p_sourceLocale indicates whether the Tuv is source
     * @return List of Token objects
     */
    public abstract List tokenize(String p_segment, long p_tuvId, long p_tuId,
        long p_tmId, GlobalSightLocale p_locale, boolean p_sourceLocale)
        throws Exception;
    
}
