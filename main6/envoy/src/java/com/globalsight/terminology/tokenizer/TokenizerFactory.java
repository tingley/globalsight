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
import java.util.Hashtable;

import org.apache.log4j.Logger;


public final class TokenizerFactory
{
    private static final Logger c_logger =
        Logger.getLogger(
            TokenizerFactory.class.getName());

    private static final Hashtable c_tokenizerCache = new Hashtable(20);

    public static ITokenizer makeTokenizer(Locale p_locale)
    {
        TokenizerParameters params =
            Locale2TokenizerMapper.getParameters(p_locale);

        ITokenizer tokenizer = null;

        try
        {
            if (c_tokenizerCache.containsKey(p_locale))
            {
                tokenizer = (ITokenizer)c_tokenizerCache.get(p_locale);
            }
            else
            {
                tokenizer = (ITokenizer)Class.forName(
                    params.getTokenizerName()).newInstance();

                tokenizer.setParameters(params);

                // add tokenizer to cache
                c_tokenizerCache.put(p_locale, tokenizer);
            }
        }

        // These exceptions should never happen if the
        // Locale2Tokenizer mappings and tokenizers are set up
        // correctly
        catch (Exception e)
        {
            c_logger.error("error creating tokenizer", e);
        }

        return tokenizer;
    }
}
