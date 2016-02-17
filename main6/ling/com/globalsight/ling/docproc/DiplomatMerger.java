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

import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.cxe.adapter.idml.IdmlHelper;
import com.globalsight.cxe.entity.filterconfiguration.BaseFilter;
import com.globalsight.cxe.entity.filterconfiguration.BaseFilterManager;
import com.globalsight.cxe.entity.filterconfiguration.Escaping;
import com.globalsight.cxe.entity.filterconfiguration.EscapingHelper;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.HtmlFilter;
import com.globalsight.cxe.entity.filterconfiguration.XMLRuleFilter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.everest.page.PageTemplate;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.common.DiplomatBasicHandler;
import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.DiplomatNames;
import com.globalsight.ling.common.EncodingChecker;
import com.globalsight.ling.common.HtmlEntities;
import com.globalsight.ling.common.HtmlEscapeSequence;
import com.globalsight.ling.common.JPEscapeSequence;
import com.globalsight.ling.common.NativeEnDecoder;
import com.globalsight.ling.common.NativeEnDecoderException;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.common.XmlWriter;
import com.globalsight.ling.docproc.extractor.html.OfficeContentPostFilterHelper;
import com.globalsight.ling.docproc.extractor.xliff.WSConstants;
import com.globalsight.ling.docproc.extractor.xml.XmlFilterHelper;
import com.globalsight.ling.docproc.worldserver.WsSkeletonDispose;
import com.globalsight.machineTranslation.MTHelper;
import com.globalsight.util.EmojiUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * <p>
 * Take localized DiplomatXML and convert it to the native format. Format
 * specific encoders are called to produce the correct escape sequences in the
 * native format.
 * </p>
 * 
 * <p>
 * For example:
 * </p>
 * <ul>
 * <li>HTML: '&gt;', '&lt;', '&amp;' would be converted to &amp;gt;, &amp;lt;,
 * and &amp;amp;</li>
 * <li>Javascript " would go to \" etc..</li>
 * </ul>
 * 
 * <p>
 * Output Data is stored in a L10NContent object. Calls to this object will give
 * the native format in the correct codeset.
 * </p>
 */
public class DiplomatMerger implements DiplomatMergerImpl, DiplomatBasicHandler, IFormatNames
{
    static private final Integer s_BPT = new Integer(TmxTagGenerator.BPT);
    static private final Integer s_EPT = new Integer(TmxTagGenerator.EPT);
    static private final Integer s_IT = new Integer(TmxTagGenerator.IT);
    static private final Integer s_PH = new Integer(TmxTagGenerator.PH);
    static private final Integer s_SUB = new Integer(TmxTagGenerator.SUB);
    static private final Integer s_UT = new Integer(TmxTagGenerator.UT);

    //
    // Private member variables
    //
    private String m_diplomat = null;
    private L10nContent m_l10nContent = null;
    private Output m_output = null;
    private ExtractorRegistry m_extractorRegistry = null;
    private DiplomatReader m_diplomatReader = null;
    private boolean m_keepGsa = false;
    private DiplomatBasicParser m_diplomatParser = null;
    private Stack m_stateStack = null;
    private Stack m_tmxStateStack = null;
    private boolean m_isFromOfficeContent = false;
    private Exception m_error = null;
    private XmlEntities m_xmlEntityConverter = null;
    // private TmxTagGenerator m_tagGenerator = null;
    private Properties m_subProperties = null;
    private EncodingChecker m_encodingChecker = null;
    private CxeMessage cxeMessage = null;
    // GBS-4261
    private boolean m_addRtlDirectionality;
    private boolean m_convertHtmlEntityForHtml;
    private boolean m_convertHtmlEntityForXml;
    private long m_filterId;
    private String m_filterTableName;
    private BaseFilter m_baseFilter;
    private List<Escaping> m_escapings;
    private boolean isXmlFilterConfigured = false;
    // For entity encoding issue
    private boolean m_isCDATA = false;

    // For secondary filter:if second parser(currently it is html parser) is
    // used,
    // convert the target contents to entity '<','>','&',''' and '"'.
    private boolean isUseSecondaryFilter = false;
    // this is from HTML filter configuration
    private boolean convertHtmlEntityFromSecondFilter = false;

    private static Pattern repairOfficeXml = Pattern
            .compile("(<w:instrText[^>]*?>)(.*?)(</w:instrText>)");

    //
    // Constructors
    //

    public DiplomatMerger()
    {
        super();
        m_xmlEntityConverter = new XmlEntities();
    }

    /**
     * Intialize merger and pass in our L10NContent reference which holds the
     * newly generated localized page.
     */
    @SuppressWarnings("unchecked")
    public void init(String p_diplomat, L10nContent p_l10nContent) throws DiplomatMergerException
    {
        m_diplomat = p_diplomat;
        m_l10nContent = p_l10nContent;
        m_diplomatParser = new DiplomatBasicParser(this);
        m_stateStack = new Stack();
        m_tmxStateStack = new Stack();
        // m_tagGenerator = new TmxTagGenerator();

        m_extractorRegistry = ExtractorRegistry.getObject();

        try
        {
            m_encodingChecker = new EncodingChecker("UTF-8");
        }
        catch (UnsupportedCharsetException e)
        {
            // Shouldn't reach here. UTF-8 is predefined in Java.
        }

        // dummy state - we immediately push it off
        m_stateStack.push(new DiplomatParserState(-1, null, null));
    }

    public String getDocumentFormat()
    {
        String format = null;

        if (m_output != null)
        {
            format = m_output.getDataFormat();
        }

        if (format != null && format.length() == 0)
        {
            format = null;
        }

        return format;
    }

    public void setTargetEncoding(String targetEncoding) throws DiplomatMergerException
    {
        try
        {
            // UnicodeBig and UnicodeLittle expects BOM at the
            // beginning of the string, which we don't have at this
            // moment.
            if (targetEncoding.equals("UnicodeBig") || targetEncoding.equals("UnicodeLittle"))
            {
                targetEncoding = "UTF-8";
            }

            m_encodingChecker = new EncodingChecker(targetEncoding);
        }
        catch (UnsupportedCharsetException e)
        {
            throw new DiplomatMergerException(ExtractorExceptionConstants.INVALID_ENCODING,
                    e.toString());
        }
    }

    private String consolidateSegments(TranslatableElement p_element)
            throws DiplomatMergerException
    {
        StringBuffer result = new StringBuffer();

        ArrayList segments = p_element.getSegments();

        for (int i = 0, max = segments.size(); i < max; i++)
        {
            SegmentNode segment = (SegmentNode) segments.get(i);
            result.append(segment.getSegment());
        }

        return result.toString();
    }

/**
     * For "Entity Encoding issue", if source segment string contains special signs,
     * For CDATA:
     *     Replace the '&lt;' to char '<'
     *     Replace the '&gt;' to char '>'
     *     Replace the '&amp;' to char '&'
     *     Replace the '&apos;' to single quote '''
     *     Replace the '&quot;' to double quote '"'
     * For Node:
     *     Replace the '&apos;' to single quote '''
     *     Replace the '&quot;' to double quote '"'
     *     Replace the '&gt;' to char '>'
     */
    private String entityEncode(String sourceSeg)
    {
        String targetSeg = sourceSeg;

        if (m_isCDATA)
        {
            targetSeg = convertHtmlEntityForXml(targetSeg, m_convertHtmlEntityForXml);
        }
        else if ((isXmlFilterConfigured && !m_convertHtmlEntityForXml) || !isXmlFilterConfigured)
        {
            targetSeg = targetSeg.replaceAll("&apos;", "\'");
            targetSeg = targetSeg.replaceAll("&quot;", "\"");
        }

        return targetSeg;
    }

    @SuppressWarnings("unchecked")
    private String decoding(String s)
    {
        HashMap<String, Character> map = new HashMap<String, Character>();
        map.putAll(HtmlEntities.mHtmlEntityToChar);
        map.putAll(HtmlEntities.mDefaultEntityToChar);
        map.remove("&nbsp");
        map.remove("&nbsp;");
        for (String key : map.keySet())
        {
            String value = map.get(key).toString();
            s = s.replace(key, value);
        }

        return s;
    }

    @SuppressWarnings("unchecked")
    private String encoding(String s, boolean encodeAllHtmlEntities)
    {
        HashMap<Character, String> map = new HashMap<Character, String>();
        map.putAll(HtmlEntities.mDefaultCharToEntity);
        if (encodeAllHtmlEntities)
        {
            map.putAll(HtmlEntities.mHtmlCharToEntity);
        }
        map.remove(new Character('&'));
        s = s.replace("&", "&amp;");

        for (Character key : map.keySet())
        {
            String value = map.get(key);
            s = s.replace(key.toString(), value);
        }

        return s;
    }

    private String convertHtmlEntityForXml(String s, boolean convertHtmlEntityForXml)
    {
        if (s == null || s.length() == 0)
            return s;

        s = decoding(s);
        s = decoding(s);

        if (convertHtmlEntityForXml)
        {
            s = encoding(s, convertHtmlEntityForXml);
        }

        return s;
    }

    private String entityDecodeForXliff(String sourceSeg)
    {
        String targetSeg = sourceSeg;
        targetSeg = targetSeg.replaceAll("&amp;", "_ampFlag_");
        targetSeg = targetSeg.replaceAll("&quot;", "_quotFlag_");
        targetSeg = targetSeg.replaceAll("&apos;", "_aposFlag_");
        targetSeg = targetSeg.replaceAll("&#xd;", "_xdFlag_");
        targetSeg = targetSeg.replaceAll("&#x9;", "_x9Flag_");
        targetSeg = targetSeg.replaceAll("&#xa;", "_xaFlag_");
        targetSeg = targetSeg.replaceAll("&", "&amp;");
        targetSeg = targetSeg.replaceAll("<", "&lt;");
        targetSeg = targetSeg.replaceAll(">", "&gt;");
        targetSeg = targetSeg.replaceAll("_ampFlag_", "&amp;");
        targetSeg = targetSeg.replaceAll("_quotFlag_", "&quot;");
        targetSeg = targetSeg.replaceAll("_aposFlag_", "&apos;");
        targetSeg = targetSeg.replaceAll("_xdFlag_", "&#xd;");
        targetSeg = targetSeg.replaceAll("_x9Flag_", "&#x9;");
        targetSeg = targetSeg.replaceAll("_xaFlag_", "&#xa;");

        return targetSeg;
    }

    /**
     * For "Entity Encoding issue" If the attribute of element contains special
     * sign(e.g. '&apos;'), Then Replace the '&apos;' to single quote ''' Else
     * Do nothing.
     */
    private String entityEncodeForSkeleton(String sourceSeg, boolean isXliff)
    {
        String targetSeg = sourceSeg;

        if (!isXliff)
        {
            targetSeg = targetSeg.replaceAll("&apos;", "\'");
        }

        // Judge to set value for "m_isCDATA".
        isInCDATA(targetSeg);

        return targetSeg;
    }

    private String entityEncodeForSkeleton(String sourceSeg)
    {
        return entityEncodeForSkeleton(sourceSeg, false);
    }

    private void isInCDATA(String documentText)
    {
        int beginIndex = documentText.lastIndexOf("<![CDATA[");
        int endIndex = documentText.lastIndexOf("]]>");

        if (m_isCDATA && endIndex >= 0 && beginIndex < endIndex)
        {
            m_isCDATA = false;
        }

        // Mark the CDATA element for "entityEncode(String sourceSeg)"
        if (beginIndex >= 0 && beginIndex > endIndex)
        {
            m_isCDATA = true;
        }
    }

    /**
     * Adds {@code <span dir="rtl">} if the paragraph is text in HTML and it
     * contains bidi characters.
     */
    private String addSpanRtl(String p_para)
    {
        DiplomatParserState state = (DiplomatParserState) m_stateStack.peek();

        String type = state.getType();
        if (type == null)
        {
            type = "text";
        }
        String originalFormat = m_output.getDataFormat();

        CxeMessageType msgType = (cxeMessage == null ? CxeMessageType
                .getCxeMessageType(CxeMessageType.HTML_LOCALIZED_EVENT) : cxeMessage
                .getMessageType());
        CxeMessageType xmlMsgType = CxeMessageType
                .getCxeMessageType(CxeMessageType.XML_LOCALIZED_EVENT);

        // This is only for HTML format, original source format is HTML.
        if (type.equals("text") && msgType.getValue() != xmlMsgType.getValue()
                && FORMAT_HTML.equals(originalFormat) && m_addRtlDirectionality
                && Text.containsBidiChar(p_para))
        {
            // When changing this string, also update the regexp in
            // merger/html/HtmlPostMergeProcessor.
            p_para = "<ph>&lt;span dir=\"rtl\"&gt;</ph>" + p_para + "<ph>&lt;/span&gt;</ph>";
        }

        return p_para;
    }

    private String applyNativeEncoding(String p_text, String p_mainFormat, String p_format,
            String p_type, char p_lastOutputChar) throws DiplomatMergerException
    {
        // Convert PUA characters back to original C0 control codes
        // before it's converted NCR or any other escaped form
        String newText = CtrlCharConverter.convertToCtrl(p_text);

        NativeEnDecoder encoder = FORMAT_PLAINTEXT.equals(p_mainFormat) ? getEncoder(p_mainFormat)
                : getEncoder(p_format);
        encoder.setLastChar(String.valueOf(p_lastOutputChar));

        String type = p_type;
        if (type == null)
        {
            type = "text";
        }

        try
        {
            // System.err.println("type=" + type + " format=" + p_format +
            // " quote=" + p_lastOutputChar + " text=" + p_text);

            // TODO: implement encoders for Javascript, HTML.
            // If a CFScript string is to be encoded, and the quote
            // that surrounds the string is known, send it to the
            // encode() method.
            if (type.equals("string") && p_format.equals(FORMAT_CFSCRIPT)
                    && (p_lastOutputChar == '"' || p_lastOutputChar == '\''))
            {
                newText = encoder
                        .encodeWithEncodingCheck(newText, String.valueOf(p_lastOutputChar));
            }
            // For GBS-3795. Don't change "OxA0" to &nbsp; It is not xml entity.
            // GBS-3906, do not change "\u00a0" to &nbsp;
            else if ((IFormatNames.FORMAT_XML.equalsIgnoreCase(p_mainFormat) || IFormatNames.FORMAT_JAVAPROP
                    .equalsIgnoreCase(p_mainFormat)) && encoder instanceof HtmlEscapeSequence)
            {
                HtmlEscapeSequence htmlEscapeSequence = (HtmlEscapeSequence) encoder;
                newText = htmlEscapeSequence.encodeWithEncodingCheck(newText, true, new char[] {});
            }
            else
            {
                if (IFormatNames.FORMAT_JAVAPROP.equalsIgnoreCase(p_mainFormat)
                        && encoder instanceof JPEscapeSequence)
                {
                    ((JPEscapeSequence) encoder).setIsJavaProperty(true);
                }

                newText = encoder.encodeWithEncodingCheck(newText);
            }
        }
        catch (NativeEnDecoderException e)
        {
            throw new DiplomatMergerException(ExtractorExceptionConstants.INTERNAL_ERROR, e);
        }

        // May have to check item type: if it's any URL, need to call
        // URLencode on the part after the first "?".
        // This is wrong, but I leave it in here for reference.

        // if (p_type != null && p_type.startsWith("url-"))
        // {
        // int index;
        // if ((index = newText.indexOf('?')) >= 0)
        // {
        // ++index;
        //
        // newText = newText.substring(0,index) +
        // URLEncoder.encode(newText.substring(index));
        // }
        // }

        return newText;
    }

    private String applyNativeEncodingForSkeleton(String p_text, NativeEnDecoder p_encoder)
            throws DiplomatMergerException
    {
        // Convert PUA characters back to original C0 control codes
        // before it's converted NCR or any other escaped form
        String newText = CtrlCharConverter.convertToCtrl(p_text);

        try
        {
            newText = p_encoder.encodeWithEncodingCheckForSkeleton(newText);
        }
        catch (NativeEnDecoderException e)
        {
            throw new DiplomatMergerException(ExtractorExceptionConstants.INTERNAL_ERROR, e);
        }
        return newText;
    }

    private void convertToOutput() throws DiplomatMergerException
    {
        m_diplomatReader = new DiplomatReader(m_diplomat);

        try
        {
            m_output = m_diplomatReader.getOutput();
        }
        catch (DiplomatReaderException e)
        {
            throw new DiplomatMergerException(ExtractorExceptionConstants.INTERNAL_ERROR, e);
        }
    }

    /**
     * Required by DiplomatBasicHandler. Throw away all Diplomat end tags.
     */
    public void handleEndTag(String p_name, String p_originalTag)
    {
        if (!m_tmxStateStack.isEmpty())
        {
            int state = ((Integer) m_tmxStateStack.pop()).intValue();
            switch (state)
            {
                case TmxTagGenerator.SUB:
                    m_subProperties = null;
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Required by DiplomatBasicHandler. Throw away all Diplomat start tags.
     */
    @SuppressWarnings("unchecked")
    public void handleStartTag(String p_name, Properties p_attributes, String p_originalString)
    {
        if (p_originalString.contains(OfficeContentPostFilterHelper.IS_FROM_OFFICE_CONTENT))
        {
            m_isFromOfficeContent = true;
        }
        else
        {
            m_isFromOfficeContent = false;
        }
        if (p_name.equals(DiplomatNames.Element.BPT))
        {
            m_tmxStateStack.push(s_BPT);
        }
        else if (p_name.equals(DiplomatNames.Element.EPT))
        {
            m_tmxStateStack.push(s_EPT);
        }
        else if (p_name.equals(DiplomatNames.Element.IT))
        {
            m_tmxStateStack.push(s_IT);
        }
        else if (p_name.equals(DiplomatNames.Element.PH))
        {
            m_tmxStateStack.push(s_PH);
        }
        else if (p_name.equals(DiplomatNames.Element.SUB))
        {
            m_tmxStateStack.push(s_SUB);
            m_subProperties = p_attributes;
        }
        else if (p_name.equals(DiplomatNames.Element.UT))
        {
            m_tmxStateStack.push(s_UT);
        }
        else if ("removeTu".equals(p_name))
        {
            StringBuffer s = m_l10nContent.m_l10nContent;
            if (s.charAt(s.length() - 1) == '>')
            {
                s = s.replace(s.length() - 1, s.length(), " removed='true'>");
                m_l10nContent.m_l10nContent = s;
            }
        }
        else
        // something we didn't expect
        {
            m_error = new Exception("DiplomatMerger: Illegal TMX tag name");
        }
    }

    /**
     * This is called while parsing diplomat snippet in <translatable> and
     * <localizable> element.
     * 
     * Required by DiplomatBasicHandler. Strip all Diplomat Tags. Call native
     * encoders. Unencode basic XML char entities.
     */
    public void handleText(String p_text)
    {
        DiplomatParserState state = (DiplomatParserState) m_stateStack.peek();
        String type = state.getType();
        String format = null;
        String mainFormat = m_output.getDataFormat();

        // non-subflow context
        if (state.getFormat() != null)
        {
            format = state.getFormat(); // sub format
        }
        else
        // default extractor format
        {
            format = m_output.getDataFormat();

            if (format == null || format.length() == 0)
            {
                m_error = new Exception("DiplomatMerger: missing format from output");
                return;
            }
        }

        if (mainFormat == null || mainFormat.length() == 0)
        {
            mainFormat = format;
        }

        // subflow context
        if (m_subProperties != null)
        {
            String subFormat = m_subProperties.getProperty(DiplomatNames.Attribute.DATATYPE, null);

            if (subFormat != null && subFormat.length() > 0)
            {
                format = subFormat;
            }
        }

        String tmp = decode(p_text);

        try
        {
            if (!isContent() && m_isFromOfficeContent)
            {
                // For GBS-2073, for a tag in office embedded content like
                // &lt;span&gt; after being decoded to <span>,
                // need additional encode back to &lt;span&gt; to ensure
                // the tag <span> is kept after converter's conversion.
                tmp = encode(tmp);
            }

            if (isContent() && !FORMAT_XLIFF.equals(format) && !FORMAT_XLIFF20.equals(format)
                    && !FORMAT_PO.equals(mainFormat) && !FORMAT_HTML.equals(mainFormat))
            {
                // Do not encode CDATA content from XML and passing HTML.
                if (!(ExtractorRegistry.FORMAT_XML.equalsIgnoreCase(mainFormat)
                        && ExtractorRegistry.FORMAT_HTML.equalsIgnoreCase(format) && m_isCDATA))
                {
                    tmp = applyNativeEncoding(tmp, mainFormat, format, type,
                            m_l10nContent.getLastChar());
                }
            }

            // For "Entity Encode issue"
            if (ExtractorRegistry.FORMAT_HTML.equalsIgnoreCase(format)
                    && m_convertHtmlEntityForHtml)
            {
                if (isContent())
                {
                    tmp = decode(tmp);
                }
            }

            if (ExtractorRegistry.FORMAT_XML.equalsIgnoreCase(mainFormat)
                    && ExtractorRegistry.FORMAT_HTML.equalsIgnoreCase(format) && m_isCDATA)
            {
                if (isContent())
                {
                    tmp = convertHtmlEntityForXml(tmp, m_convertHtmlEntityForXml);
                }
            }

            if (ExtractorRegistry.FORMAT_XML.equalsIgnoreCase(mainFormat)
                    && ExtractorRegistry.FORMAT_HTML.equalsIgnoreCase(format))
            {
                if ((isXmlFilterConfigured && !m_convertHtmlEntityForXml) || !isXmlFilterConfigured)
                {
                    tmp = tmp.replaceAll("&apos;", "\'");
                    tmp = tmp.replaceAll("&quot;", "\"");
                }

                if (m_isCDATA && isXmlFilterConfigured && m_convertHtmlEntityForXml)
                {
                    tmp = encode(tmp);
                    tmp = tmp.replace("&amp;amp;nbsp;", "&amp;nbsp;");
                }
            }

            // Always encode basic HTML entities regardless of setting.
            if (isContent() && ExtractorRegistry.FORMAT_HTML.equalsIgnoreCase(mainFormat)
                    && ExtractorRegistry.FORMAT_HTML.equalsIgnoreCase(format))
            {
                tmp = encoding(tmp, false);
                // we do not expect double encoded < and >.
                tmp = StringUtil.replace(tmp, "&amp;lt;", "&lt;");
                tmp = StringUtil.replace(tmp, "&amp;gt;", "&gt;");
                // GBS-3805: revert ", ' and & back.
                if (!m_convertHtmlEntityForHtml)
                {
                    tmp = StringUtil.replace(tmp, "&amp;", "&");
                    tmp = StringUtil.replace(tmp, "&quot;", "\"");
                    tmp = StringUtil.replace(tmp, "&apos;", "'");
                    tmp = StringUtil.replace(tmp, "&#39;", "'");
                }
            }

            if (ExtractorRegistry.FORMAT_XML.equalsIgnoreCase(format))
            {
                if (isContent())
                {
                    tmp = entityEncode(tmp);
                }
                else
                {
                    tmp = entityEncodeForSkeleton(tmp);
                }
            }
            else if (FORMAT_XLIFF.equalsIgnoreCase(format)
                    || FORMAT_XLIFF20.equalsIgnoreCase(format))
            {
                if (isContent())
                {
                    tmp = entityDecodeForXliff(tmp);
                }
            }
            else if (ExtractorRegistry.FORMAT_OFFICE_XML.equalsIgnoreCase(format))
            {
                if (!isContent())
                {
                    tmp = repair(tmp);
                }
            }

            // GBS-3596
            if (ExtractorRegistry.FORMAT_PO.equalsIgnoreCase(mainFormat)
                    && ExtractorRegistry.FORMAT_HTML.equalsIgnoreCase(format))
            {
                if (isContent())
                {
                    tmp = fixPoStr(tmp);
                }
                else
                {
                    tmp = EscapingHelper.handleString4Export(tmp, m_escapings,
                            ExtractorRegistry.FORMAT_PO, false, false, null);
                }
            }

            // Only for JavaProperties file
            if (ExtractorRegistry.FORMAT_JAVAPROP.equalsIgnoreCase(mainFormat)
                    && this.isUseSecondaryFilter)
            {
                // GBS-3906
                tmp = StringUtil.replace(tmp, "&amp;", "&");
                tmp = StringUtil.replace(tmp, "&quot;", "\"");
                if (this.convertHtmlEntityFromSecondFilter)
                {
                    char[] specXmlEncodeChar =
                    { '<', '>', '&', '"', '\'' };
                    tmp = XmlFilterHelper.encodeSpecifiedEntities(tmp, specXmlEncodeChar);
                }
            }
            // GBS-3997&GBS-4066
            tmp = EmojiUtil.parseEmojiAliasToUnicode(tmp);

            m_l10nContent.addContent(tmp);
        }
        catch (DiplomatMergerException e)
        {
            m_error = e;
        }
    }

    // For GBS-2521.
    private String repair(String s)
    {
        Matcher m = repairOfficeXml.matcher(s);

        while (m.find())
        {
            String content = m.group(2);

            content = decode(content);
            content = encode(content);

            s = s.replace(m.group(), m.group(1) + content + m.group(3));
        }

        return s;
    }

    /**
     * For PO content, "\'" and "\"" can be in 2 styles: Style one: \', \" Style
     * two: &apos; , &quot; So, if there is "\", keep unchanged; for single ",
     * encode it; for single ', keep unchanged, always output character style '
     * or \'.
     * 
     * @param s
     * @return
     */
    private String fixPoStr(String s)
    {
        // protect \' and \"
        s = s.replace("\\\'", "_LeftSlashApos_");
        s = s.replace("\\\"", "_LeftSlashQuot_");
        // encode single ' and ""
        // s = s.replace("\'", "&apos;");
        s = s.replace("\"", "&quot;");
        // revert \' and \"
        s = s.replace("_LeftSlashApos_", "\\\'");
        s = s.replace("_LeftSlashQuot_", "\\\"");

        return s;
    }

    private boolean isKeepGsa()
    {
        return m_keepGsa;
    }

    /**
     * Cycle through each Output node and handle as appropriate.
     * 
     * @throws
     */
    @SuppressWarnings("unchecked")
    public void merge()
    {
        DocumentElement de = null;

        convertToOutput();
        applyFilterConfiguration();
        // This method must be called after convertToOutput()
        NativeEnDecoder encoderForSkeleton = getEncoder(getDocumentFormat());

        String srcDataType = m_output.getDiplomatAttribute().getDataType();
        boolean isTuvLocalized = false;
        String localizedBy = new String();
        boolean isInCDATA = false;
        for (Iterator it = m_output.documentElementIterator(); it.hasNext();)
        {
            de = (DocumentElement) it.next();

            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                    m_stateStack.push(new DiplomatParserState(de.type(), ((TranslatableElement) de)
                            .getDataType(), ((TranslatableElement) de).getType()));

                    if (((TranslatableElement) de).getIsLocalized() != null)
                    {
                        if (((TranslatableElement) de).getIsLocalized().equals(PageTemplate.byMT))
                        {
                            isTuvLocalized = true;
                            localizedBy = PageTemplate.byMT;
                        }
                        else if (((TranslatableElement) de).getIsLocalized().equals(
                                PageTemplate.byUser))
                        {
                            isTuvLocalized = true;
                            localizedBy = PageTemplate.byUser;
                        }
                        else if ((((TranslatableElement) de).getIsLocalized()
                                .equals(PageTemplate.byLocalTM)))
                        {
                            isTuvLocalized = true;
                            localizedBy = PageTemplate.byLocalTM;
                        }
                        else
                        {
                            isTuvLocalized = false;
                        }
                    }
                    else
                    {
                        isTuvLocalized = false;
                    }

                    String chunk = null;
                    if (((TranslatableElement) de).hasSegments())
                    {
                        chunk = consolidateSegments(((TranslatableElement) de));
                    }
                    else
                    {
                        chunk = ((TranslatableElement) de).getChunk();
                    }

                    if (chunk.contains(OfficeContentPostFilterHelper.IS_FROM_OFFICE_CONTENT))
                    {
                        m_isFromOfficeContent = true;
                    }
                    else
                    {
                        m_isFromOfficeContent = false;
                    }
                    if (IFormatNames.FORMAT_XML.equals(srcDataType))
                    {
                        if (chunk.contains("&lt;GS-IDML-LF/&gt;"))
                        {
                            // change MARK_LF_IDML back to LINE_BREAK during
                            // export
                            chunk = StringUtil.replace(chunk, "&lt;GS-IDML-LF/&gt;",
                                    IdmlHelper.LINE_BREAK);
                        }
                        if (chunk.contains("&lt;GS-IDML-LineBreak/&gt;"))
                        {
                            // change MARK_LF_IDML back to LINE_BREAK during
                            // export
                            chunk = StringUtil.replace(chunk, "&lt;GS-IDML-LineBreak/&gt;",
                                    IdmlHelper.LINE_BREAK);
                        }
                    }
                    // GBS-3997&GBS-4066
                    chunk = EmojiUtil.parseEmojiTagToAlias(chunk);

                    // GBS-3722
                    chunk = MTHelper.cleanMTTagsForExport(chunk);
                    String escapingChars = null;

                    if (((TranslatableElement) de).getEscapingChars() != null)
                    {
                        escapingChars = EditUtil.decodeXmlEntities(((TranslatableElement) de)
                                .getEscapingChars());
                    }

                    String newchunk = EscapingHelper.handleString4Export(chunk, m_escapings,
                            srcDataType, false, true, escapingChars, isInCDATA);

                    parseDiplomatSnippet(addSpanRtl(newchunk));
                    m_stateStack.pop();
                    break;

                case DocumentElement.LOCALIZABLE:
                    m_stateStack.push(new DiplomatParserState(de.type(), ((LocalizableElement) de)
                            .getDataType(), ((LocalizableElement) de).getType()));
                    parseDiplomatSnippet(((LocalizableElement) de).getChunk());
                    m_stateStack.pop();
                    break;

                case DocumentElement.SKELETON:
                    m_stateStack.push(new DiplomatParserState(de.type(), null, null));

                    String tmp = decode(((SkeletonElement) de).getSkeleton());
                    if (tmp.indexOf("<![CDATA[") > -1 && tmp.indexOf("]]") == -1)
                    {
                        isInCDATA = true;
                    }
                    if (isInCDATA && tmp.indexOf("]]") > -1)
                    {
                        isInCDATA = false;
                    }
                    if (tmp.indexOf("<![CDATA[") > -1 && tmp.indexOf("]]") > -1)
                    {
                        isInCDATA = tmp.indexOf("<![CDATA[") > tmp.indexOf("]]");
                    }

                    if (OfficeContentPostFilterHelper.isOfficeFormat(srcDataType))
                    {
                        tmp = entityEncodeForOfficeConversion(tmp);
                    }
                    tmp = applyNativeEncodingForSkeleton(tmp, encoderForSkeleton);
                    if (FORMAT_XLIFF.equals(srcDataType) || FORMAT_XLIFF_NAME.equals(srcDataType)
                            || FORMAT_XLIFF20.equals(srcDataType)
                            || IFormatNames.FORMAT_PO.equals(srcDataType))
                    {
                        tmp = entityEncodeForSkeleton(tmp, true);
                    }
                    else
                    {
                        tmp = entityEncodeForSkeleton(tmp);
                    }

                    SkeletonDispose sd;

                    if (isTuvLocalized && tmp.indexOf("<" + WSConstants.IWS_SEGMENT_DATA) > 0)
                    {
                        sd = new WsSkeletonDispose();
                        tmp = sd.dealSkeleton(tmp, localizedBy);
                    }

                    if (IFormatNames.FORMAT_PASSOLO.equals(srcDataType))
                    {
                        tmp = entityEncodeForPassolo(tmp);
                    }

                    // For GBS-2955, idml/indd is actually xml format
                    if (IFormatNames.FORMAT_XML.equals(srcDataType))
                    {
                        if (tmp.contains(IdmlHelper.MARK_LF_IDML))
                        {
                            // change MARK_LF_IDML back to LINE_BREAK during
                            // export
                            tmp = StringUtil.replace(tmp, IdmlHelper.MARK_LF_IDML,
                                    IdmlHelper.LINE_BREAK);
                        }
                        if (tmp.contains(IdmlHelper.MARK_LineBreak_IDML))
                        {
                            // change MARK_LF_IDML back to LINE_BREAK during
                            // export
                            tmp = StringUtil.replace(tmp, IdmlHelper.MARK_LineBreak_IDML,
                                    IdmlHelper.LINE_BREAK);
                        }
                    }
                    // GBS-3997&GBS-4066
                    tmp = EmojiUtil.parseEmojiAliasToUnicode(tmp);

                    m_l10nContent.addContent(tmp);

                    m_stateStack.pop();
                    break;

                case DocumentElement.GSA_START:
                    if (isKeepGsa())
                    {
                        m_stateStack.push(new DiplomatParserState(de.type(), null, null));
                        // Need an XmlWriter that doesn't write out
                        // additional newlines (GSDEF00010104).
                        XmlWriter xmlW = new XmlWriter(false);
                        xmlW.setIndentLevel(0);
                        DiplomatAttribute da = new DiplomatAttribute();
                        ((GsaStartElement) de).toDiplomatString(da, xmlW);
                        m_l10nContent.addContent(xmlW.getXml());
                        m_stateStack.pop();
                    }
                    break;

                case DocumentElement.GSA_END:
                    if (isKeepGsa())
                    {
                        m_stateStack.push(new DiplomatParserState(de.type(), null, null));
                        m_l10nContent.addContent("</gs>");
                        m_stateStack.pop();
                    }
                    break;

                default:
                    break;
            }

            // check for handleText error
            if (m_error != null)
            {
                throw new DiplomatMergerException(
                        ExtractorExceptionConstants.DIPLOMAT_MERGER_FATAL_ERROR, m_error);
            }
        }
    }

    private String entityEncodeForPassolo(String skeleton)
    {
        XmlEntities xe = new XmlEntities();

        Pattern p = Pattern.compile("(<source[^>]*>)([\\s\\S]*?)(</source>)");
        Matcher m = p.matcher(skeleton);
        while (m.find())
        {
            String all = m.group();
            String s1 = m.group(1);
            String s2 = m.group(2);
            String s3 = m.group(3);

            String s2temp = xe.encodeStringBasic(s2);

            skeleton = skeleton.replace(all, s1 + s2temp + s3);
        }

        p = Pattern.compile("(<target[^>]*>)([\\s\\S]*?)(</target>)");
        m = p.matcher(skeleton);
        while (m.find())
        {
            String all = m.group();
            String s1 = m.group(1);
            String s2 = m.group(2);
            String s3 = m.group(3);

            String s2temp = xe.encodeStringBasic(s2);

            skeleton = skeleton.replace(all, s1 + s2temp + s3);
        }

        return skeleton;
    }

    /**
     * Encodes the tag that is from office embedded content to entities so that
     * after converter's conversion the tag will not be missing in the office
     * document.
     */
    private String entityEncodeForOfficeConversion(String skeleton)
    {
        String start = OfficeContentPostFilterHelper.SKELETON_OFFICE_CONTENT_START;
        String end = OfficeContentPostFilterHelper.SKELETON_OFFICE_CONTENT_END;
        while (skeleton.indexOf(start) > -1 || skeleton.indexOf(end) > -1)
        {
            int startIndex = skeleton.indexOf(start);
            int endIndex = skeleton.indexOf(end);
            String tag = "";
            if (startIndex > -1 && startIndex < endIndex)
            {
                // XXXXSKELETON-GS-OFFICE-CONTENT-START<TAG>SKELETON-GS-OFFICE-CONTENT-ENDXXXX
                tag = skeleton.substring(startIndex, endIndex + end.length());
                tag = encode(tag);
                tag = tag.replace(start, "").replace(end, "");
                skeleton = skeleton.substring(0, startIndex) + tag
                        + skeleton.substring(endIndex + end.length());
            }
            else if (endIndex < 0)
            {
                // XXXXSKELETON-GS-OFFICE-CONTENT-START<TAG
                tag = skeleton.substring(startIndex);
                tag = encode(tag);
                tag = tag.replace(start, "");
                skeleton = skeleton.substring(0, startIndex) + tag;
            }
            else if ((endIndex > -1 && endIndex < startIndex) || startIndex < 0)
            {
                // TAG>SKELETON-GS-OFFICE-CONTENT-ENDXXXX
                tag = skeleton.substring(0, endIndex + end.length());
                tag = encode(tag);
                tag = tag.replace(end, "");
                skeleton = tag + skeleton.substring(endIndex + end.length());
            }
        }

        return skeleton;
    }

    private void applyFilterConfiguration()
    {
        // set m_convertHtmlEntity
        if (m_filterId > 0)
        {
            boolean getValueOfconvertHtmlEntity = false;
            String format = getDocumentFormat();
            boolean isXmlFormat = ExtractorRegistry.FORMAT_XML.equalsIgnoreCase(format);
            boolean isHtmlFormat = ExtractorRegistry.FORMAT_HTML.equalsIgnoreCase(format);

            if (isHtmlFormat)
            {
                HtmlFilter htmlFilter = FilterHelper.getHtmlFilter(m_filterId);
                if (htmlFilter != null)
                {
                    m_convertHtmlEntityForHtml = htmlFilter.isConvertHtmlEntry();
                    m_addRtlDirectionality = htmlFilter.isAddRtlDirectionality();
                    getValueOfconvertHtmlEntity = true;
                }
            }

            if (isXmlFormat)
            {
                XMLRuleFilter xmlFilter = FilterHelper.getXmlFilter(m_filterId);
                if (xmlFilter != null)
                {
                    m_convertHtmlEntityForXml = xmlFilter.isConvertHtmlEntity();
                    getValueOfconvertHtmlEntity = true;
                    isXmlFilterConfigured = true;
                }
            }

            if (!getValueOfconvertHtmlEntity)
            {
                SystemConfiguration tagsProperties = SystemConfiguration
                        .getInstance("/properties/Tags.properties");
                String convertHtml = tagsProperties.getStringParameter("convertHtmlEntity");
                if (convertHtml != null)
                {
                    if (isHtmlFormat)
                    {
                        m_convertHtmlEntityForHtml = Boolean.valueOf(convertHtml.trim());
                    }
                    else if (isXmlFormat)
                    {
                        m_convertHtmlEntityForXml = Boolean.valueOf(convertHtml.trim());
                    }
                }
                else
                {
                    m_convertHtmlEntityForHtml = false;
                    m_convertHtmlEntityForXml = false;
                }
            }

            m_baseFilter = BaseFilterManager.getBaseFilterByMapping(m_filterId, m_filterTableName);
            try
            {
                m_escapings = BaseFilterManager.getEscapings(m_baseFilter);
            }
            catch (Exception e)
            {
                m_escapings = null;
            }
        }
    }

    private void parseDiplomatSnippet(String p_diplomat) throws DiplomatMergerException
    {
        try
        {
            m_diplomatParser.setCxeMessage(cxeMessage);
            m_diplomatParser.parse(p_diplomat);
        }
        catch (DiplomatBasicParserException e)
        {
            throw new DiplomatMergerException(
                    ExtractorExceptionConstants.DIPLOMAT_BASIC_PARSER_EXCEPTION, e);
        }
    }

    /**
     * Set to true to keep our internal GS tags. Default = false (strip GS
     * tags).
     */
    public void setKeepGsa(boolean p_keepGsa)
    {
        m_keepGsa = p_keepGsa;
    }

    private String decode(String p_xml)
    {
        return m_xmlEntityConverter.decodeStringBasic(p_xml);
    }

    private String encode(String p_xml)
    {
        return m_xmlEntityConverter.encodeStringBasic(p_xml);
    }

    public void handleStart()
    {
    }

    public void handleStop()
    {
    }

    private boolean isContent()
    {
        Object state = null;
        int tag = -1;
        boolean content = false;

        try
        {
            state = m_tmxStateStack.peek();
        }
        catch (EmptyStackException e)
        {
            return true;
        }

        tag = ((Integer) state).intValue();
        switch (tag)
        {
            case TmxTagGenerator.BPT:
            case TmxTagGenerator.EPT:
            case TmxTagGenerator.IT:
            case TmxTagGenerator.PH:
            case TmxTagGenerator.UT:
                content = false;
                break;

            case TmxTagGenerator.SUB:
                content = true;
                break;
        }

        return content;
    }

    private NativeEnDecoder makeEncoder(String p_path) throws DiplomatMergerException
    {
        NativeEnDecoder decoder = null;

        try
        {
            decoder = (NativeEnDecoder) Class.forName(p_path).newInstance();
        }
        catch (ClassNotFoundException e)
        {
            throw new DiplomatMergerException(ExtractorExceptionConstants.INTERNAL_ERROR, e);
        }
        catch (InstantiationException e)
        {
            throw new DiplomatMergerException(ExtractorExceptionConstants.INTERNAL_ERROR, e);
        }
        catch (IllegalAccessException e)
        {
            throw new DiplomatMergerException(ExtractorExceptionConstants.INTERNAL_ERROR, e);
        }

        return decoder;
    }

    private NativeEnDecoder getEncoder(String p_format) throws DiplomatMergerException
    {
        int format = -1;

        if ((format = m_extractorRegistry.getFormatId(p_format)) == -1)
        {
            throw new DiplomatMergerException(ExtractorExceptionConstants.FORMAT_NOT_REGISTERED);
        }

        NativeEnDecoder encoder = makeEncoder(m_extractorRegistry.getDecoderClasspath(format));
        encoder.setEncodingChecker(m_encodingChecker);

        return encoder;
    }

    public String getFilterTableName()
    {
        return m_filterTableName;
    }

    public void setFilterTableName(String filterTableName)
    {
        this.m_filterTableName = filterTableName;
    }

    public long getFilterId()
    {
        return m_filterId;
    }

    public void setFilterId(long filterId)
    {
        this.m_filterId = filterId;
    }

    public void setIsUseSecondaryFilter(boolean p_isUseSecondaryFilter)
    {
        this.isUseSecondaryFilter = p_isUseSecondaryFilter;
    }

    public boolean getIsUseSecondaryFilter()
    {
        return this.isUseSecondaryFilter;
    }

    public void setConvertHtmlEntryFromSecondFilter(boolean p_convertHtmlEntryFromSecondFilter)
    {
        this.convertHtmlEntityFromSecondFilter = p_convertHtmlEntryFromSecondFilter;
    }

    public boolean getConvertHtmlEntryFromSecondFilter()
    {
        return this.convertHtmlEntityFromSecondFilter;
    }

    public CxeMessage getCxeMessage()
    {
        return cxeMessage;
    }

    public void setCxeMessage(CxeMessage cxeMessage)
    {
        this.cxeMessage = cxeMessage;
    }

    public void setConvertHtmlEntityForHtml(boolean flag)
    {
        m_convertHtmlEntityForHtml = flag;
    }
}
