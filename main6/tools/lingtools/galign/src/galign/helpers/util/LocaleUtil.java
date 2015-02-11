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

package galign.helpers.util;

import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Wraps a Java locale providing initialization from strings, and
 * correct display strings for (iw/he, ji/yi and in/id).
 *
 */
public class LocaleUtil
{

    /**
     * Returns a RFC 1766-style string of a LocaleWrapper, that is
     * with dash (-) as separator.
     *
     * @see http://www.ietf.org/rfc/rfc1766.txt
     */
    static public String toRFC1766(Locale p_locale)
    {
        Locale l = p_locale/*.getLocale()*/;
        StringBuffer result = new StringBuffer(l.getLanguage());

        // Work around the JDK's stupid Locale behavior
        if      (result.indexOf("iw") == 0) result.replace(0, 2, "he");
        else if (result.indexOf("ji") == 0) result.replace(0, 2, "yi");
        else if (result.indexOf("in") == 0) result.replace(0, 2, "id");

        if (l.getCountry() != null)
        {
            result.append("-");
            result.append(l.getCountry());
        }

        return result.toString();
    }

    static public Locale makeLocale(String p_locale)
    {
        String language = "";
        String country = "";
        String variant = "";

        StringTokenizer st = new StringTokenizer(p_locale, "-_");

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


        return new Locale(language, country, variant);
    }
}
