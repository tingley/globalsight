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

package com.globalsight.terminology.tokenizer;

import java.util.Locale;
import java.util.Collection;

/**
 * Interface for all tokenizers.
*/
public interface ITokenizer
{
    /**
     * Return locale specific tokens.
     * @param p_string: Input string to tokenize.
    */
    public Collection tokenize(String p_string);

    public void setParameters(TokenizerParameters p_params);
}

