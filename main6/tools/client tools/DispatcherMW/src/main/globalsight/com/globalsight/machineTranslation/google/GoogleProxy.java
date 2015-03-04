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
package com.globalsight.machineTranslation.google;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.google.translate.api.v2.core.Translator;
import org.google.translate.api.v2.core.model.Translation;

import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileConstants;
import com.globalsight.machineTranslation.AbstractTranslator;
import com.globalsight.machineTranslation.MTHelper;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;

/**
 * Acts as a proxy to the free translation Machine Translation Service.
 */
public class GoogleProxy extends AbstractTranslator implements MachineTranslator
{
    private static final Logger CATEGORY =
        Logger.getLogger(GoogleProxy.class);

    /**
     * Hash Set of all supported languages.
     */
    private static final HashSet<String> s_supportedLanguage;
    
    public static final int MT_GOOGLE_MAX_CHARACTER_NUM = 4000;

    // Insert all supported languages into the hash set
    static
    {
        s_supportedLanguage = new HashSet<String>(100);

        //English
        s_supportedLanguage.add("en");
        //Afrikaans
        s_supportedLanguage.add("af");
        //Albanian
        s_supportedLanguage.add("al");//sq
        //Arabic
        s_supportedLanguage.add("ar");
        //Belarusian
        s_supportedLanguage.add("be");
        //Bulgarian
        s_supportedLanguage.add("bg");
        //Catalan
        s_supportedLanguage.add("ca");
        //Chinese (Simplified)
        s_supportedLanguage.add("zh");
        //Chinese (Traditional) tw and hk
        s_supportedLanguage.add("zt");
        //Croatian
        s_supportedLanguage.add("hr");
        //Czech
        s_supportedLanguage.add("cs");
        //Danish
        s_supportedLanguage.add("da");
        //Dutch
        s_supportedLanguage.add("nl");
        //Estonian
        s_supportedLanguage.add("et");
        //Filipino
        s_supportedLanguage.add("tl");
        //Finnish
        s_supportedLanguage.add("fi");
        //French
        s_supportedLanguage.add("fr");
        //Galician
        s_supportedLanguage.add("gl");
        //German
        s_supportedLanguage.add("de");
        //Greek
        s_supportedLanguage.add("el");
        //Hebrew
        s_supportedLanguage.add("iw");
        //Hindi
        s_supportedLanguage.add("hi");
        //Hungarian
        s_supportedLanguage.add("hu");
        //Icelandic
        s_supportedLanguage.add("is");
        //Irish
        s_supportedLanguage.add("ga");
        //Indonesian
        s_supportedLanguage.add("id");// ISO 639
//        s_supportedLanguage.add("in");// Java JDK "Locale" returns "in" as language name.
        //Italian
        s_supportedLanguage.add("it");
        //Japanese
        s_supportedLanguage.add("ja");
        //Korean
        s_supportedLanguage.add("ko");
        //Latvian
        s_supportedLanguage.add("lv");
        //Lithuanian
        s_supportedLanguage.add("lt");
        //Macedonian
        s_supportedLanguage.add("mk");
        //Malay
        s_supportedLanguage.add("ms");
        //Maltese
        s_supportedLanguage.add("mt");
        //Norwegian
        s_supportedLanguage.add("no");
        //Persian
        s_supportedLanguage.add("fa");
        //Polish
        s_supportedLanguage.add("pl");
        //Portuguese
        s_supportedLanguage.add("pt");
        //Romanian
        s_supportedLanguage.add("ro");
        //Russian
        s_supportedLanguage.add("ru");
        //Serbian
        s_supportedLanguage.add("sr");
        //Slovak
        s_supportedLanguage.add("sk");
        //Slovenian (Slovene)
        s_supportedLanguage.add("sl");
        //Spanish
        s_supportedLanguage.add("es");
        //Swahili
        s_supportedLanguage.add("sw");
        //Swedish
        s_supportedLanguage.add("sv");
        //Tagalog
        s_supportedLanguage.add("tl");
        //Thai
        s_supportedLanguage.add("th");
        //Turkish
        s_supportedLanguage.add("tr");
        //Ukrainian
        s_supportedLanguage.add("uk");
        //Vietnamese
        s_supportedLanguage.add("vi");
        //Welsh
        s_supportedLanguage.add("cy");
        //Yiddish
        s_supportedLanguage.add("yi");
        
//        Azerbaijani	
        s_supportedLanguage.add("az");
//        Basque	
        s_supportedLanguage.add("eu");
//        Bengali	
        s_supportedLanguage.add("bn");
//        Esperanto	
        s_supportedLanguage.add("eo");
//        Georgian	
        s_supportedLanguage.add("ka");
//        Gujarati	
        s_supportedLanguage.add("gu");
//        Haitian Creole	
        s_supportedLanguage.add("ht");
//        Kannada	
        s_supportedLanguage.add("kn");
//        Latin	
        s_supportedLanguage.add("la");
//        Tamil	
        s_supportedLanguage.add("ta");
//        Telugu	
        s_supportedLanguage.add("te");
//        Urdu	
        s_supportedLanguage.add("ur");
    }
    
    public GoogleProxy() throws MachineTranslationException
    {
    }

    public String getEngineName()
    {
        return ENGINE_GOOGLE;
    }

    /**
     * Returns true if the given locale pair is supported by Google MT.
     * Note: Any language pair among Google supported languages is available.
     */
    public boolean supportsLocalePair(Locale p_sourceLocale,
            Locale p_targetLocale) throws MachineTranslationException
    {
        String srcLang = mapLanguage(p_sourceLocale);
        String trgLang = mapLanguage(p_targetLocale);

        return s_supportedLanguage.contains(srcLang)
                && s_supportedLanguage.contains(trgLang);
    }

    /**
     * Returns special language identifier for certain languages such as
     * Traditional Chinese ("zt"), Indonesian.
     */
    private String mapLanguage(Locale p_locale)
    {
        String result = p_locale.getLanguage();

        if (result.equals("zh"))
        {
            if (p_locale.getCountry().equalsIgnoreCase("tw")
                    || p_locale.getCountry().equalsIgnoreCase("hk"))
            {
                result = "zt";
            }
        }
        
        // Indonesian (in_ID --> id_ID)
        if ("in".equals(p_locale.getLanguage())
                && "ID".equals(p_locale.getCountry()))
        {
            result = "id";
        }

        return result;
    }

    /**
     * Actually does the real work of communicating with the MT engine.
     */
    @SuppressWarnings("rawtypes")
    protected String doTranslation(Locale p_sourceLocale,
            Locale p_targetLocale, String p_string)
        throws MachineTranslationException
    {
		String sourceLang = p_sourceLocale.getLanguage();
		String sourceCountry = p_sourceLocale.getCountry();
		sourceLang = checkLang(sourceLang, sourceCountry);

		String targetLang = p_targetLocale.getLanguage();
		String targetCountry = p_targetLocale.getCountry();
		targetLang = checkLang(targetLang, targetCountry);

        if (sourceLang != null && targetLang != null)
        {
            HashMap paramMap = getMtParameterMap();
            String apiKey = (String) paramMap
                    .get(MTProfileConstants.MT_GOOGLE_API_KEY);
            Translator translator = new Translator(apiKey);
            Translation translation = null;

            try
            {
                translation = translator.translate(p_string, sourceLang,
                        targetLang);
            }
            catch (Exception e)
            {
                CATEGORY.error(e);
            }

            if (translation != null)
            {
                return translation.toString();
            }
        }
    	
    	return null;
    }
    
    protected String[] doBatchTranslation(Locale p_sourceLocale,
            Locale p_targetLocale, String[] p_segments)
            throws MachineTranslationException
    {
    	 if (MTHelper.isLogDetailedInfo(ENGINE_GOOGLE))
         {
             for (int i = 0; i < p_segments.length; i++)
             {
                 CATEGORY.info("Source segment[" + i + "]:" + p_segments[i]);
             }
         }

    	String sourceLang = p_sourceLocale.getLanguage();
		String sourceCountry = p_sourceLocale.getCountry();
		sourceLang = checkLang(sourceLang, sourceCountry);

		String targetLang = p_targetLocale.getLanguage();
		String targetCountry = p_targetLocale.getCountry();
		targetLang = checkLang(targetLang, targetCountry);
		Translation[] translations =null;
		
        if (sourceLang != null && targetLang != null)
        {
            String apiKey = getMtParameterMap().get(
                    MTProfileConstants.MT_GOOGLE_API_KEY).toString();
            Translator translator = new Translator(apiKey);

            try
            {
                translations = translator.translate(p_segments, sourceLang,
                        targetLang);
            }
            catch (Exception e)
            {
                CATEGORY.error(e);
            }
        }
		String[] results;
		
		if (translations != null && translations.length == p_segments.length)
		{
			results = new String[translations.length];

			for (int i=0; i<translations.length; i++)
			{
				results[i] = translations[i].getTranslatedText();
			}

			if (MTHelper.isLogDetailedInfo(ENGINE_GOOGLE))
            {
                for (int i = 0; i < results.length; i++)
                {
                    CATEGORY.info("Translated segment[" + i + "]:" + results[i]);
                }
            }

			return results;
		}

		return null;
    }
    
    private String checkLang(String lang, String country)
    {
        // ALBANIAN
        if (lang != null && "al".equals(lang))
        {
            lang = "sq";
        }
        // CHINESE_SIMPLIFIED CHINESE_TRADITIONAL
        if (country != null && "CN".equals(country))
        {
            lang = "zh-CN";
        }
        else if (country != null && "TW".equals(country))
        {
            lang = "zh-TW";
        }
        // Regard ZH-HK as ZH-TW
        else if (country != null && "HK".equals(country))
        {
            lang = "zh-TW";
        }
        // Indonesian
        if (lang != null && "in".equals(lang) && country != null
                && country.equals("ID"))
        {
            lang = "id";
        }

        return lang;
    }
    
    /**
     * Detect the language for specified string.
     * 
     * @param p_string - String to be detected.
     * 
     * @return String to indicate the language.
     */
    public static String detectLanguage(String p_string)
    {
        if (p_string == null || "".equals(p_string.trim()))
        {
            return null;
        }
        
        String result = null;
        try
        {
//            String httpReferrer = "GlobalSight";
//            Detect.setHttpReferrer(httpReferrer);
//            DetectResult dr = Detect.execute(p_string);
//            Language lang = dr.getLanguage();
//            result = lang.toString();
        }
        catch (Exception e)
        {
        }
        
        return result;
    }
}
