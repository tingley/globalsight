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
package com.globalsight.ling.common;

import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Give a language, country and variant create a Java Locale object.
 * We don't check if the locale is valid.
 */
public class LocaleCreater
{
    private static final String LOCALE_DELIM = "_";

    /**
     * Static class, private constructor.
     */
    private LocaleCreater()
    {
        super();
    }


    public static Locale makeLocale(String p_locale)
    {
        String language = "";
        String country = "";
        String variant = "";

        StringTokenizer st = new StringTokenizer(p_locale, LOCALE_DELIM);

        // language
        if (st.hasMoreTokens())
        {
            language = st.nextToken();
        }

        // country
        if (st.hasMoreTokens())
        {
            country = st.nextToken();
        }

        // variant
        if (st.hasMoreTokens())
        {
            variant = st.nextToken();
        }


        return createLocale(language, country, variant);
    }

    public static Locale makeLocale(String p_language,
        String p_country, String p_variant)
    {

        return createLocale(p_language, p_country, p_variant);
    }

    private static Locale createLocale(String p_language,
        String p_country, String p_variant)
    {
        return new Locale(p_language, p_country, p_variant);
    }
}
