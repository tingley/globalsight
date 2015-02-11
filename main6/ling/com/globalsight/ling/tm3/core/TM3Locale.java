package com.globalsight.ling.tm3.core;

/**
 * Mostly-opaque representation of a locale, designed to simplify embedding.
 * For normal GlobalSight, this is implemented by GlobalSightLocale.
 */
public interface TM3Locale {

    /**
     * Return the ID of this locale.
     */
    public long getId();

    /**
     * Return the locale code.
     */
    public String getLocaleCode();

    /**
     * Return the language code.
     */
    public String getLanguage();
}
