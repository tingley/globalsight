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

package com.globalsight.terminology.termleverager;

import java.util.*;

/**
 * Utility class.
 */
class Util
{
    static public String fixLocale(Locale p_locale)
    {
        return fixLocale(p_locale.toString());
    }

    // Mon Apr 19 23:55:40 2004 CvdL GSDEF00009929 Hebrew fixes. The
    // original code stored only the language part of the locale in
    // the database. So we do the same.
    static public String fixLocale(String p_locale)
    {
        // Note this does not handle Chinese locales (xx_yy).

        String result = p_locale/**/.substring(0, 2)/**/;

        if (p_locale.startsWith("iw"))
        {
            result = "he" /*+ p_locale.substring(2)*/;
        }
        else if (p_locale.startsWith("ji"))
        {
            result = "yi" /*+ p_locale.substring(2)*/;
        }
        else if (p_locale.startsWith("in"))
        {
            result = "id" /*+ p_locale.substring(2)*/;
        }

        return result;
    }
}

