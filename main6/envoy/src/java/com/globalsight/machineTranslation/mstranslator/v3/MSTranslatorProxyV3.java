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
package com.globalsight.machineTranslation.mstranslator.v3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.machineTranslation.AbstractTranslator;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;

/**
 * Acts as a proxy to the translation Machine Translation Service: MS
 * Translator.
 */
public class MSTranslatorProxyV3 extends AbstractTranslator implements MachineTranslator
{
    private static final Logger CATEGORY = Logger.getLogger(MSTranslatorProxyV3.class);
    private static List<String> LOCALES = new ArrayList<>();
    static
    {
        LOCALES.add("af");
        LOCALES.add("ar");
        LOCALES.add("bg");
        LOCALES.add("bn");
        LOCALES.add("bs");
        LOCALES.add("ca");
        LOCALES.add("cs");
        LOCALES.add("cy");
        LOCALES.add("da");
        LOCALES.add("de");
        LOCALES.add("el");
        LOCALES.add("en");
        LOCALES.add("es");
        LOCALES.add("et");
        LOCALES.add("fa");
        LOCALES.add("fi");
        LOCALES.add("fil");
        LOCALES.add("fj");
        LOCALES.add("fr");
        LOCALES.add("he");
        LOCALES.add("hi");
        LOCALES.add("hr");
        LOCALES.add("ht");
        LOCALES.add("hu");
        LOCALES.add("id");
        LOCALES.add("is");
        LOCALES.add("it");
        LOCALES.add("ja");
        LOCALES.add("ko");
        LOCALES.add("lt");
        LOCALES.add("lv");
        LOCALES.add("mg");
        LOCALES.add("ms");
        LOCALES.add("mt");
        LOCALES.add("mww");
        LOCALES.add("nb");
        LOCALES.add("nl");
        LOCALES.add("otq");
        LOCALES.add("pl");
        LOCALES.add("pt");
        LOCALES.add("ro");
        LOCALES.add("ru");
        LOCALES.add("sk");
        LOCALES.add("sl");
        LOCALES.add("sm");
        LOCALES.add("sr");
        LOCALES.add("sr-Cyrl");
        LOCALES.add("sr-Latn");
        LOCALES.add("sv");
        LOCALES.add("sw");
        LOCALES.add("ta");
        LOCALES.add("th");
        LOCALES.add("tlh");
        LOCALES.add("to");
        LOCALES.add("tr");
        LOCALES.add("ty");
        LOCALES.add("uk");
        LOCALES.add("ur");
        LOCALES.add("vi");
        LOCALES.add("yua");
        LOCALES.add("yue");
        LOCALES.add("zh");
        LOCALES.add("zh-Hans");
        LOCALES.add("zh-Hant");
    }

    public MSTranslatorProxyV3()
    {
    }

    public String getEngineName()
    {
        return ENGINE_MSTRANSLATOR;
    }

    /**
     * Returns true if the given locale pair is supported for MT.
     */
    public boolean supportsLocalePair(Locale p_sourceLocale, Locale p_targetLocale)
            throws MachineTranslationException
    {
        String sourceLang = p_sourceLocale.getLanguage();
        String sourceCountry = p_sourceLocale.getCountry();
        sourceLang = checkLang(sourceLang, sourceCountry);

        String targetLang = p_targetLocale.getLanguage();
        String targetCountry = p_targetLocale.getCountry();
        targetLang = checkLang(targetLang, targetCountry);
        
        CATEGORY.info("Supported languages: " + LOCALES);
        return LOCALES.contains(sourceLang) && LOCALES.contains(targetLang);
    }

    protected String doTranslation(Locale p_sourceLocale, Locale p_targetLocale, String p_string)
            throws MachineTranslationException
    {
        String sourceLang = p_sourceLocale.getLanguage();
        String sourceCountry = p_sourceLocale.getCountry();
        sourceLang = checkLang(sourceLang, sourceCountry);

        String targetLang = p_targetLocale.getLanguage();
        String targetCountry = p_targetLocale.getCountry();
        targetLang = checkLang(targetLang, targetCountry);

        try
        {
            return new MsTranslatorMTUtil().Translate(sourceLang, targetLang, p_string,
                    getMtParameterMap());
        }
        catch (Exception e)
        {
            CATEGORY.error(e);
        }

        return p_string;
    }

    protected String[] doBatchTranslation(Locale p_sourceLocale, Locale p_targetLocale,
            String[] segments) throws MachineTranslationException
    {
        HashMap paramMap = getMtParameterMap();
        MachineTranslationProfile mtProfile = (MachineTranslationProfile) paramMap
                .get(MachineTranslator.MT_PROFILE);
        if (mtProfile.isLogDebugInfo())
        {
            for (int i = 0; i < segments.length; i++)
            {
                CATEGORY.info("Source segment[" + i + "]:" + segments[i]);
            }
        }

        String sourceLang = p_sourceLocale.getLanguage();
        String sourceCountry = p_sourceLocale.getCountry();
        sourceLang = checkLang(sourceLang, sourceCountry);

        String targetLang = p_targetLocale.getLanguage();
        String targetCountry = p_targetLocale.getCountry();
        targetLang = checkLang(targetLang, targetCountry);

        MsTranslatorMTUtil util = new MsTranslatorMTUtil();
        String[] result = new String[segments.length];
        for (int i = 0; i < segments.length; i++)
        {
            try
            {
                result[i] = util.Translate(sourceLang, targetLang, segments[i],
                        getMtParameterMap());
                if (mtProfile.isLogDebugInfo())
                {
                    CATEGORY.info("Translated segment[" + i + "]:" + result[i]);
                }
            }
            catch (Exception e)
            {
                CATEGORY.error(e);
            }
        }

        return result;
    }

    private String checkLang(String lang, String country)
    {
        if (lang.equals("zh"))
        {
            if (country.equalsIgnoreCase("cn"))
            {
                lang = "zh-Hans";
            }
            else
            {
                lang = "zh-Hant";
            }
        }

        if (lang.equals("iw"))
        {
            lang = "he";
        }

        // Indonesian
        if ("in".equals(lang) && "ID".equalsIgnoreCase(country))
        {
            lang = "id";
        }

        // GBS-3985: "sr_RS" and "sr_YU" are translated to "sr-Latn" default.
        if (lang.equalsIgnoreCase("sr"))
        {
            lang = (String) getMtParameterMap().get(MachineTranslator.SR_LANGUAGE);
            if (lang == null)
                lang = "sr-Latn";
        }

	//GBS-4859 for TA,no_NO and nb_NO
	//mstranslator only does nb, and aliases no to nb.
	if (lang.equalsIgnoreCase("no"))
        {
            if (country.equalsIgnoreCase("no"))
            {
                lang = "nb";
            }
        }


        return lang;
    }
}
