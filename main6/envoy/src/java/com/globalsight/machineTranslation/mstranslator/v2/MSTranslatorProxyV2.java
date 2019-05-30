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
package com.globalsight.machineTranslation.mstranslator.v2;

import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.datacontract.schemas._2004._07.Microsoft_MT_Web_Service_V2.TranslateArrayResponse;
import org.tempuri.SoapService;
import org.tempuri.SoapServiceLocator;

import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.machineTranslation.AbstractTranslator;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;
import com.microsofttranslator.api.V2.LanguageService;

/**
 * Acts as a proxy to the translation Machine Translation Service: MS Translator.
 */
public class MSTranslatorProxyV2 extends AbstractTranslator implements MachineTranslator
{
    private static final Logger CATEGORY = Logger.getLogger(MSTranslatorProxyV2.class);
    
    //The MSMT_ACCESS_TOKEN seems to be used wrong. It should no be static. 
    private static String MSMT_ACCESS_TOKEN = null;

    private static final String MS_MT_EXPIRE_ERROR = "The incoming token has expired";

    public MSTranslatorProxyV2()
    {
    }

    public String getEngineName()
    {
        return ENGINE_MSTRANSLATOR;
    }
    
    /**
     * Returns true if the given locale pair is supported for MT.
     */
    public boolean supportsLocalePair(Locale p_sourceLocale,
            Locale p_targetLocale) throws MachineTranslationException
    {
        String sourceLang = p_sourceLocale.getLanguage();
        String sourceCountry = p_sourceLocale.getCountry();
        sourceLang = checkLang(sourceLang, sourceCountry);

        String targetLang = p_targetLocale.getLanguage();
        String targetCountry = p_targetLocale.getCountry();
        targetLang = checkLang(targetLang, targetCountry);

        HashMap paramMap = getMtParameterMap();
        String endpoint = (String) paramMap.get(MachineTranslator.MSMT_ENDPOINT);
        String msClientId = (String) paramMap.get(MachineTranslator.MSMT_CLIENTID);
        String msClientSecret = (String) paramMap.get(MachineTranslator.MSMT_CLIENT_SECRET);
        String msSubscriptionKey = (String) paramMap.get(MachineTranslator.MSMT_SUBSCRIPTION_KEY);
        MachineTranslationProfile mtProfile =  (MachineTranslationProfile) paramMap.get(MachineTranslator.MT_PROFILE);
        LanguageService service = null;
        try
        {
            if (MSMT_ACCESS_TOKEN == null)
            {
                MSMT_ACCESS_TOKEN = MSMTUtil.getMsAccessToken(msClientId, msClientSecret,
                        msSubscriptionKey, mtProfile.getMsTokenUrl());
            }
            SoapService soap = new SoapServiceLocator(endpoint);
            service = soap.getBasicHttpBinding_LanguageService();
            String[] languageArray = service.getLanguagesForTranslate(MSMT_ACCESS_TOKEN);

            Vector<String> langs = new Vector<String>();
            for (String lang : languageArray)
            {
            	langs.add(lang);
            }
            langs.add("pt-PT");//GBS-4000
            langs.add("es-419");
            langs.add("en-gb");//GBS-4007

            CATEGORY.info("Supported languages: " + langs);
            return langs.contains(sourceLang) && langs.contains(targetLang);
        }
        catch (Exception ex)
        {
            if (ex.getMessage().contains(MS_MT_EXPIRE_ERROR)
                    || ex.getMessage().toLowerCase().contains("connection timed out"))
            {
                try
                {
                    MSMT_ACCESS_TOKEN = MSMTUtil.getMsAccessToken(msClientId, msClientSecret,
                            msSubscriptionKey, mtProfile.getMsTokenUrl());
                    String[] languageArray = service.getLanguagesForTranslate(MSMT_ACCESS_TOKEN);

                    Vector<String> langs = new Vector<String>();
                    for (String lang : languageArray)
                    {
                    	langs.add(lang);
                    }
                    langs.add("pt-PT");//GBS-4000
                    langs.add("es-419");
                    langs.add("en-gb");//GBS-4007

                    CATEGORY.info("Supported languages: " + langs);
                    return langs.contains(sourceLang) && langs.contains(targetLang);
                }
                catch (Exception e)
                {
                    CATEGORY.error(e.getMessage());
                }
            }
            CATEGORY.error(ex.getMessage());
        }

        return false;
    }

    protected String doTranslation(Locale p_sourceLocale,
            Locale p_targetLocale, String p_string)
            throws MachineTranslationException
    {
    	String result = "";
    	
    	String sourceLang = p_sourceLocale.getLanguage();
		String sourceCountry = p_sourceLocale.getCountry();
		sourceLang = checkLang(sourceLang, sourceCountry);
		
		String targetLang = p_targetLocale.getLanguage();
		String targetCountry = p_targetLocale.getCountry();
		targetLang = checkLang(targetLang, targetCountry);

		HashMap paramMap = getMtParameterMap();
        String endpoint = (String) paramMap.get(MachineTranslator.MSMT_ENDPOINT);
        String msCategory = (String) paramMap.get(MachineTranslator.MSMT_CATEGORY);
        String msClientId = (String) paramMap.get(MachineTranslator.MSMT_CLIENTID);
        String msClientSecret = (String) paramMap.get(MachineTranslator.MSMT_CLIENT_SECRET);
        String msSubscriptionKey = (String) paramMap.get(MachineTranslator.MSMT_SUBSCRIPTION_KEY);
        MachineTranslationProfile mtProfile =  (MachineTranslationProfile) paramMap.get(MachineTranslator.MT_PROFILE);
   		LanguageService service = null;
   		String exceptionMsg = null;
   		try 
   		{
   		    if (MSMT_ACCESS_TOKEN == null)
   		    {
                MSMT_ACCESS_TOKEN = MSMTUtil.getMsAccessToken(msClientId, msClientSecret,
                        msSubscriptionKey, mtProfile.getMsTokenUrl());
   		    }
   		    SoapService soap = new SoapServiceLocator(endpoint);
   		    service = soap.getBasicHttpBinding_LanguageService();
    			
   		    boolean needTranslateAgain = true;
   		    int count = 0;
   		    //try at most 3 times
            while (MSMT_ACCESS_TOKEN != null && needTranslateAgain && count < 3)
            {
   		        count++;
                result = service.translate(MSMT_ACCESS_TOKEN, p_string, sourceLang, targetLang,
                        MachineTranslator.MSMT_CONTENT_TYPE, msCategory);
                needTranslateAgain = false;
   		    }
   		}
   		catch (Exception ex)
   		{
            if (ex.getMessage().contains(MS_MT_EXPIRE_ERROR)
                    || ex.getMessage().toLowerCase().contains("connection timed out"))
            {
                try
                {
                    MSMT_ACCESS_TOKEN = MSMTUtil.getMsAccessToken(msClientId, msClientSecret,
                            msSubscriptionKey, mtProfile.getMsTokenUrl());
                    boolean needTranslateAgain = true;
                    int count = 0;
                    // try at most 3 times
                    while (MSMT_ACCESS_TOKEN != null && needTranslateAgain && count < 3)
                    {
                        count++;
                        result = service.translate(MSMT_ACCESS_TOKEN, p_string, sourceLang,
                                targetLang, MachineTranslator.MSMT_CONTENT_TYPE, msCategory);
                        needTranslateAgain = false;
                    }
                }
                catch (Exception e)
                {
                    CATEGORY.error(e.getMessage());
                }
            }

   		    exceptionMsg = ex.getMessage();
   		    if (result == null || "".equals(result))
   		    {
				result = "";
			}
   		    else
   		    {
				CATEGORY.info(exceptionMsg);
			}
    	}

    	return result;
    }
    
    protected String[] batchTranslation(MSTranslateConfig config, String[] segments, int count)
    {
        if (count < 0)
            return null;
        
        try
        {
            TranslateArrayResponse[] result = MSTranslator.translator(config, segments);
            
            if (result == null || result.length == 0)
                return batchTranslation(config, segments, count - 1);
            
            return MSMTUtil.toArray(result);
        }
        catch (Exception e)
        {
            String msg = e.getMessage();
            if (msg == null)
                msg = "No Error Message";
            
            //The access token should be update if any exception happened.
            CATEGORY.error(msg);
            config.updateAccessToken();
            
            //Try it again if only connection timed out.
            if (msg.contains(MS_MT_EXPIRE_ERROR)
                    || msg.toLowerCase().contains("connection timed out"))
            {
                return batchTranslation(config, segments, count - 1);
            }
            
            //Don't try it again if the entity is too large.
            if (segments.length == 1 && msg.contains("Entity Too Large"))
            {
                return null;
            }
            
            //Translate these segments one by one
            String[] results = new String[segments.length];
            for (int i = 0; i < segments.length; i++)
            {
                String[] s2 = new String[1];
                s2[0] = segments[i];
                
                String[] s3 = batchTranslation(config, s2, 3);
                if (s3 != null)
                    results[i] = s3[0];
            }
            
            return results;
        }
    }
    
    protected String[] doBatchTranslation(Locale p_sourceLocale,
            Locale p_targetLocale, String[] segments)
            throws MachineTranslationException
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
    	String[] results = null;
    	
    	MSTranslateConfig config = new MSTranslateConfig();
    	config.init(paramMap);
    	config.setMsTokenUrl(mtProfile.getMsTokenUrl());
        config.setSourceLang(checkLang(p_sourceLocale.getLanguage(), p_sourceLocale.getCountry()));
        config.setTargetLang(checkLang(p_targetLocale.getLanguage(), p_targetLocale.getCountry()));
        results = batchTranslation(config, segments, 3);
        if (results != null)
        {
            if (mtProfile.isLogDebugInfo())
            {
                for (int i = 0; i < results.length; i++)
                {
                    CATEGORY.info("Translated segment[" + i + "]:" + results[i]);
                }
            }
        }
        else
        {
            CATEGORY.error("The translation result is null. "
                    + segments.length + " sentences are not translated.");
        }

    	return results;
    }
    
    private String checkLang(String lang, String country)
    {
        if (lang.equals("zh"))
        {
            if (country.equalsIgnoreCase("cn"))
            {
                lang = "zh-CHS";
            }
            else
            {
                lang = "zh-CHT";
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
			lang = (String) getMtParameterMap().get(
					MachineTranslator.SR_LANGUAGE);
			if (lang == null)
				lang = "sr-Latn";
		}

		if (lang.equalsIgnoreCase("pt"))
		{
			if (country.equalsIgnoreCase("pt"))
			{
				lang = "pt-PT";
			}
			else
			{
				lang = "pt";
			}
		}

		if (lang.equalsIgnoreCase("es"))
		{
			lang = "es";
			// es-419: Spanish appropriate for the Latin America and Caribbean
			// region
			if (!country.equalsIgnoreCase("es"))
			{
				lang = "es-419";
			}
		}

		if (lang.equalsIgnoreCase("en"))
		{
			if (country.equalsIgnoreCase("gb"))
			{
				lang = "en-gb";
			}
		}
		return lang;
    }
}
