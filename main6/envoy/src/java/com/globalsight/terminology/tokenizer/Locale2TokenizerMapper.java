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


import java.util.Hashtable;
import java.util.Locale;

import org.apache.log4j.Logger;

/**
 */
final class Locale2TokenizerMapper
{
    private static final Logger c_logger =
        Logger.getLogger(
            Locale2TokenizerMapper.class.getName());

    static public TokenizerParameters getParameters(Locale p_locale)
    {
        String language = p_locale.getLanguage();

        if (language.equals("ja"))
        {
            return new TokenizerParameters(2,
                "com.globalsight.terminology.tokenizer.Japanese",
                Locale.JAPANESE);
        }
        else if (language.equals("ko"))
        {
            return new TokenizerParameters(3,
                "com.globalsight.terminology.tokenizer.Korean",
                Locale.KOREAN);
        }
        else if (language.equals("zh"))
        {
            String country = p_locale.getCountry();

            if (country != null)
            {
                if (country.equalsIgnoreCase("CN"))
                {
                    return new TokenizerParameters(2,
                        "com.globalsight.terminology.tokenizer.Chinese",
                        Locale.SIMPLIFIED_CHINESE);
                }
                else if (country.equalsIgnoreCase("TW"))
                {
                    return new TokenizerParameters(2,
                        "com.globalsight.terminology.tokenizer.Chinese",
                        Locale.TRADITIONAL_CHINESE);
                }
            }
        }

        // return the default tokenizer
        return new TokenizerParameters(3,
            "com.globalsight.terminology.tokenizer.Alphabetic",
            p_locale);
    }
}
