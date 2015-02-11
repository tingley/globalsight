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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.HtmlFilter;
import com.globalsight.cxe.entity.filterconfiguration.XMLRuleFilter;
import com.globalsight.everest.page.PageTemplate;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.common.DiplomatBasicHandler;
import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.DiplomatNames;
import com.globalsight.ling.common.EncodingChecker;
import com.globalsight.ling.common.NativeEnDecoder;
import com.globalsight.ling.common.NativeEnDecoderException;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.common.XmlWriter;
import com.globalsight.util.XmlParser;

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
public class DiplomatMerger implements DiplomatMergerImpl,
        DiplomatBasicHandler, IFormatNames
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
    private Exception m_error = null;
    private XmlEntities m_unescaper = null;
    private TmxTagGenerator m_tagGenerator = null;
    private Properties m_subProperties = null;
    private EncodingChecker m_encodingChecker = null;
    private boolean m_convertHtmlEntityForHtml;
    private boolean m_convertHtmlEntityForXml;
    private long m_filterId;
    // For entity encoding issue
    private boolean m_isCDATA = false;

    // For secondary filter:if second parser(currently it is html parser) is
    // used,
    // convert the target contents to entity '<','>','&',''' and '"'.
    private boolean isUseSecondaryFilter = false;
    // this is from HTML filter configuration
    private boolean convertHtmlEntryFromSecondFilter = false;

    //
    // Constructors
    //

    public DiplomatMerger()
    {
        super();
        m_unescaper = new XmlEntities();
    }

    /**
     * Intialize merger and pass in our L10NContent reference which holds the
     * newly generated localized page.
     */
    public void init(String p_diplomat, L10nContent p_l10nContent)
            throws DiplomatMergerException
    {
        m_diplomat = p_diplomat;
        m_l10nContent = p_l10nContent;
        m_diplomatParser = new DiplomatBasicParser(this);
        m_stateStack = new Stack();
        m_tmxStateStack = new Stack();
        m_tagGenerator = new TmxTagGenerator();

        m_extractorRegistry = ExtractorRegistry.getObject();

        try
        {
            m_encodingChecker = new EncodingChecker("UTF-8");
        }
        catch (UnsupportedEncodingException e)
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

    public void setTargetEncoding(String targetEncoding)
            throws DiplomatMergerException
    {
        try
        {
            // UnicodeBig and UnicodeLittle expects BOM at the
            // beginning of the string, which we don't have at this
            // moment.
            if (targetEncoding.equals("UnicodeBig")
                    || targetEncoding.equals("UnicodeLittle"))
            {
                targetEncoding = "UTF-8";
            }

            m_encodingChecker = new EncodingChecker(targetEncoding);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new DiplomatMergerException(
                    ExtractorExceptionConstants.INVALID_ENCODING, e.toString());
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
     * For "Entity Encoding issue" If the source segment string contains special
     * signs(e.g. '&apos;'), Then For CDATA Replace the '&lt;' to char '<'
     * Replace the '&gt;' to char '>' Replace the '&amp;' to char '&' Replace
     * the '&apos;' to single quote ''' Replace the '&quot;' to double quote '"'
     * For Node Replace the '&apos;' to single quote ''' Replace the '&quot;' to
     * double quote '"' Replace the '&gt;' to char '>'
     */
    private String entityEncode(String sourceSeg)
    {
        String targetSeg = sourceSeg;

        if (m_isCDATA)
        {
            targetSeg = targetSeg.replaceAll("&lt;", "<");
            targetSeg = targetSeg.replaceAll("&gt;", ">");
            targetSeg = targetSeg.replaceAll("&apos;", "\'");
            targetSeg = targetSeg.replaceAll("&quot;", "\"");
            targetSeg = targetSeg.replaceAll("&amp;", "&");

            m_isCDATA = false;
        }
        else
        {
            // targetSeg = targetSeg.replaceAll("&apos;", "\'");
            // targetSeg = targetSeg.replaceAll("&quot;", "\"");
            // targetSeg = targetSeg.replaceAll("&gt;", ">");
        }

        return targetSeg;
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

        // Mark the CDATA element for "entityEncode(String sourceSeg)"
        if (sourceSeg.indexOf("<![CDATA[") >= 0)
        {
            m_isCDATA = true;
        }

        return targetSeg;
    }

    private String entityEncodeForSkeleton(String sourceSeg)
    {
        return entityEncodeForSkeleton(sourceSeg, false);
    }

    /**
     * Adds <span dir=rtl> if the paragraph is text in HTML and it contains bidi
     * characters.
     */
    private String addSpanRtl(String p_para)
    {
        DiplomatParserState state = (DiplomatParserState) m_stateStack.peek();

        String type = state.getType();
        if (type == null)
        {
            type = "text";
        }

        String format = state.getFormat();
        if (format == null)
        {
            format = m_output.getDataFormat();
        }
        if (type.equals("text") && format.equals(FORMAT_HTML)
                && Text.containsBidiChar(p_para))
        {
            // When changing this string, also update the regexp in
            // merger/html/HtmlPostMergeProcessor.
            p_para = "<ph>&lt;span dir=rtl&gt;</ph>" + p_para
                    + "<ph>&lt;/span&gt;</ph>";
        }

        return p_para;
    }

    private String applyNativeEncoding(String p_text, String p_format,
            String p_type, char p_lastOutputChar)
            throws DiplomatMergerException
    {
        // Convert PUA characters back to original C0 control codes
        // before it's converted NCR or any other escaped form
        String newText = CtrlCharConverter.convertToCtrl(p_text);

        NativeEnDecoder encoder = getEncoder(p_format);

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
                newText = encoder.encodeWithEncodingCheck(newText, String
                        .valueOf(p_lastOutputChar));
            }
            else
            {
                newText = encoder.encodeWithEncodingCheck(newText);
            }
        }
        catch (NativeEnDecoderException e)
        {
            throw new DiplomatMergerException(
                    ExtractorExceptionConstants.INTERNAL_ERROR, e);
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

    private String applyNativeEncodingForSkeleton(String p_text,
            NativeEnDecoder p_encoder) throws DiplomatMergerException
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
            throw new DiplomatMergerException(
                    ExtractorExceptionConstants.INTERNAL_ERROR, e);
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
            throw new DiplomatMergerException(
                    ExtractorExceptionConstants.INTERNAL_ERROR, e);
        }
    }

    /**
     * Required by DiplomatBasicHandler. Throw away all Diplomat end tags.
     */
    public void handleEndTag(String p_name, String p_originalTag)
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

    /**
     * Required by DiplomatBasicHandler. Throw away all Diplomat start tags.
     */
    public void handleStartTag(String p_name, Properties p_attributes,
            String p_originalString)
    {
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
                m_error = new Exception(
                        "DiplomatMerger: missing format from output");
                return;
            }
        }

        // subflow context
        if (m_subProperties != null)
        {
            String subFormat = m_subProperties.getProperty(
                    DiplomatNames.Attribute.DATATYPE, null);

            if (subFormat != null && subFormat.length() > 0)
            {
                format = subFormat;
            }
        }

        String tmp = unescapeXml(p_text);

        try
        {
            if (isContent() && !format.equals("xlf"))
            {
                tmp = applyNativeEncoding(tmp, format, type, m_l10nContent
                        .getLastChar());
            }

            // For "Entity Encode issue"
            if (ExtractorRegistry.FORMAT_HTML.equalsIgnoreCase(format)
                    && m_convertHtmlEntityForHtml)
            {
                if (isContent())
                {
                    tmp = unescapeXml(tmp);
                }
            }

            if (ExtractorRegistry.FORMAT_XML.equalsIgnoreCase(format))
            {
                if (isContent())
                {
                    tmp = entityEncode(tmp);

                    if (m_convertHtmlEntityForXml)
                    {
                        tmp = unescapeXml(tmp);
                    }
                }
                else
                {
                    tmp = entityEncodeForSkeleton(tmp);
                }
            }
            else if (ExtractorRegistry.FORMAT_XLIFF.equalsIgnoreCase(format))
            {
                if (isContent())
                {
                    tmp = entityDecodeForXliff(tmp);
                }
            }

            // if this segment is parsed twice(original parser and HTML parser),
            // encode again to transform them all to entity.
            // This will affect javaProperties,XML,and excel source files with
            // secondary filter settings.
            if (this.isUseSecondaryFilter
                    && this.convertHtmlEntryFromSecondFilter)
            {
                XmlEntities xe = new XmlEntities();
                char[] defaultXmlEncodeChar =
                { '<', '>', '&', '"' };
                xe.setUseDefaultXmlEncoderChar(false);
                tmp = xe.encodeString(tmp, defaultXmlEncodeChar);
            }
            m_l10nContent.addContent(tmp);
        }
        catch (DiplomatMergerException e)
        {
            m_error = e;
        }
    }

    private boolean isKeepGsa()
    {
        return m_keepGsa;
    }

    /**
     * Cycle through each Output node and handle as appropriate.
     */
    public void merge() throws DiplomatMergerException
    {
        DocumentElement de = null;

        convertToOutput();
        applyFilterConfiguration();
        // This method must be called after convertToOutput()
        NativeEnDecoder encoderForSkeleton = getEncoder(getDocumentFormat());

        String srcDataType = m_output.getDiplomatAttribute().getDataType();
        boolean isTuvLocalized = false;
        String localizedBy = new String();

        for (Iterator it = m_output.documentElementIterator(); it.hasNext();)
        {
            de = (DocumentElement) it.next();

            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                    m_stateStack.push(new DiplomatParserState(de.type(),
                            ((TranslatableElement) de).getDataType(),
                            ((TranslatableElement) de).getType()));

                    if (((TranslatableElement) de).getIsLocalized() != null)
                    {
                        if (((TranslatableElement) de).getIsLocalized().equals(
                                PageTemplate.byMT))
                        {
                            isTuvLocalized = true;
                            localizedBy = PageTemplate.byMT;
                        }
                        else if (((TranslatableElement) de).getIsLocalized()
                                .equals(PageTemplate.byUser))
                        {
                            isTuvLocalized = true;
                            localizedBy = PageTemplate.byUser;
                        }
                        else if((((TranslatableElement) de).getIsLocalized()
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

                    // add <span dir=rtl> to the paragraph if necessary
                    parseDiplomatSnippet(addSpanRtl(chunk));
                    m_stateStack.pop();
                    break;

                case DocumentElement.LOCALIZABLE:
                    m_stateStack.push(new DiplomatParserState(de.type(),
                            ((LocalizableElement) de).getDataType(),
                            ((LocalizableElement) de).getType()));
                    parseDiplomatSnippet(((LocalizableElement) de).getChunk());
                    m_stateStack.pop();
                    break;

                case DocumentElement.SKELETON:
                    m_stateStack.push(new DiplomatParserState(de.type(), null,
                            null));

                    String tmp = unescapeXml(((SkeletonElement) de)
                            .getSkeleton());
                    tmp = applyNativeEncodingForSkeleton(tmp,
                            encoderForSkeleton);
                    if (srcDataType.equals("xlf")
                            || srcDataType.equals("xliff"))
                    {
                        tmp = entityEncodeForSkeleton(tmp, true);
                    }
                    else
                    {
                        tmp = entityEncodeForSkeleton(tmp);
                    }

                    if (isTuvLocalized
                            && tmp.indexOf("<iws:segment-metadata") > 0)
                    {
                        tmp = dealSkeleton(tmp, localizedBy);
                    }

//                    if (srcDataType.equals("excel-html")
//                            && !tmp.contains("mso-spacerun:yes"))
//                        tmp = fixXmlForExcel(tmp);
                    m_l10nContent.addContent(tmp);

                    m_stateStack.pop();
                    break;

                case DocumentElement.GSA_START:
                    if (isKeepGsa())
                    {
                        m_stateStack.push(new DiplomatParserState(de.type(),
                                null, null));
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
                        m_stateStack.push(new DiplomatParserState(de.type(),
                                null, null));
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
                        ExtractorExceptionConstants.DIPLOMAT_MERGER_FATAL_ERROR,
                        m_error);
            }
        }
    }

    /*
     * Deal with the skeleton of the trados xliff file job. 
     * 1. If the tuv modified by MT, need add or modify the "translation_type" 
     * value to be "machine_translation_mt", if is modified by user, 
     * the attribute value should be "manual_translation".
     * 2. If the tuv modified by user, should set "translation_status" 
     * attribute value to be "finished".
     * 
     * 3. remove attribute "match-quality".
     * 
     * 4. remove attribute "target_content" and "source_content".
     */
    private String dealSkeleton(String tmp, String localizedBy)
    {
        boolean localizedByUser = false;
        boolean localizedByMT = false;
        boolean localizedByLocalTM = false;

        if (localizedBy != null && localizedBy.equals(PageTemplate.byUser))
        {
            localizedByUser = true;
        }
        else if (localizedBy != null && localizedBy.equals(PageTemplate.byMT))
        {
            localizedByMT = true;
        }
        else if (localizedBy != null && localizedBy.equals(PageTemplate.byLocalTM))
        {
            localizedByLocalTM = true;
        }

        int begin = tmp.indexOf("<iws:segment-metadata");
        int end = tmp.indexOf("</iws:segment-metadata")
                + "</iws:segment-metadata>".length();

        String iwsStr = tmp.substring(begin, end);
        iwsStr = "<segmentdata "
                + "xmlns:iws=\"http://www.idiominc.com/ws/asset\">" + iwsStr
                + "</segmentdata>";
        Document dom = getDom(iwsStr);
        Element root = dom.getRootElement();
        List iwsStatusList = root.selectNodes("//iws:status");

        for (int x = 0; x < iwsStatusList.size(); x++)
        {
            Element status = (Element) iwsStatusList.get(x);

            if(localizedByUser || localizedByLocalTM || localizedByMT) {
                /*
            if (status.attribute("match-quality") != null)
                status.remove(status.attribute("match-quality"));

            if (status.attribute("source_content") != null)
                status.remove(status.attribute("source_content"));

            if (status.attribute("target_content") != null)
                status.remove(status.attribute("target_content"));
                */
                List<Attribute> attrList = new ArrayList();
                attrList.addAll(status.attributes());
                
                for(int i = 0; i < attrList.size(); i++) 
				{
                    String name = attrList.get(i).getName();
                    if(!name.equals("translation_status") 
                            && !name.equals("translation_type")
                            && !name.equals("source_content") )
					{
                        status.remove(attrList.get(i));
                    }
                }
            }

            if (status.attribute("translation_status") != null)
            {
                if (localizedByUser)
                {
                    status.attribute("translation_status").setValue("finished");
                }
                else if(localizedByLocalTM) {
                    status.attribute("translation_status").setValue("pending");
                }
            }
            else
            {
                if (localizedByUser)
                {
                    status.addAttribute("translation_status", "finished");
                }
                else if(localizedByLocalTM) {
                    status.addAttribute("translation_status", "pending");
                }
            }

            if (status.attribute("translation_type") != null)
            {
                if (localizedByMT)
                {
                    status.attribute("translation_type").setValue(
                            "machine_translation_mt");
                }
                else if (localizedByUser || localizedByLocalTM)
                {
                    status.attribute("translation_type").setValue(
                            "manual_translation");
                }
            }
            else
            {
                if (localizedBy != null)
                {
                    if (localizedByMT)
                    {
                        status.addAttribute("translation_type",
                                "machine_translation_mt");
                    }
                    else if (localizedByUser || localizedByLocalTM)
                    {
                        status.addAttribute("translation_type",
                                "manual_translation");
                    }
                }

            }
        }


        iwsStr = dom.selectSingleNode("//iws:segment-metadata").asXML();
        String str = "xmlns:iws=\"http://www.idiominc.com/ws/asset\"";
        iwsStr = iwsStr.replace(str, "");
        tmp = tmp.substring(0, begin) + iwsStr + tmp.substring(end);

        return tmp;
    }

    /*
     * To convert '<', '>' to '&lt;' and '&gt;' for GBS-1118, Added by Vincent
     * Yan
     * 
     * @date 2010-07-13
     */
    private String fixXmlForExcel(String p_str)
    {
        StringBuffer sb = new StringBuffer();
        String regex = "<td[\\s\\S]*?>{1}([\\s\\S]*?)</td>";
        try
        {
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(p_str);
            int indexB = 0;
            while (matcher.find())
            {
                String tmp = matcher.group(1);
                int start = matcher.start(1);
                int end = matcher.end(1);
                sb.append(p_str.substring(indexB, start));
                for (int i = 0; i < tmp.length(); i++)
                {
                    switch (tmp.charAt(i))
                    {
                        case '<':
                            sb.append("&lt;");
                            break;
                        case '>':
                            sb.append("&gt;");
                            break;
                        default:
                            sb.append(tmp.charAt(i));
                            break;
                    }
                }
                indexB = end;
            }
            sb.append(p_str.substring(indexB));
        }
        catch (Exception e)
        {
            throw new DiplomatMergerException(
                    ExtractorExceptionConstants.DIPLOMAT_MERGER_FATAL_ERROR,
                    m_error);
        }
        return sb.toString();
    }

    private void applyFilterConfiguration()
    {
        // set m_convertHtmlEntity
        if (m_filterId > 0)
        {
            boolean getValueOfconvertHtmlEntity = false;
            String format = getDocumentFormat();
            boolean isXmlFormat = ExtractorRegistry.FORMAT_XML
                    .equalsIgnoreCase(format);
            boolean isHtmlFormat = ExtractorRegistry.FORMAT_HTML
                    .equalsIgnoreCase(format);

            if (isHtmlFormat)
            {
                HtmlFilter htmlFilter = FilterHelper.getHtmlFilter(m_filterId);
                if (htmlFilter != null)
                {
                    m_convertHtmlEntityForHtml = htmlFilter
                            .isConvertHtmlEntry();
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
                }
            }

            if (!getValueOfconvertHtmlEntity)
            {
                SystemConfiguration tagsProperties = SystemConfiguration
                        .getInstance("/properties/Tags.properties");
                String convertHtml = tagsProperties
                        .getStringParameter("convertHtmlEntity");
                if (convertHtml != null)
                {
                    if (isHtmlFormat)
                    {
                        m_convertHtmlEntityForHtml = Boolean
                                .valueOf(convertHtml.trim());
                    }
                    else if (isXmlFormat)
                    {
                        m_convertHtmlEntityForXml = Boolean.valueOf(convertHtml
                                .trim());
                    }
                }
                else
                {
                    m_convertHtmlEntityForHtml = false;
                    m_convertHtmlEntityForXml = false;
                }
            }
        }
    }

    private void parseDiplomatSnippet(String p_diplomat)
            throws DiplomatMergerException
    {
        try
        {
            m_diplomatParser.parse(p_diplomat);
        }
        catch (DiplomatBasicParserException e)
        {
            throw new DiplomatMergerException(
                    ExtractorExceptionConstants.DIPLOMAT_BASIC_PARSER_EXCEPTION,
                    e);
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

    private String unescapeXml(String p_xml)
    {
        return m_unescaper.decodeStringBasic(p_xml);
    }

    private String escapeXml(String p_xml)
    {
        return m_unescaper.encodeStringBasic(p_xml);
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

    private NativeEnDecoder makeEncoder(String p_path)
            throws DiplomatMergerException
    {
        NativeEnDecoder decoder = null;

        try
        {
            decoder = (NativeEnDecoder) Class.forName(p_path).newInstance();
        }
        catch (ClassNotFoundException e)
        {
            throw new DiplomatMergerException(
                    ExtractorExceptionConstants.INTERNAL_ERROR, e);
        }
        catch (InstantiationException e)
        {
            throw new DiplomatMergerException(
                    ExtractorExceptionConstants.INTERNAL_ERROR, e);
        }
        catch (IllegalAccessException e)
        {
            throw new DiplomatMergerException(
                    ExtractorExceptionConstants.INTERNAL_ERROR, e);
        }

        return decoder;
    }

    private NativeEnDecoder getEncoder(String p_format)
            throws DiplomatMergerException
    {
        int format = -1;

        if ((format = m_extractorRegistry.getFormatId(p_format)) == -1)
        {
            throw new DiplomatMergerException(
                    ExtractorExceptionConstants.FORMAT_NOT_REGISTERED);
        }

        NativeEnDecoder encoder = makeEncoder(m_extractorRegistry
                .getDecoderClasspath(format));
        encoder.setEncodingChecker(m_encodingChecker);

        return encoder;
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

    public void setConvertHtmlEntryFromSecondFilter(
            boolean p_convertHtmlEntryFromSecondFilter)
    {
        this.convertHtmlEntryFromSecondFilter = p_convertHtmlEntryFromSecondFilter;
    }

    public boolean getConvertHtmlEntryFromSecondFilter()
    {
        return this.convertHtmlEntryFromSecondFilter;
    }

    /**
     * Converts an XML string to a DOM document.
     */
    private Document getDom(String p_xml)
    {
        XmlParser parser = null;

        try
        {
            parser = XmlParser.hire();
            return parser.parseXml(p_xml);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("invalid GXML `" + p_xml + "': "
                    + ex.getMessage());
        }
        finally
        {
            XmlParser.fire(parser);
        }
    }
}
