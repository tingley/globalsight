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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.online.OnlineEditorManagerLocal;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.projecthandler.TMProfileMTInfo;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlException;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;
import com.globalsight.util.gxml.TextNode;

public class MTHelper
{
    private static final Logger CATEGORY = Logger.getLogger(MTHelper.class);

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
    public static Map getMtTranslationForSegEditor(SessionManager p_sessionMgr,
            EditorState p_state)
    {
        Map result = new HashMap();

        long sourcePageId = p_state.getSourcePageId();

        // MT: SHOW_IN_EDITOR
        TranslationMemoryProfile tmProfile = getTMprofileBySourcePageId(sourcePageId);
        boolean show_in_editor = false;
        MachineTranslator mt = null;
        if (tmProfile != null)
        {
            show_in_editor = tmProfile.getShowInEditor();
            if (show_in_editor)
            {
                String mtEngine = tmProfile.getMtEngine();
                mt = AbstractTranslator.initMachineTranslator(mtEngine);
                setExtraOptionsForMT(tmProfile, sourcePageId, mt);
            }
        }
        result.put(SHOW_IN_EDITOR, String.valueOf(show_in_editor));

        // MT: get MT result
        String mtString = null;
        try
        {
            SourcePage sp = null;
            try
            {
                sp = ServerProxy.getPageManager().getSourcePage(sourcePageId);
            }
            catch (Exception e)
            {
                CATEGORY.error("Could not get source page by source page ID : "
                        + sourcePageId, e);
            }
            if (mt != null)
            {
                TuvManager tuvMananger = ServerProxy.getTuvManager();
                Tuv sourceTuv = tuvMananger.getTuvForSegmentEditor(p_state
                        .getTuId(), p_state.getSourceLocale().getIdAsLong(), sp
                        .getCompanyId());

                String sourceString = null;
                long subId = p_state.getSubId();
                if (OnlineEditorManagerLocal.DUMMY_SUBID.equals(String
                        .valueOf(subId)))
                {
                    sourceString = sourceTuv.getGxmlElement().getTextValue();
                }
                else
                {
                    GxmlElement subEle = sourceTuv
                            .getSubflowAsGxmlElement(String.valueOf(subId));
                    sourceString = subEle.getTextValue();
                }

                // translate segment
                if (sourceString != null && sourceString.trim().length() > 0)
                {
                    mtString = mt
                            .translate(p_state.getSourceLocale().getLocale(),
                                    p_state.getTargetLocale().getLocale(),
                                    sourceString);
                }

                // if (sourceString.equals(mtString))
                // {
                // mtString = "";
                // }
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
     * 
     * @param sourcePageId
     *            long
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
     * @param tmProfile
     */
    private static void setExtraOptionsForMT(
            TranslationMemoryProfile tmProfile, Long p_sourcePageID,
            MachineTranslator p_mt)
    {
        if (p_mt != null)
        {
            HashMap paramHM = new HashMap();
            String mtEngine = tmProfile.getMtEngine();
            Long tmProfileID = tmProfile.getIdAsLong();

            if (MachineTranslator.ENGINE_PROMT.equalsIgnoreCase(mtEngine))
            {
                String ptsurl = tmProfile.getPtsurl();
                String ptsUsername = tmProfile.getPtsUsername();
                String ptsPassword = tmProfile.getPtsPassword();

                paramHM.put(MachineTranslator.TM_PROFILE_ID, tmProfileID);
                paramHM.put(MachineTranslator.PROMT_PTSURL, ptsurl);
                paramHM.put(MachineTranslator.PROMT_USERNAME, ptsUsername);
                paramHM.put(MachineTranslator.PROMT_PASSWORD, ptsPassword);
            }
            if (MachineTranslator.ENGINE_MSTRANSLATOR
                    .equalsIgnoreCase(mtEngine))
            {
                String msMtEndpoint = tmProfile.getMsMTUrl();
                String msMtAppId = tmProfile.getMsMTAppID();
                String msMtUrlFlag = tmProfile.getMsMTUrlFlag();
                String msMtClientID = tmProfile.getMsMTClientID();
                String msMtClientSecret = tmProfile.getMsMTClientSecret();
                String msCategory = tmProfile.getMsMTCategory();

                paramHM.put(MachineTranslator.MSMT_ENDPOINT, msMtEndpoint);
                paramHM.put(MachineTranslator.MSMT_APPID, msMtAppId);
                paramHM.put(MachineTranslator.MSMT_URLFLAG, msMtUrlFlag);
                paramHM.put(MachineTranslator.MSMT_CLIENTID, msMtClientID);
                paramHM.put(MachineTranslator.MSMT_CLIENT_SECRET,
                        msMtClientSecret);
                paramHM.put(MachineTranslator.MSMT_CATEGORY, msCategory);
            }
            if (MachineTranslator.ENGINE_ASIA_ONLINE.equalsIgnoreCase(mtEngine))
            {
                String aoMtUrl = tmProfile.getAoMtUrl();
                long aoMtPort = tmProfile.getAoMtPort();
                String aoMtUsername = tmProfile.getAoMtUsername();
                String aoMtPassword = tmProfile.getAoMtPassword();
                long aoMtAccountNumber = tmProfile.getAoMtAccountNumber();

                paramHM.put(MachineTranslator.TM_PROFILE_ID, tmProfileID);
                paramHM.put(MachineTranslator.AO_URL, aoMtUrl);
                paramHM.put(MachineTranslator.AO_PORT, (Long) aoMtPort);
                paramHM.put(MachineTranslator.AO_USERNAME, aoMtUsername);
                paramHM.put(MachineTranslator.AO_PASSWORD, aoMtPassword);
                paramHM.put(MachineTranslator.AO_ACCOUNT_NUMBER,
                        (Long) aoMtAccountNumber);
                paramHM.put(MachineTranslator.SOURCE_PAGE_ID, p_sourcePageID);
            }
            if (MachineTranslator.ENGINE_SAFABA.equalsIgnoreCase(mtEngine))
            {
                List<?> mtInfoList = TMProfileHandlerHelper
                        .getMtinfoByTMProfileIdAndEngine(tmProfile.getId(),
                                tmProfile.getMtEngine());
                for (int i = 0; i < mtInfoList.size(); i++)
                {
                    TMProfileMTInfo mtInfo = (TMProfileMTInfo) mtInfoList
                            .get(i);
                    paramHM.put(mtInfo.getMtKey(), mtInfo.getMtValue());
                }
                paramHM.put(MachineTranslator.SOURCE_PAGE_ID, p_sourcePageID);
            }

            p_mt.setMtParameterMap(paramHM);
        }
    }

    /**
     * Get all translatable TextNode list in the specified gxml.
     * 
     * @param p_gxml
     * @return List in TextNode
     */
    public static List getImmediateAndSubImmediateTextNodes(
            GxmlElement p_rootElement)
    {
        if (p_rootElement == null)
        {
            return null;
        }

        List result = new ArrayList();
        // Immediate TextNode list for the root element
        List immediateTextNodeList = p_rootElement.getTextNodeWithoutInternal();
        result.addAll(immediateTextNodeList);

        // Add immediate TextNode list for all sub GxmlElement.
        List subFlowList = p_rootElement
                .getDescendantElements(GxmlElement.SUB_TYPE);
        if (subFlowList != null && subFlowList.size() > 0)
        {
            Iterator it = subFlowList.iterator();
            while (it.hasNext())
            {
                GxmlElement subEle = (GxmlElement) it.next();
                List subImmediateTextNodeList = subEle
                        .getChildElements(GxmlElement.TEXT_NODE);
                result.addAll(subImmediateTextNodeList);
            }
        }

        return result;
    }

    /**
     * Get all text values for immediate text nodes and all sub segments in
     * current GxmlElement object.
     */
    public static String getAllTranslatableTextValue(GxmlElement p_rootElement)
    {
        List textNodeList = getImmediateAndSubImmediateTextNodes(p_rootElement);

        StringBuffer result = new StringBuffer();
        if (textNodeList != null && textNodeList.size() > 0)
        {
            Iterator it = textNodeList.iterator();
            while (it.hasNext())
            {
                TextNode tn = (TextNode) it.next();
                result.append(tn.getTextValue());
            }
        }

        return result.toString();
    }

    /**
     * Get GxmlElement object for specified GXML.
     */
    public static GxmlElement getGxmlElement(String p_gxml)
    {
        if (p_gxml == null || p_gxml.trim().length() == 0)
        {
            return null;
        }

        GxmlElement result = null;
        GxmlFragmentReader reader = null;
        try
        {
            reader = GxmlFragmentReaderPool.instance().getGxmlFragmentReader();
            result = reader.parseFragment(p_gxml);
        }
        catch (GxmlException ex)
        {
        }
        finally
        {
            GxmlFragmentReaderPool.instance().freeGxmlFragmentReader(reader);
        }

        return result;
    }

    /**
     * Retrieve source page by source page ID.
     * 
     * @return
     */
    public static SourcePage getSourcePage(Map paramMap)
    {
        Long sourcePageID = (Long) paramMap
                .get(MachineTranslator.SOURCE_PAGE_ID);
        SourcePage sp = null;
        try
        {
            sp = ServerProxy.getPageManager().getSourcePage(sourcePageID);
        }
        catch (Exception e)
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.error("Failed to get source page by pageID : "
                        + sourcePageID + ";" + e.getMessage());
            }
        }

        return sp;
    }

    /**
     * If the source page data type is XLF,need revert the segment content.
     * 
     * @return boolean
     */
    public static boolean needRevertXlfSegment(Map paramMap)
    {
        if (paramMap == null)
        {
            return false;
        }
        SourcePage sp = getSourcePage(paramMap);
        String spDataType = null;
        if (sp != null)
        {
            ExtractedSourceFile esf = (ExtractedSourceFile) sp
                    .getExtractedFile();
            spDataType = esf.getDataType();
        }
        if (spDataType != null
                && ("xlf".equalsIgnoreCase(spDataType) || "xliff"
                        .equalsIgnoreCase(spDataType)))
        {
            return true;
        }

        return false;
    }

    public static String wrappText(String p_text, String locale)
    {
        if (p_text == null || p_text.trim().length() == 0)
        {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\"?>");
        sb.append("<diplomat locale=\"").append(locale)
                .append("\" version=\"2.0\" datatype=\"xlf\">");
        sb.append("<translatable>");
        sb.append(p_text);
        sb.append("</translatable>");
        sb.append("</diplomat>");

        return sb.toString();
    }

    public static String revertXlfSegment(String text, String locale)
    {
        String result = null;

        try
        {
            DiplomatAPI diplomat = new DiplomatAPI();
            diplomat.setFileProfileId("-1");
            diplomat.setFilterId(-1);
            diplomat.setFilterTableName(null);
            diplomat.setTargetLocale(locale);
            byte[] mergeResult = diplomat.merge(text, "UTF-8", false);
            result = new String(mergeResult, "UTF-8");
        }
        catch (Exception e)
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.error("Failed to revert XLF segment : "
                        + e.getMessage());
            }
        }

        return result;
    }

    public static GlobalSightLocale getSourceLocale(Map paramMap)
    {
        SourcePage sp = MTHelper.getSourcePage(paramMap);
        GlobalSightLocale sourceLocale = null;
        if (sp != null)
        {
            sourceLocale = sp.getGlobalSightLocale();
        }

        return sourceLocale;
    }
}
