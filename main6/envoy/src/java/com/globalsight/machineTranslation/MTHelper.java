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
import java.util.Map;

import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.log.GlobalSightCategory;

public class MTHelper
{
    private static final GlobalSightCategory CATEGORY = (GlobalSightCategory) GlobalSightCategory
            .getLogger(MTHelper.class);
    
    public static final String SHOW_IN_EDITOR = "SHOW_IN_EDITOR";
    public static final String MT_TRANSLATION = "MT_TRANSLATION";
    public static final String ENGINE_NAME = "ENGINE_NAME";
    public static final String MT_TRANSLATION_DIV = "translatedString_replaced_div";
    public static final String ACTION_GET_MT_TRANSLATION = "getMtTranslation";
    
    /**
     * If "show_in_editor" is checked on TM profile >> MT Options UI, get MT
     * translation for current segment.
     * 
     * @param p_sessionMgr
     * @param p_state
     */
    public static Map getMtTranslationForSegEditor(
            SessionManager p_sessionMgr, EditorState p_state)
    {
        Map result = new HashMap();
        
        // MT: SHOW_IN_EDITOR
        TranslationMemoryProfile tmProfile = getTMprofileBySourcePageId(p_state
                .getSourcePageId());
        boolean show_in_editor = false;
        MachineTranslator mt = null;
        if (tmProfile != null)
        {
            show_in_editor = tmProfile.getShowInEditor();
            if (show_in_editor)
            {
                String mtEngine = tmProfile.getMtEngine();
                mt = AbstractTranslator.initMachineTranslator(mtEngine);
                setExtraOptionsForMT(tmProfile, p_state.getSourcePageId(), mt);
            }
        }
        result.put(SHOW_IN_EDITOR, String.valueOf(show_in_editor));

        // MT: get MT result
        String mtString = null;
        try
        {
            if (mt != null)
            {
                TuvManager tuvMananger = ServerProxy.getTuvManager();
                Tuv sourceTuv = tuvMananger.getTuvForSegmentEditor(p_state
                        .getTuId(), p_state.getSourceLocale().getIdAsLong());
                String sourceString = sourceTuv.getGxmlElement().getTextValue();
                mtString = mt.translate(p_state.getSourceLocale().getLocale(),
                        p_state.getTargetLocale().getLocale(), sourceString);
//                if (sourceString.equals(mtString))
//                {
//                    mtString = "";
//                }
                if (mtString != null && !"".equals(mtString))
                {
                    // Encode the translation before sent to web page.
                    XmlEntities xe = new XmlEntities();
                    mtString = xe.encodeStringBasic(mtString);
                }
                result.put(MT_TRANSLATION, mtString);
            }

            result.put(ENGINE_NAME, mt.getEngineName());
        }
        catch (Exception e)
        {
        }
        
        return result;
    }
    
    /**
     * Get translation memory profile by source page id
     * @param sourcePageId long
     * @return TranslationMemoryProfile
     */
    private static TranslationMemoryProfile getTMprofileBySourcePageId(
            long p_sourcePageID)
    {
        TranslationMemoryProfile tmProfile = null;
        try
        {
            SourcePage sourcePage = ServerProxy.getPageManager().getSourcePage(
                    p_sourcePageID);
            Request request = sourcePage.getRequest();
            L10nProfile l10nProfile = request.getL10nProfile();
            tmProfile = l10nProfile.getTranslationMemoryProfile();
        }
        catch (Exception e)
        {
            CATEGORY.error("Could not get tm profile by source page ID : "
                    + p_sourcePageID, e);
        }

        return tmProfile;
    }
    
    /**
     * To invoke MT, extra parameters must be transformed to MT engine for
     * "PROMT","MS_TRANSLATOR" or "ASIA_ONLINE".
     * 
     * @param p_tmProfile
     */
    private static void setExtraOptionsForMT(
            TranslationMemoryProfile p_tmProfile, Long p_sourcePageID,
            MachineTranslator p_mt)
    {
        if (p_mt != null)
        {
            HashMap paramHM = new HashMap();
            String mtEngine = p_tmProfile.getMtEngine();
            Long tmProfileID = p_tmProfile.getIdAsLong();

            if (MachineTranslator.ENGINE_PROMT.equalsIgnoreCase(mtEngine))
            {
                String ptsurl = p_tmProfile.getPtsurl();
                String ptsUsername = p_tmProfile.getPtsUsername();
                String ptsPassword = p_tmProfile.getPtsPassword();

                paramHM.put(MachineTranslator.TM_PROFILE_ID, tmProfileID);
                paramHM.put(MachineTranslator.PROMT_PTSURL, ptsurl);
                paramHM.put(MachineTranslator.PROMT_USERNAME, ptsUsername);
                paramHM.put(MachineTranslator.PROMT_PASSWORD, ptsPassword);
            }
            if (MachineTranslator.ENGINE_MSTRANSLATOR
                    .equalsIgnoreCase(mtEngine))
            {
                String msMtEndpoint = p_tmProfile.getMsMTUrl();
                String msMtAppId = p_tmProfile.getMsMTAppID();
                String msMtUrlFlag = p_tmProfile.getMsMTUrlFlag();

                paramHM.put(MachineTranslator.MSMT_ENDPOINT, msMtEndpoint);
                paramHM.put(MachineTranslator.MSMT_APPID, msMtAppId);
                paramHM.put(MachineTranslator.MSMT_URLFLAG, msMtUrlFlag);
            }
            if (MachineTranslator.ENGINE_ASIA_ONLINE.equalsIgnoreCase(mtEngine))
            {
                String aoMtUrl = p_tmProfile.getAoMtUrl();
                long aoMtPort = p_tmProfile.getAoMtPort();
                String aoMtUsername = p_tmProfile.getAoMtUsername();
                String aoMtPassword = p_tmProfile.getAoMtPassword();
                long aoMtAccountNumber = p_tmProfile.getAoMtAccountNumber();

                paramHM.put(MachineTranslator.TM_PROFILE_ID, tmProfileID);
                paramHM.put(MachineTranslator.AO_URL, aoMtUrl);
                paramHM.put(MachineTranslator.AO_PORT, (Long) aoMtPort);
                paramHM.put(MachineTranslator.AO_USERNAME, aoMtUsername);
                paramHM.put(MachineTranslator.AO_PASSWORD, aoMtPassword);
                paramHM.put(MachineTranslator.AO_ACCOUNT_NUMBER,
                        (Long) aoMtAccountNumber);
                paramHM.put(MachineTranslator.SOURCE_PAGE_ID, p_sourcePageID);
            }

            p_mt.setMtParameterMap(paramHM);
        }
    }
}
