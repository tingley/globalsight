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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.globalsight.everest.projecthandler.EngineEnum;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.tw.Tmx2PseudoHandler;
import com.globalsight.util.PropertiesFactory;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.edit.SegmentUtil2;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlException;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;
import com.globalsight.util.gxml.TextNode;

public class MTHelper
{
    private static final Logger CATEGORY = Logger.getLogger(MTHelper.class);

    public static final String MT_EXTRA_CONFIGS = "/properties/mt.config.properties";
    // GBS-3722
    private static String TAG_MT_LEADING_BPT = "<bpt internal=\"yes\" i=\"iii\" type=\"mtlid\">&lt;mtlid&gt;</bpt>";
    private static String TAG_MT_TRAILING_BPT = "<bpt internal=\"yes\" i=\"iii\" type=\"mttid\">&lt;mttid&gt;</bpt>";
    private static String TAG_MT_LEADING_EPT = "<ept i=\"iii\">&lt;/mtlid&gt;</ept>";
    private static String TAG_MT_TRAILING_EPT = "<ept i=\"iii\">&lt;/mttid&gt;</ept>";

    /**
     * Init machine translation engine
     * 
     * @param engineName
     *            MT engine name
     * @return MachineTranslator MT
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
        if (MachineTranslator.ENGINE_ASIA_ONLINE.toLowerCase()
                .equalsIgnoreCase(engineName))
        {
            return true;
        }

        return false;
    }

    /**
     * Certain MT engines will lost tag in translations, need check tag before
     * apply into target segments.
     * 
     * Safaba will lost sub segment(s) when a segment has more than one sub
     * segments.
     * 
     * @param engineName
     * @return boolean
     */
    public static boolean needCheckMTTranslationTag(String engineName)
    {
		if (MachineTranslator.ENGINE_IPTRANSLATOR.equalsIgnoreCase(engineName)
				|| MachineTranslator.ENGINE_DOMT.equalsIgnoreCase(engineName)
				|| MachineTranslator.ENGINE_SAFABA.equalsIgnoreCase(engineName)
				|| MachineTranslator.ENGINE_MSTRANSLATOR
						.equalsIgnoreCase(engineName))
        {
            return true;
        }

        return false;
    }

    /**
     * Get all translatable TextNode list in the specified gxml.
     * 
     * @param p_gxml
     * @return List in TextNode
     */
    @SuppressWarnings(
    { "rawtypes", "unchecked" })
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
     * Check if the given gxml's all tags have "id" attribute. "id" attribute is
     * required by most tags in XLIFF specification. If false, translate the
     * GXML in pure text way.
     * 
     * @param gxml
     * @return boolean
     */
    @SuppressWarnings("unchecked")
    public static boolean isAllTagsHaveIdAttr(String gxml)
    {
        if (gxml == null || gxml.trim().length() == 0)
            return true;

        String gxmlWithRoot = gxml;
        // Remove root tag with all attributes in "<segment..>".
        if (gxml.toLowerCase().endsWith("</segment>"))
        {
            gxmlWithRoot = GxmlUtil.stripRootTag(gxml);
        }
        gxmlWithRoot = "<segment>" + gxmlWithRoot + "</segment>";
        gxmlWithRoot = gxmlWithRoot.replace(" i=", " id=");
        GxmlElement gxmlRootWithID = MTHelper.getGxmlElement(gxmlWithRoot);

        List<GxmlElement> result = new ArrayList<GxmlElement>();
        result.addAll(gxmlRootWithID.getChildElements());
        result.addAll(gxmlRootWithID
                .getDescendantElements(GxmlElement.SUB_TYPE));
        Iterator<GxmlElement> it = result.iterator();
        Set<String> tags = getTagsRequireID();
        while (it.hasNext())
        {
            GxmlElement ele = it.next();
            String tagName = ele.getName();
            if (tags.contains(tagName))
            {
                String id = ele.getAttribute("id");
                if (id == null)
                {
                    return false;
                }
            }
        }

        return true;
    }

    private static Set<String> tagsRequireIDAttribute = new HashSet<String>();

    private static Set<String> getTagsRequireID()
    {
        if (tagsRequireIDAttribute.size() == 0)
        {
            tagsRequireIDAttribute.add("bpt");
            tagsRequireIDAttribute.add("ept");
            tagsRequireIDAttribute.add("it");
            tagsRequireIDAttribute.add("ph");
        }

        return tagsRequireIDAttribute;
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
     * In GlobalSight, segments from XLF file are "wrapped" with "ph" again. So,
     * before send such segments to MT engine, need revert them back; After get
     * translations from MT engine, need wrap them again.
     * 
     * So, if the segment is from GlobalSight and from XLF file, this should be
     * set to TRUE; If not from GlobalSight or not from XLF file, this should be
     * set to FALSE.
     * 
     * @return boolean
     */
    @SuppressWarnings("rawtypes")
    public static boolean needSpecialProcessingXlfSegs(Map paramMap)
    {
        if (paramMap == null)
        {
            return false;
        }

        String needSpecialProcessingXlfSegs = (String) paramMap
                .get(MachineTranslator.NEED_SPECAIL_PROCESSING_XLF_SEGS);
        if ("true".equalsIgnoreCase(needSpecialProcessingXlfSegs))
        {
            return true;
        }

        return false;
    }

    /**
     * Check if the segments are from XLF source file.
     * 
     * @param paramMap
     * @return boolean
     */
    @SuppressWarnings("rawtypes")
    public static boolean isXlf(Map paramMap)
    {
        String spDataType = (String) paramMap.get(MachineTranslator.DATA_TYPE);
        if ("xlf".equalsIgnoreCase(spDataType)
                || "xliff".equalsIgnoreCase(spDataType))
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
     * 
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

    public static String getMTConfig(String paramName)
    {
        Properties mtProperties = getMTExtraConfigs();
        String param = mtProperties.getProperty(paramName);
        if (param != null)
        {
            return param;
        }

        return null;
    }

    private static Properties getMTExtraConfigs()
    {
        PropertiesFactory factory = new PropertiesFactory();
        return factory.getProperties(MT_EXTRA_CONFIGS);
    }

    /**
     * User may want more detailed information in "GlobalSight.log" when use MT,
     * flag "mt.log.detailed.info" is for this purpose, default false.
     * 
     * @return boolean
     */
    public static boolean isLogDetailedInfo(String engineName)
    {
        if (engineName == null || engineName.trim().length() == 0)
            return false;

        String key = engineName.toLowerCase() + ".log.detailed.info";
        String logDetailedInfo = getMTConfig(key);
        if ("true".equalsIgnoreCase(logDetailedInfo))
        {
            return true;
        }
        return false;
    }

/**
     * If XML attribute has "<" in it ,it will parse error. This method replaces
     * the attribute "<" into "&lt;".
     * 
     * As PROMT can not support ">" in attribute value,also replace ">" to "_gt;_".
     * 
     * Note that this method should keep private!
     */
    public static String encodeLtGtInGxmlAttributeValue(String segement)
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
                sb.append("_gt;_");
            }
            else
            {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * For segment from XLF file, after translation, put the "id" and "x" values
     * back to ensure the re-wrapped translated segment has same "id" and "x"
     * sequence as original.
     * 
     * @param p_segString
     *            -- MT translated and re-wrapped segment, no root tag.
     * @return
     */
    public static String resetIdAndXAttributesValues(String p_segString,
            List<String> idList, List<String> xList)
    {
        if (p_segString == null || p_segString.trim().length() == 0)
        {
            return null;
        }

        String result = null;
        int count = 0;
        try
        {
            StringBuffer sb = new StringBuffer();
            sb.append("<segment>").append(p_segString).append("</segment>");
            GxmlElement gxmlElement = SegmentUtil2
                    .getGxmlElement(sb.toString());

            resetIdAndXBack(gxmlElement, idList, xList, count);

            String gxml = gxmlElement.toGxml("xlf");
            result = GxmlUtil.stripRootTag(gxml);
        }
        catch (Exception e)
        {
            result = p_segString;
        }

        return result;
    }

    private static void resetIdAndXBack(GxmlElement element,
            List<String> idList, List<String> xList, int count)
    {
        if (element == null)
        {
            return;
        }

        String id = element.getAttribute("id");
        if (id != null)
        {
            String idValue = null;
            if (idList != null && idList.size() > count)
            {
                idValue = (String) idList.get(count);
            }
            else
            {
                idValue = String.valueOf(count + 1);
            }
            element.setAttribute("id", idValue);
        }

        String x = element.getAttribute("x");
        if (x != null)
        {
            String xValue = null;
            if (xList != null && xList.size() > count)
            {
                xValue = (String) xList.get(count);
            }
            else
            {
                xValue = String.valueOf(count + 1);
            }
            element.setAttribute("x", xValue);
        }

        if (id != null || x != null)
        {
            count++;
        }

        Iterator<?> childIt = element.getChildElements().iterator();
        while (childIt.hasNext())
        {
            GxmlElement ele = (GxmlElement) childIt.next();
            resetIdAndXBack(ele, idList, xList, count);
        }
    }

    /**
     * Transform an XML node object to XML string.
     * 
     * @param node
     * @return
     * @throws TransformerException
     */
    public static String outputNode2Xml(Node node) throws TransformerException
    {
        StringWriter writer = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer();
        transformer.transform(new DOMSource(node), new StreamResult(writer));

        return writer.toString();
    }

    /**
     * Office2010 requires a "good style" for "xml:space", "&quot;" is required.
     * 
     * @param machineTranslatedGxml
     * @return String
     */
    public static String fixMtTranslatedGxml(String machineTranslatedGxml)
    {
        if (StringUtil.isEmpty(machineTranslatedGxml))
            return machineTranslatedGxml;

        String badStyle = "xml:space=\"preserve\"";
        String goodStyle = "xml:space=&quot;preserve&quot;";

        String tmp = machineTranslatedGxml.toLowerCase();
        int index = tmp.indexOf(badStyle);
        if (index == -1)
        {
            return machineTranslatedGxml;
        }

        StringBuffer sb = new StringBuffer();
        String str1 = "";
        String str2 = machineTranslatedGxml;
        while (index > -1)
        {
            str1 = str2.substring(0, index) + goodStyle;
            sb.append(str1);
            str2 = str2.substring(index + badStyle.length());
            index = str2.indexOf(badStyle);
        }
        sb.append(str2);

        return sb.toString();
    }

    /**
     * Tags machine translated content with leading and trailing identifiers
     * configured in MT Profile.
     * 
     * @since GBS-3722
     */
    public static String tagMachineTranslatedContent(String gxml,
            String leading, String trailing)
    {
        String regex = "(<segment[^>]*>)([\\d\\D]*?)(</segment>)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(gxml);
        StringBuilder result = new StringBuilder();
        if (m.find())
        {
            String segment = m.group(2);
            int maxI = maxI(segment);
            result.append(m.group(1));
            if (!StringUtil.isEmpty(leading))
            {
                maxI++;
                result.append(TAG_MT_LEADING_BPT.replace("iii", "" + maxI));
                result.append(leading);
                result.append(TAG_MT_LEADING_EPT.replace("iii", "" + maxI));
            }
            result.append(segment);
            if (!StringUtil.isEmpty(trailing))
            {
                maxI++;
                result.append(TAG_MT_TRAILING_BPT.replace("iii", "" + maxI));
                result.append(trailing);
                result.append(TAG_MT_TRAILING_EPT.replace("iii", "" + maxI));
            }
            result.append(m.group(3));
            gxml = result.toString();
        }

        return gxml;
    }

    /**
     * Removes MT tags for export (keep leading and trailing content).
     * 
     * @since GBS-3722
     */
    public static String cleanMTTagsForExport(String chunk)
    {
        String replaced = chunk.replaceFirst(
                Tmx2PseudoHandler.R_MT_IDENTIFIER_LEADING_BPT, "");
        while (!replaced.equals(chunk))
        {
            chunk = replaced;
            replaced = chunk.replaceFirst(
                    Tmx2PseudoHandler.R_MT_IDENTIFIER_LEADING_BPT, "");
        }

        replaced = chunk.replaceFirst(
                Tmx2PseudoHandler.R_MT_IDENTIFIER_LEADING_EPT, "");
        while (!replaced.equals(chunk))
        {
            chunk = replaced;
            replaced = chunk.replaceFirst(
                    Tmx2PseudoHandler.R_MT_IDENTIFIER_LEADING_EPT, "");
        }

        replaced = chunk.replaceFirst(
                Tmx2PseudoHandler.R_MT_IDENTIFIER_TRAILING_BPT, "");
        while (!replaced.equals(chunk))
        {
            chunk = replaced;
            replaced = chunk.replaceFirst(
                    Tmx2PseudoHandler.R_MT_IDENTIFIER_TRAILING_BPT, "");
        }

        replaced = chunk.replaceFirst(
                Tmx2PseudoHandler.R_MT_IDENTIFIER_TRAILING_EPT, "");
        while (!replaced.equals(chunk))
        {
            chunk = replaced;
            replaced = chunk.replaceFirst(
                    Tmx2PseudoHandler.R_MT_IDENTIFIER_TRAILING_EPT, "");
        }

        return chunk;
    }

    /**
     * Removes MT tags including leading and trailing content for TM storage.
     * 
     * @since GBS-3722
     */
    public static String cleanMTTagsForTMStorage(String chunk)
    {
        String replaced = chunk.replaceFirst(
                Tmx2PseudoHandler.R_MT_IDENTIFIER_LEADING, "");
        while (!replaced.equals(chunk))
        {
            chunk = replaced;
            replaced = chunk.replaceFirst(
                    Tmx2PseudoHandler.R_MT_IDENTIFIER_LEADING, "");
        }

        replaced = chunk.replaceFirst(
                Tmx2PseudoHandler.R_MT_IDENTIFIER_TRAILING, "");
        while (!replaced.equals(chunk))
        {
            chunk = replaced;
            replaced = chunk.replaceFirst(
                    Tmx2PseudoHandler.R_MT_IDENTIFIER_TRAILING, "");
        }

        return chunk;
    }

    /**
     * Checks if the segment is MT identifier tagged.
     * 
     * @since GBS-3722
     */
    public static boolean isMTTaggedSegment(String segment)
    {
        String regexLeading = Tmx2PseudoHandler.R_MT_IDENTIFIER_LEADING
                + Tmx2PseudoHandler.R_TEXT;
        String regexTrailing = Tmx2PseudoHandler.R_TEXT
                + Tmx2PseudoHandler.R_MT_IDENTIFIER_TRAILING;
        String regexBoth = Tmx2PseudoHandler.R_MT_IDENTIFIER_LEADING
                + Tmx2PseudoHandler.R_TEXT
                + Tmx2PseudoHandler.R_MT_IDENTIFIER_TRAILING;

        return segment.matches(regexLeading) || segment.matches(regexTrailing)
                || segment.matches(regexBoth);
    }

    private static int maxI(String segment)
    {
        Pattern p = Pattern.compile("<[^>]*?i=\"(\\d+)\"[^>]*?>");
        Matcher m = p.matcher(segment);
        int i = 1;
        while (m.find())
        {
            int tmp = Integer.parseInt(m.group(1));
            if (tmp > i)
            {
                i = tmp;
            }
        }
        return i;
    }

    /**
	 * After MT translation, internal text is often translated too. So try to
	 * revert them to avoid tag check failure.
	 * 
	 * @param srcGxml -- source segment with "<segment>" root tag.
	 * @param trgGxml -- target segment with "<segment>" root tag.
	 * 
	 * @return target GXML
	 */
	@SuppressWarnings("rawtypes")
	public static String fixInternalTextAfterMTTranslation(String srcGxml,
			String trgGxml)
    {
		try
		{
			HashMap<Integer, String> map = new HashMap<Integer, String>();

			// Try to find original internal text values and their "i" values.
			String iValue = null;
			boolean inInternalTag = false;
			GxmlElement srcEle = getGxmlElement(srcGxml);
			List srcInternalBpts = srcEle.getAllDescendantByAttributeValue(
					"internal", "yes", GxmlElement.BPT);
			if (srcInternalBpts != null && srcInternalBpts.size() > 0)
			{
				for (int i = 0; i < srcInternalBpts.size(); i++)
				{
					GxmlElement curElement = (GxmlElement) srcInternalBpts.get(i);
					List brothers = curElement.getParent().getChildElements();
					for (int j = 0; j < brothers.size(); j++)
					{
						GxmlElement brother = (GxmlElement) brothers.get(j);
						if (brother.equals(curElement))
						{
							inInternalTag = true;
							iValue = brother.getAttribute("i");
							continue;
						}

						if (inInternalTag
								&& brother.getType() == GxmlElement.TEXT_NODE)
						{
							map.put(Integer.parseInt(iValue),
									brother.getTextNodeValue());
							iValue = null;
							inInternalTag = false;
							break;
						}
					}
				}
			}

			// revert internal text values by "i".
			iValue = null;
			inInternalTag = false;
			GxmlElement trgEle = getGxmlElement(trgGxml);
			List trgInternalBpts = trgEle.getAllDescendantByAttributeValue(
					"internal", "yes", GxmlElement.BPT);
			if (trgInternalBpts != null && trgInternalBpts.size() > 0)
			{
				for (int i = 0; i < trgInternalBpts.size(); i++)
				{
					GxmlElement curElement = (GxmlElement) trgInternalBpts.get(i);
					List brothers = curElement.getParent().getChildElements();
					for (int j = 0; j < brothers.size(); j++)
					{
						GxmlElement brother = (GxmlElement) brothers.get(j);
						if (brother.equals(curElement))
						{
							inInternalTag = true;
							iValue = brother.getAttribute("i");
							continue;
						}

						if (inInternalTag
								&& brother.getType() == GxmlElement.TEXT_NODE)
						{
							String originalValue = map.get(Integer.parseInt(iValue));
							((TextNode) brother).setTextBuffer(
									new StringBuffer(originalValue));
							inInternalTag = false;
							iValue = null;
							break;
						}
					}
				}
			}

			return trgEle.toGxml();			
		}
		catch (Exception e)
		{
			CATEGORY.warn(e);
		}

		return trgGxml;
    }
}
