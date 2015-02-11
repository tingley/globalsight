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

import org.apache.log4j.Logger;

import com.globalsight.dispatcher.bo.EngineEnum;
import com.globalsight.dispatcher.bo.MachineTranslationProfile;
import com.globalsight.dispatcher.dao.DispatcherDAOFactory;
import com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileConstants;
import com.globalsight.machineTranslation.safaba.SafabaProxy;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;
import com.globalsight.util.gxml.TextNode;

/**
 * An abstract base class that implements functionality common to all
 * MT proxies.
 */
public abstract class AbstractTranslator implements MachineTranslator
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            AbstractTranslator.class);

    private static final String TAG_REGEX = "<.pt.*?>[^<]*?</.pt>";
    private static final String TAG_REGEX_ALONE = "<[^>]*?>";
    private HashMap parameterMap = null;
    
    public AbstractTranslator()
    {
    }

    /**
     * Returns special language identifier for certain languages such as
     * Traditional Chinese ("zt"), Indonesian.
     */
    public String mapLanguage(Locale p_locale)
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
     * Machine translate the given string.
     */
    public String translate (Locale p_sourceLocale, Locale p_targetLocale, String p_string)
        throws MachineTranslationException
    {
        return doTranslation(p_sourceLocale, p_targetLocale, p_string);
    }

    /**
     * Machine translate the given GXML segment by walking the structure and
     * machine translating only the translatable portions, not localizable. This
     * have the unfortunate behavior of
     * 
     * @param p_sourceLocale
     *            - source locale
     * @param p_targetLocale
     *            - target locale
     * @param p_segment
     * @return GXML segment XML snippet
     * @exception MachineTranslationException
     */
    public String translateSegment (Locale p_sourceLocale,
        Locale p_targetLocale, String p_segment)
        throws MachineTranslationException
    {
        GxmlFragmentReader reader =
            GxmlFragmentReaderPool.instance().getGxmlFragmentReader();

        try
        {
            GxmlElement gxmlRoot = reader.parseFragment(p_segment);

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("gxmlRoot=\r\n"  +gxmlRoot);
                CATEGORY.debug("original:\r\n" + gxmlRoot.toGxml());
            }

            List items = gxmlRoot.getTextNodeWithoutInternal();

            CATEGORY.debug("items ="  + items.size());

            for (Iterator iter = items.iterator(); iter.hasNext(); )
            {
                TextNode textNode = (TextNode) iter.next();

                CATEGORY.debug("element type: " + textNode.getType());

                String value = textNode.getTextNodeValue();

                CATEGORY.debug("value='" + value + "'");

                if (value == null || value.length() < 2)
                {
                    CATEGORY.debug("Skipping...");
                    continue;
                }

                //machine translate
                String newValue = doTranslation(p_sourceLocale, p_targetLocale, value);

                //if can't get machine translation,return the source segment.
                if (newValue == null || "".equals(newValue)
                        || "null".equals(newValue))
                {
                    newValue = value;
                }

                CATEGORY.debug("after mt:'" + newValue + "'");

                textNode.setTextBuffer(new StringBuffer(newValue));
            }

            String finalSegment = gxmlRoot.toGxml();

            CATEGORY.debug("final:\r\n" + finalSegment);

            return finalSegment;
        }
        catch (MachineTranslationException mte)
        {
            throw mte;
        }
        catch (Exception e)
        {
            throw new MachineTranslationException(
                "Failed to execute machine translation for gxml segment",
                (Throwable)e);
        }
        finally
        {
            GxmlFragmentReaderPool.instance().freeGxmlFragmentReader(reader);
        }
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
     * 
     * @return String[] translated segments in sequence.
     * 
     * @exception MachineTranslationException
     */
    public String[] translateBatchSegments(Locale sourceLocale,
            Locale targetLocale, String[] segments, boolean containTags)
            throws MachineTranslationException
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

        String[] results = new String[segments.length];
        EngineEnum ee = EngineEnum.getEngine(engineName);
        switch (ee)
        {
        // trGoogle(sourceLocale, targetLocale, segments, containTags, results);
            case ProMT:
                // As PROMT does not support batch translation, translate one by one.
                trPromt(sourceLocale, targetLocale, segments, containTags, results);
                break;
            case Asia_Online:
                // Asia Online engine supports batch translation via XLIFF file.
                return this.doBatchTranslation(sourceLocale, targetLocale,
                        segments);
            case Safaba:
                trSafaba(sourceLocale, targetLocale, segments, results);
                break;
            case MS_Translator:
                trMs(sourceLocale, targetLocale, segments, containTags, results);
                break;
            case IPTranslator:
                trIPTranslator(sourceLocale, targetLocale, segments,
                        containTags, results);
                break;
        }

        return results;
    }

    private void trIPTranslator(Locale sourceLocale, Locale targetLocale,
            String[] segments, boolean containTags, String[] results)
    {
        getMtParameterMap().put(MachineTranslator.CONTAIN_TAGS, containTags);
        try
        {
                results = doBatchTranslation(sourceLocale, targetLocale,
                        segments);
        }
        catch (MachineTranslationException e)
        {
            e.printStackTrace();
        }

    }

    private void trGoogle(Locale sourceLocale, Locale targetLocale,
            String[] segments, boolean containTags, String[] results)
            throws MachineTranslationException
    {
        int batchSize = determineBatchSize();
        List translatedList = new ArrayList();
        List unTranslatedList = Arrays.asList(segments);
        
        boolean hasMore = true;
        while (hasMore)
        {
            List subList = null;
            String[] subResults = null;
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

            if (containTags)
            {
                subResults = translateAndSplitSegmentsWithTags(sourceLocale,
                        targetLocale, segmentsArray);
            }
            else if (!containTags)
            {
                subResults = translateSegmentsWithoutTags(sourceLocale,
                        targetLocale, segmentsArray);
            }
            
            if (subResults != null) {
                translatedList.addAll(Arrays.asList(subResults));
            }
        }

        for (int j = 0; j < translatedList.size(); j++)
        {
            results[j] = (String) translatedList.get(j);
        }
    }

    private void trMs(Locale sourceLocale, Locale targetLocale,
            String[] segments, boolean containTags, String[] results)
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
                    subResults = translateAndSplitSegmentsWithTags(
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
        for (int j = 0; j < translatedList.size(); j++)
        {
            results[j] = (String) translatedList.get(j);
        }
        CATEGORY.debug("Used time: " + (System.currentTimeMillis() - start));
    }

    private void trSafaba(Locale sourceLocale, Locale targetLocale,
            String[] segments, String[] results)
            throws MachineTranslationException
    {
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

            boolean isXlf = MTHelper.needRevertXlfSegment(parameterMap);
            String[] subResults = null;
            if (SafabaProxy.DIRECT_TRANSLATE)
            {
                subResults = doBatchTranslation(sourceLocale, targetLocale,
                        segmentsArray);
            }
            else if (isXlf)
            {
                subResults = translateAndPutTagsInFront(sourceLocale,
                        targetLocale, segmentsArray);
            }
            else
            {
                subResults = translateAndSplitSegmentsWithTags(sourceLocale,
                        targetLocale, segmentsArray);
            }
            if (subResults != null)
            {
                translatedList.addAll(Arrays.asList(subResults));
            }
        }

        for (int j = 0; j < translatedList.size(); j++)
        {
            results[j] = (String) translatedList.get(j);
        }
    }

    private void trPromt(Locale sourceLocale, Locale targetLocale,
            String[] segments, boolean containTags, String[] results)
            throws MachineTranslationException
    {
        for (int i = 0; i < segments.length; i++)
        {
            String translatedSegment = null;

            if (containTags)
            {
                String segment = GxmlUtil.stripRootTag(segments[i]);
                getMtParameterMap().put(MachineTranslator.CONTAIN_TAGS, "Y");
                translatedSegment = doTranslation(sourceLocale, targetLocale,
                        segment);
                translatedSegment = encodeSeparatedAndChar(translatedSegment);
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
                getMtParameterMap().put(MachineTranslator.CONTAIN_TAGS, "N");
                translatedSegment = doTranslation(sourceLocale, targetLocale,
                        segments[i]);
                results[i] = translatedSegment;
            }
        }
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

    public void setMtParameterMap(HashMap hm) {
    	parameterMap = hm;
    }

    public HashMap getMtParameterMap() {
    	return this.parameterMap;
    }

    private String[] getSegmentInGxml(String segmentInGxml)
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
    
    /**
     * Translate batch segments with tags in segments.
     * 
     * @param sourceLocale
     * @param targetLocale
     * @param segments
     * @return
     * @throws MachineTranslationException
     */
    private String[] translateAndSplitSegmentsWithTags(Locale sourceLocale,
            Locale targetLocale, String[] segments)
            throws MachineTranslationException
    {
        if (sourceLocale == null || targetLocale == null
                || segments == null || segments.length < 1)
        {
            return null;
        }

        String[] results = new String[segments.length];

        HashMap map = new HashMap();
        for (int k = 0; k < segments.length; k++)
        {
            String[] segmentsFromGxml = getSegmentInGxml(segments[k]);
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

        // for MS MT, Google MT(batch translation)
        if (map.size() > 0)
        {
            // Put all keys into "keysInArray"
            String[] keysInArray = new String[map.keySet().size()];
            Iterator keysInIter = map.keySet().iterator();
            int countKey = 0;
            while (keysInIter.hasNext())
            {
                keysInArray[countKey] = (String) keysInIter.next();
                countKey++;
            }

            // Put all values into "valuesInArray"
            String[] valuesInArray = new String[map.values().size()];
            Iterator valuesInIter = map.values().iterator();
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
            String[] segmentsFromGxml = getSegmentInGxml(segments[k]);
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
                return 100;
            case MS_Translator:
                return 50;
            case IPTranslator:
                return 100;

        }
        return 0;
    }
    
    protected MachineTranslationProfile getMTProfile()
    {
        MachineTranslationProfile result = null;

        HashMap paramMap = getMtParameterMap();
        String mtId = (String) paramMap.get(MachineTranslator.MT_PROFILE_ID);
        try
        {
//            result = MTProfileHandlerHelper.getMTProfileById(mtId);
            result = DispatcherDAOFactory.getMTPRofileDAO().getMTProfile(Long.valueOf(mtId));
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to get translation memory profile for profile ID : "
                    + mtId);
        }

        return result;
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
     * Init machine translation engine
     * @param engineName MT engine name
     * @return MachineTranslator mt
     */
    // TODO MOVE TO ME
    public static MachineTranslator initMachineTranslator(String engineName)
    {
        if (engineName == null || "".equals(engineName.trim()))
        {
            return null;
        }

        MachineTranslator mt = null;
        EngineEnum e = EngineEnum.getEngine(engineName);
        try
        {

            mt = e.getProxy();
        }
        catch (Exception ex)
        {
            CATEGORY.error(
                    "Could not initialize machine translation engine from class ",
                    ex);
        }

        return mt;
    }

}
