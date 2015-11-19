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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.projecthandler.EngineEnum;
import com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileConstants;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.machineTranslation.google.GoogleProxy;
import com.globalsight.machineTranslation.safaba.SafabaProxy;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.edit.SegmentUtil2;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.TextNode;

/**
 * An abstract base class that implements functionality common to all
 * MT proxies.
 */
public abstract class AbstractTranslator implements MachineTranslator
{
    private static final Logger CATEGORY = Logger
            .getLogger(AbstractTranslator.class);

    private static final String TAG_REGEX = "<.pt.*?>[^<]*?</.pt>";
    private static final String TAG_REGEX_ALONE = "<[^>]*?>";

    private HashMap parameterMap = null;

    private DiplomatAPI m_diplomat = null;

    /**
     * Machine translate the given string.
     */
    public String translate(Locale p_sourceLocale, Locale p_targetLocale,
            String p_string) throws MachineTranslationException
    {
        return doTranslation(p_sourceLocale, p_targetLocale, p_string);
    }

    /**
     * Translate segments in batch. Google,MS_Translator and Asia Online support
     * batch translation,while PROMT does not support this.
     * 
     * @param sourceLocale
     *            source locale
     * @param targetLocale
     *            target locale
     * @param segments
     *            - the segments to be translated
     * @param containTags
     *            - if the segments contains tags in them.
     * @param isHaveRootTag
     *            - if the segments have root tag such as
     *            "<segment...>..</segment>".
     *
     * @return String[] translated segments in sequence.
     * 
     * @exception MachineTranslationException
     */
    public String[] translateBatchSegments(Locale sourceLocale,
            Locale targetLocale, String[] segments, boolean containTags,
            boolean isHaveRootTag) throws MachineTranslationException
    {
        if (sourceLocale == null || targetLocale == null
                || segments == null || segments.length < 1)
        {
            return null;
        }

        String engineName = this.getEngineName();
        if (engineName == null || "".equals(engineName.trim()))
        {
            return null;
        }

        // If segments have no root tag, add root tags for them.
        if (!isHaveRootTag)
        {
            for (int i = 0; i < segments.length; i++)
            {
                segments[i] = "<segment>" + segments[i] + "</segment>";
            }
        }

        String[] translatedSegs = null;
        EngineEnum ee = EngineEnum.getEngine(engineName);
        switch (ee)
        {
            case ProMT:
                translatedSegs = trPromt(sourceLocale, targetLocale, segments, containTags);
                break;
            case Asia_Online:
                // Asia Online engine supports batch translation via XLIFF file.
                translatedSegs = doBatchTranslation(sourceLocale, targetLocale, segments);
                break;
            case Safaba:
                translatedSegs = trSafaba(sourceLocale, targetLocale, segments);
                break;
            case MS_Translator:
                String type = MTHelper.getMTConfig("ms_translator.translate.type");
                // old version
                if ("1".equals(type))
                {
                    translatedSegs = trMs1(sourceLocale, targetLocale, segments, containTags);
                }
                else
                {
                    // Seems MS Translator cannot return valid GXML for certain languages
                    // such as "sr_Cyrl", we have to try pure text way.
                    String trgSrLang = (String) getMtParameterMap().get(
                            MachineTranslator.SR_LANGUAGE);
                    if ("sr-Cyrl".equalsIgnoreCase(trgSrLang)) {
                        translatedSegs = trMs1(sourceLocale, targetLocale, segments, containTags);
                    } else {
                        translatedSegs = trMs2(sourceLocale, targetLocale, segments);
                    }
                }
                break;
            case IPTranslator:
                translatedSegs = trIPTranslator(sourceLocale, targetLocale, segments);
                break;
            case DoMT:
                translatedSegs = doBatchTranslation(sourceLocale, targetLocale, segments);
                break;
            case Google_Translate:
                translatedSegs = trGoogle(sourceLocale, targetLocale, segments, containTags);
                break;
        }

        if (translatedSegs != null && translatedSegs.length > 0 && !isHaveRootTag)
        {
            for (int i = 0; i < translatedSegs.length; i++)
            {
                translatedSegs[i] = GxmlUtil.stripRootTag(translatedSegs[i]);
            }
        }

        return translatedSegs;
    }

    private String[] trIPTranslator(Locale sourceLocale, Locale targetLocale,
            String[] segments)
    {
        String[] results = new String[segments.length];

        List<String> allSegments = new ArrayList<String>();
        String[] heads = new String[segments.length];
        for (int i = 0; i < segments.length; i++)
        {
            int index = segments[i].indexOf(">");
            heads[i] = segments[i].substring(0, index + 1);

            GxmlElement gxmlRoot = MTHelper.getGxmlElement(segments[i]);
            List subFlowList = gxmlRoot.getDescendantElements(GxmlElement.SUB_TYPE);
            // If segment gxml has NO subs, send whole gxml to MT.
            if (subFlowList == null || subFlowList.size() == 0)
            {
                allSegments.add(GxmlUtil.stripRootTag(segments[i]));
            }
            else
            {
                // If segment gxml HAS subs, send texts to MT then compose
                // back again. This is because IPTranslator can not handle
                // segment with sub segments well.
                List items = MTHelper.getImmediateAndSubImmediateTextNodes(gxmlRoot);
                for (int subIndex = 0; subIndex < items.size(); subIndex++)
                {
                    TextNode textNode = (TextNode) items.get(subIndex);
                    if (StringUtil.isNotEmpty(textNode.toGxml()))
                    {
                        allSegments.add(textNode.toGxml());
                    }
                }
            }
        }

        StringBuffer segmentsInXlf = new StringBuffer("<body>");
        for (int i = 0; i < allSegments.size(); i++)
        {
            segmentsInXlf.append("<trans-unit id=\"" + i + "\"><source>"
                    + allSegments.get(i)
                    + "</source><target></target></trans-unit>");
        }
        segmentsInXlf.append("</body>");

        try
        {
            String translatedBody = doTranslation(sourceLocale, targetLocale,
                    segmentsInXlf.toString());
            if (org.apache.commons.lang3.StringUtils.isBlank(translatedBody))
            {
                return null;
            }

            String[] trans = translatedBody.split("</trans-unit>");
            List<String> validTrans = new ArrayList<String>();
            if (trans != null)
            {
                for (int i = 0; i < trans.length; i++)
                {
                    if (trans[i].contains("</target>"))
                    {
                        validTrans.add(trans[i]);
                    }
                }
            }
            if (validTrans.size() != allSegments.size())
                return null;

            int counter = 0;
            for (int mainIndex = 0; mainIndex < segments.length; mainIndex++)
            {
                GxmlElement gxmlRoot = MTHelper.getGxmlElement(segments[mainIndex]);
                List subFlowList = gxmlRoot.getDescendantElements(GxmlElement.SUB_TYPE);
                // If segment gxml has NO subs, send whole gxml to MT.
                if (subFlowList == null || subFlowList.size() == 0)
                {
                    String transStr = getTranslatedStringForIPTranslator(validTrans.get(counter));
                    results[mainIndex] = heads[mainIndex] + transStr + "</segment>";
                    counter++;
                }
                else
                {
                    // If segment gxml HAS subs, send texts to MT then compose
                    // back again. This is because IPTranslator can not handle
                    // segment with sub segments well.
                    List items = MTHelper.getImmediateAndSubImmediateTextNodes(gxmlRoot);
                    if (items != null && items.size() > 0)
                    {
                        for (int subIndex = 0; subIndex < items.size(); subIndex++)
                        {
                            TextNode textNode = (TextNode) items.get(subIndex);
                            if (StringUtil.isNotEmpty(textNode.toGxml()))
                            {
                                String transStr = getTranslatedStringForIPTranslator(validTrans.get(counter));
                                textNode.setTextBuffer(new StringBuffer(transStr));
                                counter++;
                            }
                        }
                        results[mainIndex] = heads[mainIndex] + GxmlUtil.stripRootTag(gxmlRoot.toGxml()) + "</segment>";
                    }
                }
            }
        }
        catch (MachineTranslationException e)
        {
            CATEGORY.error(e.getMessage());
        }

        return results;
    }

    private String getTranslatedStringForIPTranslator(String transStr)
    {
        String result = "";
        if (transStr.contains("</target>"))
        {
            String[] target = transStr.split("<target>");
//            String id = transStr.split("id")[1].split("\"")[1];
            int index = target[1].indexOf("</target>");
            result = target[1].substring(0, index);
        }

        return result;
    }

    private String[] trGoogle(Locale sourceLocale, Locale targetLocale,
            String[] segments, boolean containTags)
            throws MachineTranslationException
    {
         long start = System.currentTimeMillis();
         List<String> translatedList = new ArrayList<String>();
         List<String> subList = new ArrayList<String>();
         int charCount = 0;

         for (int i = 0; i < segments.length; i++)
         {
             subList.add(segments[i]);
             
             if (i == segments.length - 1 
                     || charCount + GxmlUtil.stripRootTag(segments[i + 1]).length() > GoogleProxy.MT_GOOGLE_MAX_CHARACTER_NUM)
             {
                 Object[] segmentObjArray = subList.toArray();
                 String[] segmentsArray = new String[segmentObjArray.length];
                 for (int j = 0; j < segmentObjArray.length; j++)
                 {
                     segmentsArray[j] = (String) segmentObjArray[j];
                 }
                 String[] subResults = null;

                 if (containTags)
                 {
                    subResults = translateSegmentsWithTags(sourceLocale,
                            targetLocale, segmentsArray);
                 }
                 else if (!containTags)
                 {
                     subResults = translateSegmentsWithoutTags(sourceLocale,
                             targetLocale, segmentsArray);
                 }

                 if (subResults != null)
                 {
                     translatedList.addAll(Arrays.asList(subResults));
                 }
                 subList.clear();
                 charCount = 0;
             }
             else
             {
                 charCount += GxmlUtil.stripRootTag(segments[i]).length();
             }
         }

         String[] results = new String[segments.length];
         for (int i = 0; i < translatedList.size(); i++)
         {
             results[i] = (String) translatedList.get(i);
         }

         if (CATEGORY.isDebugEnabled())
         {
             CATEGORY.debug("Used time: " + (System.currentTimeMillis() - start));
         }
         return results;
        
        
    }

    // The whole segment GXML is sent for translation.
    private String[] trMs2(Locale sourceLocale, Locale targetLocale,
            String[] segments) throws MachineTranslationException
    {
        String seg = null;
        List<String> heads = new ArrayList<String>();
        String[] segmentsNoStyleInfo = new String[segments.length];
        for (int i = 0; i < segments.length; i++)
        {
            seg = segments[i];
            String head = seg.substring(0, seg.indexOf(">") + 1);
            heads.add(head);

            BaseTmTuv srcTuv = TmUtil.createTmSegment(seg, 0, null, "unknown",
                    true);
            segmentsNoStyleInfo[i] = GxmlUtil.stripRootTag(srcTuv.getSegment());
        }

        List<String> translatedList = new ArrayList<String>();
        List<String> subList = new ArrayList<String>();
        int charCount = 0;
        for (int i = 0; i < segmentsNoStyleInfo.length; i++)
        {
            if (segmentsNoStyleInfo[i].length() > TMProfileConstants.MT_MS_MAX_CHARACTER_NUM)
            {
                if (subList.size() > 0)
                {
                    translatedList.addAll(doMSTranslation(subList, sourceLocale, targetLocale));
                }
                translatedList.add("");
                subList.clear();
                charCount = 0;
            }
            else if (charCount + segmentsNoStyleInfo[i].length() > TMProfileConstants.MT_MS_MAX_CHARACTER_NUM)
            {
                i--;
                translatedList.addAll(doMSTranslation(subList, sourceLocale,
                        targetLocale));
                subList.clear();
                charCount = 0;
            }
            else
            {
                subList.add(segmentsNoStyleInfo[i]);
                charCount += segmentsNoStyleInfo[i].length();
            }
        }
        if (subList.size() > 0)
        {
            translatedList.addAll(doMSTranslation(subList, sourceLocale,
                    targetLocale));
            subList.clear();
            charCount = 0;
        }
        String[] results = new String[segments.length];
        Map<String, String> separatedSegmentMap = new HashMap<String, String>();
        String injected = null;
        for (int i = 0; i < translatedList.size(); i++)
        {
            if (StringUtil.isEmpty(translatedList.get(i)))
            {
                results[i] = "";
                continue;
            }
            separatedSegmentMap.clear();
            separatedSegmentMap.put("0", translatedList.get(i));
            try
            {
                injected = TmUtil.composeCompleteText(segments[i],
                        separatedSegmentMap);
                // MS Translator will add extra space before < and after > like
                // "> <", " <" and "> ".
                // If original segment has "><", not regardless it has "> <" or not.
                if (segments[i].indexOf("><") > -1)
                {
                    injected = StringUtil.replace(injected, "> <", "><");
                }
//              injected = StringUtil.replace(injected, "> ", ">");
//              injected = StringUtil.replace(injected, " <", "<");
                injected = heads.get(i) + injected + "</segment>";

                results[i] = MTHelper.fixInternalTextAfterMTTranslation(
                        segments[i], injected);
            }
            catch (Exception e)
            {
                results[i] = null;
                CATEGORY.error(e);
            }
        }

        return results;
    }

    private List<String>  doMSTranslation(List<String> subList, Locale sourceLocale,
            Locale targetLocale) throws MachineTranslationException
    {
        List<String> translatedResult = new ArrayList<String>();
        Object[] segmentObjArray = subList.toArray();
        String[] segmentsArray = new String[segmentObjArray.length];
        for (int j = 0; j < segmentObjArray.length; j++)
        {
            segmentsArray[j] = (String) segmentObjArray[j];
        }
        String[] subResults = doBatchTranslation(sourceLocale,
                targetLocale, segmentsArray);
        if (subResults != null)
        {
            translatedResult.addAll(Arrays.asList(subResults));
        }
        else
        {
            for (int k = 0; k < segmentsArray.length; k++)
            {
                translatedResult.add("");
            }
        }
        return translatedResult;
    }

    // Text nodes are sent to MS separately, return bad translation.
    private String[] trMs1(Locale sourceLocale, Locale targetLocale,
            String[] segments, boolean containTags)
            throws MachineTranslationException
    {
        long start = System.currentTimeMillis();
        List<String> translatedList = new ArrayList<String>();
        List<String> subList = new ArrayList<String>();
        int charCount = 0;

        for (int i = 0; i < segments.length; i++)
        {
            subList.add(segments[i]);
            // sentences passed to MS MT should be less than 1000 characters.
            if (i == segments.length - 1
                    || charCount + removeTags(segments[i + 1]).length() > TMProfileConstants.MT_MS_MAX_CHARACTER_NUM)
            {
                Object[] segmentObjArray = subList.toArray();
                String[] segmentsArray = new String[segmentObjArray.length];
                for (int j = 0; j < segmentObjArray.length; j++)
                {
                    segmentsArray[j] = (String) segmentObjArray[j];
                }
                String[] subResults = null;

                if (containTags)
                {
                    subResults = translateSegmentsWithTags(
                            sourceLocale, targetLocale, segmentsArray);
                }
                else if (!containTags)
                {
                    subResults = translateSegmentsWithoutTags(sourceLocale,
                            targetLocale, segmentsArray);
                }

                if (subResults != null)
                {
                    translatedList.addAll(Arrays.asList(subResults));
                }
                subList.clear();
                charCount = 0;
            }
            else
            {
                charCount += removeTags(segments[i]).length();
            }
        }

        String[] results = new String[segments.length];
        for (int i = 0; i < translatedList.size(); i++)
        {
            results[i] = (String) translatedList.get(i);
        }

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Used time: " + (System.currentTimeMillis() - start));
        }
        return results;
    }

    private String[] trSafaba(Locale sourceLocale, Locale targetLocale,
            String[] segments) throws MachineTranslationException
    {
        boolean isXlf = MTHelper.isXlf(parameterMap);
        boolean needSpecialProcessingXlfSegs = MTHelper.needSpecialProcessingXlfSegs(parameterMap);
        int batchSize = determineBatchSize();

        List<String> translatedList = new ArrayList<String>();
        List<String> unTranslatedList = Arrays.asList(segments);

        boolean hasMore = true;
        while (hasMore)
        {
            List<String> subList = null;
            if (unTranslatedList.size() <= batchSize)
            {
                subList = unTranslatedList;
                hasMore = false;
            }
            else
            {
                subList = unTranslatedList.subList(0, batchSize);
                unTranslatedList = unTranslatedList.subList(batchSize,
                        unTranslatedList.size());
            }

            Object[] segmentObjArray = subList.toArray();
            String[] segmentsArray = new String[segmentObjArray.length];
            for (int i = 0; i < segmentObjArray.length; i++)
            {
                segmentsArray[i] = (String) segmentObjArray[i];
            }

            String[] subResults = null;
            if (SafabaProxy.DIRECT_TRANSLATE)
            {
                HashMap<Integer, List<String>> idListMap = new HashMap<Integer, List<String>>();
                HashMap<Integer, List<String>> xListMap = new HashMap<Integer, List<String>>();
                GxmlElement ge = null;
                List<String> heads = new ArrayList<String>();
                String[] segmentsArray2 = new String[segmentsArray.length];
                for (int i = 0; i < segmentsArray.length; i++)
                {
                    int index = segmentsArray[i].indexOf(">");
                    String head = segmentsArray[i].substring(0, index + 1);
                    heads.add(head);

                    List<String> idList = null;
                    List<String> xList = null;
                    if (needSpecialProcessingXlfSegs)
                    {
                        ge = SegmentUtil2.getGxmlElement(segmentsArray[i]);
                        if (ge != null)
                        {
                            idList = SegmentUtil2.getAttValuesByName(ge, "id");
                            idListMap.put(i, idList);
                            xList = SegmentUtil2.getAttValuesByName(ge, "x");
                            xListMap.put(i, xList);
                        }
                        String locale = sourceLocale.getLanguage() + "_"
                                + sourceLocale.getCountry();
                        String segNoRoot = GxmlUtil.stripRootTag(segmentsArray[i]);
                        segNoRoot = MTHelper.wrappText(segNoRoot, locale);
                        segNoRoot = MTHelper.revertXlfSegment(segNoRoot, locale);
                        segNoRoot = MTHelper.encodeLtGtInGxmlAttributeValue(segNoRoot);
                        segmentsArray2[i] = segNoRoot;
                    }
                    else
                    {
                        segmentsArray2[i] = GxmlUtil.stripRootTag(segmentsArray[i]);
                    }
                }

                subResults = doBatchTranslation(sourceLocale, targetLocale,
                        segmentsArray2);

                if (subResults != null && subResults.length == segmentsArray2.length)
                {
                    for (int i = 0; i < subResults.length; i++)
                    {
                        String transResult = subResults[i];
                        if (needSpecialProcessingXlfSegs && StringUtil.isNotEmpty(transResult))
                        {
                            transResult = transResult.replace("_gt;_", ">");
                            // handle single '&' in MT translation
                            transResult = MTHelper
                                    .encodeSeparatedAndChar(transResult);
                            // Parse the translation back
                            DiplomatAPI api = getDiplomatApi();
                            SegmentNode sn = SegmentUtil2.extractSegment(
                                    api, transResult, "xlf", sourceLocale);
                            if (sn != null)
                            {
                                transResult = sn.getSegment();
                                // Handle entity
                                transResult = MTHelper
                                        .resetIdAndXAttributesValues(
                                                transResult, idListMap.get(i),
                                                xListMap.get(i));
                            }
                            else
                            {
                                transResult = "";
                            }
                        }

                        subResults[i] = heads.get(i) + transResult + "</segment>";
                    }
                }
            }
            else if (isXlf)
            {
                subResults = translateAndPutTagsInFront(sourceLocale,
                        targetLocale, segmentsArray);
            }
            else
            {
                subResults = translateSegmentsWithTags(sourceLocale,
                        targetLocale, segmentsArray);
            }

            if (subResults != null)
            {
                translatedList.addAll(Arrays.asList(subResults));
            }
        }

        String[] results = new String[segments.length];
        for (int i = 0; i < translatedList.size(); i++)
        {
            String translatedSeg = translatedList.get(i);
            // If Safaba fails to translate, it will return source.
            String srcSegment = GxmlUtil.stripRootTag(segments[i]).trim();
            String trgSegment = GxmlUtil.stripRootTag(translatedSeg).trim();
            if (srcSegment != null && srcSegment.equals(trgSegment))
            {
                translatedSeg = "";
            }
            results[i] = translatedSeg;
        }
        return results;
    }

    // PROMT does not support batch translation, translate one by one.
    @SuppressWarnings("unchecked")
    private String[] trPromt(Locale sourceLocale, Locale targetLocale,
            String[] segments, boolean containTags)
            throws MachineTranslationException
    {
        if (containTags)
        {
            getMtParameterMap().put(MachineTranslator.CONTAIN_TAGS, "Y");            
        }
        else
        {
            getMtParameterMap().put(MachineTranslator.CONTAIN_TAGS, "N");            
        }

        String[] results = new String[segments.length];
        for (int i = 0; i < segments.length; i++)
        {
            String translatedSegment = null;

            if (containTags)
            {
                String segment = GxmlUtil.stripRootTag(segments[i]);
                translatedSegment = doTranslation(sourceLocale, targetLocale,
                        segment);
                translatedSegment = MTHelper
                        .encodeSeparatedAndChar(translatedSegment);
                int index = segments[i].indexOf(">");
                String head = segments[i].substring(0, index + 1);
                if (translatedSegment == null)
                {
                    translatedSegment = "";
                }
                results[i] = head + translatedSegment + "</segment>";
            }
            else
            {
                translatedSegment = doTranslation(sourceLocale, targetLocale,
                        segments[i]);
                results[i] = translatedSegment;
            }
        }
        return results;
    }

    private String removeTags(String segment)
    {
        String s1, s2;
        s2 = segment;
        s1 = segment.replaceAll(TAG_REGEX, "");
        while (!s1.equals(s2))
        {
            s2 = s1;
            s1 = segment.replaceAll(TAG_REGEX, "");
        }

        s1 = s1.replaceAll(TAG_REGEX_ALONE, "");
        return s1;
    }

    abstract protected String doTranslation(Locale p_sourceLocale,
        Locale p_targetLocale, String p_string)
        throws MachineTranslationException;

    /**
     * Batch translation. See extended class for implementation.
     * 
     * @param p_sourceLocale
     * @param p_targetLocale
     * @param p_string[]
     * @return
     * @throws MachineTranslationException
     */
    protected String[] doBatchTranslation(Locale p_sourceLocale,
            Locale p_targetLocale, String[] p_segments)
            throws MachineTranslationException
    {
        return null;
    }

    public void setMtParameterMap(HashMap hm)
    {
        parameterMap = hm;
    }

    public HashMap getMtParameterMap()
    {
        return this.parameterMap;
    }

    /**
     * Translate batch segments with tags in segments.
     * 
     * @param sourceLocale
     * @param targetLocale
     * @param segments
     * @return
     * @throws MachineTranslationException
     */
    private String[] translateSegmentsWithTags(Locale sourceLocale,
            Locale targetLocale, String[] segments)
            throws MachineTranslationException
    {
        if (sourceLocale == null || targetLocale == null
                || segments == null || segments.length < 1)
        {
            return null;
        }

        String[] results = new String[segments.length];

        HashMap<String, String> map = new HashMap<String, String>();
        for (int k = 0; k < segments.length; k++)
        {
            String[] segmentsFromGxml = MTHelper.getSegmentsInGxml(segments[k]);
            if (segmentsFromGxml == null || segmentsFromGxml.length < 1)
            {
                results[k] = segments[k];
            }
            else
            {
                for (int count = 0; count < segmentsFromGxml.length; count++)
                {
                    String key = String.valueOf(k) + "-" + count;
                    map.put(key, segmentsFromGxml[count]);
                }
            }
        }

        // for MS & Google MT(batch translation)
        if (map.size() > 0)
        {
            // Put all keys into "keysInArray"
            String[] keysInArray = new String[map.keySet().size()];
            Iterator<String> keysInIter = map.keySet().iterator();
            int countKey = 0;
            while (keysInIter.hasNext())
            {
                keysInArray[countKey] = (String) keysInIter.next();
                countKey++;
            }

            // Put all values into "valuesInArray"
            String[] valuesInArray = new String[map.values().size()];
            Iterator<String> valuesInIter = map.values().iterator();
            int countValue = 0;
            while (valuesInIter.hasNext())
            {
                valuesInArray[countValue] = (String) valuesInIter.next();
                countValue++;
            }

            // Do batch translation
            String[] translatedSegments = doBatchTranslation(sourceLocale,
                    targetLocale, valuesInArray);
            
            // Put sub texts back to GXML corresponding positions.
            if (translatedSegments != null)
            {
                for (int m = 0; m < segments.length; m++)
                {
                    String gxml = segments[m];
                    // Retrieve all TextNode that need translate.
                    GxmlElement gxmlRoot = MTHelper.getGxmlElement(gxml);
                    List items2 = 
                        MTHelper.getImmediateAndSubImmediateTextNodes(gxmlRoot);

                    int count = 0;
                    for (Iterator iter = items2.iterator(); iter.hasNext();)
                    {
                        TextNode textNode = (TextNode) iter.next();

                        for (int n = 0; n < translatedSegments.length; n++)
                        {
                            int dashIndex = keysInArray[n].indexOf("-");
                            int index = -1;
                            int subIndex = -1;
                            if (dashIndex < 0)
                            {
                                index = Integer.parseInt(keysInArray[n]);
                            }
                            else
                            {
                                index = Integer.parseInt(keysInArray[n]
                                        .substring(0, dashIndex));
                                subIndex = Integer.parseInt(keysInArray[n]
                                        .substring(dashIndex + 1));
                            }

                            if (index == m && subIndex == count)
                            {
                                textNode.setTextBuffer(new StringBuffer(
                                        translatedSegments[n] == null ? "" : translatedSegments[n]));
                                count++;
                                break;
                            }
                        }
                    }

                    String finalSegment = gxmlRoot.toGxml();
                    results[m] = finalSegment;
                }
            }
        }

        return results;
    }
    
    /**
     * Translate batch segments without tags in segments.
     * 
     * @param p_sourceLocale
     * @param p_targetLocale
     * @param p_segments
     * @return
     * @throws MachineTranslationException
     */
    private String[] translateSegmentsWithoutTags(Locale p_sourceLocale,
            Locale p_targetLocale, String[] p_segments)
            throws MachineTranslationException
    {
        if (p_sourceLocale == null || p_targetLocale == null
                || p_segments == null || p_segments.length < 1)
        {
            return null;
        }

        String[] results = new String[p_segments.length];
        results = doBatchTranslation(p_sourceLocale, p_targetLocale, p_segments);

        return results;
    }
    
    /**
     * After translation, put translations at the last place of tags, 
     * other tags will put a empty text. 
     * 
     * @param sourceLocale
     * @param targetLocale
     * @param segmentsArray
     * @return
     * @throws MachineTranslationException
     */
    private String[] translateAndPutTagsInFront(Locale sourceLocale,
            Locale targetLocale, String[] segments)
            throws MachineTranslationException
    {
        if (sourceLocale == null || targetLocale == null
                || segments == null || segments.length < 1)
        {
            return null;
        }

        String[] results = new String[segments.length];
        String[] textToBeTranslated = new String[segments.length];

        for (int k = 0; k < segments.length; k++)
        {
            String[] segmentsFromGxml = MTHelper.getSegmentsInGxml(segments[k]);
            if (segmentsFromGxml == null || segmentsFromGxml.length < 1)
            {
                textToBeTranslated[k] = segments[k];
            }
            else
            {
                StringBuffer segmentWithOutTags = new StringBuffer();
                for (int count = 0; count < segmentsFromGxml.length; count++)
                {
                    segmentWithOutTags.append(segmentsFromGxml[count]);
                }
                textToBeTranslated[k] = segmentWithOutTags.toString();
            }
        }

        // for Safaba MT(batch translation)
        if (textToBeTranslated.length > 0)
        {
            // Do batch translation
            String[] translatedSegments = doBatchTranslation(sourceLocale,
                    targetLocale, textToBeTranslated);
            
            // Put sub texts back to GXML corresponding positions.
            if (translatedSegments != null)
            {
                for (int m = 0; m < translatedSegments.length; m++)
                {
                    String gxml = segments[m];
                    // Retrieve all TextNode that need translate.
                    GxmlElement gxmlRoot = MTHelper.getGxmlElement(gxml);
                    List items2 = 
                        MTHelper.getImmediateAndSubImmediateTextNodes(gxmlRoot);

                    for (int c = 0; c < items2.size(); c++)
                    {
                        TextNode textNode = (TextNode) items2.get(c);
                        // put all translation text in the last text block between tags
                        if (c == items2.size() - 1)
                        {
                            textNode.setTextBuffer(new StringBuffer(
                                    translatedSegments[m] == null ? "" : translatedSegments[m]));
                        }
                        else
                        {
                            textNode.setTextBuffer(new StringBuffer(""));
                        }
                    }

                    String finalSegment = gxmlRoot.toGxml();
                    results[m] = finalSegment;
                }
            }
        }

        return results;
    }
    
    /**
     * Determine batch translation size.
     * 
     * @return int batchSize
     */
    private int determineBatchSize()
    {
        String engineName = this.getEngineName();
        EngineEnum ee = EngineEnum.getEngine(engineName);
        switch (ee)
        {
        // google 20
            case ProMT:
                return 1;
            case Asia_Online:
                return 99999;
            case Safaba:
                return 50;
            case MS_Translator:
                return 50;
            case IPTranslator:
                return 100;
        }
        return 0;
    }
    
    private DiplomatAPI getDiplomatApi()
    {
        if (m_diplomat == null)
        {
            m_diplomat = new DiplomatAPI();
        }

        m_diplomat.reset();

        return m_diplomat;
    }
}
