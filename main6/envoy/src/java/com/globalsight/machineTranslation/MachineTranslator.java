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
package com.globalsight.machineTranslation;

import java.util.HashMap;
import java.util.Locale;

/**
 * All Machine Translators used with GlobalSight should
 * implement this interface so that GlobalSight can make calls to
 * them.
 */
public interface MachineTranslator
{
    public static final String ENGINE_GOOGLE = "Google_Translate";
    public static final String ENGINE_PROMT = "ProMT";
    public static final String ENGINE_MSTRANSLATOR = "MS_Translator";
    public static final String ENGINE_ASIA_ONLINE = "Asia_Online";
    public static final String ENGINE_SAFABA = "Safaba";
    public static final String ENGINE_IPTRANSLATOR = "IPTranslator";
    public static final String ENGINE_DOMT = "DoMT";

    public static final String PROMT_PTSURL = "ptsUrl";
    public static final String PROMT_USERNAME = "username";
    public static final String PROMT_PASSWORD = "password";
    
    public static final String PROMT_PTS8_GENERAL_TOPIC_TEMPLATE = "General";
    public static final String PROMT_PTS9_GENERAL_TOPIC_TEMPLATE = "General lexicon";
    public static final String PROMT_PTS9_FILE_TYPE = "text/xliff";
    
    public static final String MSMT_ENDPOINT = "msMtEndpoint";
    public static final String MSMT_APPID = "msMtAppID";
    public static final String MSMT_CATEGORY = "msMtCategory";
    public static final String MSMT_CONTENT_TYPE = "text/plain";
    public static final String MSMT_URLFLAG = "msMtUrlFlag";
    public static final String MSMT_CLIENTID = "msMtClientID";
    public static final String MSMT_CLIENT_SECRET = "msMtClientSecret";
    
    public static final String AO_URL = "aoMtUrl";
    public static final String AO_PORT = "aoMtPort";
    public static final String AO_USERNAME = "aoMtUsername";
    public static final String AO_PASSWORD = "aoMtPassword";
    public static final String AO_ACCOUNT_NUMBER = "aoMtAccountNumber";

    public static final String DOMT_URL = "doMtUrl";
    public static final String DOMT_PORT = "doMtPort";
    public static final String DOMT_ENGINE_NAME = "doMtCategory";
        
    // AO MT needs this to judge if support locale pair.
    public static final String MT_PROFILE_ID = "mtProfileID";
    public static final String SOURCE_PAGE_ID = "sourcePageID";
    public static final String CONTAIN_TAGS = "containTags";
    public static final String TARGET_LOCALE_ID = "targetLocaleId";

    /**
     * In GlobalSight, segments from XLF file are "wrapped" with "ph" again.
     * So, before send such segments to MT engine, need revert them back; After
     * get translations from MT engine, need wrap them again. So, if the segment
     * is from GlobalSight and from XLF file, this should be set to TRUE; If not
     * from GlobalSight or not from XLF file, this should be set to FALSE.
     */
    public static final String NEED_SPECAIL_PROCESSING_XLF_SEGS = "needSpecialProcessingXlfSegments";

    /**
     * The source file format such as "xlf", "docx" etc.
     */
    public static final String DATA_TYPE = "dataType";

    /**
	 * For MS_Translator and "SR_xx" workflows, we can configure to translate to
	 * "sr_Latn" or "sr_Cyrl" dynamically.
	 */
    public static final String SR_LANGUAGE = "rsLanguage";

    public static final String[] gsSupportedMTEngines =
    { ENGINE_MSTRANSLATOR, ENGINE_PROMT, ENGINE_ASIA_ONLINE, ENGINE_SAFABA,
            ENGINE_IPTRANSLATOR, ENGINE_DOMT, ENGINE_GOOGLE};

    /**
     * Returns the MT engine name.
     *
     * @return name
     */
    public String getEngineName();
    
    /**
     * Returns true if the given locale pair is supported for MT.
     *
     * @param p_sourceLocale source
     * @param p_targetLocale target
     * @return true | false
     * @exception MachineTranslationException
     */
    boolean supportsLocalePair(Locale p_sourceLocale, Locale p_targetLocale)
        throws MachineTranslationException;

    /**
     * Machine translate the given string.
     *
     * @param p_sourceLocale source locale
     * @param p_targetLocale
     * @param p_string
     * @return String
     * @exception MachineTranslationException
     */
    public String translate(Locale p_sourceLocale, Locale p_targetLocale,
            String p_string) throws MachineTranslationException;

    /**
     * Machine translate the given GXML segments
     * 
     * @param sourceLocale
     *            source locale
     * @param targetLocale
     *            target locale
     * @param segments
     * @param containTags
     * @param isHaveRootTag
     * @param needSpecialProcessingXlfSegs
     * 
     * @return GXML segment XML snippet
     * @exception MachineTranslationException
     */
    public String[] translateBatchSegments(Locale sourceLocale,
            Locale targetLocale, String[] segments, boolean containTags,
            boolean isHaveRootTag) throws MachineTranslationException;

    /**
     * Used to set necessary parameters into sub mt engine
     * @param hm HashMap parameter user specified for special purpose.
     */
    public void setMtParameterMap(HashMap hm);
    
    public HashMap getMtParameterMap();
}

