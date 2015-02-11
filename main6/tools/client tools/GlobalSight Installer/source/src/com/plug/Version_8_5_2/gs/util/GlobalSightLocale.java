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

package com.plug.Version_8_5_2.gs.util;

// Core Java classes
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

/**
 * This class is used to wrap the Locale object.
 */
public class GlobalSightLocale
{
    private static final long serialVersionUID = -7304301652233786576L;

    static public String ID = "";
    static public String COUNTRY = "m_country";
    static public String LANGUAGE = "m_language";
    static public String IS_UI_LOCALE = "m_isUiLocale";

    private String m_language;
    private String m_country;
    private String m_displayName = null;
    private boolean m_isUiLocale = false;

    // create from the language and country
    private Locale m_locale = null;
    private long m_id = -1l;

    /**
     * Constructor
     */
    public GlobalSightLocale()
    {
    }

    /**
     * Constructor
     */
    public GlobalSightLocale(String p_language, String p_country, boolean p_isUiLocale)
    {
        m_language = p_language;
        m_country = p_country;
        m_isUiLocale = p_isUiLocale;
    }

    public boolean isUiLocale()
    {
        return m_isUiLocale;
    }

    public String getLanguageCode()
    {
        return m_language;
    }

    public String getCountryCode()
    {
        return m_country;
    }

    /**
     * Returns the Java Locale object that this class wraps.
     */
    public Locale getLocale()
    {
        // if "m_locale" hasn't been created yet.
        if (m_locale == null)
        {
            if (m_country == null)
            {
                m_locale = new Locale(m_language, "");
            }
            else
            {
                m_locale = new Locale(m_language, m_country);
            }
        }
        return m_locale;
    }

    /**
     * Wraps the call to the Java locale object to return the language and
     * country in a nice format (en_US = English (United States)) The nice
     * format also includes the locale code in brackets.
     * 
     * @return String A displayable/readable name for the locale.
     */
    public String getDisplayName()
    {
        if (m_displayName == null)
        {
            // uses the method to instantiate the locale if not set yet.
            Locale l = getLocale();
            m_displayName = l.getDisplayName(Locale.US) + " [" + this.toString() + "]";
        }
        return m_displayName;
    }

    /**
     * Wraps the call to the Java locale object to return the language and
     * country in a nice format.
     * 
     * @param p_locale
     *            The locale to return this locale's name in.
     * @return String A displayable/readable name for the locale.
     */
    public String getDisplayName(GlobalSightLocale p_locale)
    {
        // uses the method to instantiate the locale if not set yet.
        Locale l = getLocale();
        return l.getDisplayName(p_locale.getLocale()) + " [" + this.toString() + "]";
    }

    /**
     * Wraps the call to the Java locale object to return the language and
     * country in a nice format.
     * 
     * @param p_locale
     *            The locale to return this locale's name in.
     * @return String A displayable/readable name for the locale.
     */
    public String getDisplayName(Locale p_locale)
    {
        // uses the method to instantiate the locale if not set yet.
        Locale l = getLocale();
        return l.getDisplayName(p_locale) + " [" + this.toString() + "]";
    }

    /**
     * Wraps the call to the Java locale object to return the language in a nice
     * format (en = English)
     * 
     * @return String A displayable/readable name for the locale's language.
     */
    public String getDisplayLanguage()
    {
        // uses the method to instantiate the locale if not set yet.
        Locale l = getLocale();
        return l.getDisplayLanguage();
    }

    /**
     * Wraps the call to the Java locale object to return the language in a nice
     * format (en = English)
     * 
     * @param p_locale
     *            The locale to return this locale's language in.
     * @return String A displayable/readable name for the locale's language.
     */
    public String getDisplayLanguage(GlobalSightLocale p_locale)
    {
        // uses the method to instantiate the locale if not set yet.
        Locale l = getLocale();
        return l.getDisplayLanguage(p_locale.getLocale());
    }

    /**
     * Wraps the call to the Java locale object to return the language in a nice
     * format (en = English)
     * 
     * @param p_locale
     *            The locale to return this locale's language in.
     * @return String A displayable/readable name for the locale's language.
     */
    public String getDisplayLanguage(Locale p_locale)
    {
        // uses the method to instantiate the locale if not set yet.
        Locale l = getLocale();
        return l.getDisplayLanguage(p_locale);
    }

    /**
     * Wraps the call to the Java locale object to return the country in a nice
     * format (US = United States)
     * 
     * @return String A displayable/readable name for the locale's country.
     */
    public String getDisplayCountry()
    {
        // uses the method to instantiate the locale if not set yet.
        Locale l = getLocale();
        return l.getDisplayCountry();
    }

    /**
     * Wraps the call to the Java locale object to return the country in a nice
     * format (US = United States)
     * 
     * @param p_locale
     *            The locale to return this locale's country in.
     * @return String A displayable/readable name for the locale's country.
     */
    public String getDisplayCountry(GlobalSightLocale p_locale)
    {
        // uses the method to instantiate the locale if not set yet.
        Locale l = getLocale();
        return l.getDisplayCountry(p_locale.getLocale());
    }

    /**
     * Wraps the call to the Java locale object to return the country in a nice
     * format (US = United States)
     * 
     * @param p_locale
     *            The locale to return this locale's country in.
     * @return String A displayable/readable name for the locale's country.
     */
    public String getDisplayCountry(Locale p_locale)
    {
        // uses the method to instantiate the locale if not set yet.
        Locale l = getLocale();
        return l.getDisplayCountry(p_locale);
    }

    /**
     * Returns 'true' if the ids of the Locale objects are equal, 'false' if
     * they aren't.
     * 
     * @param p_locale
     *            The GlobalSightLocale object to compare with
     * @return 'true' or 'false'
     */
    public boolean equals(Object p_locale)
    {
        if (p_locale instanceof GlobalSightLocale)
        {
            return (getId() == ((GlobalSightLocale) p_locale).getId());
        }
        return false;
    }

    public long getId()
    {
        return m_id;
    }
    
    public void setId(long id)
    {
        this.m_id = id;
    }

    /**
     * Returns id
     * 
     * @return the value of id
     */
    public int hashCode()
    {
        return (int) getId();
    }

    /**
     * Returns a string representation of the object. Converts any old iso
     * language codes to the new codes. The Java locale object preserves the old
     * ones.
     * 
     * @return a string representation of the object.
     */
    public String toString()
    {
        String localeString = getLocale().toString();
        if (localeString.startsWith("iw"))
        {
            localeString = "he" + localeString.substring(2);
        }
        else if (localeString.startsWith("ji"))
        {
            localeString = "yi" + localeString.substring(2);
        }
        else if (localeString.startsWith("in"))
        {
            localeString = "id" + localeString.substring(2);
        }

        return localeString;
    }

    /**
     * Returns a string representation of the object for debugging purposes.
     * 
     * @return a string representation of the object for debugging purposes.
     */
    public String toDebugString()
    {
        return super.toString() + " " + toString();
    }

    // takes in a locale string (en_US_var) and returns a locale object
    static public Locale makeLocaleFromString(String p_localeName) throws NoSuchElementException
    {
        StringTokenizer st = new StringTokenizer(p_localeName, "_");
        String language = st.nextToken();
        String country = st.nextToken();
        String variant = null;
        Locale locale = null;

        if (st.hasMoreTokens())
            variant = st.nextToken();

        if (variant == null)
            locale = new Locale(language, country);
        else
            locale = new Locale(language, country, variant);

        return locale;
    }

    public String getCountry()
    {
        return m_country;
    }

    public void setCountry(String m_country)
    {
        this.m_country = m_country;
    }

    public boolean isIsUiLocale()
    {
        return m_isUiLocale;
    }

    public void setIsUiLocale(boolean uiLocale)
    {
        m_isUiLocale = uiLocale;
    }

    public String getLanguage()
    {
        return m_language;
    }

    public void setLanguage(String m_language)
    {
        this.m_language = m_language;
    }

    public String getLocaleCode()
    {
        return getLanguageCode() + "_" + getCountryCode();
    }

}
