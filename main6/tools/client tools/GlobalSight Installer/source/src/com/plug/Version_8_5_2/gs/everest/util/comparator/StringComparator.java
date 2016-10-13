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
package com.plug.Version_8_5_2.gs.everest.util.comparator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * This class can be used to compare String objects in a locale sensitive way.
 */
public class StringComparator implements Comparator<Object>, Serializable
{
    private static final long serialVersionUID = 1L;

    protected int m_type;
    protected Locale m_locale;
    private transient Collator m_collator;

    protected void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException
    {
        in.defaultReadObject();
        m_collator = Collator.getInstance(m_locale);
    }

    /**
     * Creates a StringComparator with the given locale.
     */
    public StringComparator(Locale p_locale)
    {
        m_locale = p_locale;
        m_collator = Collator.getInstance(m_locale);
    }

    /**
     * Creates a StringComparator with the given locale and column to sort on.
     */
    public StringComparator(int p_type, Locale p_locale)
    {
        m_type = p_type;
        m_locale = p_locale;
        m_collator = Collator.getInstance(m_locale);
    }

    /**
     * Set the type (column to sort on).
     */
    public void setType(int p_type)
    {
        m_type = p_type;
    }

    /**
     * Performs a comparison of two LocalePair objects.
     */
    public int compare(Object p_A, Object p_B)
    {
        String a = (String) p_A;
        String b = (String) p_B;
        return compareStrings(a, b);
    }

    /**
     * Compares two strings in a locale sensitive way.
     */
    public int compareStrings(String p_A, String p_B)
    {
        String a = p_A == null ? "" : p_A;
        String b = p_B == null ? "" : p_B;
        return getCollationKey(a).compareTo(getCollationKey(b));
    }

    /**
     * Gets the locale of this StringComparator.
     */
    public Locale getLocale()
    {
        return m_locale;
    }

    /**
     * Extracts the filename part of an MsOffice multipart file:
     * "(header) en_US/ppt.ppt" --&gt; "en_US/ppt.ppt".
     */
    public String getMainFileName(String p_filename)
    {
        int index = p_filename.indexOf(")");
        if (index > 0 && p_filename.startsWith("("))
        {
            index++;
            while (Character.isSpace(p_filename.charAt(index)))
            {
                index++;
            }

            return p_filename.substring(index, p_filename.length());
        }

        return p_filename;
    }

    /**
     * Extracts the sub-file part of an MsOffice multipart file:
     * "(header) en_US/ppt.ppt" --&gt; "(header)".
     */
    public String getSubFileName(String p_filename)
    {
        int index = p_filename.indexOf(")");
        if (index > 0 && p_filename.startsWith("("))
        {
            return p_filename.substring(0, p_filename.indexOf(")") + 1);
        }

        return "";
    }

    protected CollationKey getCollationKey(String s)
    {
        return m_collator.getCollationKey(s);
    }
}
