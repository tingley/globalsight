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

import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileConstants;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;

import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.TextNode;
import com.globalsight.util.gxml.GxmlException;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * An abstract base class that implements functionality common to all
 * MT proxies.
 */
public abstract class AbstractTranslator
    implements MachineTranslator
{
    private static final GlobalSightCategory CATEGORY =
        (GlobalSightCategory) GlobalSightCategory.getLogger(
            AbstractTranslator.class);

    private HashMap parameterMap = null;
    
    public AbstractTranslator()
    {
    }

    /**
     * Returns special language identifier for Traditional Chinese ("zt").
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

            List items = gxmlRoot.getChildElements(GxmlElement.TEXT_NODE);

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
     * @param p_sourceLocale
     *            source locale
     * @param p_targetLocale
     *            target locale
     * @param p_segments
     *            - the segments to be translated
     * @param p_containTags
     *            - if the segments contains tags in them.
     * 
     * @return String[] translated segments in sequence.
     * 
     * @exception MachineTranslationException
     */
    public String[] translateBatchSegments(Locale p_sourceLocale,
            Locale p_targetLocale, String[] p_segments, boolean p_containTags)
            throws MachineTranslationException
    {
        if (p_sourceLocale == null || p_targetLocale == null
                || p_segments == null || p_segments.length < 1)
        {
            return null;
        }

        String engineName = this.getEngineName();
        if (engineName == null || "".equals(engineName.trim()))
        {
            return null;
        }

        String[] results = new String[p_segments.length];
        // Asia Online engine supports batch translation via XLIFF file.
        if (engineName != null
                && engineName
                        .equalsIgnoreCase(MachineTranslator.ENGINE_ASIA_ONLINE))
        {
            results = this.doBatchTranslation(p_sourceLocale, p_targetLocale,
                    p_segments);
        }
        // As PROMT does not support batch translation, translate one by one.
        else if (engineName != null
                && engineName.equalsIgnoreCase(MachineTranslator.ENGINE_PROMT))
        {
            // get ptsUrlFlag
            String ptsUrlFlag = null;
            TranslationMemoryProfile tmProfile = getTMProfile();
            if (tmProfile != null)
            {
                ptsUrlFlag = tmProfile.getPtsUrlFlag();
            }
            
            for (int i = 0; i < p_segments.length; i++)
            {
                String translatedSegment = null;
                
                // Translate via PTS8 APIs
                if (ptsUrlFlag != null
                        && TMProfileConstants.MT_PTS_URL_FLAG_V8
                                .equals(ptsUrlFlag))
                {
                    // With tags
                    if (p_containTags)
                    {
                        translatedSegment = translateSegment(p_sourceLocale,
                                p_targetLocale, p_segments[i]);
                    }
                    // No tags
                    else
                    {
                        translatedSegment = doTranslation(p_sourceLocale,
                                p_targetLocale, p_segments[i]);
                    }

                    results[i] = translatedSegment;
                }
                // Translate via PTS9 APIs
                else if (ptsUrlFlag != null
                        && TMProfileConstants.MT_PTS_URL_FLAG_V9
                                .equals(ptsUrlFlag))
                {
                    if (p_containTags)
                    {
                        String segment = GxmlUtil.stripRootTag(p_segments[i]);
                        getMtParameterMap().put("containTags", "Y");
                        translatedSegment = doTranslation(p_sourceLocale,
                                p_targetLocale, segment);
                        translatedSegment = encodeSeparatedAndChar(translatedSegment);
                        int index = p_segments[i].indexOf(">");
                        String head = p_segments[i].substring(0, index + 1);
                        if (translatedSegment == null)
                        {
                            translatedSegment = "";
                        }
                        results[i] = head + translatedSegment + "</segment>";                        
                    }
                    else
                    {
                        getMtParameterMap().put("containTags", "N");
                        translatedSegment = doTranslation(p_sourceLocale,
                                p_targetLocale, p_segments[i]);
                        results[i] = translatedSegment;
                    }
                }
            }
        }
        // For MS Translator and Google
        else
        {
            int batchSize = determineBatchSize();
            List translatedList = new ArrayList();
            List unTranslatedList = Arrays.asList(p_segments);
            
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

                if (p_containTags)
                {
                    subResults = translateBatchSegments2(p_sourceLocale,
                            p_targetLocale, segmentsArray);
                }
                else if (!p_containTags)
                {
                    subResults = translateBatchSegments3(p_sourceLocale,
                            p_targetLocale, segmentsArray);
                }

                translatedList.addAll(Arrays.asList(subResults));
            }

            for (int j = 0; j < translatedList.size(); j++)
            {
                results[j] = (String) translatedList.get(j);
            }
        }

        return results;
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
    	String[] segmentsFromGxml = null;
        GxmlFragmentReader reader = GxmlFragmentReaderPool.instance().getGxmlFragmentReader();

        try {
            GxmlElement gxmlRoot = reader.parseFragment(segmentInGxml);
            List items = gxmlRoot.getChildElements(GxmlElement.TEXT_NODE);
            
            segmentsFromGxml = new String[items.size()];
            int count = 0;
            for (Iterator iter = items.iterator(); iter.hasNext(); )
            {
                TextNode textNode = (TextNode) iter.next();
                String value = textNode.getTextNodeValue();//.trim();
                segmentsFromGxml[count] = value;
                count++;
            }
        } catch (GxmlException ex) {
        	
        } finally {
            GxmlFragmentReaderPool.instance().freeGxmlFragmentReader(reader);
        }
        
        return segmentsFromGxml;
    }
    
    /**
     * Translate batch segments with tags in segments.
     * 
     * @param p_sourceLocale
     * @param p_targetLocale
     * @param p_segments
     * @return
     * @throws MachineTranslationException
     */
    private String[] translateBatchSegments2(Locale p_sourceLocale,
            Locale p_targetLocale, String[] p_segments)
            throws MachineTranslationException
    {
        if (p_sourceLocale == null || p_targetLocale == null
                || p_segments == null || p_segments.length < 1)
        {
            return null;
        }

        String[] results = new String[p_segments.length];

        HashMap map = new HashMap();
        for (int k = 0; k < p_segments.length; k++)
        {
            String[] segmentsFromGxml = getSegmentInGxml(p_segments[k]);
            if (segmentsFromGxml == null || segmentsFromGxml.length < 1)
            {
                results[k] = p_segments[k];
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

        // for MS MT and Google MT (batch translation)
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
            String[] translatedSegments = doBatchTranslation(p_sourceLocale,
                    p_targetLocale, valuesInArray);
            
            // Put sub texts back to GXML corresponding positions.
            if (translatedSegments != null)
            {
                for (int m = 0; m < p_segments.length; m++)
                {
                    String gxml = p_segments[m];
                    GxmlFragmentReader reader = GxmlFragmentReaderPool
                            .instance().getGxmlFragmentReader();
                    GxmlElement gxmlRoot2 = reader.parseFragment(gxml);
                    List items2 = gxmlRoot2
                            .getChildElements(GxmlElement.TEXT_NODE);

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
                                textNode
                                        .setTextBuffer(new StringBuffer(
                                                translatedSegments[n] == null ? "" : translatedSegments[n]));
                                count++;
                                break;
                            }
                        }
                    }

                    String finalSegment = gxmlRoot2.toGxml();
                    results[m] = finalSegment;
                }
            }
//            else
//            {
//                results = p_segments;
//            }
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
    private String[] translateBatchSegments3(Locale p_sourceLocale,
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
     * Determine batch translation size.
     * 
     * @return int batchSize
     */
    private int determineBatchSize()
    {
        int batchSize = 1;

        String engineName = this.getEngineName();
        if (engineName.equalsIgnoreCase(MachineTranslator.ENGINE_MSTRANSLATOR))
        {
            batchSize = 50;
        }
        else if (engineName.equalsIgnoreCase(MachineTranslator.ENGINE_GOOGLE))
        {
            batchSize = 20;
        }
        else if (engineName
                .equalsIgnoreCase(MachineTranslator.ENGINE_ASIA_ONLINE))
        {
            batchSize = 99999;
        }
        else if (engineName.equalsIgnoreCase(MachineTranslator.ENGINE_PROMT))
        {
            batchSize = 1;
        }

        return batchSize;
    }
    
    /**
     * Obtain the translation memory profile by profile ID.
     * 
     * @return TranslationMemoryProfile object
     */
    protected TranslationMemoryProfile getTMProfile()
    {
        TranslationMemoryProfile result = null;

        HashMap paramMap = getMtParameterMap();
        Long tmProfileID = (Long) paramMap.get(MachineTranslator.TM_PROFILE_ID);
        try
        {
            result = ServerProxy.getProjectHandler().getTMProfileById(
                    tmProfileID.longValue(), false);
        }
        catch (Exception e)
        {
            CATEGORY
                    .error("Failed to get translation memory profile for profile ID : "
                            + tmProfileID);
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
    public static MachineTranslator initMachineTranslator(String engineName)
    {
        if (engineName == null || "".equals(engineName.trim()))
        {
            return null;
        }

        String engineClass = null;
        MachineTranslator mt = null;
        try
        {
            if (engineName.equalsIgnoreCase(MachineTranslator.ENGINE_GOOGLE))
            {
                engineClass = "com.globalsight.machineTranslation.google.GoogleProxy";
            }
            else if (engineName
                    .equalsIgnoreCase(MachineTranslator.ENGINE_PROMT))
            {
                engineClass = "com.globalsight.machineTranslation.promt.ProMTProxy";
            }
            else if (engineName
                    .equalsIgnoreCase(MachineTranslator.ENGINE_MSTRANSLATOR))
            {
                engineClass = "com.globalsight.machineTranslation.mstranslator.MSTranslatorProxy";
            }
            else if (engineName
                    .equalsIgnoreCase(MachineTranslator.ENGINE_ASIA_ONLINE))
            {
                engineClass = "com.globalsight.machineTranslation.asiaOnline.AsiaOnlineProxy";
            }

            mt = (MachineTranslator) Class.forName(engineClass).newInstance();
        }
        catch (Exception ex)
        {
            CATEGORY.error(
                    "Could not initialize machine translation engine from class "
                            + engineClass, ex);
        }

        return mt;
    }

}
