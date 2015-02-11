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

import org.apache.log4j.Logger;

import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.machineTranslation.AbstractTranslator;

import com.globalsight.everest.util.system.SystemConfiguration;
import com.google.api.detect.Detect;
import com.google.api.detect.DetectResult;
import com.google.api.translate.Language;
import com.google.api.translate.Translate;

import java.util.*;

/**
 * Acts as a proxy to the free translation Machine Translation Service.
 */
public class GoogleProxy extends AbstractTranslator implements MachineTranslator
{
    private static final Logger CATEGORY =
        Logger.getLogger(GoogleProxy.class);

    private static final String ENGINE_NAME = "Google";

    /**
     * Hash Set of all supported languages.
     */
    private static final HashSet s_supportedLanguage;

    // Insert all supported languages into the hash set
    static
    {
        s_supportedLanguage = new HashSet(100);

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
    }
    
    public GoogleProxy() throws MachineTranslationException
    {
    }

    public String getEngineName()
    {
        return ENGINE_NAME;
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
     * Actually does the real work of communicating with the MT engine.
     */
    protected String doTranslation(Locale p_sourceLocale,
            Locale p_targetLocale, String p_string)
        throws MachineTranslationException
    {
    	String result = "";
    	try 
    	{
    		String sourceLang = p_sourceLocale.getLanguage();
    		String sourceCountry = p_sourceLocale.getCountry();
    		sourceLang = checkLang(sourceLang, sourceCountry);
    		
    		String targetLang = p_targetLocale.getLanguage();
    		String targetCountry = p_targetLocale.getCountry();
    		targetLang = checkLang(targetLang, targetCountry);

    		Language srcLang = Language.fromString(sourceLang);
    		Language trgLang = Language.fromString(targetLang);
    		if (srcLang != null && trgLang != null) 
    		{
    			SystemConfiguration config = SystemConfiguration.getInstance();
                String server_host = config.getStringParameter(SystemConfiguration.SERVER_HOST);
                String server_port = config.getStringParameter(SystemConfiguration.SERVER_PORT);
                String httpReferrer = "http://" + server_host + ":" + server_port;
    			Translate.setHttpReferrer(httpReferrer);
        		result = Translate.execute(p_string.trim(), srcLang, trgLang );
    		}

    		if (result == null || "null".equalsIgnoreCase(result)) {
                result = "";
            }
    	}
    	catch (Exception ex)
    	{
    		CATEGORY.error(ex.getMessage());
    	}
    	
    	return result;
    }
    
    /**
     * Actually does the real work of communicating with the MT engine.
     */
    protected String[] doBatchTranslation(Locale p_sourceLocale,
            Locale p_targetLocale, String[] p_segments)
            throws MachineTranslationException
    {
        String[] translatedSegments = null;
        try
        {
            String sourceLang = p_sourceLocale.getLanguage();
            String sourceCountry = p_sourceLocale.getCountry();
            sourceLang = checkLang(sourceLang, sourceCountry);

            String targetLang = p_targetLocale.getLanguage();
            String targetCountry = p_targetLocale.getCountry();
            targetLang = checkLang(targetLang, targetCountry);

            Language srcLang = Language.fromString(sourceLang);
            Language trgLang = Language.fromString(targetLang);
            if (srcLang != null && trgLang != null)
            {
                SystemConfiguration config = SystemConfiguration.getInstance();
                String server_host = config
                        .getStringParameter(SystemConfiguration.SERVER_HOST);
                String server_port = config
                        .getStringParameter(SystemConfiguration.SERVER_PORT);
                String httpReferrer = "http://" + server_host + ":" + server_port;
                Translate.setHttpReferrer(httpReferrer);
                // Invoke 3 times at most.
                int count = 0;
                while (count < 3) {
                    count++;
                    try {
                        translatedSegments =
                            Translate.execute(p_segments, srcLang, trgLang);
                        break;
                    } catch (Exception e) {
                        // If exception occurs,sleep 5 seconds before next time.
                        if (count != 3) {
                            Thread.sleep(5000);
                        }
                        if (CATEGORY.isDebugEnabled()) {
                            CATEGORY.error(e.getMessage());
                        }
                    }
                }
            }
            
            /**
            if (translatedSegments == null
                    || translatedSegments.length != p_segments.length)
            {
                translatedSegments = p_segments;
            }
            */
        }
        catch (Exception ex)
        {
            if (CATEGORY.isDebugEnabled()) {
                CATEGORY.error(ex.getMessage());                
            }
        }

        return translatedSegments;
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
            return "";
        }
        
        String result = "";
        try
        {
            String httpReferrer = "GlobalSight";
            Detect.setHttpReferrer(httpReferrer);
            DetectResult dr = Detect.execute(p_string);
            Language lang = dr.getLanguage();
            result = lang.toString();
        }
        catch (Exception e)
        {
        }
        
        //CATEGORY.info("Google detectLanguage "+result+"\t"+p_string);
        return result;
    }
}
