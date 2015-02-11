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

package com.globalsight.util.resourcebundle;

import java.io.Serializable;
import java.util.Vector;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;


/**
 * Creates a Locale object from a locale string (i.e., "en_US").
 *
 * @see com.globalsight.ling.common.LocaleCreater
 */
public class LocaleWrapper
    implements Serializable
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            LocaleWrapper.class);

    private static final String SEPARATOR = "_";

    /**
     * Get a java.util.Locale object based on the given locale string.
     * This method requires the locale to be at least a combination of
     * language and country (i.e. ll_cc).
     * @param p_locale - The string representation of the locale (i.e. en_US).
     * @return A Locale object.  Note that if the string is null, the
     * default system locale will be returned.
     */
    public static Locale getLocale(String p_locale)
    {
        if (p_locale == null || p_locale.length() < 5)
        {
            return Locale.getDefault();
        }

        StringTokenizer tokenizer = new StringTokenizer(p_locale, SEPARATOR);

        // should at least be 2 (ll_cc) or at most 3 (ll_cc_vv)
        int count = tokenizer.countTokens();
        String language = tokenizer.nextToken();
        String country = tokenizer.nextToken();

        if (count == 3)
        {
            String variant = tokenizer.nextToken();
            return new Locale(language, country, variant);
        }
        else
        {
            return new Locale(language, country);
        }
    }
}

