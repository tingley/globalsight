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
    private static final Logger CATEGORY = Logger
            .getLogger(AbstractTranslator.class);

    private static final String TAG_REGEX = "<.pt.*?>[^<]*?</.pt>";
    private static final String TAG_REGEX_ALONE = "<[^>]*?>";
    private HashMap parameterMap = null;
    
    public AbstractTranslator()
    {
    }

    /**
     * Machine translate the given string.
     */
    public String translate(Locale p_sourceLocale, Locale p_targetLocale,
            String p_string) throws MachineTranslationException
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
     * @param p_gxml
     * @return GXML segment XML snippet
     * @exception MachineTranslationException
     */
    public String translateSegment(Locale p_sourceLocale,
            Locale p_targetLocale, String p_gxml)
            throws MachineTranslationException
    {
        GxmlFragmentReader reader =
            GxmlFragmentReaderPool.instance().getGxmlFragmentReader();

        try
        {
            GxmlElement gxmlRoot = reader.parseFragment(p_gxml);

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("gxmlRoot=\r\n"  +gxmlRoot);
                CATEGORY.debug("original:\r\n" + gxmlRoot.toGxml());
            }

            List items = gxmlRoot.getTextNodeWithoutInternal();
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("items ="  + items.size());
            }

            for (Iterator iter = items.iterator(); iter.hasNext(); )
            {
                TextNode textNode = (TextNode) iter.next();
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("element type: " + textNode.getType());                    
                }

                String value = textNode.getTextNodeValue();
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("value='" + value + "'");
                }

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

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("after mt:'" + newValue + "'");                    
                }

                textNode.setTextBuffer(new StringBuffer(newValue));
            }

            String finalSegment = gxmlRoot.toGxml();

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("final:\r\n" + finalSegment);                    
            }

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

        EngineEnum ee = EngineEnum.getEngine(engineName);
        switch (ee)
        {
        // trGoogle(sourceLocale, targetLocale, segments, containTags, results);
            case ProMT:
                return trPromt(sourceLocale, targetLocale, segments, containTags);
            case Asia_Online:
                // Asia Online engine supports batch translation via XLIFF file.
                return doBatchTranslation(sourceLocale, targetLocale, segments);
            case Safaba:
                return trSafaba(sourceLocale, targetLocale, segments);
            case MS_Translator:
                return trMs(sourceLocale, targetLocale, segments, containTags);
            case IPTranslator:
                return trIPTranslator(sourceLocale, targetLocale, segments,
                        containTags);
            case DoMT:
                return doBatchTranslation(sourceLocale, targetLocale, segments);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private String[] trIPTranslator(Locale sourceLocale, Locale targetLocale,
            String[] segments, boolean containTags)
    {
        String[] results = new String[segments.length];
        getMtParameterMap().put(MachineTranslator.CONTAIN_TAGS, containTags);
        String[] heads = new String[segments.length];
        StringBuffer p_string = new StringBuffer("<body>");

        for (int i = 0; i < segments.length; i++)
        {
            int index = segments[i].indexOf(">");
            heads[i] = segments[i].substring(0, index + 1);
            p_string.append("<trans-unit id=\"" + i + "\"><source>"
                    + GxmlUtil.stripRootTag(segments[i])
                    + "</source><target></target></trans-unit>");
        }
        p_string.append("</body>");
        try
        {
            String translatedBody = doTranslation(sourceLocale, targetLocale,
                    p_string.toString());
            if (isBlank(translatedBody))
            {
                return null;
            }

            String[] trans = translatedBody.split("</trans-unit>");
            int index;
            for (int i = 0; i < trans.length; i++)
            {
                if (trans[i].contains("</target>"))
                {
                    String[] target = trans[i].split("<target>");
                    String id = trans[i].split("id")[1].split("\"")[1];
                    index = target[1].indexOf("</target>");
                    String translatedSegment = target[1].substring(0, index);
                    results[i] = heads[Integer.parseInt(id)]
                            + translatedSegment + "</segment>";
                }
            }
        }
        catch (MachineTranslationException e)
        {
            CATEGORY.error(e.getMessage());
        }

        return results;
    }

    private String[] trGoogle(Locale sourceLocale, Locale targetLocale,
            String[] segments, boolean containTags)
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

        String[] results = new String[segments.length];
        for (int j = 0; j < translatedList.size(); j++)
        {
            results[j] = (String) translatedList.get(j);
        }
        return results;
    }

    private String[] trMs(Locale sourceLocale, Locale targetLocale,
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

        String[] results = new String[segments.length];
        for (int j = 0; j < translatedList.size(); j++)
        {
            results[j] = (String) translatedList.get(j);
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

        String[] results = new String[segments.length];
        for (int j = 0; j < translatedList.size(); j++)
        {
            results[j] = (String) translatedList.get(j);
        }
        return results;
    }

    // PROMT does not support batch translation, translate one by one.
    private String[] trPromt(Locale sourceLocale, Locale targetLocale,
            String[] segments, boolean containTags)
            throws MachineTranslationException
    {
        String[] results = new String[segments.length];
        for (int i = 0; i < segments.length; i++)
        {
            String translatedSegment = null;

            if (containTags)
            {
                String segment = GxmlUtil.stripRootTag(segments[i]);
                getMtParameterMap().put(MachineTranslator.CONTAIN_TAGS, "Y");
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
                getMtParameterMap().put(MachineTranslator.CONTAIN_TAGS, "N");
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
        if(gxmlRoot == null)
            return null;
        
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
                    if(finalSegment.startsWith("<segment>") && finalSegment.endsWith("</segment>"))
                    {
                        finalSegment = finalSegment.substring(9, finalSegment.length()-10);
                    }
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
                return 50;
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
     * Init machine translation engine
     * @param engineName MT engine name
     * @return MachineTranslator mt
     */
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

    /**
     * To get more accurate translations from MT engine, below engines will be
     * hit twice, for one segment, one time WITH tag and one time WITHOUT tag.
     * 
     * @return boolean
     */
    public static boolean willHitTwice(String engineName)
    {
        if (ENGINE_ASIA_ONLINE.toLowerCase().equalsIgnoreCase(engineName)
                || ENGINE_MSTRANSLATOR.toLowerCase().equalsIgnoreCase(
                        engineName))
        {
            return true;
        }
        return false;
    }

    /**
     * Certain MT engines will lost tag in translations, need check tag before
     * apply into target segments.
     * 
     * @param engineName
     * @return boolean
     */
    public static boolean needCheckMTTranslationTag(String engineName)
    {
        if (ENGINE_IPTRANSLATOR.equalsIgnoreCase(engineName)
                || ENGINE_DOMT.equalsIgnoreCase(engineName))
        {
            return true;
        }
        return false;
    }
    
    /**
     * <p>Checks if a CharSequence is whitespace, empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is null, empty or whitespace
     * @since 2.0
     * @since 3.0 Changed signature from isBlank(String) to isBlank(CharSequence)
     */
    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }
}
