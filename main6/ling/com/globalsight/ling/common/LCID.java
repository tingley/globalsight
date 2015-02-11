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
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * <P>Maps from a Java locale to a Microsoft Windows Locale Identifier
 * (LCID).</P>
 */
public class LCID
{
    /**
     * Cached resource bundle that maps locale names to locale
     * identifiers (LCIDs).  If the properties file is updated, the
     * application has to be restarted to pick up the changes.
     */
    static ResourceBundle res =
        ResourceBundle.getBundle("properties/LCID");

    /**
     * <p>Returns the Windows locale id code (e.g., 1033 = 0x409 =
     * en_US).  See the {@link <A
     * href="http://msdn.microsoft.com/library/specs/rtfspec.htm">RTF
     * 1.6 Documentation</>}, p.46 ff.</P>
     *
     * <p>Locales are tried as "language-country" first, then as
     * "language" only.</p>
     *
     * <p>Note that this class knows all locales that are known to
     * Windows 2000.  The returned values may not be supported by
     * previous versions of Windows.</p>
     *
     * @throws MissingResourceException when the locale is invalid
     * and/or cannot be mapped.
     *
     * @throws NumberFormatException when the lcid read from the
     * properties file is not a decimal integer.
     */
    public static int getLocaleId(Locale p_locale)
        throws MissingResourceException
    {
        int lcid = 1033;                          // en_US
        String key = p_locale.getLanguage();
        String value;

        // Work around the JDK's stupid Locale behavior
        if      (key.startsWith("iw")) key = "he";
        else if (key.startsWith("ji")) key = "yi";
        else if (key.startsWith("in")) key = "id";

        if (p_locale.getCountry() != null)
        {
            key = key + "-" + p_locale.getCountry();
        }

        try
        {
            value = res.getString(key.toLowerCase());
            lcid = Integer.parseInt(value.trim(), 10);

            return lcid;
        }
        catch (MissingResourceException ex)
        {
            // lang-country may not be defined, try again
        }

        // reset key to language only
        key = p_locale.getLanguage();

        // Work around the JDK's stupid Locale behavior
        if      (key.startsWith("iw")) key = "he";
        else if (key.startsWith("ji")) key = "yi";
        else if (key.startsWith("in")) key = "id";

        try
        {
            value = res.getString(key.toLowerCase());
            lcid = Integer.parseInt(value.trim(), 10);
        }
        catch (MissingResourceException ex)
        {
            // out of luck
            throw ex;
        }

        return lcid;
    }

    public static int getLocaleId(String p_locale)
        throws MissingResourceException
    {
        int lcid = 1033;                          // en_US
        String key = p_locale;
        String value;

        if (key.length() > 5)                     // check full string
        {
            try
            {
                // ignore xx_XX/xx-XX differences
                key = key.substring(0,2) + "-" + key.substring(3);

                value = res.getString(key.toLowerCase());
                lcid = Integer.parseInt(value.trim(), 10);

                return lcid;
            }
            catch (MissingResourceException ex)
            {
                // lang-country-sub may not be defined, try again
            }
        }

        key = p_locale;
        if (key.length() >= 5)                    // check xx_XX
        {
            try
            {
                // ignore xx_XX/xx-XX differences
                key = key.substring(0,2) + "-" + key.substring(3,5);

                value = res.getString(key.toLowerCase());
                lcid = Integer.parseInt(value.trim(), 10);

                return lcid;
            }
            catch (MissingResourceException ex)
            {
                // lang-country may not be defined, try again
            }
        }

        // reset key and throw exception if not found
        key = p_locale.substring(0,2);
        try
        {
            value = res.getString(key.toLowerCase());
            lcid = Integer.parseInt(value.trim(), 10);
        }
        catch (MissingResourceException ex)
        {
            // out of luck
            throw ex;
        }

        return lcid;
    }
}
