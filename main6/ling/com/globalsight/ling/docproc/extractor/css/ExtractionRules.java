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
package com.globalsight.ling.docproc.extractor.css;

import com.globalsight.ling.docproc.extractor.html.DynamicRules;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
 * <P>Describes properties of CSS1/CSS2/IE5 styles.  Properties are:
 * <UL>
 * <LI>localizable - a style has cultural/locale-specific significance
 * <LI>multiple-values - a style can contain multiple values
 * </UL>
 */
public class ExtractionRules
{
    static private HashMap s_styles = new HashMap ();
    static private HashMap s_prefixes = new HashMap ();
    static private Boolean s_default  = Boolean.FALSE;

    static void setDefault(ResourceBundle res, String key)
    {
        String val = res.getString(key);

        s_default = Boolean.valueOf(val.trim().toLowerCase());
    }

    static void setStyle(ResourceBundle res, String key, HashMap map)
    {
        String val = res.getString(key);

        String style = key.trim();
        String value = val.trim().toLowerCase();
        map.put(style, Boolean.valueOf(value));
    }

    static void fillMapMap(ResourceBundle res, String key, HashMap map)
    {
        String line = res.getString(key);

        StringTokenizer tok = new StringTokenizer (line, ",");
        while (tok.hasMoreTokens())
        {
            String token = tok.nextToken().trim();
            StringTokenizer tok1 = new StringTokenizer (token, ":");
            while (tok1.hasMoreTokens())
            {
                String tag = tok1.nextToken().trim().toLowerCase();
                String val = tok1.nextToken().trim().toLowerCase();
                map.put(tag, Boolean.valueOf(val));
            }
        }
    }

    static
    {
        try
        {
            ResourceBundle res =
                ResourceBundle.getBundle("properties/Styles", Locale.US);

            Enumeration keys = res.getKeys();
            while (keys.hasMoreElements())
            {
                String key = (String)keys.nextElement();
                String tmp = key.toLowerCase();

                if (tmp.equalsIgnoreCase("extract_unknown"))
                {
                    setDefault(res, tmp);
                }
                else if (tmp.equalsIgnoreCase("extract_prefix"))
                {
                    fillMapMap(res, key, s_prefixes);
                }
                else
                {
                    setStyle(res, key, s_styles);
                }
            }
        }
        catch (MissingResourceException e)
        {
            System.err.println("ExtractionRules initialization error:");
            e.printStackTrace();
            // Log an error with Logger class.
        }
        catch (Throwable e)
        {
            System.err.println("ExtractionRules initialization error:");
            e.printStackTrace();
            // Log an error with Logger class.
        }
    }

    //
    // Private & Protected Constants
    //

    /**
     * <p>Map that holds exceptional extraction rules.  If null, no
     * rules have been loaded.</p>
     */
    private DynamicRules m_rules = new DynamicRules();

    //
    // Constructor
    //
    public ExtractionRules()
    {
    }

    /**
     * <p>Loads rules to guide extraction process from a string.</p>
     */
    public final void loadRules(String p_rules)
        throws ExtractorException
    {
        // not implemented yet
    }

    /**
     * <p>Loads rules to guide extraction process from an object.</p>
     */
    public final void loadRules(Object p_rules)
        throws ExtractorException
    {
        if (p_rules != null && p_rules instanceof DynamicRules)
        {
            m_rules = (DynamicRules)p_rules;
        }
    }

    public final boolean canLocalize(String p_style)
    {
        return m_rules.canExtractStyle(p_style);
    }

    public final boolean isLocalizable(String p_style)
    {
        String key = p_style.toLowerCase();

        // Check the known styles
        Boolean value = (Boolean)s_styles.get(key);
        if (value != null)
        {
            return value.booleanValue();
        }

        // Check the prefix defaults
        if (!s_prefixes.isEmpty())
        {
            for (Iterator it = s_prefixes.keySet().iterator(); it.hasNext(); )
            {
                String prefix = (String)it.next();

                if (key.startsWith(prefix))
                {
                    return ((Boolean)s_prefixes.get(prefix)).booleanValue();
                }
            }
        }

        // return the default extraction value
        return s_default.booleanValue();
    }

    public String getLocalizationType(String p_style)
    {
        return "css-" + p_style.toLowerCase();
    }
}
