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
package com.globalsight.machineTranslation.mstranslator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.datacontract.schemas._2004._07.Microsoft_MT_Web_Service_V2.TranslateArrayResponse;
import org.datacontract.schemas._2004._07.Microsoft_MT_Web_Service_V2.TranslateOptions;
import org.tempuri.SoapService;
import org.tempuri.SoapServiceLocator;

import com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileConstants;
import com.globalsight.machineTranslation.AbstractTranslator;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;
import com.microsoft.schemas.MSNSearch._2005._09.fex.LanguagePair;
import com.microsoft.schemas.MSNSearch._2005._09.fex.TranslationRequest;
import com.microsofttranslator.api.V2.LanguageService;

/**
 * Acts as a proxy to the translation Machine Translation Service: MS Translator.
 */
public class MSTranslatorProxy extends AbstractTranslator implements MachineTranslator
{
    private static final Logger CATEGORY =
        Logger.getLogger(MSTranslatorProxy.class);
    
    private static final String ENGINE_NAME = "MS_Translator";
    
    /**
     * Hash Set of all supported language pairs ("General" domain)
     */
    private static final HashSet s_supportedGeneralLanguagePairs;
    
    /**
     * Hash Set of all supported language pairs ("Technical" domain)
     */
    private static final HashSet s_supportedTechnicalLanguagePairs;
    
    private static String MSMT_ACCESS_TOKEN = null;
    private static final String MS_MT_EXPIRE_ERROR = "The incoming token has expired";

    /**
     * Only list English to others and others to English.
     */
    static
    {
        s_supportedGeneralLanguagePairs = new HashSet(100);
    	
        //Arabic
    	s_supportedGeneralLanguagePairs.add("en_ar");
    	s_supportedGeneralLanguagePairs.add("ar_en");
        //Bulgarian
    	s_supportedGeneralLanguagePairs.add("en_bg");
    	s_supportedGeneralLanguagePairs.add("bg_en");
        //Chinese (Simplified)
    	s_supportedGeneralLanguagePairs.add("en_zh"); //zh-CN for MS MT
    	s_supportedGeneralLanguagePairs.add("zh_en");
        //Chinese (Traditional)
    	s_supportedGeneralLanguagePairs.add("en_zt"); //zh-TW for MS MT
    	s_supportedGeneralLanguagePairs.add("zt_en");
        //Czech
    	s_supportedGeneralLanguagePairs.add("en_cs");
    	s_supportedGeneralLanguagePairs.add("cs_en");
        //Danish
        s_supportedGeneralLanguagePairs.add("en_da");
        s_supportedGeneralLanguagePairs.add("da_en");
        //Dutch
        s_supportedGeneralLanguagePairs.add("en_nl");
        s_supportedGeneralLanguagePairs.add("nl_en");
        //Finnish
        s_supportedGeneralLanguagePairs.add("en_fi");
        s_supportedGeneralLanguagePairs.add("fi_en");
        //French
        s_supportedGeneralLanguagePairs.add("en_fr");
        s_supportedGeneralLanguagePairs.add("fr_en");
    	//German
        s_supportedGeneralLanguagePairs.add("en_de");
        s_supportedGeneralLanguagePairs.add("de_en");
        //Greek
        s_supportedGeneralLanguagePairs.add("en_el");
        s_supportedGeneralLanguagePairs.add("el_en");
        //Hebrew
        s_supportedGeneralLanguagePairs.add("en_iw"); //he for MS MT
        s_supportedGeneralLanguagePairs.add("iw_en"); 
        //Italian
        s_supportedGeneralLanguagePairs.add("en_it");
        s_supportedGeneralLanguagePairs.add("it_en");
        //Japanese
        s_supportedGeneralLanguagePairs.add("en_ja");
        s_supportedGeneralLanguagePairs.add("ja_en");
        //Korean
        s_supportedGeneralLanguagePairs.add("en_ko");
        s_supportedGeneralLanguagePairs.add("ko_en");
        //Polish
        s_supportedGeneralLanguagePairs.add("en_pl");
        s_supportedGeneralLanguagePairs.add("pl_en");        
        //Portuguese
        s_supportedGeneralLanguagePairs.add("en_pt");
        s_supportedGeneralLanguagePairs.add("pt_en");
        //Russian
        s_supportedGeneralLanguagePairs.add("en_ru");
        s_supportedGeneralLanguagePairs.add("ru_en");
        //Spanish
        s_supportedGeneralLanguagePairs.add("en_es");
        s_supportedGeneralLanguagePairs.add("es_en");
        //Swedish
        s_supportedGeneralLanguagePairs.add("en_sv");
        s_supportedGeneralLanguagePairs.add("sv_en");
        //Thai
        s_supportedGeneralLanguagePairs.add("en_th");
        s_supportedGeneralLanguagePairs.add("th_en");
    }
    
    /**
     * MS Translator "Technical" domain supports below language pairs.
     */
    static
    {
    	s_supportedTechnicalLanguagePairs = new HashSet(100);
        //English to Arabic
    	s_supportedTechnicalLanguagePairs.add("en_ar");
        //English to Chinese (Simplified)
    	s_supportedTechnicalLanguagePairs.add("en_zh");
        //English to Chinese (Traditional)
    	s_supportedTechnicalLanguagePairs.add("en_zt");
        //English to Czech
    	s_supportedTechnicalLanguagePairs.add("en_cs");
        //English to Danish
        s_supportedTechnicalLanguagePairs.add("en_da");
        //English to Dutch
        s_supportedTechnicalLanguagePairs.add("en_nl");
        //English to Finnish
        s_supportedTechnicalLanguagePairs.add("en_fi");
        //English to French
        s_supportedTechnicalLanguagePairs.add("en_fr");
    	//English to German
        s_supportedTechnicalLanguagePairs.add("en_de");
        //English to Greek
        s_supportedTechnicalLanguagePairs.add("en_el");
        //English to Hebrew
        s_supportedTechnicalLanguagePairs.add("en_iw"); //he
        //English to Hindi
        s_supportedTechnicalLanguagePairs.add("en_hi");
        //English to Hungarian
        s_supportedTechnicalLanguagePairs.add("en_hu");
        //English to Italian
        s_supportedTechnicalLanguagePairs.add("en_it");
        //English to Japanese
        s_supportedTechnicalLanguagePairs.add("en_ja");
        //English to Korean
        s_supportedTechnicalLanguagePairs.add("en_ko");
        //English to Norwegian
        s_supportedTechnicalLanguagePairs.add("en_no");
        //English to Polish
        s_supportedTechnicalLanguagePairs.add("en_pl");
        //English to Portuguese(Brazilian & European)
        s_supportedTechnicalLanguagePairs.add("en_pt");
        //English to Russian
        s_supportedTechnicalLanguagePairs.add("en_ru");
        //English to Spanish
        s_supportedTechnicalLanguagePairs.add("en_es");
        //English to Swedish
        s_supportedTechnicalLanguagePairs.add("en_sv");
        //English to Thai
        s_supportedTechnicalLanguagePairs.add("en_th");
        //English to Turkish
        s_supportedTechnicalLanguagePairs.add("en_tr");
        //Japanese to Korean
        s_supportedTechnicalLanguagePairs.add("ja_ko");
        //Chinese Simplified to Chinese Traditional
        s_supportedTechnicalLanguagePairs.add("zh_zt");
        // Chinese Traditional to Chinese Simplified
        s_supportedTechnicalLanguagePairs.add("zt_zh");
    }

    public MSTranslatorProxy() throws MachineTranslationException
    {
    }

    public String getEngineName()
    {
        return ENGINE_NAME;
    }
    
    /**
     * Returns true if the given locale pair is supported for MT.
     */
    public boolean supportsLocalePair(Locale p_sourceLocale,
            Locale p_targetLocale) throws MachineTranslationException
    {
        HashMap paramMap = getMtParameterMap();
        String msMtUrlFlag = TMProfileConstants.MT_MS_URL_FLAG_PUBLIC;

        String sourceLang = p_sourceLocale.getLanguage();
        String sourceCountry = p_sourceLocale.getCountry();
        sourceLang = checkLang(sourceLang, sourceCountry, msMtUrlFlag);

        String targetLang = p_targetLocale.getLanguage();
        String targetCountry = p_targetLocale.getCountry();
        targetLang = checkLang(targetLang, targetCountry, msMtUrlFlag);

        /*
         * if (msMtUrlFlag != null &&
         * msMtUrlFlag.equals(TMProfileConstants.MT_MS_URL_FLAG_INTERNAL)) {
         * StringBuffer langPair = new StringBuffer();
         * 
         * langPair.append(mapLanguage(p_sourceLocale)); langPair.append("_");
         * langPair.append(mapLanguage(p_targetLocale));
         * 
         * // This may is related with ms translator domain id ("general" or
         * "technical") return
         * s_supportedGeneralLanguagePairs.contains(langPair.toString()) ||
         * s_supportedTechnicalLanguagePairs.contains(langPair.toString()); }
         * else if (msMtUrlFlag != null &&
         * msMtUrlFlag.equals(TMProfileConstants.MT_MS_URL_FLAG_PUBLIC)) {
         */
            String endpoint = (String) paramMap
                    .get(MachineTranslator.MSMT_ENDPOINT);
//            String msAppId = (String) paramMap
//                    .get(MachineTranslator.MSMT_APPID);
            String msClientId = (String) paramMap
                    .get(MachineTranslator.MSMT_CLIENTID);
            String msClientSecret = (String) paramMap
                    .get(MachineTranslator.MSMT_CLIENT_SECRET);
            LanguageService service = null;
            try
            {
                if (MSMT_ACCESS_TOKEN == null)
                {
                    MSMT_ACCESS_TOKEN = MSMTUtil.getAccessToken(msClientId, msClientSecret);
                }
                SoapService soap = new SoapServiceLocator(endpoint);
                service = soap.getBasicHttpBinding_LanguageService();
                String[] languageArray = service.getLanguagesForTranslate(MSMT_ACCESS_TOKEN);
                
                List<String> tmp = Arrays.asList(languageArray);
                return tmp.contains(sourceLang) && tmp.contains(targetLang);
            }
            catch (Exception ex)
            {
                if (ex.getMessage().contains(MS_MT_EXPIRE_ERROR))
                {
                    try
                    {
                        MSMT_ACCESS_TOKEN = MSMTUtil.getAccessToken(msClientId, msClientSecret);
                        String[] languageArray = service.getLanguagesForTranslate(MSMT_ACCESS_TOKEN);
                        List<String> tmp = Arrays.asList(languageArray);
                        return tmp.contains(sourceLang) && tmp.contains(targetLang);
                    }
                    catch (Exception e)
                    {
                        CATEGORY.error(e.getMessage(), e);
                    }
                }
                CATEGORY.error(ex.getMessage(), ex);
            }
        // }

        return false;
    }
    
    protected String doTranslation(Locale p_sourceLocale, Locale p_targetLocale, String p_string)
    	throws MachineTranslationException
    {
    	String result = "";
    	
    	HashMap paramMap = getMtParameterMap();

        String msMtUrlFlag = TMProfileConstants.MT_MS_URL_FLAG_PUBLIC;
    	String sourceLang = p_sourceLocale.getLanguage();
		String sourceCountry = p_sourceLocale.getCountry();
		sourceLang = checkLang(sourceLang, sourceCountry, msMtUrlFlag);
		
		String targetLang = p_targetLocale.getLanguage();
		String targetCountry = p_targetLocale.getCountry();
		targetLang = checkLang(targetLang, targetCountry, msMtUrlFlag);
    	
    	// the ms translator use the internal url
    	/*if (msMtUrlFlag != null && msMtUrlFlag.equals(TMProfileConstants.MT_MS_URL_FLAG_INTERNAL)) {
    		MSTranslatorInvoker ms_mt = getMSTranslatorInvoker();

    		//TranslationRequest
    		TranslationRequest transRequest = new TranslationRequest();
    		transRequest.setTexts(new String[]{p_string.trim()});
    		
    		//language pair
    		LanguagePair lp = new LanguagePair(sourceLang, targetLang);
    		transRequest.setLangPair(lp);

    		String[] results = null;

    		boolean needTranslateAgain = true;
    		int count = 0;
    		String exceptionMsg = null;
    		//try at most 3 times
    		while (needTranslateAgain && count < 3)
    		{
        		try {
        			count++;
        			results = ms_mt.translate(transRequest);
        			needTranslateAgain = false;
        		} catch (Exception ex) {
        			exceptionMsg = ex.getMessage();
        		}
    		}
    		
    		if (results != null && results.length > 0) {
    			result = results[0];
    			if (result == null || "".equals(result)) {
    				result = "";
    			}
    		} else {
    			CATEGORY.error(exceptionMsg);
//    			throw new MachineTranslationException("Can't retrieve MS MT translation : " + exceptionMsg);
    		}
    	} 
    	// the MS translator use the public URL
    	else if (msMtUrlFlag != null && msMtUrlFlag.equals(TMProfileConstants.MT_MS_URL_FLAG_PUBLIC)) {*/
    		String endpoint = (String) paramMap.get(MachineTranslator.MSMT_ENDPOINT);
//    		String msAppId = (String) paramMap.get(MachineTranslator.MSMT_APPID);
    		String msCategory = (String) paramMap.get(MachineTranslator.MSMT_CATEGORY);
    		String msClientId = (String) paramMap.get(MachineTranslator.MSMT_CLIENTID);
            String msClientSecret = (String) paramMap.get(MachineTranslator.MSMT_CLIENT_SECRET);
    		
            LanguageService service = null;
    		String exceptionMsg = null;
    		try {
    		    if (MSMT_ACCESS_TOKEN == null)
                {
                    MSMT_ACCESS_TOKEN = MSMTUtil.getAccessToken(msClientId, msClientSecret);
                }
    		    SoapService soap = new SoapServiceLocator(endpoint);
                service = soap.getBasicHttpBinding_LanguageService();
    			
    			boolean needTranslateAgain = true;
        		int count = 0;
        		//try at most 3 times
        		while (MSMT_ACCESS_TOKEN != null && needTranslateAgain && count < 3) {
	    			count++;
	    			result = service.translate(MSMT_ACCESS_TOKEN, p_string, sourceLang,
                            targetLang, MachineTranslator.MSMT_CONTENT_TYPE,
                            msCategory);
	    			needTranslateAgain = false;
        		}
    		} catch (Exception ex) {
    		    if (ex.getMessage().contains(MS_MT_EXPIRE_ERROR))
    		    {
    		        try
                    {
                        MSMT_ACCESS_TOKEN = MSMTUtil.getAccessToken(msClientId, msClientSecret);
                        boolean needTranslateAgain = true;
                        int count = 0;
                        //try at most 3 times
                        while (MSMT_ACCESS_TOKEN != null && needTranslateAgain && count < 3) {
                            count++;
                            result = service.translate(MSMT_ACCESS_TOKEN, p_string, sourceLang,
                                    targetLang, MachineTranslator.MSMT_CONTENT_TYPE,
                                    msCategory);
                            needTranslateAgain = false;
                        }
                    }
                    catch (Exception e)
                    {
                        CATEGORY.error(e.getMessage(), e);
                    }
    		    }
    		    
    			exceptionMsg = ex.getMessage();
            // }
    		if (result == null || "".equals(result)) {
				result = "";
			} else {
				CATEGORY.info(exceptionMsg);
			}
    	}
    	
    	return result;
    }
    
    protected String[] doBatchTranslation(Locale p_sourceLocale,
            Locale p_targetLocale, String[] segments)
            throws MachineTranslationException
    {
    	String[] results = null;

        HashMap paramMap = getMtParameterMap();
        String msMtUrlFlag = TMProfileConstants.MT_MS_URL_FLAG_PUBLIC;

        String sourceLang = p_sourceLocale.getLanguage();
        String sourceCountry = p_sourceLocale.getCountry();
        sourceLang = checkLang(sourceLang, sourceCountry, msMtUrlFlag);

        String targetLang = p_targetLocale.getLanguage();
        String targetCountry = p_targetLocale.getCountry();
        targetLang = checkLang(targetLang, targetCountry, msMtUrlFlag);

        String exceptionMsg = null;
        /*
         * if (TMProfileConstants.MT_MS_URL_FLAG_INTERNAL.equals(msMtUrlFlag)) {
         * MSTranslatorInvoker ms_mt = getMSTranslatorInvoker();
         * 
         * // TranslationRequest TranslationRequest transRequest = new
         * TranslationRequest(); transRequest.setTexts(segments); // language
         * pair LanguagePair lp = new LanguagePair(sourceLang, targetLang);
         * transRequest.setLangPair(lp);
         * 
         * boolean needTranslateAgain = true; int count = 0; // try at most 3
         * times while (needTranslateAgain && count < 3) { try { count++;
         * results = ms_mt.translate(transRequest); needTranslateAgain = false;
         * } catch (Exception ex) { exceptionMsg = ex.getMessage(); } } } else
         * if (TMProfileConstants.MT_MS_URL_FLAG_PUBLIC.equals(msMtUrlFlag)) {
         */
            String endpoint = (String) paramMap
                    .get(MachineTranslator.MSMT_ENDPOINT);
            String msCategory = (String) paramMap
                    .get(MachineTranslator.MSMT_CATEGORY);
            String msClientId = (String) paramMap
                    .get(MachineTranslator.MSMT_CLIENTID);
            String msClientSecret = (String) paramMap
                    .get(MachineTranslator.MSMT_CLIENT_SECRET);

            LanguageService service = null;
            TranslateArrayResponse[] result = null;
            TranslateOptions options = new TranslateOptions();
            options.setCategory(msCategory);
            options.setContentType(MachineTranslator.MSMT_CONTENT_TYPE);
            try
            {
                if (MSMT_ACCESS_TOKEN == null)
                {
                    MSMT_ACCESS_TOKEN = MSMTUtil.getAccessToken(msClientId, msClientSecret);
                }
                SoapService soap = new SoapServiceLocator(endpoint);
                service = soap.getBasicHttpBinding_LanguageService();

                boolean needTranslateAgain = true;
                int count = 0;
                // try at most 3 times
                while (MSMT_ACCESS_TOKEN != null && needTranslateAgain && count < 3)
                {
                    count++;
                    result = service.translateArray(MSMT_ACCESS_TOKEN, segments,
                            sourceLang, targetLang, options);
                    needTranslateAgain = false;
                }
            }
            catch (Exception ex)
            {
                if (ex.getMessage().contains(MS_MT_EXPIRE_ERROR))
                {
                    try
                    {
                        MSMT_ACCESS_TOKEN = MSMTUtil.getAccessToken(msClientId, msClientSecret);
                        boolean needTranslateAgain = true;
                        int count = 0;
                        // try at most 3 times
                        while (MSMT_ACCESS_TOKEN != null && needTranslateAgain && count < 3)
                        {
                            count++;
                            result = service.translateArray(MSMT_ACCESS_TOKEN, segments,
                                    sourceLang, targetLang, options);
                            needTranslateAgain = false;
                        }
                    }
                    catch (Exception e)
                    {
                        exceptionMsg = e.getMessage();
                    }
                }
                exceptionMsg = ex.getMessage();
            }
            if (result != null)
            {
                results = new String[result.length];
                for (int i = 0; i < result.length; i++)
                {
                    results[i] = result[i].getTranslatedText();
                }
            }
            else
            {
                CATEGORY.error("The translation result is null. "
                        + segments.length + " sentences are not translated.");
            }
        // }MT_MS_URL_FLAG_INTERNAL is not useful now.

        if ((results == null || results.length < 1) && exceptionMsg != null)
        {
            CATEGORY.error(exceptionMsg);
        }

    	return results;
    }
    
    private MSTranslatorInvoker getMSTranslatorInvoker()
    {
    	HashMap paramMap = getMtParameterMap();
    	String endpoint = (String) paramMap.get(MachineTranslator.MSMT_ENDPOINT);
    	MSTranslatorInvoker ms_mt = new MSTranslatorInvoker(endpoint);
    	
    	return ms_mt;
    }
    
    private String checkLang(String lang, String country, String msMtUrlFlag)
    {
        if (lang.equals("zh"))
        {
            if (msMtUrlFlag != null
                    && msMtUrlFlag.equals(TMProfileConstants.MT_MS_URL_FLAG_INTERNAL))
            {
                if (country.equalsIgnoreCase("cn"))
                {
                    lang = "zh-CN";
                }
                else
                {
                    lang = "zh-TW";
                }
            }
            else if (msMtUrlFlag != null
                    && msMtUrlFlag.equals(TMProfileConstants.MT_MS_URL_FLAG_PUBLIC))
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
        }

		if (lang.equals("iw")) 
		{
			lang = "he";
		}
		
		// Indonesian
		if ("in".equals(lang) && "ID".equals(country)){
		    lang = "id";
		}
		
		return lang;
    }
}
