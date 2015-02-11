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
package com.globalsight.ling.tw;

import java.util.Enumeration;
import java.util.Hashtable;
import java.text.Collator;
import java.text.CollationKey;
import java.util.Locale;
import java.text.RuleBasedCollator;


/**
 * <p>Creates a HTML table from a hashtable.</p>
 */
public class HtmlTableWriter
{
    public static String getPtagString(Hashtable p_hash)
    {
        if (p_hash == null)
        {
            return "";
        }

        String[] s = getSortedPtagKeys(p_hash);
        if (s == null)
        {
            return "";
        }

        StringBuffer result = new StringBuffer();

        for (int i = 0; i < s.length; ++i)
        {
            result.append(s[i]);

            if (i < s.length - 1)
            {
                result.append(",");
            }
        }

        return result.toString();
    }


    /**
     * <p>Creates HTML table rows from the hashtable.</p>
     *
     * <p>The hash key is in column one and value is in column
     * two. Sorted by locale.</p>
     *
     * @param p_hash
     * @return the html table rows as a string.
     */
    public static String getSortedHtmlRows(Hashtable p_hash, Locale p_locale)
    {
        if (p_hash == null)
        {
            return "";
        }

        String[] s = getSortedPtagKeys(p_hash, p_locale);
        if (s == null)
        {
            return "";
        }

        return makeHtmlTable(p_hash, s, false);
    }

    /**
     * Creates HTML table rows from the hashtable.<p>
     *
     * The hash key is in column one and value is in column two.<p>
     *
     * Sorts lexicographically. The comparison is based on the Unicode
     * value of each character in the strings.
     * @param p_hash
     * @return the html table rows as a string.
     */
    public static String getSortedHtmlRows(Hashtable p_hash)
    {
        if (p_hash == null)
        {
            return "";
        }

        String[] s = getSortedPtagKeys(p_hash);
        if (s == null)
        {
            return "";
        }

        return makeHtmlTable(p_hash, s, false);
    }

    /**
     * Creates a complete HTML table from the hashtable.<p>
     *
     * The hash key is in column one and value is in column two.<p>
     *
     * @param p_hash
     * @return the html table rows as a string.
     */
    public static String getSortedHtmlTable(Hashtable p_hash, Locale p_locale)
    {
        if (p_hash == null)
        {
            return "";
        }

        String[] s = getSortedPtagKeys(p_hash, p_locale);
        if (s == null)
        {
            return "";
        }

        return makeHtmlTable(p_hash, s, true);
    }

    /**
     * Creates a complete HTML table from the hashtable.<p>
     *
     * The hash key is in column one and value is in column two.<p>
     *
     * Sorts lexicographically. The comparison is based on the Unicode
     * value of each character in the strings.
     *
     * @param p_hash
     * @return the html table rows as a string.
     */
    public static String getSortedHtmlTable(Hashtable p_hash)
    {
        if (p_hash == null)
        {
            return "";
        }

        String[] s = getSortedPtagKeys(p_hash);
        if (s == null)
        {
            return "";
        }

        return makeHtmlTable(p_hash, s, true);
    }

    /**
     * Returns the keys as a sorted array of strings. Sorted by
     * locale.
     * @return String[] or null if there is an error.
     */
    public static String[] getSortedPtagKeys(Hashtable p_hash, Locale p_locale)
    {
        if (p_hash == null)
        {
            return null;
        }

        int n = p_hash.size();
        int i = 0;
        String tmp;

        Collator ptagCollator = Collator.getInstance(p_locale);

        // Create an array of CollationKeys for the Strings to be sorted.
        CollationKey[] a = new CollationKey[n];
        Enumeration e = p_hash.keys();
        while (e.hasMoreElements())
        {
            tmp = (String)e.nextElement();

            if (tmp.startsWith("[/"))
            {
                // remove close signature for sort
                tmp = tmp.substring(2);
            }
            else
            {
                // remove open signature for sort
                tmp = tmp.substring(1);
            }

            a[i++] = ptagCollator.getCollationKey(tmp);
        }

        // shell sort
        int incr = n / 2;
        while (incr >= 1)
        {
            for (i = incr; i < n; i++)
            {
                CollationKey temp = a[i];
                int j = i;
                while (j >= incr && (temp.compareTo(a[j - incr]) < 0))
                {
                    a[j] = a[j - incr];
                    j -= incr;
                }
                a[j] = temp;
            }
            incr /= 2;
        }

        String prev = "";
        String[] result = new String[n];

        for (i = 0; i < n; i++)
        {
            if (prev.equals(a[i].getSourceString()))
            {
                // restore close signature
                tmp = "[/" + a[i].getSourceString();
            }
            else
            {
                // restore open signature
                tmp = "[" + a[i].getSourceString();
            }

            prev = a[i].getSourceString();
            result[i] = tmp;
        }

        return result;
    }

    /**
     * Returns the keys as a sorted array of strings.<p>
     * @return String[] or null if there is an error.
     */
    public static String[] getSortedPtagKeys(Hashtable p_hash)
    {
        if (p_hash == null)
        {
            return null;
        }

        int n = p_hash.size();
        int i = 0;
        String tmp;

        // Create an array of Strings to be sorted.
        String[] a = new String[n];
        Enumeration e = p_hash.keys();
        while (e.hasMoreElements())
        {
            tmp = (String)e.nextElement();

            if (tmp.startsWith("[/"))
            {
                // remove close signature for sort
                tmp = tmp.substring(2);
            }
            else
            {
                // remove open signature for sort
                tmp = tmp.substring(1);
            }

            a[i++] = tmp;
        }

        // shell sort
        int incr = n / 2;
        while (incr >= 1)
        {
            for (i = incr; i < n; i++)
            {
                String temp = a[i];
                int j = i;
                while (j >= incr && (temp.compareTo(a[j - incr]) < 0))
                {
                    a[j] = a[j - incr];
                    j -= incr;
                }
                a[j] = temp;
            }
            incr /= 2;
        }

        String prev = "";
        String[] result = new String[n];

        for (i = 0; i < n; i++)
        {
            if (prev.equals(a[i]))
            {
                // restore close signature
                tmp = "[/" + a[i];
            }
            else
            {
                // restore open signature
                tmp = "[" + a[i];
            }

            prev = a[i];
            result[i] = tmp;
        }

        return result;
    }

    /*
     * Creates an HTML table using the hash key as the first column
     * and the value as second column.<p>
     * @param p_map hashtable.
     * @param p_keys a sorted list of hash keys.
     * @param p_full - when true a full table is created, when false
     * only the rows are returned.
     * @return the html table as a string.
     */
    private static String makeHtmlTable(Hashtable p_map,
        String[] p_sortedKeys, boolean p_full)
    {
        if (p_map == null)
        {
            return "";
        }

        HtmlEntities m_htmlCodec = new HtmlEntities();
        StringBuffer htmlTable = new StringBuffer();
        String pTag = null;
        String nativeTag = null;

        // unsorted table
        if (p_sortedKeys == null || p_sortedKeys.length <= 0)
        {
            Enumeration keys = p_map.keys();

            if (p_full)
            {
                htmlTable.append("<table border=\"\">");
            }

            while (keys.hasMoreElements())
            {
                pTag = (String)keys.nextElement();
                nativeTag = (String)p_map.get(pTag);

                htmlTable.append("<tr><td class='standardText'>");
                htmlTable.append(pTag);
                htmlTable.append("</td><td class='standardText'>");
                htmlTable.append(m_htmlCodec.encodeString(nativeTag));
                htmlTable.append("</td></tr>");
            }

            if (p_full)
            {
                htmlTable.append("</table>");
            }


            return htmlTable.toString();
        }
        else  // sorted table
        {
            if (p_full)
            {
                htmlTable.append("<table border=\"\">");
            }

            for (int i=0; i< p_sortedKeys.length; i++)
            {
                pTag = p_sortedKeys[i];
                nativeTag = (String)p_map.get(pTag);
                htmlTable.append("<tr><td class='standardText'>");
                htmlTable.append(pTag);
                htmlTable.append("</td><td class='standardText'>");
                htmlTable.append(m_htmlCodec.encodeString(nativeTag));
                htmlTable.append("</td></tr>");
            }

            if (p_full)
            {
                htmlTable.append("</table>");
            }

            return htmlTable.toString();
        }
    }

}
