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
package com.globalsight.ling.docproc;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.globalsight.cxe.entity.filterconfiguration.InternalTextHelper;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.system.DynamicPropertiesSystemConfiguration;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.common.DiplomatNames;
import com.globalsight.ling.common.LocaleCreater;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.docproc.extractor.xliff.Extractor;
import com.globalsight.util.EmojiUtil;
import com.globalsight.util.edit.SegmentUtil2;

/**
 * <p>
 * This class counts words in the segments of a Diplomat XML input or a
 * standalone string (translatable snippet).
 * 
 * <p>
 * Input is accepted from both an <code>Output</code> object or an xml string,
 * which is automatically converted to an internal <code>Output</code> object.
 * 
 * <p>
 * Results of the counting are returned as <code>Output</code> object or string
 * representation.
 */
public class DiplomatWordCounter
{
    private SAXReader m_parser = null;
    private DiplomatReader m_diplomatReader = null;
    private Output m_output = null;
    private Locale m_locale = null;
    private boolean isCJK = false;
    private GlobalsightBreakIterator m_si = null;

    private int m_totalWordCount = 0;

    /**
     * Indicates as how many words a localizable should be counted (0 or 1). See
     * Diplomat.properties for system-wide defaults.
     */
    private int m_localizableCount = 1;

    /**
     * <p>
     * Indicates if tokens starting with digits should be counted as words.
     * </p>
     * 
     * <p>
     * Default is false. For Trados compatibility, set to true.
     * </p>
     * 
     * <p>
     * See Wordcounter.properties for system-wide defaults.
     * </p>
     */
    private boolean m_countNumerics = false;

    /**
     * <p>
     * Indicates if tokens containing dashes should be counted as multiple
     * words.
     * </p>
     * 
     * <p>
     * Default is false. For Trados compatibility, set to true.
     * </p>
     * 
     * <p>
     * See Wordcounter.properties for system-wide defaults.
     * </p>
     */
    private boolean m_countDashedTokens = false;

    private String[] m_placeHolderRegexArray = null;

    static private final String WORD_COUNTER_PROPERTY_FILE = "/properties/Wordcounter.properties";
    static private HashMap s_company_wordcounter_properties_map = new HashMap();

    /**
     * Load the wordcounter.properties and store the attributes to hashmap for
     * current company. One company only load the property file once.
     * 
     */
    private void loadWordcounterProperties()
    {
        DynamicPropertiesSystemConfiguration dpsc = (DynamicPropertiesSystemConfiguration) SystemConfiguration
                .getInstance(WORD_COUNTER_PROPERTY_FILE);
        Properties props = dpsc.getProperties();

        HashMap companyProMap = new HashMap();
        String companyId = CompanyWrapper.getCurrentCompanyId();
        String value = props.getProperty("wordcounter_count_numerics");
        Boolean countNumerics = Boolean.FALSE;
        if (value.equalsIgnoreCase("true"))
        {
            countNumerics = Boolean.TRUE;
        }
        value = props.getProperty("wordcounter_count_dashed_tokens");
        Boolean countDashedTokens = Boolean.FALSE;
        if (value.equalsIgnoreCase("true"))
        {
            countDashedTokens = Boolean.TRUE;
        }
        // Get word count place holder from properties file
        value = props.getProperty("wordcounter_count_placeholders");
        String[] placeHolderRegexArray = setPlaceHolderRegexArray(value);
        companyProMap.put("wordcounter_count_numerics", countNumerics);
        companyProMap.put("wordcounter_count_dashed_tokens", countDashedTokens);
        companyProMap.put("wordcounter_count_placeholders",
                placeHolderRegexArray);
        s_company_wordcounter_properties_map.put(companyId, companyProMap);
    }

    //
    // Constructor
    //

    // For debug purpose
    public DiplomatWordCounter(HashMap map)
    {
        s_company_wordcounter_properties_map = map;
        m_parser = createXmlParser();
    }

    public DiplomatWordCounter()
    {
        m_parser = createXmlParser();

        String companyId = CompanyWrapper.getCurrentCompanyId();
        HashMap companyProperties = (HashMap) s_company_wordcounter_properties_map
                .get(companyId);
        while (companyProperties == null)
        {
            loadWordcounterProperties();
            companyProperties = (HashMap) s_company_wordcounter_properties_map
                    .get(companyId);
        }
        m_countNumerics = ((Boolean) companyProperties
                .get("wordcounter_count_numerics")).booleanValue();
        m_countDashedTokens = ((Boolean) companyProperties
                .get("wordcounter_count_dashed_tokens")).booleanValue();
        m_placeHolderRegexArray = (String[]) companyProperties
                .get("wordcounter_count_placeholders");
    }

    public void setLocalizableWordcount(int p_arg)
    {
        m_localizableCount = p_arg;
    }

    public void setCountNumerics(boolean p_arg)
    {
        m_countNumerics = p_arg;
    }

    public void setCountDashedTokens(boolean p_arg)
    {
        m_countDashedTokens = p_arg;
    }

    /**
     * Recount the word count of the input document element
     * 
     * @param p_de
     *            input document element
     * @param p_diplomat
     *            for set m_locale
     */
    public void countDocumentElement(DocumentElement p_de, Output p_diplomat)
    {
        if (m_si == null)
        {
            m_output = p_diplomat;
            m_locale = getLocale();
            m_si = GlobalsightRuleBasedBreakIterator.getWordInstance(m_locale);
        }

        isCJK = DiplomatWordCountUtil.isCJKLocale(m_locale);

        switch (p_de.type())
        {
            case DocumentElement.TRANSLATABLE:
                TranslatableElement element = (TranslatableElement) p_de;

                int translatableWordCount = 0;

                ArrayList segments = element.getSegments();
                for (int i = 0, max = segments.size(); i < max; i++)
                {
                    SegmentNode segment = (SegmentNode) segments.get(i);

                    int segmentWordCount = countSegment(segment);

                    segment.setWordCount(segmentWordCount);
                    translatableWordCount += segmentWordCount;
                }

                element.setWordcount(translatableWordCount);
                break;

            case DocumentElement.LOCALIZABLE:
                LocalizableElement locElement = (LocalizableElement) p_de;

                // Localizables count as 1 token or 0, depending on
                // the configuration (Diplomat.properties).
                locElement.setWordcount(m_localizableCount);
                break;

            default:
                break;
        }
    }

    /**
     * <p>
     * Takes Diplomat XML inside an <code>Output</code> object and counts the
     * words in all segments.
     * 
     * <p>
     * The result can be retrieved as <code>Output</code> object (
     * <code>getOutput()</code>), or as string (<code>getDiplomatXml()</code>).
     */
    public void countDiplomatDocument(Output p_diplomat)
            throws DiplomatWordCounterException
    {
        m_totalWordCount = 0;

        m_output = p_diplomat;
        m_locale = getLocale();
        isCJK = DiplomatWordCountUtil.isCJKLocale(m_locale);

        m_si = GlobalsightRuleBasedBreakIterator.getWordInstance(m_locale);

        traverseOutput();

        m_output.setTotalWordCount(m_totalWordCount);
    }

    /**
     * Input DiplomatXml and return new DiplomatXml string with word counts.
     * 
     * @return java.lang.String
     * @param p_diplomat
     *            java.lang.String
     */
    public String countDiplomatDocument(String p_diplomat)
            throws DiplomatWordCounterException
    {
        m_totalWordCount = 0;

        convertToOutput(p_diplomat);
        m_locale = getLocale();
        isCJK = DiplomatWordCountUtil.isCJKLocale(m_locale);

        m_si = GlobalsightRuleBasedBreakIterator.getWordInstance(m_locale);

        traverseOutput();

        m_output.setTotalWordCount(m_totalWordCount);
        String result = DiplomatWriter.WriteXML(m_output);

        return result;
    }

    /**
     * Return the segmented Diplomat XML as <code>Output</code> object.
     */
    public Output getOutput()
    {
        return m_output;
    }

    /**
     * Return the segmented Diplomat XML as an XML string
     */
    public String getDiplomatXml()
    {
        return DiplomatWriter.WriteXML(m_output);
    }

    //
    // Private Methods
    //

    /**
     * Parses a Diplomat XML string into an internal <code>Output</code> object.
     */
    private void convertToOutput(String p_diplomat)
            throws DiplomatWordCounterException
    {
        m_diplomatReader = new DiplomatReader(p_diplomat);

        try
        {
            m_output = m_diplomatReader.getOutput();
        }
        catch (DiplomatReaderException e)
        {
            System.err.println(e);

            throw new DiplomatWordCounterException(
                    ExtractorExceptionConstants.WORD_COUNTER_ERROR, e);
        }
    }

    /**
     * <p>
     * Traverses the list of tags in the internal <code>Output</code> object.
     * Possible tags in the list are &lt;skeleton&gt;, &lt;translatable&gt; and
     * &lt;localizable&gt;.
     * 
     * <p>
     * Here lies the problem to attach the wordcount attribute to the segment
     * nodes, they only appear in textual form in this structure.
     */
    private void traverseOutput() throws DiplomatWordCounterException
    {
        DocumentElement de = null;

        for (Iterator it = m_output.documentElementIterator(); it.hasNext();)
        {
            de = (DocumentElement) it.next();

            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                    TranslatableElement element = (TranslatableElement) de;

                    int translatableWordCount = 0;

                    ArrayList segments = element.getSegments();
                    for (int i = 0, max = segments.size(); i < max; i++)
                    {
                        SegmentNode segment = (SegmentNode) segments.get(i);

                        int segmentWordCount = countSegment(segment);
                        // For WS XLF,if it has word count info in original
                        // source file, use it to replace that of GS.
                        segmentWordCount = getWordCountForXLFElement(element,
                                segmentWordCount);

                        segment.setWordCount(segmentWordCount);
                        translatableWordCount += segmentWordCount;
                    }

                    element.setWordcount(translatableWordCount);
                    m_totalWordCount += translatableWordCount;
                    break;

                case DocumentElement.LOCALIZABLE:
                    LocalizableElement locElement = (LocalizableElement) de;

                    // Localizables count as 1 token or 0, depending on
                    // the configuration (Diplomat.properties).
                    locElement.setWordcount(m_localizableCount);
                    m_totalWordCount += m_localizableCount;
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Get the word count for WS XLF file.
     * 
     * 1. If not XLF file, return GlobalSight word count. 2. If is XLF file,
     * current section is "source", and it has a "ws_word_count" attribute(this
     * should be a WS XLF file), use this instead of GlobalSight word count; 3.
     * If is XLF file, current section is "source", but it has no
     * "ws_word_count" attribute, use GlobalSight word count regardless if it is
     * WS file; 4. If is XLF file, but current section is NOT "source"(possible
     * be "target","altSource" or "altTarget"),return 0;
     * 
     */
    private int getWordCountForXLFElement(TranslatableElement p_element,
            int p_generalValue)
    {
        // Default value that should be returned.
        int result = p_generalValue;

        if (p_element != null && p_element.getXliffPart() != null)
        {
            Map xlfPart = p_element.getXliffPart();
            String xlfPartAtt = (String) xlfPart.get(Extractor.XLIFF_PART);

            // If it has "ws_word_count",this should be a WS XLF file;
            // And if this is "source" section, should keep the word
            // count info from original source XLF file.
            if (Extractor.XLIFF_PART_SOURCE.equalsIgnoreCase(xlfPartAtt))
            {
                if (xlfPart.get(Extractor.IWS_WORDCOUNT) != null)
                {
                    String s = (String) xlfPart.get(Extractor.IWS_WORDCOUNT);
                    try
                    {
                        result = Integer.parseInt(s);
                    }
                    catch (NumberFormatException e)
                    {
                    }
                }
            }
            else if (Extractor.XLIFF_PART_SEGSOURCE
                    .equalsIgnoreCase(xlfPartAtt))
            {
                if (xlfPart.get(Extractor.IWS_WORDCOUNT) != null)
                {
                    String s = (String) xlfPart.get(Extractor.IWS_WORDCOUNT);
                    try
                    {
                        result = Integer.parseInt(s);
                    }
                    catch (NumberFormatException e)
                    {
                    }
                }
            }
            else
            {
                result = 0;
            }
        }

        return result;
    }

    /**
     * Counts words in a segment node and the individual subflows.
     */
    private int countSegment(SegmentNode p_segment)
            throws DiplomatWordCounterException
    {
        int segmentWordCount = 0;

        try
        {
            String segment = p_segment.getSegment();
            // replace internal text and internal tags as " "
            if (segment.contains("internal=\"yes\""))
            {
                segment = removeInternalTagsForWordCounter(segment);
            }
            // GBS-3997&GBS-4066, remove emoji alias occurrences before word
            // counting
            segment = segment.replaceAll(EmojiUtil.TYPE_EMOJI + ":[^:]*?:", "");

            Document doc = parse("<AllYourBaseAreBelongToUs>" + segment
                    + "</AllYourBaseAreBelongToUs>");
            Element root = doc.getRootElement();
            segmentWordCount = countWords(root);

            // for has Sub, does not have Ph code
            String oriSegment = p_segment.getSegment();
            Document oridoc = parse("<AllYourBaseAreBelongToUs>" + oriSegment
                    + "</AllYourBaseAreBelongToUs>");
            Element oriroot = oridoc.getRootElement();
            int oriCount = countWords(root);
            boolean hasSub = countSubs(oriroot);
            boolean noPh = oriSegment != null
                    && oriSegment.indexOf("<ph") == -1;

            if (noPh || hasSub)
            {
                p_segment.setSegment(getInnerXml(oriroot));
            }
        }
        catch (Exception e)
        {
            throw new DiplomatWordCounterException(
                    ExtractorExceptionConstants.DIPLOMAT_XML_PARSE_ERROR, e);
        }

        return segmentWordCount;
    }

    /**
     * Replace internal text and internal tags with whitespace for word counter
     * 
     * @param segment
     * @return
     */
    private String removeInternalTagsForWordCounter(String segment)
    {
        List<String> internalTexts = new ArrayList<String>();
        String temp = InternalTextHelper.protectInternalTexts(segment,
                internalTexts);

        if (internalTexts.size() > 0)
        {
            for (int i = 0; i < internalTexts.size(); i++)
            {
                internalTexts.set(i, " ");
            }

            segment = InternalTextHelper.restoreInternalTexts(temp,
                    internalTexts);
        }

        return segment;
    }

    /**
     * Assigns word counts to all sub-flows in the segment.
     * 
     * @ return true if has sub.
     */
    private boolean countSubs(Element p_element)
    {
        int words;
        ArrayList elems = new ArrayList();

        findSubElements(elems, p_element);

        for (int i = 0, max = elems.size(); i < max; i++)
        {
            Element sub = (Element) elems.get(i);

            if (!isSkipElement(sub))
            {
                String subLocType = sub
                        .attributeValue(DiplomatNames.Attribute.LOCTYPE);
                if (subLocType == null
                        || subLocType
                                .equals(DiplomatNames.Element.TRANSLATABLE))
                {
                    words = countWords(sub);
                }
                else
                {
                    // Localizables count as 1 token or 0, depending on
                    // the configuration (Diplomat.properties).
                    words = m_localizableCount;
                }

                // Sub-flow word counts contribute to overall word count.
                m_totalWordCount += words;
                sub.addAttribute(DiplomatNames.Attribute.WORDCOUNT,
                        String.valueOf(words));
            }
            else
            {
                // Currently, this only affect the JavaScrpt embedded in the
                // HTML
                // Attribute.
                sub.addAttribute(DiplomatNames.Attribute.WORDCOUNT, "0");
            }
        }

        return elems.size() > 0;
    }

    private void findSubElements(ArrayList p_result, Element p_element)
    {
        // Depth-first traversal: add embedded <sub> to the list first.
        for (int i = 0, max = p_element.nodeCount(); i < max; i++)
        {
            Node child = (Node) p_element.node(i);

            if (child instanceof Element)
            {
                findSubElements(p_result, (Element) child);
            }
        }

        if (p_element.getName().equals(DiplomatNames.Element.SUB))
        {
            p_result.add(p_element);
        }
    }

    private boolean isSkipElement(Element element)
    {
        Attribute attribute = element.attribute("isSkip");
        if (attribute != null)
        {
            return "true".equals(attribute.getValue());
        }
        return false;
    }

    private int countWords(Element root)
    {
        String text = getTextWithWhite(root);
        int n = countAllWords(text);

        List sentences = SegmentUtil2.getNotTranslateWords(root.asXML());
        for (int i = 0; i < sentences.size(); i++)
        {
            String sentence = (String) sentences.get(i);
            // repalce somything like &amp;copy; to &copy;
            sentence = sentence.replace("&amp;", "&");
            sentence = sentence.replace("&lt;", "<");
            sentence = sentence.replace("&gt;", ">");
            int m = countAllWords(sentence);
            n -= m;
        }

        return n;
    }

    private Stack<String> changeArrayToStack(String[] array)
    {
        Stack<String> stack = new Stack<String>();
        if (array == null || array.length == 0)
        {
            return stack;
        }
        for (int i = 0; i < array.length; i++)
        {
            stack.push(array[i]);
        }
        return stack;
    }

    private String[] stackToArray(Stack<String> stack)
    {
        if (stack == null || stack.size() == 0)
        {
            return new String[]
            {};
        }
        String[] ss = new String[stack.size()];
        int i = 0;
        while (!stack.isEmpty())
        {
            String s = stack.pop();
            ss[i] = s;
            i++;
        }
        return ss;
    }

    private String[] spitTextByREArray(String text,
            String[] m_placeHolderRegexArray)
    {
        if (m_placeHolderRegexArray == null
                || m_placeHolderRegexArray.length == 0)
        {
            return new String[]
            { text };
        }
        Stack<String> REStack = changeArrayToStack(m_placeHolderRegexArray);
        Stack<String> processed = new Stack<String>();
        Stack<String> stored = new Stack<String>();
        processed.push(text);
        while (!REStack.isEmpty())
        {
            String re = REStack.pop();
            while (!processed.isEmpty())
            {
                String processingText = processed.pop();
                String[] splitedText = processingText.split(re);
                for (int i = 0; i < splitedText.length; i++)
                {
                    if (splitedText[i].length() > 0)
                    {
                        stored.push(splitedText[i]);
                    }
                }
            }
            while (!stored.isEmpty())
            {
                processed.push(stored.pop());
            }
        }
        return stackToArray(processed);
    }

    /**
     * The routine that actually counts the words in a string.
     */
    private int countAllWords(String p_text)
    {
        int words = 0;

        // do Locale sensitive word count breaking
        String[] text = spitTextByREArray(p_text, m_placeHolderRegexArray);
        for (int index = 0; index < text.length; index++)
        {
            p_text = text[index];
            m_si.setText(p_text);
            int iStart = m_si.first();
            for (int iEnd = m_si.next(); iEnd != GlobalsightBreakIterator.DONE; iStart = iEnd, iEnd = m_si
                    .next())
            {
                char ch = p_text.charAt(iStart);

                // Token must begin with a letter or ideogram to be
                // counted as a word.
                if (Character.isLetter(ch))
                {
                    // Trados compatibility: count dashed tokens as
                    // multiple words.
                    if (m_countDashedTokens)
                    {
                        String token = p_text.substring(iStart, iEnd);
                        words += countDashedTokens(token);
                    }
                    // Default behavior: count token as one word.
                    else
                    {
                        words++;
                    }

                    if (isCJK && DiplomatWordCountUtil.isCJKChar(ch))
                    {
                        int len = iEnd - iStart;
                        words = words + (len - 1);
                    }
                }
                // Or with a digit when Trados compatible
                else if (Character.isDigit(ch) && m_countNumerics)
                {
                    // Trados compatibility: I don't think numbers
                    // separated by dashes are counted as multiple tokens
                    // (i.e. phone numbers: 408-392-3634). But what about
                    // digits+characters as in "800-900MHz" ?
                    // For now, count as 1 word.

                    words++;
                }
                else if (m_placeHolderRegexArray != null)
                {
                    // Filt the word count place holder, do not count the word.
                    // The wordcounter place holder be configurated in
                    // wordcounter.properties
                    String matchStr = "";
                    Pattern pattern = null;
                    for (int i = 0; i < m_placeHolderRegexArray.length; i++)
                    {
                        matchStr = m_placeHolderRegexArray[i];
                        if (matchStr == null || matchStr.length() == 0)
                        {
                            continue;
                        }
                        pattern = Pattern.compile(matchStr);
                        String matcherText = p_text.substring(iStart);
                        Matcher matcher = pattern.matcher(matcherText);
                        if (matcher.find() && matcher.start() == 0)
                        {
                            p_text = matcherText.substring(matcher.end());
                            m_si.setText(p_text);
                            iEnd = m_si.first();
                            iStart = iEnd;
                            break;
                        }
                    }
                }
            }
        }

        return words;
    }

    /**
     * For Trados compatibility, this counts multiple words in tokens if the
     * words are separated by dashes ("-"), as in "over-rated".
     * 
     * No further check to the sub-tokens are made. If this method is called, it
     * counts "DDR2-533MHz" and "123-456" as 2 words.
     */
    private int countDashedTokens(String p_token)
    {
        int result = 0;

        String[] parts = p_token.split("-");

        for (int i = 0, max = parts.length; i < max; i++)
        {
            String part = parts[i];

            if (part.length() > 0)
            {
                result++;
            }
        }

        return result;
    }

    //
    // Helper Methods
    //

    private Locale getLocale()
    {
        return LocaleCreater.makeLocale(m_output.getLocale());
    }

    private SAXReader createXmlParser()
    {
        SAXReader result = new SAXReader();

        try
        {
            result.setXMLReaderClassName("org.dom4j.io.aelfred.SAXDriver");
            result.setValidation(false);
        }
        catch (Exception ex)
        {
            System.err.println("org.dom4j.io.aelfred.SAXDriver not found");

            // Use the system default parser, better than nothing.
            result = new SAXReader();
            result.setValidation(false);
        }

        return result;
    }

    private Document parse(String p_xml) throws DocumentException
    {
        return m_parser.read(new StringReader(p_xml));
    }

    /**
     * Returns the XML representation like Element.asXML() but without the
     * top-level tag.
     */
    static public String getInnerXml(Element p_node)
    {
        StringBuffer result = new StringBuffer();

        List content = p_node.content();

        for (int i = 0, max = content.size(); i < max; i++)
        {
            Node node = (Node) content.get(i);

            // Work around a specific behaviour of DOM4J text nodes:
            // The text node asXML() returns the plain Unicode string,
            // so we need to encode entities manually.
            if (node.getNodeType() == Node.TEXT_NODE)
            {
                result.append(encodeXmlEntities(node.getText()));
            }
            else
            {
                // Element nodes write their text nodes correctly.
                result.append(node.asXML());
            }
        }

        return result.toString();
    }

    static public String getTranslateInnerXml(Element p_node)
    {
        StringBuilder result = new StringBuilder();
        List content = p_node.content();
        for (int i = 0; i < content.size(); i++)
        {
            Node node = (Node) content.get(i);
            if (node.getNodeType() == Node.TEXT_NODE)
            {
                result.append(encodeXmlEntities(node.getText()));
            }
        }
        return result.toString();
    }

    /**
     * Returns the string value of an element with tags representing whitespace
     * replaced by either whitespace or nbsps.
     */
    static public String getTextWithWhite(Element p_node, boolean... bs)
    {
        StringBuffer result = new StringBuffer();

        List content = p_node.content();

        for (int i = 0, max = content.size(); i < max; i++)
        {
            Node node = (Node) content.get(i);

            if (node.getNodeType() == Node.TEXT_NODE && bs.length == 0)
            {
                boolean isInternalText = isInternalText(content, i);
                if (!isInternalText)
                {
                    result.append(node.getText());
                }
                else
                {
                    // add space around internal text
                    result.append(" ").append(node.getText()).append(" ");
                }
            }
            else if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                Element elem = (Element) node;
                String type = elem.attributeValue("type");
                int childNodes = elem.content().size();
                // For word counting, always treat TMX whitespace tags
                // as white.
                if (Text.isTmxWhitespaceNode(type)
                        || Text.isTmxMsoWhitespaceNode(type))
                {
                    result.append(" ");
                }
                else
                {
                    if (childNodes > 0)
                    {
                        boolean isExtract = false;
                        for (int j = 0; j < childNodes; j++)
                        {
                            if (((Node) elem.content().get(j)).getNodeType() == Node.ELEMENT_NODE)
                            {
                                String s = ((Element) elem.content().get(j))
                                        .attributeValue("isTranslate");
                                String innerTextNodeIndex = ((Element) elem
                                        .content().get(j))
                                        .attributeValue("innerTextNodeIndex");
                                if (s != null && Boolean.parseBoolean(s))
                                {
                                    isExtract = true;
                                    // getTextWithWhite((Element)elem.content().get(j),
                                    // true);
                                    // ((Element)elem.content().get(j)).
                                    // result.append(getTranslateInnerXml((Element)
                                    // elem.content().get(j)));
                                }
                                else
                                {
                                    isExtract = false;
                                }

                            }
                            else if (((Node) elem.content().get(j))
                                    .getNodeType() == Node.TEXT_NODE
                                    && isExtract)
                            {
                                result.append(((Node) elem.content().get(j))
                                        .getText());
                            }
                        }
                    }
                }
            }
            else
            {
                System.err.println("Please fix the word counter: " + node);
            }
        }

        return result.toString();
    }

    private static boolean isInternalText(List content, int i)
    {
        if (i == 0 || i + 1 >= content.size())
        {
            return false;
        }

        Node prenode = (Node) content.get(i - 1);
        Node nextnode = (Node) content.get(i + 1);

        if (prenode.getNodeType() != Node.ELEMENT_NODE
                || nextnode.getNodeType() != Node.ELEMENT_NODE)
        {
            return false;
        }

        Element preElem = (Element) prenode;
        Element nextElem = (Element) nextnode;

        String preelemName = preElem.getName();
        String nextelemName = nextElem.getName();
        String isInternal = preElem.attributeValue("internal");

        if ("bpt".equalsIgnoreCase(preelemName)
                && "ept".equalsIgnoreCase(nextelemName)
                && "yes".equalsIgnoreCase(isInternal))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    static public String encodeXmlEntities(String s)
    {
        if (s == null)
        {
            return s;
        }

        int len = s.length();

        if (len == 0)
        {
            return s;
        }

        StringBuffer res = new StringBuffer(len + 10);

        for (int i = 0; i < len; i++)
        {
            char c = s.charAt(i);

            switch (c)
            {
                case '<':
                    res.append("&lt;");
                    break;
                case '>':
                    res.append("&gt;");
                    break;
                case '&':
                    res.append("&amp;");
                    break;
                default:
                    res.append(c);
                    break;
            }
        }

        return res.toString();
    }

    /**
     * Convert the wordcounter placeholder Expression to regex String.
     * 
     * @param p_oriStr
     * @return Converted regex expression string
     */
    private static String convetToRegexStr(String p_oriStr)
    {
        StringBuffer newRegexStr = new StringBuffer();
        HashMap regexStrMap = new HashMap();
        regexStrMap.put("[", "\\['"); // [ --> \[
        regexStrMap.put("]", "\\]'"); // ] --> \]
        regexStrMap.put("\\", "\\\\"); // \ --> \\
        regexStrMap.put("^", "\\^"); // ^ --> \^
        regexStrMap.put("$", "\\$"); // $ --> \$
        regexStrMap.put(".", "\\."); // . --> \.
        regexStrMap.put("|", "\\|"); // | --> \|
        regexStrMap.put("?", "\\?"); // ? --> \?
        regexStrMap.put("*", "\\*"); // * --> \?
        regexStrMap.put("+", "\\+"); // + --> \+
        regexStrMap.put("(", "\\("); // ( --> \(
        regexStrMap.put(")", "\\)"); // ) --> \)
        regexStrMap.put("\"", "\\\""); // \ --> \\
        regexStrMap.put("{", "\\{"); // { --> \{
        regexStrMap.put("}", "\\}"); // } --> \}
        regexStrMap.put("{*", "\\{[^\\{]*"); // {* --> \{[^{]*
        regexStrMap.put("(*", "\\([^\\(]*"); // (* --> \([^(]*
        regexStrMap.put("[*", "\\[[^\\[]*"); // [* --> \[[^\[]*
        regexStrMap.put("*", ".*?"); // * --> .*?

        int oriStrLength = p_oriStr.length();
        for (int i = 0; i < oriStrLength; i++)
        {
            char ch = p_oriStr.charAt(i);
            String key = String.valueOf(ch);
            String value = null;
            String lastChar = p_oriStr
                    .substring(oriStrLength - 1, oriStrLength);
            if (key.equals("{") && "}".equals(lastChar)
                    && "{*".equals(p_oriStr.substring(i, i + 2)))
            {
                // key is "{*"
                key = p_oriStr.substring(i, i + 2);
                i++;
            }
            else if (key.equals("(") && ")".equals(lastChar)
                    && "(*".equals(p_oriStr.substring(i, i + 2)))
            {
                // key is "(*"
                key = p_oriStr.substring(i, i + 2);
                i++;
            }
            else if (key.equals("[") && "]".equals(lastChar)
                    && "[*".equals(p_oriStr.substring(i, i + 2)))
            {
                // key is "[*"
                key = p_oriStr.substring(i, i + 2);
                i++;
            }
            value = (String) regexStrMap.get(key);
            if (value == null)
            {
                value = key;
            }
            newRegexStr = newRegexStr.append(value);
        }
        return newRegexStr.toString();
    }

    /**
     * 
     * Generate a placeholder regex array based on the placeholder string from
     * property file
     * 
     * @param p_placeHolderStr
     * @return
     */
    private static String[] setPlaceHolderRegexArray(String p_placeHolderStr)
    {

        String[] placeHoldArray = null;
        String[] placeHolderRegexArray = null;
        if (p_placeHolderStr != null && !"".equals(p_placeHolderStr))
        {
            placeHoldArray = p_placeHolderStr.split("\n");
            String placeHolderStr = "";
            String placeHolderRegexStr = "";
            placeHolderRegexArray = new String[placeHoldArray.length];
            for (int i = 0; i < placeHoldArray.length; i++)
            {
                placeHolderStr = placeHoldArray[i];
                if (placeHolderStr != null && placeHolderStr.length() > 0)
                {
                    // placeHolderRegexStr = convetToRegexStr(placeHolderStr);
                    // placeHolderRegexArray[i] = placeHolderRegexStr;
                    placeHolderRegexArray[i] = placeHolderStr;
                }

            }
        }
        return placeHolderRegexArray;
    }

}
