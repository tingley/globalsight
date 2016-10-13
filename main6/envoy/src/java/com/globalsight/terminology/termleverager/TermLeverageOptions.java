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

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class that holds options for term leveraging.
 *
 * Term leveraging is performed as a series of steps:
 *
 * - find source candidates with scores > threshold
 * - if target terms should be loaded (setLoadTargetTerms(true)):
 *     -> load target terms
 *     -  if matches should be persisted (setSaveToDatabase(true)):
 *         -> persist matches in LeverageMatch format
 * - return matches
 *
 * This gets further optimized by the "prepareForExactMatching" flag (TODO).
 */
public class TermLeverageOptions
    implements Serializable
{
    static private int s_fuzzyThreshold = 50;
    static public final String s_propertyFile = "Terminology";

    static
    {
        try
        {
            ResourceBundle res =
                ResourceBundle.getBundle(s_propertyFile, Locale.US);

            String value;

            try
            {
                value = res.getString("fuzzy_matching_threshold");
                s_fuzzyThreshold = Integer.parseInt(value);

                if (s_fuzzyThreshold < 1 || s_fuzzyThreshold > 100)
                {
                    s_fuzzyThreshold = 50;
                }
            }
            catch (MissingResourceException e) {}
            catch (Throwable e) {}
        }
        catch (MissingResourceException e)
        {
            // Do nothing if configuration file was not found.
        }
    }

    //
    // Private Members
    //

    private int m_fuzzyThreshold = s_fuzzyThreshold;
    private Locale m_sourcePageLocale;
    private HashMap m_targetPageLocale2LangNameMap;
    private ArrayList m_sourcePageLangNames;
    private HashMap m_langNames2LocaleMap; // one to one mapping
    private ArrayList m_termBases;
    private boolean m_leverageOnlyApproved = false;
    private boolean m_leverageOnlyPreferred = false;
    private boolean m_loadTargets = true;
    private boolean m_saveMatches = false;

    //
    // Constructor
    //

    /** Creates a new instance of TermLeverageOptions. */
    public TermLeverageOptions()
    {
        m_sourcePageLangNames = new ArrayList();
        m_targetPageLocale2LangNameMap = new HashMap(20);
        m_langNames2LocaleMap = new HashMap(20);
        m_termBases = new ArrayList();
    }

    public int getFuzzyThreshold()
    {
        return m_fuzzyThreshold;
    }

    public void setFuzzyThreshold(int p_fuzzyThreshold)
    {
        m_fuzzyThreshold = p_fuzzyThreshold;
    }

    public boolean isLeverageOnlyApproved()
    {
        return m_leverageOnlyApproved;
    }

    public void setLeverageOnlyApproved(boolean p_arg)
    {
        m_leverageOnlyApproved = p_arg;
    }

    public boolean isLeverageOnlyPreferred()
    {
        return m_leverageOnlyPreferred;
    }

    public void setLeverageOnlyPreferred(boolean p_arg)
    {
        m_leverageOnlyPreferred = p_arg;
    }

    public boolean getLoadTargetTerms()
    {
        return m_loadTargets;
    }

    public void setLoadTargetTerms(boolean p_arg)
    {
        m_loadTargets = p_arg;
    }

    public boolean getSaveToDatabase()
    {
        return m_saveMatches;
    }

    public void setSaveToDatabase(boolean p_arg)
    {
        m_saveMatches = p_arg;

        if (p_arg == true)
        {
            m_loadTargets = true;
        }
    }

    /** The source locale to leverage from. */
    public Locale getSourcePageLocale()
    {
        return m_sourcePageLocale;
    }

    /** The source locale to leverage from. */
    public void setSourcePageLocale(Locale p_sourcePageLocale)
    {
        m_sourcePageLocale = p_sourcePageLocale;
    }

    /**
     * Add a language name to leverage from (one source locale can map
     * to multiple language names).
     */
    public void addSourcePageLocale2LangName(String p_langName)
    {
        m_sourcePageLangNames.add(p_langName);
    }

    /**
     * The language names to leverage from (one source locale can map
     * to multiple language names).
     */
    public ArrayList getSourcePageLangNames()
    {
        return m_sourcePageLangNames;
    }

    /** The target language names to leverage to. */
    public ArrayList getTargetPageLangNames(Locale p_locale)
    {
        return (ArrayList)m_targetPageLocale2LangNameMap.get(p_locale);
    }

    public Collection getAllTargetPageLocales()
    {
        return m_targetPageLocale2LangNameMap.keySet();
    }

    public ArrayList getAllTargetPageLangNames()
    {
        ArrayList allLangNames = new ArrayList(20);
        Collection langNames = m_targetPageLocale2LangNameMap.values();
        for (Iterator it = langNames.iterator(); it.hasNext();)
        {
            allLangNames.addAll((ArrayList)it.next());
        }

        return allLangNames;
    }

    /**
     * Page locale to lang name mappings are one to many.
     * Page locales are always 5 character names (en_US, en_GB etc.)
     * @param p_locale - source or target page locale.
     * @param p_langName - the term base language name we want to map to.
     */
    public void addTargetPageLocale2LangName(Locale p_locale, String p_langName)
    {
        if (m_targetPageLocale2LangNameMap.containsKey(p_locale))
        {
            ArrayList list =
                (ArrayList)m_targetPageLocale2LangNameMap.get(p_locale);
            list.add(p_langName);
        }
        else
        {
            ArrayList list = new ArrayList();
            list.add(p_langName);
            m_targetPageLocale2LangNameMap.put(p_locale, list);
        }
    }

    /**
     * Returns a list of termbase names (String).
     */
    public ArrayList getTermBases()
    {
        return m_termBases;
    }

    public void addTermBase(String p_termbaseName)
    {
        m_termBases.add(p_termbaseName);
    }

    public Locale getLocale(String p_langName)
    {
        return (Locale)m_langNames2LocaleMap.get(p_langName.toLowerCase());
    }

    /**
     * The lang name to locale mappings are one to one, that is, each
     * lang name will have assigned to it a single locale (i.e., fr or
     * fr_FR etc..). Locales can be languages, or languages and
     * country (for Chinese).
     */
    public void addLangName2Locale(String p_langName, Locale p_locale)
    {
        // put lowercase langName into the map, and get the parameter using lowercase too
        m_langNames2LocaleMap.put(p_langName.toLowerCase(), p_locale);
    }


    public String toString()
    {
        return "{TermLeverageOptions" +
            " m_fuzzyThreshold=" + m_fuzzyThreshold +
            " m_sourcePageLocale=" + m_sourcePageLocale +
            " m_targetPageLocale2LangNameMap=" + m_targetPageLocale2LangNameMap +
            " m_sourcePageLangNames=" + m_sourcePageLangNames +
            " m_langNames2LocaleMap=" + m_langNames2LocaleMap +
            " m_termBases=" + m_termBases +
            " m_leverageOnlyApproved=" + m_leverageOnlyApproved +
            " m_leverageOnlyPreferred=" + m_leverageOnlyPreferred +
            "}";
    }
}
