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

import com.globalsight.machineTranslation.MachineTranslationException;

import java.util.HashMap;
import java.util.Locale;

/**
 * All Machine Translators used with GlobalSight should
 * implement this interface so that GlobalSight can make calls to
 * them.
 */
public interface MachineTranslator
{
    public static final String ENGINE_BABELFISH = "babelfish";
    public static final String ENGINE_FREETRANSLATION = "freetranslation";
    public static final String ENGINE_SYSTRAN = "systran";
    public static final String ENGINE_GOOGLE = "google";
    public static final String ENGINE_PROMT = "promt";
    public static final String ENGINE_MSTRANSLATOR = "ms_translator";
    public static final String ENGINE_ASIA_ONLINE = "asia_online";
    
//    public static final String PROMT_INFO = "promtParamMap";
    public static final String PROMT_PTSURL = "ptsUrl";
    public static final String PROMT_USERNAME = "username";
    public static final String PROMT_PASSWORD = "password";
    
    public static final String PROMT_PTS8_GENERAL_TOPIC_TEMPLATE = "General";
    public static final String PROMT_PTS9_GENERAL_TOPIC_TEMPLATE = "General lexicon";
    public static final String PROMT_PTS9_FILE_TYPE = "text/xliff";
    
    public static final String MSMT_ENDPOINT = "msMtEndpoint";
    public static final String MSMT_APPID = "msMtAppID";
    public static final String MSMT_URLFLAG = "msMtUrlFlag";
    
    public static final String AO_URL = "aoMtUrl";
    public static final String AO_PORT = "aoMtPort";
    public static final String AO_USERNAME = "aoMtUsername";
    public static final String AO_PASSWORD = "aoMtPassword";
    public static final String AO_ACCOUNT_NUMBER = "aoMtAccountNumber";
    // AO MT needs this to judge if support locale pair.
    public static final String TM_PROFILE_ID = "tmProfileID";
    public static final String SOURCE_PAGE_ID = "sourcePageID";
    public static final String CONTAIN_TAGS = "containTags";

    public static final String[] gsSupportedMTEngines =
    { ENGINE_GOOGLE, ENGINE_MSTRANSLATOR, ENGINE_PROMT, ENGINE_ASIA_ONLINE };

    /**
     * Returns the MT engine name (SysTran,Babelfish,etc.)
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
     * @return
     * @exception MachineTranslationException
     */
    public String translate (Locale p_sourceLocale,
        Locale p_targetLocale, String p_string)
        throws MachineTranslationException;

    /**
     * Machine translate the given GXML segment
     *
     * @param p_sourceLocale source locale
     * @param p_targetLocale target locale
     * @param p_segment
     * @return GXML segment XML snippet
     * @exception MachineTranslationException
     */
    public String translateSegment(Locale p_sourceLocale,
        Locale p_targetLocale, String p_segment)
        throws MachineTranslationException;
    
    /**
     * Machine translate the given GXML segments
     *
     * @param p_sourceLocale source locale
     * @param p_targetLocale target locale
     * @param p_segments
     * @param p_batchSize
     * @return GXML segment XML snippet
     * @exception MachineTranslationException
     */
    public String[] translateBatchSegments(Locale p_sourceLocale,
        Locale p_targetLocale, String[] p_segments, boolean containTags)
        throws MachineTranslationException;
    
    /**
     * Used to set necessary parameters into sub mt engine
     * @param hm HashMap parameter user specified for special purpose.
     */
    public void setMtParameterMap(HashMap hm);
    
    public HashMap getMtParameterMap();
}

