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
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileHandlerHelper;
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Map getMtTranslationForSegEditor(SessionManager p_sessionMgr,
            EditorState p_state)
    {
        Map result = new HashMap();

        long sourcePageId = p_state.getSourcePageId();
        // MT: SHOW_IN_EDITOR
        MachineTranslationProfile mtProfile = MTProfileHandlerHelper
                .getMtProfileBySourcePageId(sourcePageId,
                        p_state.getTargetLocale());
        boolean show_in_editor = false;
        MachineTranslator mt = null;
        if (mtProfile != null)
        {
            show_in_editor = mtProfile.isShowInEditor();
            if (show_in_editor)
            {
                String mtEngine = mtProfile.getMtEngine();
                mt = AbstractTranslator.initMachineTranslator(mtEngine);
                HashMap hashMap = mtProfile.getParamHM();
                hashMap.put(MachineTranslator.SOURCE_PAGE_ID, sourcePageId);
                mt.setMtParameterMap(hashMap);
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
     * Get all translatable TextNode list in the specified gxml.
     * 
     * @param p_gxml
     * @return List in TextNode
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
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

    /**
     * Only encode single '&' in PROMT returned translation.
     * 
     * @param p_string
     * 
     * @return
     */
    public static String encodeSeparatedAndChar(String p_string)
    {
        if (p_string == null || "".equals(p_string.trim()))
        {
            return p_string;
        }

        p_string = p_string.replaceAll("&lt;", "_ltEntity_");
        p_string = p_string.replaceAll("&gt;", "_gtEntity_");
        p_string = p_string.replaceAll("&quot;", "_quotEntity_");
        p_string = p_string.replaceAll("&apos;", "_aposEntity_");
        p_string = p_string.replaceAll("&#x9;", "_#x9Entity_");
        p_string = p_string.replaceAll("&#xa;", "_#xaEntity_");
        p_string = p_string.replaceAll("&#xd;", "_#xdEntity_");
        p_string = p_string.replaceAll("&amp;", "_amp_");
        
        p_string = p_string.replaceAll("&", "&amp;");

        p_string = p_string.replaceAll("_amp_", "&amp;");
        p_string = p_string.replaceAll("_#xdEntity_", "&#xd;");
        p_string = p_string.replaceAll("_#xaEntity_", "&#xa;");
        p_string = p_string.replaceAll("_#x9Entity_", "&#x9;");
        p_string = p_string.replaceAll("_aposEntity_", "&apos;");
        p_string = p_string.replaceAll("_quotEntity_", "&quot;");
        p_string = p_string.replaceAll("_gtEntity_", "&gt;");
        p_string = p_string.replaceAll("_ltEntity_", "&lt;");

        return p_string;
    }

    /**
     * Get all pure sub texts in a segment GXML.
     * @param segmentInGxml
     * @return String[]
     */
    @SuppressWarnings("rawtypes")
    public static String[] getSegmentsInGxml(String segmentInGxml)
    {
        // Retrieve all TextNode that need translate.
        GxmlElement gxmlRoot = MTHelper.getGxmlElement(segmentInGxml);
        List items = MTHelper.getImmediateAndSubImmediateTextNodes(gxmlRoot);

        String[] segmentsFromGxml = null;
        segmentsFromGxml = new String[items.size()];
        int count = 0;
        for (Iterator iter = items.iterator(); iter.hasNext();)
        {
            String textValue = ((TextNode) iter.next()).getTextValue();
            segmentsFromGxml[count] = textValue;
            count++;
        }

        return segmentsFromGxml;
    }
}
