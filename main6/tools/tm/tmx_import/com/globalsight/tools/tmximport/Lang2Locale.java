/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

package com.globalsight.tools.tmximport;

import java.util.Map;
import java.util.HashMap;


/**
 * This class converts two letter lang code to five letter locale code
 */
public class Lang2Locale
{
    private static final Map DEFAULT_LANG2LOCALE_MAP
        = getDefaultLang2LocaleMap();
    
    private Map m_lang2LocaleMap;
    
    public Lang2Locale()
    {
        m_lang2LocaleMap = new HashMap(DEFAULT_LANG2LOCALE_MAP);
    }
    

    /**
     * Override the default map.
     *
     * @param p_lang two letter lang code
     * @param p_locale locale name in the format of "lang code" +
     * underscore ("_") + "country code"
     */
    public void setMapEntry(String p_lang, String p_locale)
    {
        m_lang2LocaleMap.put(p_lang, p_locale);
    }


    /**
     * get locale name from two letter lang code
     *
     * @param p_lang two letter language code
     * @return locale name in the format of "lang code" +
     * underscore ("_") + "country code"
     */
    public String getLocaleName(String p_lang)
    {
        return (String)m_lang2LocaleMap.get(p_lang);
    }
    

    // default lang code to locale mapping
    private static Map getDefaultLang2LocaleMap()
    {
        Map map = new HashMap(16 * 3);
        map.put("ar", "ar_SA"); // Arabic - Saudi Arabia
        map.put("be", "be_BY"); // Byelorussian - Byelorussia
        map.put("bg", "bg_BG"); // Bulgarian  - Bulgaria
        map.put("ca", "ca_ES"); // Catalan - Spain
        map.put("cs", "cs_CZ"); // Czech - Czech Republic
        map.put("da", "da_DK"); // Danish - Denmark 
        map.put("de", "de_DE"); // German - Germany 
        map.put("el", "el_GR"); // Greek - Greece 
        map.put("en", "en_US"); // English - United States 
        map.put("es", "es_ES"); // Spanish - Spain 
        map.put("et", "et_EE"); // Estonian - Estonia
        map.put("fi", "fi_FI"); // Finnish - Finland 
        map.put("fr", "fr_FR"); // French - France 
        map.put("hr", "hr_HR"); // Croatian - Croatia
        map.put("hu", "hu_HU"); // Hungarian  - Hungary
        map.put("is", "is_IS"); // Icelandic  - Iceland
        map.put("it", "it_IT"); // Italian - Italy 
        map.put("iw", "iw_IL"); // Hebrew - Israel
        map.put("ja", "ja_JP"); // Japanese - Japan 
        map.put("ko", "ko_KR"); // Korean - Korea
        map.put("lt", "lt_LT"); // Lithuanian - Lithuania
        map.put("lv", "lv_LV"); // Latvian - Latvia
        map.put("mk", "mk_MK"); // Macedonian - Macedonia
        map.put("nl", "nl_NL"); // Dutch - Netherlands 
        map.put("no", "no_NO"); // Norwegian (Nynorsk) - Norway
        map.put("pl", "pl_PL"); // Polish - Poland
        map.put("pt", "pt_PT"); // Portuguese - Portugal 
        map.put("ro", "ro_RO"); // Romanian - Romania
        map.put("ru", "ru_RU"); // Russian - Russia
        map.put("sh", "sh_YU"); // Serbo-Croatian  - Yugoslavia
        map.put("sk", "sk_SK"); // Slovakian  - Slovakia
        map.put("sl", "sl_SL"); // Slovenian  - Slovenia
        map.put("sq", "sq_AL"); // Albanian - Albania
        map.put("sr", "sr_YU"); // Serbian (Cyrillic) - Yugoslavia
        map.put("sv", "sv_SE"); // Swedish - Sweden 
        map.put("th", "th_TH"); // Thai  - Thailand
        map.put("tr", "tr_TR"); // Turkish - Turkey 
        map.put("uk", "uk_UA"); // Ukranian - Ukraine
        map.put("vi", "vi_VN"); // Vietnamese - Vietnam

        return map;
    }
    

}
