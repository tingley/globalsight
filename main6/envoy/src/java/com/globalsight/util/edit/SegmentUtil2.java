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

package com.globalsight.util.edit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.util.system.DynamicPropertiesSystemConfiguration;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.ling.docproc.extractor.html.DynamicRules;
import com.globalsight.ling.tm.LeverageSegment;
import com.globalsight.ling.tw.PseudoCodec;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.PseudoErrorChecker;
import com.globalsight.ling.tw.Tmx2PseudoHandler;

import java.util.Locale;

import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;
import com.globalsight.util.gxml.TextNode;

public class SegmentUtil2
{
    private static String TAGS;
    private static SegmentUtil UTIL;
    private static int count = 0;

    private static final String STYLE_KEY = "untranslatableWordCharacterStyles";
    private static final String PROPERTY_PATH = "/properties/WordExtractor.properties";

    private static final Logger LOG = Logger.getLogger(SegmentUtil2.class
            .getName());

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static List getNotTranslateWords(String src)
    {
        List words = getUTIL().getNotTranslateWords(src);
        words.addAll(getUTIL().getInternalWords(src));
        return words;
    }

    public static SegmentUtil getUTIL()
    {
        if (UTIL == null)
        {
            UTIL = new SegmentUtil(getTAGS());
        }
        return UTIL;
    }

    public static String[] getTags()
    {
        return getTAGS().split(",");
    }

    public static String getTAGS()
    {
        if (TAGS == null)
        {
            Properties props = ((DynamicPropertiesSystemConfiguration) SystemConfiguration
                    .getInstance(PROPERTY_PATH)).getProperties();
            TAGS = props.getProperty(STYLE_KEY);
            if (TAGS == null)
            {
                TAGS = "";
            }

            String[] tagList = TAGS.split(",");
            TAGS = "";
            for (int i = 0; i < tagList.length; i++)
            {
                String tag = tagList[i].trim();
                tag = DynamicRules.normalizeWordStyle(tag);

                if (i > 0)
                {
                    TAGS += ",";
                }
                TAGS += tag;
            }
        }

        return TAGS;
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * Construct a GxmlElement object with the specified segment string.
     * 
     * This is from TuvImpl,but that is not static.
     * 
     * @param p_segmentString
     * @return
     */
    public static GxmlElement getGxmlElement(String p_segmentString)
    {
        GxmlElement m_gxmlElement = null;

        String segment = encodeLtGtInGxmlAttributeValue(p_segmentString);
        GxmlFragmentReader reader = null;

        try
        {
            reader = GxmlFragmentReaderPool.instance().getGxmlFragmentReader();
            m_gxmlElement = reader.parseFragment(segment);
        }
        catch (Exception e)
        {

        }
        finally
        {
            GxmlFragmentReaderPool.instance().freeGxmlFragmentReader(reader);
        }

        return m_gxmlElement;
    }

/**
     * If XML attribute has "<" in it ,it will parse error. This method replaces
     * the attribute "<" into "&lt;".
     * 
     * As some tools may not support ">" in attribute value,also replace ">" to "&gt;".
     * 
     */
    private static String encodeLtGtInGxmlAttributeValue(String segement)
    {
        // this flag for recording the xml element begin.
        boolean flagXML = false;
        // this flag for recording the attribute begin, because if in "<  >",
        // and begin as double quote, it will be a attribute.
        boolean flagQuote = false;
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < segement.length(); i++)
        {
            char c = segement.charAt(i);

            if (!flagXML && !flagQuote && c == '<')
            {
                flagXML = true;
            }
            else if (flagXML && !flagQuote && c == '"')
            {
                flagQuote = true;
            }
            else if (flagXML && !flagQuote && c == '>')
            {
                flagXML = false;
            }
            else if (flagXML && flagQuote && c == '"')
            {
                flagQuote = false;
            }

            if (flagXML && flagQuote && c == '<')
            {
                sb.append("&lt;");
            }
            else if (flagXML && flagQuote && c == '>')
            {
                sb.append("&gt;");
            }
            else
            {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * Get specified attribute's values in List from GxmlElement.
     */
    public static List<String> getAttValuesByName(GxmlElement element,
            String attName)
    {
        List<String> list = new ArrayList<String>();
        if (element != null)
        {
            String value = element.getAttribute(attName);
            if (value != null)
            {
                list.add(value);
            }
            if (element.getChildElements() != null)
            {
                Iterator childIt = element.getChildElements().iterator();
                while (childIt != null && childIt.hasNext())
                {
                    GxmlElement ele = (GxmlElement) childIt.next();
                    List<String> subList = getAttValuesByName(ele, attName);
                    list.addAll(subList);
                }
            }
        }

        return list;
    }

    /**
     * Extract segment to get translatable element.
     */
    public static SegmentNode extractSegment(DiplomatAPI p_api,
            String p_segment, String p_datatype, Locale p_sourceLocale)
    {
        if (p_segment == null || "null".equalsIgnoreCase(p_segment)
                || "".equals(p_segment.trim()))
        {
            return null;
        }

        DiplomatAPI api = null;
        if (p_api == null)
        {
            api = new DiplomatAPI();
        }
        else
        {
            p_api.reset();
            api = p_api;
        }
        api.setEncoding("UTF-8");
        api.setLocale(p_sourceLocale);
        api.setInputFormat(p_datatype);
        api.setSentenceSegmentation(false);
        api.setSegmenterPreserveWhitespace(true);
        StringBuffer sourceString = new StringBuffer();
        sourceString.append("<trans-unit><source>" + p_segment + "</source>");
        sourceString.append("<target>" + p_segment + "</target></trans-unit>");
        api.setSourceString(sourceString.toString());

        try
        {
            api.extract();
            Output output = api.getOutput();

            for (Iterator it = output.documentElementIterator(); it.hasNext();)
            {
                DocumentElement element = (DocumentElement) it.next();
                if (element instanceof TranslatableElement)
                {
                    TranslatableElement trans = (TranslatableElement) element;
                    return (SegmentNode) (trans.getSegments().get(0));
                }
            }
        }
        catch (Exception e)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.error(e.getMessage());
            }
        }

        return null;
    }

    /**
     * Check if the specified TUV has the same tags with the specified target
     * content.
     * 
     * @param tuv
     *            -- Tuv to be modified.
     * @param p_segment
     *            -- Segment string that is to be applied.
     * @param p_jobId
     *            -- job ID current TUV belongs to.
     * @return -- true or false
     */
    public static boolean canBeModified(Tuv tuv, String p_segment, long p_jobId)
    {
        try
        {
            String tuvGxmlExcludeTopTags = tuv.getGxmlExcludeTopTags();
            PseudoData pseudoData = toPsedoData(tuvGxmlExcludeTopTags);
            String segmentStripRootTag = GxmlUtil.stripRootTag(p_segment);
            String dataType = tuv.getDataType(p_jobId);
            String ptagTargetString = toPsedoData(segmentStripRootTag).getPTagSourceString();
            
            pseudoData.setPTagTargetString(ptagTargetString);
            pseudoData.setDataType(dataType);
            PseudoErrorChecker checker = new PseudoErrorChecker();

            boolean result = checker.check(pseudoData, tuvGxmlExcludeTopTags, 0,
                    "utf8", 0, "utf8") == null;
            
            return result;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Get a PseudoData object from specified string.
     */
    public static PseudoData toPsedoData(String s)
            throws DiplomatBasicParserException
    {
        PseudoCodec codec = new PseudoCodec();
        s = codec.encode(s);
        PseudoData pseudoData = new PseudoData();
        pseudoData.setMode(PseudoConstants.PSEUDO_COMPACT);
        Tmx2PseudoHandler eventHandler = new Tmx2PseudoHandler(pseudoData);
        s = eventHandler.preProcessInternalText(s);
        DiplomatBasicParser parser = new DiplomatBasicParser(eventHandler);
        parser.parse(s);
        pseudoData = eventHandler.getResult();
        return eventHandler.getResult();
    }

    /**
	 * The attribute values should keep same in 2 Gxml Elements.
	 * 
	 * @param sourceGxmlElement
	 * @param targetGxmlElement
	 * @param dataType
	 * @return
	 */
	public static String adjustSegmentAttributeValues(
			GxmlElement sourceGxmlElement, GxmlElement targetGxmlElement,
			String dataType)
    {
		// Get values in list for "id" and "x" attributes
        List<String> idList = new ArrayList<String>();
        List<String> xList = new ArrayList<String>();
        List<String> iList = new ArrayList<String>();

        idList = SegmentUtil2.getAttValuesByName(sourceGxmlElement, "id");
        xList = SegmentUtil2.getAttValuesByName(sourceGxmlElement, "x");
        iList = SegmentUtil2.getAttValuesByName(sourceGxmlElement, "i");

        count = 0;
        resetAttributeValues(targetGxmlElement, idList, "id");

        count = 0;
        resetAttributeValues(targetGxmlElement, xList, "x");

        count = 0;
        resetAttributeValues(targetGxmlElement, iList, "i");

        return targetGxmlElement.toGxml(dataType);
    }

	// Require "synchronized" for "count", believe this will not impact job
	// creation and offline uploading.
    @SuppressWarnings("rawtypes")
	private synchronized static void resetAttributeValues(GxmlElement element,
			List<String> attValueList, String p_attName)
    {
		if (element == null || attValueList == null || attValueList.size() == 0)
        {
            return;
        }

        String currentAttValue = element.getAttribute(p_attName);
        if (currentAttValue != null)
        {
            // Set new attribute value
            String newAttValue = null;
            if (attValueList != null && attValueList.size() > count)
            {
                newAttValue = (String) attValueList.get(count);
            }
            else
            {
                newAttValue = String.valueOf(count + 1);
            }
            element.setAttribute(p_attName, newAttValue);
            // Set new text node value
            if (element.getChildElements() != null)
            {
                Iterator it = element.getChildElements().iterator();
                while (it.hasNext())
                {
                    GxmlElement ele = (GxmlElement) it.next();
                    if (ele.getType() == GxmlElement.TEXT_NODE)
                    {
                        TextNode textNode = (TextNode) ele;
                        String nodeValue = textNode.getTextNodeValue();
                        String currentTextNodeValue = "{" + currentAttValue
                                + "}";
                        if (nodeValue.equals(currentTextNodeValue))
                        {
                            StringBuffer newNodeValue = new StringBuffer();
                            newNodeValue.append("{").append(newAttValue)
                                    .append("}");
                            textNode.setTextBuffer(newNodeValue);
                        }
                    }
                }
            }

            count++;
        }
        // add missing first X, X=1
        else if ("bpt".equals(element.getName()) && attValueList != null
                && attValueList.size() > count && "x".equals(p_attName))
        {
            String newAttValue = (String) attValueList.get(count);
            element.setAttribute(p_attName, newAttValue);
            count++;
        }

        // Loop deeper
        if (element.getChildElements() != null)
        {
            Iterator childIt = element.getChildElements().iterator();
            while (childIt.hasNext())
            {
                GxmlElement ele = (GxmlElement) childIt.next();
                resetAttributeValues(ele, attValueList, p_attName);
            }
        }
    }

	/**
	 * Return the "best" exact match to populate into target TUV. If tag does
	 * not match, re-invoke this method to get next one to try.
	 * 
	 * @param srcTuv
	 *            -- Source TUV
	 * @param lsList
	 *            -- ArrayList<LeverageSegment>
	 * @return LeverageSegment
	 */
	public static LeverageSegment getNextBestLeverageSegment(Tuv srcTuv,
			ArrayList<LeverageSegment> lsList)
    {
		if (srcTuv == null || lsList == null || lsList.size() == 0)
			return null;

		// return SID matched leverage segment
    	String sid = srcTuv.getSid();
    	if (sid != null)
    	{
    		for (int i = 0; i < lsList.size(); i++)
        	{
        		LeverageSegment ls = lsList.get(i);
        		if (sid.equals(ls.getSid()))
        		{
        			return lsList.remove(i);
        		}
        	}
    	}

    	// return previous/next hash matched leverage segment
    	long preHash = srcTuv.getPreviousHash();
    	long nextHash = srcTuv.getNextHash();
    	for (int j = 0; j < lsList.size(); j++)
    	{
    		LeverageSegment ls = lsList.get(j);
			if (preHash != -1 && preHash == ls.getPreviousHash()
					&& nextHash != -1 && nextHash == ls.getNextHash())
        	{
        		return lsList.remove(j);
        	}
    	}

    	return lsList.remove(0);
    }
}
