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
package com.globalsight.ling.docproc.extractor.xml;

// Java
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.globalsight.cxe.adapter.ling.ExtractRule;
import com.globalsight.cxe.entity.filterconfiguration.BaseFilter;
import com.globalsight.cxe.entity.filterconfiguration.BaseFilterManager;
import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.HtmlFilter;
import com.globalsight.cxe.entity.filterconfiguration.InternalText;
import com.globalsight.cxe.entity.filterconfiguration.InternalTextHelper;
import com.globalsight.cxe.entity.filterconfiguration.MSOffice2010Filter;
import com.globalsight.cxe.entity.filterconfiguration.XMLRuleFilter;
import com.globalsight.cxe.entity.filterconfiguration.XmlFilterConfigParser;
import com.globalsight.cxe.entity.filterconfiguration.XmlFilterConstants;
import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.ling.common.RegEx;
import com.globalsight.ling.common.RegExException;
import com.globalsight.ling.common.RegExMatchInterface;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.common.srccomment.SrcCmtXmlComment;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.DocumentElementException;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorInterface;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.Segmentable;
import com.globalsight.ling.docproc.SkeletonElement;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.ling.docproc.extractor.xml.xmlrule.CommentRuleItem;
import com.globalsight.util.FileUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.SegmentUtil;

/**
 * XML Extractor.
 * 
 * <p>
 * The behavior of the XML Extractor is rule-file-driven (see schemarules.rng).
 * If no rules are specified, the default rules are:
 * </p>
 * 
 * <ul>
 * <li>Contents of all element nodes are extracted as translatable.</li>
 * <li>All elements break a segment. In other words, all tags go into the
 * skeleton and no tag is included in an extracted translatable segment.</li>
 * <li>No attributes are extracted.</li>
 * </ul>
 * 
 * The rule file basically contains two sets of rules:
 * 
 * <ol>
 * <li>&lt;dont-translate&gt; elements specify elements or attributes that
 * should not be extracted.</li>
 * 
 * <li>&lt;translate&gt; elements specify elements or attributes that are to be
 * extracted for translation or localization.</li>
 * </ol>
 * 
 * <p>
 * Attributes on &lt;translatable&gt;:
 * </p>
 * 
 * <ul>
 * <li>path: XPath expression to address the elements and attributes that are to
 * be extracted for translation or localization.</li>
 * 
 * <li>loctype: Localization type. Specifies whether the extracted data are
 * translatable or localizable. Possible values are "translatable" or
 * "localizable". The default value is "translatable".</li>
 * 
 * <li>datatype: Format of the data. If the extracted data needs further
 * extraction, the data format should be specified in this attribute. The
 * typical use case is that a HTML snippet is stored in an XML element. When the
 * datatype attribute has the value "html", the XML extractor extracts the
 * content of the element and calls the HTML extractor, passing it the extracted
 * content.</li>
 * 
 * <li>type: Type of the data. This attribute is used when the type of the
 * extracted data needs to be explicitly specified. Examples of types are
 * "link", "bold", "underline" etc.</li>
 * 
 * <li>inline: This attribute specifies whether the elements specified by the
 * path attribute break a segment. If an element breaks a segment, the element
 * tag is not included in the extracted data. If an element does not break a
 * segment (if the tag is inline), the element tag is included in the extracted
 * data. Possible values for the attribute are "yes" or "no". "yes" means the
 * tag does not break segments. "no" means the tag breaks segments. The default
 * value is "no".</li>
 * 
 * <li>movable: the DiplomatXML attribute for bpt,it,ut,ph tags, specifying
 * whether these tags can be moved around in the editor.</li>
 * 
 * <li>erasable: the DiplomatXML attribute for bpt,it,ut,ph tags, specifying
 * whether these tags can be deleted in the editor.</li>
 * </ul>
 * 
 * <p>
 * When multiple rules match a single node, the rules are merged according to
 * the algorithm in Rule.java. A side-effect of merging is that the first
 * matching rule determines whether a node is translatable or not; sub-sequent
 * rule matches will never change the type of the first rule.
 * </p>
 * 
 * <p>
 * A tag that switches to a different extractor can not be embeddable.
 * </p>
 */
public class Extractor extends AbstractExtractor implements ExtractorInterface,
        EntityResolver, ExtractorExceptionConstants, ErrorHandler
{
    static private final Logger s_logger = Logger.getLogger(Extractor.class);

    private ExtractorAdmin m_admin = new ExtractorAdmin(null);

    // Rules Engine.
    private RuleSet m_rules = null;
    private Map m_ruleMap = null;
    private boolean m_useEmptyTag = false;

    // XML declaration flags
    private boolean m_haveXMLDecl = false;
    private String m_version = null;
    private String m_standalone = null;
    private String m_encoding = null;

    // XML encoder
    private XmlEntities m_xmlEncoder = new XmlEntities();

    // for extractor switching
    private String m_switchExtractionBuffer = new String();
    private String m_switchExtractionSid = null;
    private String m_otherFormat = null;

    // for xml converted from InDesign files.
    private final String PARAGRAPH_NODE_NAME = "Inddgsparagraph";
    private final String PARAGRAPH_HAS_DIFFERENT_STYLE = "hasDifferentStyle";
    private final String VALUE_TRUE = "true";
    private final String VALUE_FALSE = "false";

    // for xml filter implement
    private XMLRuleFilter m_xmlFilter = null;
    private XmlFilterHelper m_xmlFilterHelper = null;
    private BaseFilter m_baseFilter = null;
    private List<InternalText> m_internalTexts = null;
    private boolean m_checkWellFormed = true;
    private String m_elementPostFormat = null;
    private String m_cdataPostFormat = null;
    private boolean m_isElementPost = false;
    private boolean m_isElementPostToHtml = false;
    private boolean m_isOriginalXmlNode = false;
    private boolean m_isCdataPost = false;
    // for GBS-2493
    private boolean m_preserveEmptyTag = true;
    private final String ATTRIBUTE_PRESERVE_CLOSED_TAG = "GS_XML_ATTRIBUTE_PRESERVE_CLOSED_TAG";

    // for office 2010
    private MSOffice2010Filter m_office2010Filter = null;
    private OfficeXmlContentPostFilter m_office2010ContentPostFilter = null;
    // for GBS-1649
    private boolean m_isUrlTranslate = false;
    private boolean m_isToolTipsTranslate = false;
    private final String RE_URL = "https?://[^\\s<]+";
    private boolean m_isOfficeXml = false;
    protected boolean m_isIdmlXml = false;
    private static String[] wtNodeNames = new String[]
    { "w:t", "t", "a:t" };
    private static String[] wrprNodeNames = new String[]
    { "w:rPr", "rpr", "a:rPr" };
    private static String[] wrNodeNames = new String[]
    { "w:r", "r", "a:r" };
    private static String[] xlsxHeaderFooters = new String[]
    { "oddHeader", "oddFooter" };
    private static String[] m_uselessWords = new String[]
    { "&L", "&C", "&R" };

    private List<ExtractRule> rules = new ArrayList<ExtractRule>();

    //
    // Constructors
    //
    public Extractor()
    {
        super();

        m_admin = new ExtractorAdmin(null);
        m_rules = new RuleSet();
        m_ruleMap = null;
        m_haveXMLDecl = false;
        m_version = null;
        m_standalone = null;
        m_encoding = null;
        m_switchExtractionBuffer = new String();
        m_switchExtractionSid = null;
        m_otherFormat = null;
        m_elementPostFormat = null;
        m_cdataPostFormat = null;
    }

    //
    // Will be overwritten in classes derived from XML extractor (eBay PRJ)
    //
    public void setFormat()
    {
        setMainFormat(ExtractorRegistry.FORMAT_XML);
    }

    /**
     * Extracts the input document.
     * 
     * Parses the XML File into DOM using xerces.
     * 
     * Skips the external entity (DTD, etc) by providing a null byte array.
     * 
     * Then invokes domNodeVisitor for the Document 'Node' ('virtual root') to
     * traverse the DOM tree recursively, using the AbstractExtractor API to
     * write out skeleton and segments.
     */
    public void extract() throws ExtractorException
    {
        try
        {
            // Set the main format depending on which (derived) class
            // we're called in.
            setFormat();

            m_isOfficeXml = ExtractorRegistry.FORMAT_OFFICE_XML
                    .equals(getMainFormat());

            // init for xml filter
            Filter mainFilter = getMainFilter();
            m_xmlFilter = (mainFilter != null && mainFilter instanceof XMLRuleFilter) ? (XMLRuleFilter) mainFilter
                    : null;
            m_xmlFilterHelper = new XmlFilterHelper(m_xmlFilter);
            m_xmlFilterHelper.init();
            m_xmlFilterHelper.setXmlEntities(m_xmlEncoder);
            m_checkWellFormed = m_xmlFilterHelper.isCheckWellFormed();

            if (m_xmlFilter != null)
            {
                m_baseFilter = BaseFilterManager.getBaseFilterByMapping(
                        m_xmlFilter.getId(), m_xmlFilter.getFilterTableName());
                m_internalTexts = BaseFilterManager
                        .getInternalTexts(m_baseFilter);
            }

            m_office2010Filter = (mainFilter != null && mainFilter instanceof MSOffice2010Filter) ? (MSOffice2010Filter) mainFilter
                    : null;
            if (m_office2010Filter != null)
            {
                m_isUrlTranslate = m_office2010Filter.isUrlTranslate();
                m_isToolTipsTranslate = m_office2010Filter
                        .isToolTipsTranslate();
                // internal text post-filter
                m_baseFilter = BaseFilterManager.getBaseFilterByMapping(
                        m_office2010Filter.getId(),
                        m_office2010Filter.getFilterTableName());
                m_internalTexts = BaseFilterManager
                        .getInternalTexts(m_baseFilter);
                // content post-filter
                long contentPostFilterId = m_office2010Filter
                        .getContentPostFilterId();
                if (contentPostFilterId > 0)
                {
                    HtmlFilter contentPostFilter = FilterHelper
                            .getHtmlFilter(contentPostFilterId);
                    if (contentPostFilter != null)
                    {
                        m_office2010ContentPostFilter = new OfficeXmlContentPostFilter(
                                contentPostFilter);
                    }
                }
            }

            setMainBaseFilter(m_baseFilter);
            // # GBS-2894 : do segmentation before internal text
            if (isDoSegBeforeInlText())
            {
                setMainBaseFilter(null);
                m_internalTexts = null;
            }

            m_preserveEmptyTag = m_xmlFilterHelper.preserveEmptyTag();
            if (!m_isOfficeXml && m_preserveEmptyTag)
            {
                // for GBS-2493 XML filter need preserve empty tag format per
                // source files
                preserveEmptyTag();
            }

            Reader reader = readInput();
            if (m_checkWellFormed)
            {
                XmlFilterChecker.checkWellFormed(reader);
                reader = readInput();
            }

            GsDOMParser parser = new GsDOMParser(
                    "org.apache.xerces.jaxp.GSDocumentBuilderFactoryImpl");
            // don't read external DTDs
            parser.setEntityResolver(this);
            // provide detailed error report
            parser.setErrorHandler(this);

            // parse and create DOM tree
            Document document = parser.parse(new InputSource(reader));

            // preserve the values in the inputs' XML declaration
            preserveAttributesInXmlDeclaration(document);

            // for xml filter implement
            m_elementPostFormat = m_xmlFilterHelper.getElementPostFormat();
            m_isElementPost = m_xmlFilterHelper.isElementPostFilter();
            m_isElementPostToHtml = m_isElementPost ? (IFormatNames.FORMAT_HTML
                    .equals(m_elementPostFormat)) : false;
            m_cdataPostFormat = m_xmlFilterHelper.getCdataPostFormat();
            m_isCdataPost = m_xmlFilterHelper.isCdataPostFilter();

            String mainFormat = getMainFormat();

            // process document.xml
            if (IFormatNames.FORMAT_XML.equals(mainFormat)
                    && "w:document".equals(document.getDocumentElement()
                            .getNodeName()))
            {
                mainFormat = IFormatNames.FORMAT_OFFICE_XML;
                m_isOfficeXml = true;
            }

            // get rule map for the document
            m_ruleMap = m_rules.buildRulesWithFilter(document,
                    m_xmlFilterHelper.getXmlFilterTags(), mainFormat);

            for (ExtractRule rule : rules)
            {
                rule.buildRule(document, m_ruleMap);
            }

            m_useEmptyTag = m_rules.usesEmptyTag();
            m_useEmptyTag = m_xmlFilterHelper.usesEmptyTag();
            if (m_isOfficeXml)
            {
                m_useEmptyTag = true;
            }

            // traverse the DOM tree
            domNodeVisitor(document, false, true, false);
        }
        catch (Exception e)
        {
            s_logger.error(e);
            throw new ExtractorException(e);
        }
    }

    /**
     * This method is invoked by AbstractExractor framework. It is used to point
     * the XML Extracator to the file containing the XML extraction rules.
     * 
     * If the path to Extraction Rules is not specified via
     * Input.m_strProjectRules, it defaults to "file:/gsrules.xml". (CvdL: I
     * think it defaults to a null string.)
     */
    public void loadRules() throws ExtractorException
    {
        String ruleString = getInput().getRules();
        m_rules.loadRule(ruleString);
    }

    /** Provide an alternate way to load rules */
    public void loadRules(String p_rules) throws ExtractorException
    {
        m_rules.loadRule(p_rules);
    }

    /**
     * Overrides EntityResolver#resolveEntity.
     * 
     * The purpose of this method is to read Schemarules.dtd from resource and
     * feed it to the validating parser, but what it really does is returning a
     * null byte array to the XML parser.
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException
    {
        return new InputSource(new ByteArrayInputStream(new byte[0]));
    }

    // ErrorHandler interface methods

    public void error(SAXParseException e) throws SAXException
    {
        String s = e.getMessage();
        // ignore below errors
        if (s.matches("Attribute .*? was already specified for element[\\s\\S]*"))
        {
            return;
        }

        throw new SAXException("XML parse error at\n  line "
                + e.getLineNumber() + "\n  column " + e.getColumnNumber()
                + "\n  Message:" + e.getMessage());
    }

    public void fatalError(SAXParseException e) throws SAXException
    {
        error(e);
    }

    public void warning(SAXParseException e)
    {
        System.err.println("XML parse warning at\n  line " + e.getLineNumber()
                + "\n  column " + e.getColumnNumber() + "\n  Message:"
                + e.getMessage());
    }

    private void outputXMLDeclaration()
    {
        outputSkeleton("<?xml");
        if (m_version != null)
        {
            outputSkeleton(" version=\"" + m_version + "\"");
        }
        if (m_encoding != null)
        {
            outputSkeleton(" encoding=\"" + m_encoding + "\"");
        }
        if (m_standalone != null)
        {
            outputSkeleton(" standalone=\"" + m_standalone + "\"");
        }
        outputSkeleton(" ?>\n");
    }

    private void docTypeProcessor(DocumentType docType)
    {
        String systemId = docType.getSystemId();
        String publicId = docType.getPublicId();
        String internalSubset = docType.getInternalSubset();
        if (systemId != null || publicId != null || internalSubset != null)
        {
            outputSkeleton("<!DOCTYPE " + docType.getName() + " ");

            String externalId = null;

            if (systemId != null && publicId != null)
            {
                externalId = "PUBLIC \"" + publicId + "\" \"" + systemId + "\"";
            }
            else if (systemId != null)
            {
                externalId = "SYSTEM \"" + systemId + "\"";
            }

            if (externalId != null)
            {
                outputSkeleton(externalId);
            }
            if (internalSubset != null)
            {
                outputSkeleton(" [" + internalSubset + "]>\n");
            }
            else
            {
                outputSkeleton(">\n");
            }
        }
    }

    private void commentProcessor(boolean switchesExtraction, Node p_node,
            boolean isInExtraction, boolean isTranslatable)
    {
        if (switchesExtraction || m_isElementPost)
        {
            outputOtherFormat();
        }

        String nodeValue = p_node.getNodeValue();
        if (!processGsaSnippet(nodeValue))
        {
            String comment = "<!--" + nodeValue + "-->";

            if (isInExtraction)
            {
                String stuff = "<ph type=\"comment\">"
                        + m_xmlEncoder.encodeStringBasic(comment) + "</ph>";
                outputExtractedStuff(stuff, isTranslatable, false);
            }
            else
            {
                outputSkeleton(comment);
            }
        }
        // else: the GSA snippet is to be ignored.

        // handle source comment
        if (isInExtraction)
        {
            XmlFilterTags tags = m_xmlFilterHelper.getXmlFilterTags();
            List<SrcCmtXmlComment> srcComments = tags.getSrcCmtXmlComment();
            String srcCommentText = SrcCmtXmlComment.getSrcCommentContent(
                    srcComments, nodeValue);
            setSrcComment(p_node, srcCommentText, isTranslatable);
        }
    }

    private void setSrcComment(Node p_node, String srcCommentText,
            boolean isTranslatable)
    {
        Node nextNode = p_node.getNextSibling();
        if (m_ruleMap == null)
        {
            m_ruleMap = new HashMap();
        }

        while (nextNode != null)
        {

            if (nextNode.getNodeType() == Node.TEXT_NODE)
            {
                CommentRuleItem.updateCommend(m_ruleMap, nextNode,
                        srcCommentText);
            }
            else
            {
                break;
            }

            nextNode = nextNode.getNextSibling();
        }
    }

    private void outputSrcComment(String srcCommentText, boolean isTranslatable)
    {
        if (srcCommentText != null && !"".equals(srcCommentText.trim()))
        {
            String stuff = "<ph type=\"srcComment\" value=\""
                    + m_xmlEncoder.encodeStringBasic(srcCommentText)
                    + "\"></ph>";
            outputExtractedStuff(stuff, isTranslatable, false);
        }
    }

    private void outputPi(Node p_node, boolean switchesExtraction,
            boolean isInExtraction, boolean isTranslatable)
    {
        if (switchesExtraction)
            outputOtherFormat();

        String nodeName = p_node.getNodeName();
        XmlFilterProcessIns xmlPI = m_xmlFilterHelper
                .getMatchedProcessIns(nodeName);
        String piValue = p_node.getNodeValue();
        String piStart = "<?" + nodeName + " ";
        String piEnd = "?>";
        String piString = piStart + piValue + piEnd;
        boolean handled = false;
        int bptIndex = 0;

        if (xmlPI != null)
        {
            if (xmlPI.getHandleType() == XmlFilterConstants.PI_MARKUP)
            {
                outputSkeleton(piString);
                handled = true;
            }
            else if (xmlPI.getHandleType() == XmlFilterConstants.PI_MARKUP_EMB)
            {
                if (isInExtraction)
                {
                    String stuff = "<ph type=\"pi\">"
                            + m_xmlEncoder.encodeStringBasic(piString)
                            + "</ph>";
                    outputExtractedStuff(stuff, isTranslatable, false);
                }
                else
                {
                    outputSkeleton(piString);
                }

                handled = true;
            }
            else if (xmlPI.getHandleType() == XmlFilterConstants.PI_REMOVE)
            {
                handled = true;
            }
            else if (xmlPI.getHandleType() == XmlFilterConstants.PI_TRANSLATE)
            {
                List<String> attributes = xmlPI.getTransAttributes();
                if (attributes == null || piValue == null
                        || piValue.length() == 0)
                {
                    handled = false;
                }
                else if (attributes.size() == 0)
                {
                    if (isInExtraction)
                    {
                        String stuff = "<ph type=\"piStart\">"
                                + m_xmlEncoder.encodeStringBasic(piStart)
                                + "</ph>";
                        outputExtractedStuff(stuff, isTranslatable, false);
                        outputExtractedStuff(piValue, isTranslatable, false);
                        stuff = "<ph type=\"piEnd\">"
                                + m_xmlEncoder.encodeStringBasic(piEnd)
                                + "</ph>";
                        outputExtractedStuff(stuff, isTranslatable, false);
                    }
                    else
                    {
                        outputSkeleton(piStart);
                        outputExtractedStuff(piValue, isTranslatable, false);
                        outputSkeleton(piEnd);
                    }
                    handled = true;
                }
                else
                {
                    List<Integer> starts = new ArrayList<Integer>();
                    List<String> gs = new ArrayList<String>();
                    for (String att : attributes)
                    {
                        String re = "[\\s]+" + att
                                + "[\\s]*=[\\s]*\"([^\"]+)\"";
                        Pattern pre = Pattern.compile(re);
                        Matcher mre = pre.matcher(piString);

                        if (mre.find())
                        {
                            int start1 = mre.start(1);
                            String g1 = mre.group(1);

                            if (starts.isEmpty())
                            {
                                starts.add(start1);
                                gs.add(g1);
                            }
                            else
                            {
                                boolean added = false;
                                for (int i = 0; i < starts.size(); i++)
                                {
                                    if (start1 < starts.get(i))
                                    {
                                        starts.add(i, start1);
                                        gs.add(i, g1);
                                        added = true;
                                        break;
                                    }
                                }

                                if (!added)
                                {
                                    starts.add(start1);
                                    gs.add(g1);
                                }
                            }
                        }
                    }

                    if (starts.isEmpty())
                    {
                        handled = false;
                    }
                    else
                    {
                        int index_s = 0;
                        int ssize = starts.size();
                        for (int i = 0; i < ssize; i++)
                        {
                            int start1 = starts.get(i);
                            String g1 = gs.get(i);

                            String s1 = piString.substring(index_s, start1);
                            index_s = start1 + g1.length();

                            boolean isLast = (i == ssize - 1);
                            boolean isFirst = (i == 0);

                            if (isInExtraction)
                            {
                                String stuff = null;
                                if (isFirst)
                                {
                                    bptIndex = m_admin.incrementBptIndex();
                                    stuff = "<bpt i=\"" + bptIndex
                                            + "\" type=\"pi\" isTranslate=\""
                                            + isTranslatable + "\">";
                                    outputExtractedStuff(stuff, isTranslatable,
                                            false);
                                }

                                stuff = m_xmlEncoder.encodeStringBasic(s1);
                                outputExtractedStuff(stuff, isTranslatable,
                                        false);

                                stuff = createSubTag(isTranslatable, null, null)
                                        + g1 + "</sub>";
                                outputExtractedStuff(stuff, isTranslatable,
                                        false);

                                if (isLast)
                                {
                                    String s_end = piString.substring(index_s);
                                    stuff = m_xmlEncoder
                                            .encodeStringBasic(s_end);
                                    outputExtractedStuff(stuff, isTranslatable,
                                            false);
                                    stuff = "</bpt>";
                                    outputExtractedStuff(stuff, isTranslatable,
                                            false);
                                }
                            }
                            else
                            {
                                outputSkeleton(piStart);
                                outputExtractedStuff(piValue, isTranslatable,
                                        false);
                                if (isLast)
                                {
                                    String s_end = piString.substring(index_s);
                                    outputSkeleton(s_end);
                                }
                            }
                        }

                        handled = true;
                    }
                }

                if (!handled)
                {
                    if (isInExtraction)
                    {
                        String stuff = "<ph type=\"pi\">"
                                + m_xmlEncoder.encodeStringBasic(piString)
                                + "</ph>";
                        outputExtractedStuff(stuff, isTranslatable, false);
                    }
                    else
                    {
                        outputSkeleton(piString);
                    }

                    handled = true;
                }
            }
            else
            {
                handled = false;
            }
        }

        if (!handled)
        {
            if (isInExtraction)
            {
                String stuff = "<ph type=\"pi\">"
                        + m_xmlEncoder.encodeStringBasic(piString) + "</ph>";
                outputExtractedStuff(stuff, isTranslatable, false);
            }
            else
                outputSkeleton(piString);
        }
    }

    private void textProcessor(Node p_node, boolean switchesExtraction,
            boolean isInExtraction, boolean isTranslatable,
            boolean... isTextNodeDontTranslateInline)
    {
        String nodeValue = p_node.getNodeValue();
        Node parentNode = p_node.getParentNode();
        // GBS-3218
        if (isOfficeInstrText(parentNode.getNodeName()))
        {
            handleHyperlinkInInstrText(nodeValue, parentNode, isTranslatable);
            return;
        }
        if (m_isOfficeXml && "t".equals(parentNode.getNodeName()))
        {
            // indicates this is in excel 2010 document, sharedStrings.xml
            // "_x000D_" are useless characters which need to be removed from
            // extraction
            nodeValue = nodeValue.replace("_x000D_", "");
        }
        if (isInExtraction)
        {
            // Marks words that not need count and translate.
            Set words = Rule.getWords(m_ruleMap, p_node);
            if (words != null && words.size() > 0)
            {
                String text = nodeValue;
                Iterator iterator = words.iterator();
                while (iterator.hasNext())
                {
                    String w = (String) iterator.next();
                    String w1 = w.trim();

                    StringBuffer temp = new StringBuffer("<");
                    temp.append(SegmentUtil.XML_NOTCOUNT_TAG).append(">");
                    temp.append(w1).append("</");
                    temp.append(SegmentUtil.XML_NOTCOUNT_TAG).append(">");

                    String w2 = StringUtil.replace(w, w1, temp.toString());
                    text = StringUtil.replace(text, w, w2);
                }

                p_node.setNodeValue(text);
            }

            boolean outputLinkTipInInstrText = isOfficeTipInInstrText(parentNode);
            boolean isXlsxHeaderFooter = false;
            if (m_isOfficeXml
                    && parentNode != null
                    && StringUtil.isIncludedInArray(xlsxHeaderFooters,
                            parentNode.getNodeName()))
            {
                isXlsxHeaderFooter = true;
            }
            if ((switchesExtraction || m_isElementPost) && !isXlsxHeaderFooter)
            {
                m_switchExtractionBuffer += nodeValue;
                String sid = Rule.getSid(m_ruleMap, p_node);
                if (StringUtil.isNotEmpty(sid))
                {
                    m_switchExtractionSid = sid;
                }
            }
            else
            {
                // added for GBS-2406, content post-filter in office 2010
                if (m_office2010ContentPostFilter != null)
                {
                    List<OfficeXmlContentTag> tagsInContent = m_office2010ContentPostFilter
                            .detectTags(nodeValue);
                    for (OfficeXmlContentTag tag : tagsInContent)
                    {
                        String tagName = tag.getName();
                        if (m_office2010ContentPostFilter.isInlineTag(tagName))
                        {
                            // output text
                            int tagIndex = nodeValue.indexOf(tag.toString());
                            String textBeforeTag = nodeValue.substring(0,
                                    tagIndex);
                            if (isXlsxHeaderFooter)
                            {
                                handleXlsxHeaderFooter(textBeforeTag, true,
                                        false);
                            }
                            else if (outputLinkTipInInstrText)
                            {
                                handleLinkTipInInstrText(textBeforeTag, true,
                                        false);
                            }
                            else
                            {
                                String text = textBeforeTag;
                                boolean containsUrl = false;
                                Map<String, String> urlMap = null;
                                if (!m_isUrlTranslate)
                                {
                                    Object[] o = preProcessUrl(textBeforeTag);
                                    text = (String) o[0];
                                    if (!text.equals(textBeforeTag))
                                    {
                                        containsUrl = true;
                                        urlMap = (Map<String, String>) o[1];
                                    }
                                }
                                // internal text
                                text = handleInternalText(text, true, false);
                                if (containsUrl)
                                {
                                    text = postProcessUrl(text, urlMap);
                                }
                                outputExtractedStuff(text, true, false);
                            }
                            // output inline element "tag"
                            StringBuilder stuff = new StringBuilder();
                            if (tag.isPaired())
                            {
                                if (tag.isEndTag())
                                {
                                    stuff.append("<ept i=\"");
                                    stuff.append(tag.getPairedTag()
                                            .getBptIndex());
                                    stuff.append("\"");
                                }
                                else
                                {
                                    int bptIndex = m_admin.incrementBptIndex();
                                    tag.setBptIndex(bptIndex);
                                    stuff.append("<bpt i=\"");
                                    stuff.append(bptIndex);
                                    stuff.append("\"");
                                }
                            }
                            else
                            {
                                stuff.append("<ph");
                            }
                            stuff.append(OfficeXmlContentPostFilter.IS_FROM_OFFICE_CONTENT);
                            stuff.append(">");
                            if (tag.isMerged())
                            {
                                stuff.append(m_xmlFilterHelper.processText(
                                        tag.toString(), true, false));
                            }
                            else
                            {
                                stuff.append("&lt;");
                                if (tag.isEndTag())
                                {
                                    stuff.append("/");
                                }
                                stuff.append(tagName);
                                if (tag.isClosed() && !tag.hasAttributes())
                                {
                                    stuff.append("/");
                                }
                            }
                            outputExtractedStuff(stuff.toString(), true, false);
                            // output attributes
                            outputAttributesForOfficeXmlContentTag(tag);

                            stuff = new StringBuilder();
                            if (!tag.isMerged())
                            {
                                if (tag.isClosed() && tag.hasAttributes())
                                {
                                    stuff.append("/");
                                }
                                stuff.append("&gt;");
                            }
                            if (tag.isPaired())
                            {
                                if (tag.isEndTag())
                                {
                                    stuff.append("</ept>");
                                }
                                else
                                {
                                    stuff.append("</bpt>");
                                }
                            }
                            else
                            {
                                stuff.append("</ph>");
                            }
                            outputExtractedStuff(stuff.toString(), true, false);

                            // text after tag - new node value
                            nodeValue = nodeValue.substring(tagIndex
                                    + tag.toString().length());
                        }
                    }
                }
                // end for GBS-2406

                // Special treat for xml files coverted from Indesign files
                // (.indd)
                // Because we have inserted font style information into the text
                // of a paragraph (a unit in Indesign application) if this
                // paragraph's text has different font styles, we should process
                // these font style information tags.
                // The purpose of processing to these tags is for them being
                // converted into Ptags easily.
                String newValue = null;
                if (parentNode != null
                        && parentNode.getNodeName().equals(PARAGRAPH_NODE_NAME))
                {
                    // Here we are in a Indesign file's paragraph text
                    NamedNodeMap attrs = parentNode.getAttributes();
                    String attname = null;
                    String value = null;
                    for (int i = 0; i < attrs.getLength(); ++i)
                    {
                        Node att = attrs.item(i);
                        attname = att.getNodeName();
                        if (attname.equals(PARAGRAPH_HAS_DIFFERENT_STYLE))
                        {
                            value = att.getNodeValue();
                            break;
                        }
                    }
                    if (value != null && value.equals(VALUE_TRUE))
                    {
                        // This attribute indicates the text of this
                        // paragraph has different
                        // font styles.
                        newValue = markIndesignParagraphTextStyleTag(nodeValue);
                    }
                }

                String sid = Rule.getSid(m_ruleMap, p_node);
                String srcComment = Rule.getSrcComment(m_ruleMap, p_node);

                if (newValue != null)
                {
                    outputExtractedStuff(newValue, isTranslatable, false);
                }
                else
                {
                    boolean isInline = Rule.isInline(m_ruleMap,
                            getChildNode(parentNode, 1));
                    boolean isPreserveWS = Rule.isPreserveWhiteSpace(m_ruleMap,
                            parentNode,
                            m_xmlFilterHelper.isPreserveWhiteSpaces());
                    boolean isParentTagInternal = Rule.isInternal(m_ruleMap,
                            parentNode);

                    // make font setting for oddHeader oddFooter as ph
                    // &amp;C&amp;15Header &amp;"-,Bold"bold&amp;"-,Normal" 1
                    if (isXlsxHeaderFooter)
                    {
                        handleXlsxHeaderFooter(nodeValue, isTranslatable,
                                isPreserveWS);
                    }
                    else if (outputLinkTipInInstrText)
                    {
                        handleLinkTipInInstrText(nodeValue, isTranslatable,
                                isPreserveWS);
                    }
                    else if (isParentTagInternal)
                    {
                        String temp = m_xmlFilterHelper.processText(nodeValue,
                                isInline, isPreserveWS);
                        outputExtractedStuff(temp, isTranslatable, isPreserveWS);
                    }
                    else
                    {
                        String temp = nodeValue;
                        boolean containsUrl = false;
                        Map<String, String> urlMap = null;
                        if (m_isOfficeXml && !m_isUrlTranslate)
                        {
                            Object[] o = preProcessUrl(nodeValue);
                            temp = (String) o[0];
                            if (!temp.equals(nodeValue))
                            {
                                containsUrl = true;
                                urlMap = (Map<String, String>) o[1];
                            }
                        }
                        // internal text
                        temp = handleInternalText(temp, isInline, isPreserveWS);
                        StringBuffer preBlank = new StringBuffer();
                        int i = 0;
                        if (srcComment != null && temp != null
                                && !"".equals(temp))
                        {
                            for (; i < temp.length(); i++)
                            {
                                char c = temp.charAt(i);
                                if (c == ' ' || c == '\n' || c == '\r')
                                {
                                    preBlank.append(c);
                                }
                                else
                                {
                                    break;
                                }
                            }

                            if (i >= temp.length())
                            {
                                temp = "";
                            }
                            else
                            {
                                temp = temp.substring(i);
                            }
                        }

                        if (preBlank.length() != 0)
                        {
                            outputExtractedStuff(preBlank.toString(),
                                    isTranslatable, isPreserveWS);
                        }

                        if (!"".equals(temp))
                        {
                            outputSrcComment(srcComment, isTranslatable);
                        }

                        if (containsUrl)
                        {
                            temp = postProcessUrl(temp, urlMap);
                        }

                        outputExtractedStuff(temp, isTranslatable, isPreserveWS);
                    }
                }
                setSid(sid);
            }
        }
        else
        {
            boolean parentInline = Rule.isInline(m_ruleMap, parentNode);
            if (parentInline)
            {
                outputExtractedStuff(m_xmlEncoder.encodeStringBasic(nodeValue),
                        isTranslatable, false);
                if (isTextNodeDontTranslateInline != null
                        && isTextNodeDontTranslateInline.length > 0
                        && !isTextNodeDontTranslateInline[0])
                {
                    outputExtractedStuff("</bpt>", isTranslatable, false);
                }
            }
            else
            {
                outputSkeleton(m_xmlEncoder.encodeStringBasic(nodeValue));
            }
        }
    }

    /**
     * Handles the hyperlink in node value.
     * <p>
     * For example, HYPERLINK
     * "http://cdn.tripadvisor.com/pdfs/email/32NewPromoteAwards_US.pdf" \t
     * "_blank"
     * <p>
     * 
     * @since GBS-3218.
     */
    private void handleHyperlinkInInstrText(String nodeValue, Node parentNode,
            boolean isTranslatable)
    {
        boolean isPreserveWS = Rule.isPreserveWhiteSpace(m_ruleMap, parentNode,
                m_xmlFilterHelper.isPreserveWhiteSpaces());
        if (nodeValue.trim().startsWith("TOC"))
        {
            // ignore extracting the TOC
            outputSkeleton(m_xmlEncoder.encodeStringBasic(nodeValue));
            return;
        }
        if (m_isUrlTranslate)
        {
            Pattern p = Pattern
                    .compile("([\\d\\D]*?HYPERLINK[\\d\\D]*?\")([\\d\\D]*?)(\"[\\d\\D]*)");
            Matcher m = p.matcher(nodeValue);
            if (m.find())
            {
                String start = m.group(1);
                String hyperlink = m.group(2);
                String end = m.group(3);
                outputSkeleton(m_xmlEncoder.encodeStringBasic(start));
                outputExtractedStuff(m_xmlEncoder.encodeStringBasic(hyperlink),
                        isTranslatable, isPreserveWS);
                if (m_isToolTipsTranslate)
                {
                    handleLinkTipInInstrText(end, isTranslatable, isPreserveWS);
                }
                else
                {
                    outputSkeleton(m_xmlEncoder.encodeStringBasic(end));
                }
            }
            else
            {
                if (m_isToolTipsTranslate)
                {
                    handleLinkTipInInstrText(nodeValue, isTranslatable,
                            isPreserveWS);
                }
                else
                {
                    outputSkeleton(m_xmlEncoder.encodeStringBasic(nodeValue));
                }
            }
        }
        else
        {
            if (m_isToolTipsTranslate)
            {
                handleLinkTipInInstrText(nodeValue, isTranslatable,
                        isPreserveWS);
            }
            else
            {
                outputSkeleton(m_xmlEncoder.encodeStringBasic(nodeValue));
            }
        }
    }

    private void handleLinkTipInInstrText(String nodeValue,
            boolean isTranslatable, boolean isPreserveWS)
    {
        int indexOfO = nodeValue.indexOf("\\o");
        if (indexOfO == -1)
        {
            outputSkeleton(m_xmlEncoder.encodeStringBasic(nodeValue));
        }
        else
        {
            int indexOfA = nodeValue.indexOf("\"", indexOfO);
            if (indexOfA == -1)
            {
                outputSkeleton(m_xmlEncoder.encodeStringBasic(nodeValue));
            }
            else
            {
                int indexOfB = nodeValue.indexOf("\"", indexOfA + 1);
                String before = nodeValue.substring(0, indexOfA + 1);
                String tip = nodeValue.substring(indexOfA + 1, indexOfB);
                String after = nodeValue.substring(indexOfB);

                outputSkeleton(m_xmlEncoder.encodeStringBasic(before));
                outputExtractedStuff(m_xmlEncoder.encodeStringBasic(tip),
                        isTranslatable, isPreserveWS);
                outputSkeleton(m_xmlEncoder.encodeStringBasic(after));
            }
        }
    }

    private void handleXlsxHeaderFooter(String nodeValue,
            boolean isTranslatable, boolean isPreserveWS)
    {
        List<String> result = processOfficeXmlStyleInText(nodeValue);
        for (int i = 0; i < result.size(); i++)
        {
            String tempStr = result.get(i);

            if (StringUtil.isIncludedInArray(m_uselessWords, tempStr))
            {
                tempStr = m_xmlEncoder.encodeStringBasic(tempStr);
                outputSkeleton(tempStr);
            }
            else
            {
                outputExtractedStuff(tempStr, isTranslatable, isPreserveWS);
            }
        }
    }

    private List<String> processOfficeXmlStyleInText(String nodeValue)
    {
        List<String> result = new ArrayList<String>();
        StringBuffer temp = new StringBuffer();
        int len = nodeValue.length();
        for (int i = 0; i < len; i++)
        {
            char c = nodeValue.charAt(i);

            if (c == '&')
            {
                String tempStr = temp.toString();
                if (tempStr.length() > 0)
                {
                    result.add(tempStr);
                    temp.delete(0, temp.length());
                }
                temp.append(c);
            }
            else if (temp.length() == 1 && temp.charAt(0) == '&')
            {
                temp.append(c);

                if (i + 1 < len)
                {
                    if (c == '"')
                    {
                        char nextC = nodeValue.charAt(++i);

                        while (nextC != '"')
                        {
                            temp.append(nextC);
                            if (i + 1 == len)
                            {
                                break;
                            }
                            nextC = nodeValue.charAt(++i);
                        }
                        temp.append('"');
                    }
                    else if (c >= 48 && c <= 57)
                    {
                        char nextC = nodeValue.charAt(++i);

                        while (nextC >= 48 && nextC <= 57)
                        {
                            temp.append(nextC);
                            if (i + 1 == len)
                            {
                                break;
                            }
                            nextC = nodeValue.charAt(++i);
                        }

                        --i;
                    }
                }

                String tempStr = temp.toString();
                temp.delete(0, temp.length());

                if (StringUtil.isIncludedInArray(m_uselessWords, tempStr))
                {
                    result.add(tempStr);
                }
                else
                {
                    temp.append("<ph type=\"headerFooter\"  erasable=\"no\" movable=\"no\">");
                    temp.append("&amp;amp;");
                    temp.append(m_xmlEncoder.encodeStringBasic(tempStr
                            .substring(1)));
                    temp.append("</ph>");

                    result.add(temp.toString());
                    temp.delete(0, temp.length());
                }
            }
            else
            {
                temp.append(c);
            }

            if (i == len - 1)
            {
                String tempStr = temp.toString();
                if (tempStr.length() > 0)
                {
                    result.add(tempStr);
                    temp.delete(0, temp.length());
                }
            }
        }

        return result;
    }

    private void cdataProcessor(Node p_node, boolean switchesExtraction,
            boolean isInExtraction, boolean isTranslatable)
    {
        String nodeValue = p_node.getNodeValue();
        String preservedTag = ATTRIBUTE_PRESERVE_CLOSED_TAG + "=\"\"";
        if (nodeValue.contains(preservedTag))
        {
            nodeValue = StringUtil.replace(nodeValue, preservedTag, "");
        }
        XmlFilterCDataTag tag = m_xmlFilterHelper.getRuleForCData(p_node);
        boolean isCdataTranslatable = (tag == null || tag.isTranslatable());
        // String cdata = "<![CDATA[" + p_node.getNodeValue() + "]]>";

        // (CvdL: yes, this will lose CDATA nodes.)
        // 2006-09-12 Updated For CDATA issue: this will pick up CDATA
        // nodes.
        if (isInExtraction && isCdataTranslatable)
        {
            Filter postFilter = null;
            try
            {
                postFilter = (tag != null) ? tag.getPostFilter() : null;
            }
            catch (Exception e)
            {
                CATEGORY.error("Can not get post filter for CData", e);
            }

            String otherFormat = (postFilter != null) ? m_xmlFilterHelper
                    .getFormatForFilter(postFilter.getFilterTableName()) : null;

            if (switchesExtraction || m_isCdataPost || postFilter != null)
            {
                outputOtherFormat();
                outputSkeleton("<![CDATA[");
                m_switchExtractionBuffer += nodeValue;

                if (postFilter != null && otherFormat != null)
                {
                    outputOtherFormatForCdata(otherFormat, postFilter, false);
                }
                else if (m_isCdataPost)
                {
                    outputOtherFormatForCdata(null, null, true);
                }
                else
                {
                    outputOtherFormatForCdata(null, null, false);
                }

                outputSkeleton("]]>");
            }
            else
            {
                outputSkeleton("<![CDATA[");
                // handle internal text for cdata
                if (m_internalTexts != null && m_internalTexts.size() > 0)
                {
                    List<String> handled = InternalTextHelper
                            .handleStringWithListReturn(nodeValue,
                                    m_internalTexts, getMainFormat());

                    for (int i = 0; i < handled.size(); i++)
                    {
                        String s = handled.get(i);
                        if (!s.startsWith(InternalTextHelper.GS_INTERNALT_TAG_START))
                        {
                            s = m_xmlEncoder.encodeStringBasic(s);
                            handled.set(i, s);
                        }
                    }

                    int oriIndex = m_admin.getBptIndex();
                    int newIndex = InternalTextHelper.assignIndexToBpt(
                            oriIndex, handled);
                    for (int k = 0; k < newIndex - oriIndex; k++)
                    {
                        m_admin.incrementBptIndex();
                    }

                    String temp = InternalTextHelper.listToString(handled);
                    outputExtractedStuff(temp, isTranslatable, false);
                }
                else
                {
                    outputExtractedStuff(
                            m_xmlEncoder.encodeStringBasic(nodeValue),
                            isTranslatable, false);
                }
                outputSkeleton("]]>");
            }
        }
        else
        {
            outputSkeleton("<![CDATA[" + nodeValue + "]]>");
        }
    }

    private void entityProcessor(Node p_node, boolean switchesExtraction,
            boolean isInExtraction, boolean isTranslatable)
    {
        String entityTag = p_node.getNodeName();
        String name = "&" + entityTag + ";";
        XmlFilterEntity xmlEntity = m_xmlFilterHelper
                .getMatchedXmlFilterEntity(entityTag);
        boolean handled = false;

        if (xmlEntity != null && !switchesExtraction && !m_isElementPost)
        {
            if (xmlEntity.getHandleType() == XmlFilterConstants.ENTITY_PLACEHOLDER)
            {
                if (isInExtraction)
                {
                    String entityRef = m_xmlEncoder.encodeStringBasic(name);

                    StringBuffer temp = new StringBuffer();
                    temp.append("<ph type=\"").append("entity-");
                    temp.append(entityTag).append("\">").append(entityRef);
                    temp.append("</ph>");

                    String stuff = temp.toString();
                    outputExtractedStuff(stuff, isTranslatable, false);
                }
                else
                {
                    outputSkeleton(name);
                }
                handled = true;
            }
            else if (xmlEntity.getHandleType() == XmlFilterConstants.ENTITY_TEXT)
            {
                if (xmlEntity.getSaveAs() == XmlFilterConstants.ENTITY_SAVE_AS_ENTITY)
                {
                    if (isInExtraction)
                    {
                        String entityRef = m_xmlEncoder.encodeStringBasic(name);

                        StringBuffer temp = new StringBuffer();
                        temp.append("<ph type=\"").append("entity-");
                        temp.append(entityTag).append("\">").append(entityRef);
                        temp.append("</ph>");

                        String stuff = temp.toString();
                        outputExtractedStuff(stuff, isTranslatable, false);
                    }
                    else
                    {
                        outputSkeleton(name);
                    }
                    handled = true;
                }
                else if (xmlEntity.getSaveAs() == XmlFilterConstants.ENTITY_SAVE_AS_CHAR)
                {
                    m_admin.addContent(xmlEntity.getEntityCharacter());
                    handled = true;
                }
                else
                {
                    handled = false;
                }
            }
            else
            {
                handled = false;
            }
        }

        if (!handled)
        {
            if (isInExtraction)
            {
                if (switchesExtraction || m_isElementPost)
                {
                    m_switchExtractionBuffer += name;
                }
                else
                {
                    String entityRef = m_xmlEncoder.encodeStringBasic(name);

                    StringBuffer temp = new StringBuffer();
                    temp.append("<ph type=\"").append("entity-");
                    temp.append(entityTag).append("\">").append(entityRef);
                    temp.append("</ph>");

                    String stuff = temp.toString();

                    outputExtractedStuff(stuff, isTranslatable, false);
                }
            }
            else
            {
                outputSkeleton(name);
            }
        }
    }

    /**
     * A visitor that recursivly traverses the input document. Element nodes are
     * handed off to domElementProcessor().
     */
    private void domNodeVisitor(Node p_node, boolean isInExtraction,
            boolean isTranslatable, boolean switchesExtraction,
            boolean... isTextNodeDontTranslateInline) throws ExtractorException
    {
        while (true)
        {
            if (p_node == null)
            {
                return;
            }

            switch (p_node.getNodeType())
            {
                case Node.DOCUMENT_NODE: // the document itself
                    // XML Declaration
                    if (m_haveXMLDecl)
                        outputXMLDeclaration();

                    // Document Type Declaration <!DOCTYPE...>
                    DocumentType docType = ((Document) p_node).getDoctype();
                    if (docType != null)
                        docTypeProcessor(docType);

                    domNodeVisitor(p_node.getFirstChild(), isInExtraction,
                            isTranslatable, switchesExtraction);

                    return;

                case Node.PROCESSING_INSTRUCTION_NODE: // PI
                    outputPi(p_node, switchesExtraction, isInExtraction,
                            isTranslatable);
                    p_node = p_node.getNextSibling();

                    break;

                case Node.ELEMENT_NODE:
                    domElementProcessor(p_node, isInExtraction,
                            switchesExtraction);
                    p_node = p_node.getNextSibling();

                    break;

                case Node.COMMENT_NODE:
                    commentProcessor(switchesExtraction, p_node,
                            isInExtraction, isTranslatable);
                    p_node = p_node.getNextSibling();

                    break;

                case Node.ENTITY_REFERENCE_NODE:
                    entityProcessor(p_node, switchesExtraction, isInExtraction,
                            isTranslatable);
                    p_node = p_node.getNextSibling();

                    break;

                case Node.TEXT_NODE:
                    textProcessor(p_node, switchesExtraction, isInExtraction,
                            isTranslatable, isTextNodeDontTranslateInline);
                    p_node = p_node.getNextSibling();

                    break;

                case Node.CDATA_SECTION_NODE:
                    // String cdata = "<![CDATA[" + p_node.getNodeValue() +
                    // "]]>";
                    // (CvdL: yes, this will lose CDATA nodes.)
                    // 2006-09-12 Updated For CDATA issue: this will pick up
                    // CDATA
                    // nodes.
                    cdataProcessor(p_node, switchesExtraction, isInExtraction,
                            isTranslatable);

                    p_node = p_node.getNextSibling();

                    break;

                default:
                    // shouldn't reach here.
                    // outputSkeleton(domDumpXML(p_node));
                    domNodeVisitor(p_node.getNextSibling(), false,
                            isTranslatable, switchesExtraction);

                    return;
            }
        }
    }

    /**
     * This method is just for xml files converted from Indesign files (.indd).
     * Mark a font style tag by TMX specification tags. After being marked by
     * TMX specification tags, these font style tag can be easily converted into
     * Ptags.
     * 
     * @param p_paraText
     * @return
     */
    private String markIndesignParagraphTextStyleTag(String p_paraText)
    {
        StringBuffer newParaText = new StringBuffer();
        int bptIndex = 0;
        // [contents-contents-contents]xxxxxx[/contents-contents-contents]
        Pattern p = Pattern
                .compile("\\[([^/-]+-[^-]+-[^\\]]+)\\]([^\\[]*)\\[/(\\1)\\]");
        Matcher m = p.matcher(p_paraText);
        while (m.find())
        {
            String tagName = m.group(1);
            bptIndex = m_admin.incrementBptIndex();
            // Mark open font style tag
            newParaText.append("<bpt i=\"" + bptIndex + "\" type=\"");
            newParaText.append(tagName + "\"");
            newParaText.append(">[" + tagName + "]</bpt>");
            // Append translatable fragment
            newParaText.append(m_xmlEncoder.encodeStringBasic(m.group(2)));
            // Mark close font style tag
            newParaText.append("<ept i=\"" + bptIndex + "\">[/");
            newParaText.append(tagName + "]</ept>");
        }
        return newParaText.length() > 0 ? newParaText.toString() : p_paraText;
    }

    /**
     * Recursively processes an element node, its attributes and children. The
     * rules are consulted to determine whether the node needs to be extracted
     * etc. Attributes are handed off to outputAttributes(), and all nodes below
     * this node are passed to domNodeVisitor().
     */
    private void domElementProcessor(Node p_node, boolean isInExtraction,
            boolean switchesExtraction) throws ExtractorException
    {
        String name = p_node.getNodeName();
        int bptIndex = 0;

        String dataFormat = null;
        String type = null;
        boolean isEmbeddable = false;
        boolean isTranslatable = true;
        boolean isMovable = true;
        boolean isErasable = false;
        boolean extracts = Rule.extracts(m_ruleMap, p_node);
        boolean containedInHtml = Rule.isContainedInHtml(m_ruleMap, p_node);
        isEmbeddable = Rule.isInline(m_ruleMap, p_node);
        boolean isInternal = Rule.isInternal(m_ruleMap, p_node);
        boolean isInternalWR = false;
        boolean isPreserveWS = false;
        boolean isInstrText = isOfficeInstrText(name);
        boolean outputLinkTipInInstrText = isOfficeTipInInstrText(p_node);

        if (extracts)
        {
            isTranslatable = Rule.isTranslatable(m_ruleMap, p_node);
            isMovable = Rule.isMovable(m_ruleMap, p_node);
            isErasable = Rule.isErasable(m_ruleMap, p_node);
            dataFormat = Rule.getDataFormat(m_ruleMap, p_node);
            type = Rule.getType(m_ruleMap, p_node);
            isPreserveWS = Rule.isPreserveWhiteSpace(m_ruleMap, p_node,
                    m_xmlFilterHelper.isPreserveWhiteSpaces());
        }

        if (m_isOfficeXml)
        {
            if ("w:pPr".equals(name))
            {
                isEmbeddable = false;
            }

            if (isInternalWRNode(p_node, isInternal))
            {
                isInternalWR = true;
            }
        }

        boolean isEmptyTag = p_node.getFirstChild() == null ? true : false;
        boolean isClosedTag = false;
        if (isEmptyTag && m_preserveEmptyTag)
        {
            isClosedTag = isClosedTag(p_node);
        }

        boolean hasProcessInlineChildNode = false;
        // Wed Dec 14 16:27:49 2005 CvdL: Process XML element in
        // another XML element that gets extracted as HTML.
        // Note this is UNTESTED CODE that may break many files.
        if ((isInExtraction && switchesExtraction && containedInHtml)
                || (isInExtraction && extracts && isEmbeddable && m_isElementPostToHtml))
        {
            m_isOriginalXmlNode = true;
            m_switchExtractionBuffer += "<" + name;

            // Write out all attributes.
            NamedNodeMap attrs = p_node.getAttributes();
            for (int i = 0; i < attrs.getLength(); ++i)
            {
                Node att = attrs.item(i);
                String attname = att.getNodeName();
                String value = att.getNodeValue();

                m_switchExtractionBuffer += " " + attname + "=\"" + value
                        + "\"";
            }

            if (isEmptyTag)
            {
                m_switchExtractionBuffer += "/>";
            }
            else
            {
                m_switchExtractionBuffer += ">";

                // Traverse the tree in isInExtraction mode
                domNodeVisitor(p_node.getFirstChild(), true, true, true);

                m_switchExtractionBuffer += "</" + name + ">";
            }

            return;
        }

        if (switchesExtraction || m_isElementPost)
        {
            outputOtherFormat(this.m_switchExtractionSid);
        }

        int phConsolidationCount = 0;
        int phTrimCount = 0;
        boolean usePhForNode = false;
        boolean combinWR = false;
        // Open the element
        if (extracts)
        {
            if (dataFormat != null)
            {
                m_otherFormat = dataFormat;
                switchesExtraction = true;
                isEmbeddable = false;
            }

            String stuff = null;

            // output internal WR tag with childs
            if (isInternalWR)
            {
                outputInternalWR(p_node, isTranslatable, isPreserveWS);
                return;
            }

            if (isEmbeddable)
            {
                if (isEmptyTag)
                {
                    stuff = "<ph type=\"" + (type != null ? type : name) + "\"";
                }
                else if (isInternal)
                {
                    bptIndex = m_admin.incrementBptIndex();
                    stuff = "<bpt i=\"" + bptIndex + "\" internal=\"yes\"";
                }
                else
                {
                    if (m_isOfficeXml && isWRNode(name))
                    {
                        combinWR = isCombinWR(p_node);
                    }

                    if (!combinWR)
                    {
                        int mode = -1;
                        if (m_isOfficeXml)
                        {
                            mode = XmlFilterConfigParser.PH_CONSOLIDATE_ADJACENT;
                        }
                        else if (m_isIdmlXml)
                        {
                            mode = XmlFilterConfigParser.PH_CONSOLIDATE_ADJACENT_IGNORE_SPACE;
                        }

                        if (mode != 1)
                        {
                            phConsolidationCount = m_xmlFilterHelper
                                    .countPhConsolidation(p_node, m_ruleMap,
                                            mode, m_isIdmlXml);
                        }
                        else
                        {
                            phConsolidationCount = m_xmlFilterHelper
                                    .countPhConsolidation(p_node, m_ruleMap);
                        }

                        // move the current node if consolidate
                        if (phConsolidationCount > 0)
                        {
                            p_node = getChildNode(p_node, phConsolidationCount);
                        }
                    }
                    else
                    {
                        p_node = getCombinWT(p_node);
                    }

                    if (combinWR || phConsolidationCount > 0)
                    {
                        isMovable = Rule.isMovable(m_ruleMap, p_node);
                        isErasable = Rule.isErasable(m_ruleMap, p_node);
                        type = Rule.getType(m_ruleMap, p_node);
                        isPreserveWS = Rule.isPreserveWhiteSpace(m_ruleMap,
                                p_node,
                                m_xmlFilterHelper.isPreserveWhiteSpaces());
                        name = p_node.getNodeName();
                    }

                    boolean isTranslate = true;
                    if (checkTextNode(p_node))
                    {
                        isTranslate = true;
                    }
                    else
                    {
                        isTranslate = false;
                    }
                    String innerTextNodeIndex = getTextNodeIndex(p_node);
                    bptIndex = m_admin.incrementBptIndex();
                    stuff = "<bpt i=\"" + bptIndex + "\" type=\""
                            + (type != null ? type : name)
                            + "\" isTranslate=\"" + isTranslate
                            + "\" innerTextNodeIndex=\"" + innerTextNodeIndex
                            + "\"";
                }

                if (isErasable && !isInternal)
                {
                    stuff += " erasable=\"yes\"";
                }

                if (!isMovable && !isInternal)
                {
                    stuff += " movable=\"no\"";
                }

                stuff += ">";
                outputExtractedStuff(stuff, isTranslatable, isPreserveWS);

                // output tags into bpt
                if (phConsolidationCount > 0)
                {
                    Node startNode = p_node;
                    String startNodeName = null;
                    startNode = getParentNode(startNode, phConsolidationCount);

                    for (int i = 0; i < phConsolidationCount; i++)
                    {
                        startNodeName = startNode.getNodeName();
                        outputExtractedStuff("&lt;" + startNodeName,
                                isTranslatable, isPreserveWS);

                        // Process the attributes
                        NamedNodeMap startNodeAttrs = startNode.getAttributes();
                        boolean shouldNotExtractAttrs = isShouldNotExtract(
                                startNodeName, startNodeAttrs);
                        outputAttributes(startNode, startNodeAttrs,
                                isEmbeddable, shouldNotExtractAttrs);

                        outputExtractedStuff("&gt;", isTranslatable,
                                isPreserveWS);

                        NodeList nodes = startNode.getChildNodes();
                        for (int j = 0; j < nodes.getLength(); j++)
                        {
                            Node node = nodes.item(j);
                            if (node.getNodeType() == Node.ELEMENT_NODE)
                            {
                                startNode = node;

                                if (m_isIdmlXml && p_node.equals(node))
                                {
                                    break;
                                }

                            }
                            else if (node.getNodeType() == Node.TEXT_NODE)
                            {
                                outputExtractedStuff(node.getNodeValue(),
                                        isTranslatable, isPreserveWS);
                            }
                        }
                    }
                }

                // output combined w:r, w:r is parent of w:t
                if (combinWR)
                {
                    Node wrNode = p_node.getParentNode();
                    outputExtractedStuff("&lt;" + wrNode.getNodeName(),
                            isTranslatable, isPreserveWS);
                    outputAttributes(wrNode, wrNode.getAttributes(),
                            isEmbeddable, false);
                    outputExtractedStuff("&gt;", isTranslatable, isPreserveWS);

                    NodeList wrChild = wrNode.getChildNodes();

                    if (wrChild.getLength() > 1)
                    {
                        Node wrprNode = wrChild.item(0);
                        String wrprNodeName = wrprNode.getNodeName();
                        outputExtractedStuff("&lt;" + wrprNodeName,
                                isTranslatable, isPreserveWS);
                        outputAttributes(wrprNode, wrprNode.getAttributes(),
                                isEmbeddable, false);
                        outputExtractedStuff("&gt;", isTranslatable,
                                isPreserveWS);

                        outputChildUsePh(wrprNode, isTranslatable,
                                isPreserveWS, false);

                        outputExtractedStuff("&lt;/" + wrprNodeName + "&gt;",
                                isTranslatable, isPreserveWS);
                    }
                }

                stuff = "&lt;" + name;
                outputExtractedStuff(stuff, isTranslatable, isPreserveWS);
            }
            else
            // isEmbeddable
            {
                outputSkeleton("<" + name);
            }
        }
        else
        // extracts
        {
            // Process the don't translate inline attribute
            String stuff = null;

            if (isEmbeddable)
            {
                if (!isEmptyTag && !outputLinkTipInInstrText)
                {
                    // usePhForNode = checkIsAllNonExtract(p_node);
                    usePhForNode = true;
                }

                if (usePhForNode)
                {
                    stuff = "<ph type=\"" + (type != null ? type : name) + "\"";
                    if (isErasable)
                    {
                        stuff += " erasable=\"yes\"";
                    }

                    if (!isMovable)
                    {
                        stuff += " movable=\"no\"";
                    }
                }
                else if (isEmptyTag)
                {
                    stuff = "<ph type=\"" + (type != null ? type : name) + "\"";
                }
                else if (isInternal)
                {
                    bptIndex = m_admin.incrementBptIndex();
                    stuff = "<bpt i=\"" + bptIndex + "\" internal=\"yes\"";
                }
                else
                {
                    bptIndex = m_admin.incrementBptIndex();
                    stuff = "<bpt i=\"" + bptIndex + "\" type=\""
                            + (type != null ? type : name) + "\"";
                }

                if (outputLinkTipInInstrText && !isInternal)
                {
                    stuff += " isTranslate=\"false\" innerTextNodeIndex=\"\"";
                }

                if (isErasable && !isInternal)
                {
                    stuff += " erasable=\"yes\"";
                }

                if (!isMovable && !isInternal)
                {
                    stuff += " movable=\"no\"";
                }

                stuff += ">&lt;" + name;

                outputExtractedStuff(stuff, isTranslatable, isPreserveWS);
            }
            else
            // isEmbeddable
            {
                outputSkeleton("<" + name);
            }
        }

        // Process the attributes
        NamedNodeMap attrs = p_node.getAttributes();
        boolean shouldNotExtract = isShouldNotExtract(name, attrs);
        outputAttributes(p_node, attrs, isEmbeddable, shouldNotExtract);

        if (extracts && isEmbeddable)
        {
            if (isEmptyTag)
            {
                if (m_useEmptyTag || isClosedTag)
                {
                    outputExtractedStuff("/&gt;</ph>", isTranslatable,
                            isPreserveWS);
                }
                else
                {
                    outputExtractedStuff("&gt;&lt;/" + name + "&gt;</ph>",
                            isTranslatable, isPreserveWS);
                }
            }
            else
            {
                outputExtractedStuff("&gt;</bpt>", isTranslatable, isPreserveWS);
            }
        }
        else
        // extracts && isEmbeddable
        {
            if (isEmbeddable)
            {
                if (usePhForNode)
                {
                    outputExtractedStuff("&gt;", isTranslatable, isPreserveWS);
                    outputChildUsePh(p_node, isTranslatable, isPreserveWS,
                            isInstrText);
                    outputExtractedStuff("&lt;/" + name + "&gt;</ph>",
                            isTranslatable, isPreserveWS);
                    hasProcessInlineChildNode = true;
                }
                else if (isEmptyTag)
                {
                    if (m_useEmptyTag || isClosedTag)
                    {
                        outputExtractedStuff("/&gt;</ph>", isTranslatable,
                                isPreserveWS);
                    }
                    else
                    {
                        outputExtractedStuff("&gt;&lt;/" + name + "&gt;</ph>",
                                isTranslatable, isPreserveWS);
                    }
                }
                else
                {
                    if (p_node.getFirstChild() != null
                            && p_node.getFirstChild().getNodeType() == Node.TEXT_NODE)
                    {
                        if (p_node.getChildNodes().getLength() == 1)
                        {
                            String endtext = outputLinkTipInInstrText ? "&gt;</bpt>"
                                    : "&gt;";
                            outputExtractedStuff(endtext, isTranslatable,
                                    isPreserveWS);
                            domNodeVisitor(p_node.getFirstChild(), true,
                                    isTranslatable, switchesExtraction);
                            String endtext2 = outputLinkTipInInstrText ? ""
                                    : "</bpt>";
                            outputExtractedStuff(endtext2, isTranslatable,
                                    isPreserveWS);
                            hasProcessInlineChildNode = true;
                        }
                        else
                        {
                            outputExtractedStuff("&gt;", isTranslatable,
                                    isPreserveWS);
                            domNodeVisitor(p_node.getFirstChild(), false,
                                    isTranslatable, switchesExtraction,
                                    isInExtraction);
                            if (isInExtraction)
                            {
                                outputExtractedStuff("</bpt>", isTranslatable,
                                        isPreserveWS);
                            }
                            hasProcessInlineChildNode = true;
                        }
                    }
                    if (p_node.getFirstChild() != null
                            && p_node.getFirstChild().getNodeType() == Node.ELEMENT_NODE)
                    {
                        if (Rule.isInline(m_ruleMap, p_node.getFirstChild()))
                        {
                            outputExtractedStuff("&gt;", isTranslatable,
                                    isPreserveWS);
                            domNodeVisitor(p_node.getFirstChild(),
                                    isInExtraction, isTranslatable,
                                    switchesExtraction, isInExtraction);
                            outputExtractedStuff("</bpt>", isTranslatable,
                                    isPreserveWS);
                        }
                        else
                        {
                            outputExtractedStuff("&gt;</bpt>", isTranslatable,
                                    isPreserveWS);
                            domNodeVisitor(p_node.getFirstChild(),
                                    isInExtraction, isTranslatable,
                                    switchesExtraction, isInExtraction);
                        }

                        hasProcessInlineChildNode = true;
                    }
                }
            }
            else
            // isEmbeddable
            {
                if (isEmptyTag)
                {
                    if (m_useEmptyTag || isClosedTag)
                    {
                        outputSkeleton("/>");
                    }
                    else
                    {
                        outputSkeleton("></" + name + ">");
                    }
                }
                else
                {
                    outputSkeleton(">");
                    boolean force = m_isOfficeXml | m_isIdmlXml;
                    phTrimCount = m_xmlFilterHelper.countPhTrim(p_node,
                            m_ruleMap, force);

                    // output trimmed tags into skeleton
                    if (phTrimCount > 0)
                    {
                        Node startNode = p_node;
                        String startNodeName = null;

                        for (int i = 0; i < phTrimCount; i++)
                        {
                            startNode = getChildNode(startNode, 1);
                            startNodeName = startNode.getNodeName();
                            outputSkeleton("<" + startNodeName);

                            // Process the attributes
                            NamedNodeMap startNodeAttrs = startNode
                                    .getAttributes();
                            boolean shouldNotExtractAttrs = isShouldNotExtract(
                                    startNodeName, startNodeAttrs);
                            outputAttributes(startNode, startNodeAttrs,
                                    isEmbeddable, shouldNotExtractAttrs);

                            outputSkeleton(">");
                        }

                        p_node = startNode;
                        name = p_node.getNodeName();
                    }
                }
            }
        }

        // Traverse the tree
        if (!hasProcessInlineChildNode)
        {
            domNodeVisitor(p_node.getFirstChild(), extracts, isTranslatable,
                    switchesExtraction);
        }

        if (phTrimCount > 0 || phConsolidationCount > 0)
        {
            int count = phTrimCount > 0 ? phTrimCount : phConsolidationCount;
            Node currentNode = p_node;

            if (!m_isIdmlXml)
            {
                domNodeVisitor(currentNode.getNextSibling(), extracts,
                        isTranslatable, switchesExtraction);

                for (int i = 0; i < count - 1; i++)
                {
                    currentNode = getParentNode(currentNode, 1);
                    domNodeVisitor(currentNode.getNextSibling(), extracts,
                            isTranslatable, switchesExtraction);
                }
            }
        }

        if (switchesExtraction || m_isElementPost)
        {
            outputOtherFormat(this.m_switchExtractionSid);
        }

        // Close the element.
        if (!isEmptyTag && !usePhForNode)
        {
            // if (extracts && isEmbeddable)
            if (isEmbeddable)
            {
                outputExtractedStuff("<ept i=\"" + bptIndex + "\">&lt;/" + name
                        + "&gt;", isTranslatable, isPreserveWS);

                // output consolidation tags into ept
                if (combinWR)
                {
                    Node wrNode = p_node.getParentNode();
                    String wrNodeName = wrNode.getNodeName();
                    outputExtractedStuff("&lt;/" + wrNodeName + "&gt;",
                            isTranslatable, isPreserveWS);
                }
                else if (phConsolidationCount > 0)
                {
                    Node parentNode = p_node;

                    if (m_isIdmlXml)
                    {
                        domNodeVisitor(parentNode.getNextSibling(), extracts,
                                isTranslatable, switchesExtraction);
                    }

                    String parentNodeName = null;
                    for (int i = 0; i < phConsolidationCount; i++)
                    {
                        parentNode = parentNode.getParentNode();
                        parentNodeName = parentNode.getNodeName();
                        outputExtractedStuff("&lt;/" + parentNodeName + "&gt;",
                                isTranslatable, isPreserveWS);
                    }
                }

                outputExtractedStuff("</ept>", isTranslatable, isPreserveWS);
            }
            else
            {
                outputSkeleton("</" + name + ">");

                // output trimed tags into ept
                if (phTrimCount > 0)
                {
                    Node parentNode = p_node;
                    String parentNodeName = null;
                    for (int i = 0; i < phTrimCount; i++)
                    {
                        parentNode = parentNode.getParentNode();
                        parentNodeName = parentNode.getNodeName();
                        outputSkeleton("</" + parentNodeName + ">");
                    }
                }
            }
        }
    }

    /**
     * special handle for TextVariableInstance tag from idml xml
     */
    private boolean handleIdmlTextVariable(Node p_node, String name,
            String type, boolean isTranslatable, boolean extracts,
            boolean isPreserveWS)
    {
        boolean handled = false;
        if (extracts && m_isIdmlXml && name.equals("TextVariableInstance"))
        {
            String phTag = "<ph type=\"" + (type != null ? type : name)
                    + "\" movable=\"no\">";

            String stuff = phTag + "&lt;" + name;

            NamedNodeMap attrs = p_node.getAttributes();
            int length = attrs.getLength();
            for (int i = 0; i < length; ++i)
            {
                Node att = attrs.item(i);
                String attname = att.getNodeName();
                if (ATTRIBUTE_PRESERVE_CLOSED_TAG.equals(attname))
                {
                    // ignore outputting ATTRIBUTE_PRESERVE_CLOSED_TAG attribute
                    // to
                    // GXML
                    continue;
                }

                stuff += " " + attname + "=&quot;";
                String nodeValue = att.getNodeValue();
                if (attname.equals("ResultText"))
                {
                    stuff += "</ph>";
                    stuff += m_xmlEncoder.encodeStringBasic(nodeValue);
                    stuff += phTag;
                }
                else
                {
                    stuff += m_xmlEncoder.encodeStringBasic(m_xmlEncoder
                            .encodeStringBasic(nodeValue));
                }

                stuff += "&quot;";
            }

            stuff += "/&gt;</ph>";

            outputExtractedStuff(stuff, isTranslatable, isPreserveWS);
            handled = true;
        }
        return handled;
    }

    /**
     * Internal w:r with w:t element
     * 
     * @param p_node
     * @param isInternal
     * @return
     */
    private boolean isInternalWRNode(Node p_node, boolean isInternal)
    {
        String name = p_node.getNodeName();
        if (isInternal && "w:r".equals(name))
        {
            NodeList nodes = p_node.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    String nodeName = node.getNodeName();

                    if ("w:t".equals(nodeName))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Output internal w:t with childs
     * 
     * @param wrNode
     * @param isTranslatable
     * @param isPreserveWS
     */
    private void outputInternalWR(Node wrNode, boolean isTranslatable,
            boolean isPreserveWS)
    {
        String stuff;
        String name = "w:r";
        int bptIndex = m_admin.incrementBptIndex();
        stuff = "<bpt i=\"" + bptIndex + "\" internal=\"yes\">";
        outputExtractedStuff(stuff, isTranslatable, isPreserveWS);
        stuff = "&lt;" + name;
        outputExtractedStuff(stuff, isTranslatable, isPreserveWS);
        outputAttributes(wrNode, wrNode.getAttributes(), true, true);
        stuff = "&gt;";
        outputExtractedStuff(stuff, isTranslatable, isPreserveWS);

        NodeList nodes = wrNode.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                String nodeName = node.getNodeName();
                if (nodeName.equals("w:t"))
                {
                    stuff = "&lt;w:t&gt;</bpt>";
                    outputExtractedStuff(stuff, isTranslatable, isPreserveWS);

                    // output text
                    Node fnode = node.getFirstChild();
                    String text = fnode == null ? "" : fnode.getNodeValue();
                    text = m_xmlEncoder.encodeStringBasic(text);
                    outputExtractedStuff(text, isTranslatable, isPreserveWS);

                    stuff = "<ept i=\"" + bptIndex + "\">&lt;/w:t&gt;";
                    outputExtractedStuff(stuff, isTranslatable, isPreserveWS);
                }
                else
                {
                    outputChildToExtractedStuff(node, isTranslatable,
                            isPreserveWS);
                }
            }
        }

        stuff = "&lt;/" + name + "&gt;</ept>";
        outputExtractedStuff(stuff, isTranslatable, isPreserveWS);
    }

    private void outputChildToExtractedStuff(Node node, boolean isTranslatable,
            boolean isPreserveWS)
    {
        boolean isEmptyTag = node.getFirstChild() == null ? true : false;
        String nodeName = node.getNodeName();
        String stuff = "&lt;" + nodeName;
        outputExtractedStuff(stuff, isTranslatable, isPreserveWS);
        outputAttributes(node, node.getAttributes(), true, true);
        stuff = isEmptyTag ? "/&gt;" : "&gt;";
        outputExtractedStuff(stuff, isTranslatable, isPreserveWS);

        if (!isEmptyTag)
        {
            NodeList nodes = node.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node cnode = nodes.item(i);
                if (cnode.getNodeType() == Node.ELEMENT_NODE)
                {
                    outputChildToExtractedStuff(cnode, isTranslatable,
                            isPreserveWS);
                }
            }

            stuff = "&lt;/" + nodeName + "&gt;";
            outputExtractedStuff(stuff, isTranslatable, isPreserveWS);
        }
    }

    /**
     * Checks if the given node is a closed empty tag by checking it has an
     * attribute ATTRIBUTE_PRESERVE_CLOSED_TAG added in method
     * preserveEmptyTag().
     */
    private boolean isClosedTag(Node p_node)
    {
        NamedNodeMap attributes = p_node.getAttributes();
        int length = attributes.getLength();
        if (length == 0)
        {
            // no ATTRIBUTE_PRESERVE_CLOSED_TAG attribute
            return false;
        }
        for (int i = 0; i < length; i++)
        {
            Node attribute = attributes.item(i);
            String attname = attribute.getNodeName();
            if (ATTRIBUTE_PRESERVE_CLOSED_TAG.equals(attname))
            {
                // found ATTRIBUTE_PRESERVE_CLOSED_TAG attribute
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the text is from a tooltip text, excluding TOC text.
     */
    private boolean isOfficeTipInInstrText(Node p_node)
    {
        if (isOfficeInstrText(p_node.getNodeName()))
        {
            try
            {
                String text = p_node.getFirstChild().getNodeValue();
                if (!text.trim().startsWith("TOC") && text.contains(" \\o \""))
                {
                    int index = text.indexOf(" \\o \"");
                    int index2 = text.indexOf("\"", index + 5);

                    if (index2 != -1)
                    {
                        return true;
                    }
                }
            }
            catch (Exception eee)
            {
                return false;
            }
        }

        return false;
    }

    private boolean isOfficeInstrText(String name)
    {
        return m_isOfficeXml && "w:instrText".equals(name);
    }

    private boolean isWRNode(String name)
    {
        return StringUtil.isIncludedInArray(wrNodeNames, name);
    }

    /**
     * get w:t for combined w:r
     * 
     * @param pNode
     * @return
     */
    private Node getCombinWT(Node pNode)
    {
        NodeList childList = pNode.getChildNodes();
        return childList.getLength() == 1 ? childList.item(0) : childList
                .item(1);
    }

    /**
     * check if is this format w:r w:rPr w:t for office xml
     * 
     * @param pNode
     * @return
     */
    private boolean isCombinWR(Node pNode)
    {
        NodeList childList = pNode.getChildNodes();

        if (childList.getLength() == 2)
        {
            Node node0 = childList.item(0);
            Node node1 = childList.item(1);

            if (StringUtil
                    .isIncludedInArray(wrprNodeNames, node0.getNodeName())
                    && StringUtil.isIncludedInArray(wtNodeNames,
                            node1.getNodeName()))
            {
                return true;
            }
        }

        if (childList.getLength() == 1)
        {
            Node node0 = childList.item(0);

            if (StringUtil.isIncludedInArray(wtNodeNames, node0.getNodeName()))
            {
                return true;
            }
        }

        return false;
    }

    private void outputChildUsePh(Node p_node, boolean isTranslatable,
            boolean isPreserveWS, boolean isInstrText)
    {
        NodeList list = p_node.getChildNodes();
        if (list != null)
        {
            for (int i = 0; i < list.getLength(); i++)
            {
                Node node = list.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    String name = node.getNodeName();
                    boolean isEmptyTag = node.getFirstChild() == null ? true
                            : false;
                    boolean isClosedTag = false;
                    if (isEmptyTag && m_preserveEmptyTag)
                    {
                        isClosedTag = isClosedTag(node);
                    }
                    outputExtractedStuff("&lt;" + name, isTranslatable,
                            isPreserveWS);

                    // Process the attributes
                    NamedNodeMap attrs = node.getAttributes();
                    boolean shouldNotExtract = isShouldNotExtract(name, attrs);
                    outputAttributes(node, attrs, true, shouldNotExtract);

                    if (isEmptyTag)
                    {
                        if (m_useEmptyTag || isClosedTag)
                        {
                            outputExtractedStuff("/&gt;", isTranslatable,
                                    isPreserveWS);
                        }
                        else
                        {
                            outputExtractedStuff("&gt;&lt;/" + name + "&gt;",
                                    isTranslatable, isPreserveWS);
                        }
                    }
                    else
                    {
                        outputExtractedStuff("&gt;", isTranslatable,
                                isPreserveWS);
                        outputChildUsePh(node, isTranslatable, isPreserveWS,
                                isInstrText);
                        outputExtractedStuff("&lt;/" + name + "&gt;",
                                isTranslatable, isPreserveWS);
                    }
                }
                else if (node.getNodeType() == Node.TEXT_NODE)
                {
                    String text = node.getNodeValue();

                    text = m_xmlEncoder.encodeStringBasic(text);

                    if (!isInstrText)
                    {
                        text = m_xmlEncoder.encodeStringBasic(text);
                    }

                    outputExtractedStuff(text, isTranslatable, isPreserveWS);
                }
                else
                {
                    // should not here
                }
            }
        }
    }

    private Node getParentNode(Node p_node, int p_parentLayer)
    {
        for (int i = 0; i < p_parentLayer; i++)
        {
            p_node = p_node.getParentNode();
        }
        return p_node;
    }

    private Node getChildNode(Node p_node, int p_childLayer)
    {
        for (int i = 0; i < p_childLayer; i++)
        {
            NodeList nodes = p_node.getChildNodes();
            for (int j = 0; j < nodes.getLength(); j++)
            {
                Node node = nodes.item(j);
                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    p_node = node;
                    break;
                }
            }
        }

        return p_node;
    }

    private String getTextNodeIndex(Node p_node)
    {
        String s = "";
        NodeList nodes = p_node.getChildNodes();
        int length = nodes.getLength();
        if (length > 0)
        {
            for (int i = 0; i < length; i++)
            {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.TEXT_NODE)
                {
                    s += i + ",";
                }
            }
        }
        return s;
    }

    private boolean checkTextNode(Node p_node)
    {
        return getTextNodeIndex(p_node).length() != 0;
    }

    /**
     * Handles internal text.
     */
    private String handleInternalText(String value, boolean isInline,
            boolean isPreserveWS)
    {
        int oriIndex = m_admin.getBptIndex();
        List<String> handled = InternalTextHelper.handleStringWithListReturn(
                value, m_internalTexts, getMainFormat());

        for (int i = 0; i < handled.size(); i++)
        {
            String s = handled.get(i);
            if (!s.startsWith(InternalTextHelper.GS_INTERNALT_TAG_START))
            {
                s = m_xmlFilterHelper.processText(s, isInline, isPreserveWS);
                handled.set(i, s);
            }
        }

        int newIndex = InternalTextHelper.assignIndexToBpt(oriIndex, handled);
        for (int k = 0; k < newIndex - oriIndex; k++)
        {
            m_admin.incrementBptIndex();
        }

        return InternalTextHelper.listToString(handled);
    }

    /**
     * Outputs the attributes of the given {@link OfficeXmlContentTag}.
     */
    private void outputAttributesForOfficeXmlContentTag(OfficeXmlContentTag tag)
    {
        List<OfficeXmlContentTag.Attribute> attributeList = tag
                .getAttributeList();
        if (attributeList.isEmpty())
        {
            return;
        }

        for (OfficeXmlContentTag.Attribute a : attributeList)
        {
            String attname = a.getName();
            String attvalue = a.getValue();
            String strValue = attvalue;
            String quote = "";
            if (attvalue != null)
            {
                strValue = Text.removeQuotes(attvalue);
                quote = Text.getQuoteCharacter(attvalue);
            }

            StringBuilder stuff = new StringBuilder();
            stuff.append(" ");
            stuff.append(attname);
            if (attvalue != null)
            {
                stuff.append("=");
                stuff.append(quote);
                if (m_office2010ContentPostFilter
                        .isTranslatableAttribute(attname))
                {
                    if (!m_isUrlTranslate && isPureUrl(strValue))
                    {
                        stuff.append(m_xmlEncoder.encodeStringBasic(strValue));
                    }
                    else
                    {
                        stuff.append(createSubTag(true, null, null));
                        String temp = m_xmlEncoder.encodeStringBasic(strValue);
                        if (!m_isUrlTranslate)
                        {
                            temp = processUrlInline(temp);
                        }
                        stuff.append(temp);
                        stuff.append("</sub>");
                    }
                }
                else
                {
                    stuff.append(m_xmlEncoder.encodeStringBasic(strValue));
                }

                stuff.append(quote);
            }
            outputExtractedStuff(stuff.toString(), true, false);
        }
    }

    /**
     * <p>
     * Outputs the attributes of the element node being processed by
     * domElementProcessor().
     * </p>
     * 
     * <p>
     * Note that the <code>isInTranslatable</code> argument is not used.
     * </p>
     */
    private void outputAttributes(Node parentNode, NamedNodeMap attrs,
            boolean isEmbeded, boolean shouldNotExtract)
            throws ExtractorException
    {
        if (attrs == null)
        {
            return;
        }
        boolean isEmptyTag = parentNode.getFirstChild() == null ? true : false;
        boolean isClosedTag = false;
        if (isEmptyTag && m_preserveEmptyTag)
        {
            isClosedTag = isClosedTag(parentNode);
        }
        // make xml declare version first
        // <xml version="1.0" encoding="UTF-8" standalone="yes"/>
        Node versionNode = null;
        int length = attrs.getLength();
        if (isClosedTag && "xml".equals(parentNode.getNodeName()))
        {
            if (attrs.getNamedItem("version") != null && length > 2)
            {
                versionNode = attrs.removeNamedItem("version");
                length--;
            }
        }

        for (int i = 0; i < length; ++i)
        {
            Node att = attrs.item(i);
            String attname = att.getNodeName();
            if (isClosedTag && ATTRIBUTE_PRESERVE_CLOSED_TAG.equals(attname))
            {
                // ignore outputting ATTRIBUTE_PRESERVE_CLOSED_TAG attribute to
                // GXML
                continue;
            }
            String value = att.getNodeValue();
            String sid = Rule.getSid(m_ruleMap, att);
            boolean extracts = Rule.extracts(m_ruleMap, att);
            // Only for xml files converted from Indesign.
            if (shouldNotExtract)
            {
                extracts = false;
            }

            boolean isTranslatable = true;
            String dataFormat = null;
            String type = null;

            isTranslatable = Rule.isTranslatable(m_ruleMap, att);
            dataFormat = Rule.getDataFormat(m_ruleMap, att);
            type = Rule.getType(m_ruleMap, att);

            if (isEmbeded)
            {
                String stuff = null;
                if (extracts)
                {
                    stuff = " " + attname + "=&quot;";

                    if (dataFormat != null)
                    {
                        try
                        {
                            Output output = switchExtractor(value, dataFormat);
                            Iterator it = output.documentElementIterator();

                            while (it.hasNext())
                            {
                                DocumentElement element = (DocumentElement) it
                                        .next();
                                boolean isTransOrLoc = false; // true=translatable

                                switch (element.type())
                                {
                                    case DocumentElement.TRANSLATABLE:
                                        isTransOrLoc = true;
                                        // fall through
                                    case DocumentElement.LOCALIZABLE:
                                        Segmentable seg = (Segmentable) element;
                                        stuff += createSubTag(isTransOrLoc,
                                                seg.getType(),
                                                seg.getDataType())
                                                + seg.getChunk() + "</sub>";
                                        break;

                                    case DocumentElement.SKELETON:
                                        stuff += ((SkeletonElement) element)
                                                .getSkeleton();
                                        break;
                                }
                            }
                        }
                        catch (ExtractorException ex)
                        {
                            stuff += createSubTag(isTranslatable, type,
                                    dataFormat)
                                    + m_xmlEncoder.encodeStringBasic(value)
                                    + "</sub>";
                        }
                    }
                    else
                    {
                        if (m_isOfficeXml && !m_isUrlTranslate
                                && isPureUrl(value))
                        {
                            // encoding twice to avoid issue :
                            // w:tooltip="http://support.microsoft.com/contactus/?ws=support&ln=pt-br"
                            stuff += m_xmlEncoder
                                    .encodeStringBasic(m_xmlEncoder
                                            .encodeStringBasic(value));
                        }
                        else
                        {
                            stuff += createSubTag(isTranslatable, type,
                                    dataFormat);
                            String temp = m_xmlEncoder.encodeStringBasic(value);
                            if (m_isOfficeXml && !m_isUrlTranslate)
                            {
                                temp = processUrlInline(temp);
                            }
                            stuff += temp + "</sub>";
                        }
                    }

                    stuff += "&quot;";
                }
                else
                // extracts
                {
                    // encode twice to get a correct merge result
                    stuff = " "
                            + attname
                            + "=&quot;"
                            + m_xmlEncoder.encodeStringBasic(m_xmlEncoder
                                    .encodeStringBasic(value)) + "&quot;";
                }

                m_admin.addContent(stuff);
            }
            else
            // isEmbeded Not embeddable. But is it translatable ?
            {
                if (extracts)
                {
                    boolean isPreserveWS = Rule.isPreserveWhiteSpace(m_ruleMap,
                            parentNode,
                            m_xmlFilterHelper.isPreserveWhiteSpaces());
                    outputSkeleton(" " + attname + "=\"");

                    if (dataFormat != null)
                    {
                        try
                        {
                            Output output = switchExtractor(value, dataFormat);
                            Iterator it = output.documentElementIterator();
                            while (it.hasNext())
                            {
                                outputDocumentElement(
                                        (DocumentElement) it.next(), sid);
                            }
                        }
                        catch (ExtractorException ex)
                        {
                            String stuff = m_xmlEncoder
                                    .encodeStringBasic(value);
                            outputExtractedStuff(stuff, isTranslatable,
                                    isPreserveWS);
                            setSid(sid);
                        }
                    }
                    else
                    {
                        String stuff = value;
                        boolean containsUrl = false;
                        Map<String, String> urlMap = null;
                        if (m_isOfficeXml && !m_isUrlTranslate)
                        {
                            Object[] o = preProcessUrl(value);
                            stuff = (String) o[0];
                            if (!stuff.equals(value))
                            {
                                containsUrl = true;
                                urlMap = (Map<String, String>) o[1];
                            }
                        }
                        // internal text
                        stuff = handleInternalText(stuff, isEmbeded,
                                isPreserveWS);
                        if (containsUrl)
                        {
                            stuff = postProcessUrl(stuff, urlMap);
                        }
                        outputExtractedStuff(stuff, isTranslatable,
                                isPreserveWS);
                        setSid(sid);
                    }

                    outputSkeleton("\"");
                }
                else
                {
                    if (versionNode != null)
                    {
                        String versionName = versionNode.getNodeName();
                        String versionValue = versionNode.getNodeValue();
                        outputSkeleton(" " + versionName + "=\"" + versionValue
                                + "\"");
                        versionNode = null;
                    }

                    outputSkeleton(" " + attname + "=\""
                            + m_xmlEncoder.encodeStringBasic(value) + "\"");
                }
            }
        }
    }

    /**
     * Output translatable text
     */
    private void outputTranslatable(String p_ToAdd, boolean isPreserveWS)
    {
        if (m_admin.getOutputType() != OutputWriter.TRANSLATABLE)
        {
            TranslatableWriter tw = new TranslatableWriter(getOutput());
            tw.setXmlFilterHelper(m_xmlFilterHelper);
            tw.setPreserveWhiteSpace(isPreserveWS);
            m_admin.reset(tw);
        }
        m_admin.addContent(p_ToAdd);
    }

    private void setSid(String sid)
    {
        m_admin.setSid(sid);
    }

    /**
     * Output localizable text
     */
    private void outputLocalizable(String p_ToAdd)
    {
        if (m_admin.getOutputType() != OutputWriter.LOCALIZABLE)
        {
            LocalizableWriter lw = new LocalizableWriter(getOutput());
            lw.setXmlFilterHelper(m_xmlFilterHelper);
            m_admin.reset(lw);
        }
        m_admin.addContent(p_ToAdd);
    }

    /**
     * Outputs skeleton text.
     */
    private void outputSkeleton(String p_ToAdd)
    {
        if (m_admin.getOutputType() != OutputWriter.SKELETON)
        {
            m_admin.reset(new SkeletonWriter(getOutput()));
        }
        m_admin.addContent(p_ToAdd);
    }

    private void outputDocumentElement(DocumentElement element)
    {
        outputDocumentElement(element, null);
    }

    private void outputDocumentElement(DocumentElement element, String sid)
    {
        if (sid != null && element instanceof TranslatableElement)
        {
            ((TranslatableElement) element).setSid(sid);
        }
        m_admin.reset(null);
        getOutput().addDocumentElement(element, true);
    }

    /**
     * Utility function that outputs translatable or localizable text.
     * 
     */
    private void outputExtractedStuff(String stuff, boolean isTranslatable,
            boolean isPreserveWS)
    {
        if (isTranslatable)
        {
            outputTranslatable(stuff, isPreserveWS);
        }
        else
        {
            outputLocalizable(stuff);
        }
    }

    private String createSubTag(boolean isTranslatable, String type,
            String dataFormat)
    {
        String stuff = "<sub";
        stuff += " locType=\""
                + (isTranslatable ? "translatable" : "localizable") + "\"";

        if (type != null)
        {
            stuff += " type=\"" + type + "\"";
        }

        if (dataFormat != null)
        {
            stuff += " datatype=\"" + dataFormat + "\"";
        }
        stuff += ">";

        return stuff;
    }

    /**
     * Flushes text collected for another Extractor by calling the Extractor on
     * the text in m_switchExtractionBuffer and writing its output to the output
     * object.
     */
    private void outputOtherFormat() throws ExtractorException
    {
        try
        {
            String otherFormat = (m_isElementPost) ? m_elementPostFormat
                    : m_otherFormat;
            Filter otherFilter = (m_isElementPost) ? m_xmlFilterHelper
                    .getElementPostFilter() : null;
            outputOtherFormat(otherFormat, otherFilter, false, null);
        }
        catch (Exception ex)
        {
            CATEGORY.error("Output other format with error: ", ex);
            outputTranslatable(
                    m_xmlEncoder.encodeStringBasic(m_switchExtractionBuffer),
                    false);
            m_switchExtractionBuffer = new String();
            m_switchExtractionSid = null;
        }
    }

    private void outputOtherFormat(String sid) throws ExtractorException
    {
        try
        {
            String otherFormat = (m_isElementPost) ? m_elementPostFormat
                    : m_otherFormat;
            Filter otherFilter = (m_isElementPost) ? m_xmlFilterHelper
                    .getElementPostFilter() : null;
            outputOtherFormat(otherFormat, otherFilter, false, sid);
        }
        catch (Exception ex)
        {
            CATEGORY.error("Output other format with error: ", ex);
            outputTranslatable(
                    m_xmlEncoder.encodeStringBasic(m_switchExtractionBuffer),
                    false);
            setSid(sid);
            m_switchExtractionBuffer = new String();
            m_switchExtractionSid = null;
        }
    }

    private void outputOtherFormatForCdata(String p_otherFormat,
            Filter p_otherFilter, boolean p_useGlobal)
            throws ExtractorException
    {
        try
        {
            String otherFormat = (p_useGlobal) ? m_cdataPostFormat : null;
            if (otherFormat == null)
            {
                otherFormat = (p_otherFormat != null) ? p_otherFormat
                        : m_otherFormat;
            }

            Filter otherFilter = (p_useGlobal) ? m_xmlFilterHelper
                    .getCdataPostFilter() : null;
            if (otherFilter == null)
            {
                otherFilter = (p_otherFilter != null) ? p_otherFilter : null;
            }

            outputOtherFormat(otherFormat, otherFilter, true, null);
        }
        catch (Exception ex)
        {
            CATEGORY.error("Output other format with error: ", ex);
            outputTranslatable(
                    m_xmlEncoder.encodeStringBasic(m_switchExtractionBuffer),
                    false);
            m_switchExtractionBuffer = new String();
            m_switchExtractionSid = null;
        }
    }

    /**
     * Flushes text collected for another Extractor by calling the Extractor on
     * the text in m_switchExtractionBuffer and writing its output to the output
     * object.
     * 
     * @param otherFormat
     * @param otherFilter
     * @param isCdata
     * @param sid
     */
    private void outputOtherFormat(String otherFormat, Filter otherFilter,
            boolean isCdata, String sid)
    {
        if (m_switchExtractionBuffer.length() == 0)
        {
            return;
        }

        if (m_xmlFilterHelper.isBlankOrExblank(m_switchExtractionBuffer))
        {
            outputSkeleton(m_switchExtractionBuffer);
        }
        else
        {
            try
            {
                Output output = switchExtractor(m_switchExtractionBuffer,
                        otherFormat, otherFilter);
                Iterator it = output.documentElementIterator();
                while (it.hasNext())
                {
                    DocumentElement element = (DocumentElement) it.next();
                    switch (element.type())
                    {
                        case DocumentElement.TRANSLATABLE: // fall through
                        case DocumentElement.LOCALIZABLE:
                            Segmentable segmentableElement = (Segmentable) element;
                            segmentableElement.setDataType(otherFormat);
                            fixEntitiesForOtherFormat(segmentableElement,
                                    (isCdata || m_isOriginalXmlNode));
                            outputDocumentElement(element, sid);
                            break;

                        case DocumentElement.SKELETON:
                            String skeleton = ((SkeletonElement) element)
                                    .getSkeleton();
                            skeleton = (isCdata || m_isOriginalXmlNode) ? m_xmlEncoder
                                    .decodeStringBasic(skeleton) : skeleton;
                            outputSkeleton(skeleton);
                            break;
                    }
                }
            }
            catch (ExtractorException ex)
            {
                CATEGORY.error("Output other format with error: ", ex);
                outputTranslatable(
                        m_xmlEncoder
                                .encodeStringBasic(m_switchExtractionBuffer),
                        false);
                setSid(sid);
            }
        }

        m_switchExtractionBuffer = new String();
        m_switchExtractionSid = null;
        m_isOriginalXmlNode = false;
        // m_otherFormat = null;
    }

    /**
     * encode twice for this kind of element text : &amp;lt;p&amp;gt; here is p
     * &amp;lt;/p&amp;gt; TODO : but not for original XML element
     */
    private void fixEntitiesForOtherFormat(Segmentable element, boolean isCdata)
    {
        if (isCdata)
        {
            return;
        }

        String[] tagNames =
        { "bpt", "ept", "it", "ph" };
        String result = encodingEntitiesForOtherFormat(element.getChunk(),
                tagNames);
        element.setChunk(result);
    }

    private String encodingEntitiesForOtherFormat(String chunk,
            String[] tagNames)
    {
        String result = chunk;
        for (String tagName : tagNames)
        {
            result = encodingEntitiesForOtherFormat(result, tagName);
        }

        return result;
    }

    private String encodingEntitiesForOtherFormat(String chunk, String tagName)
    {
        StringBuffer ori = new StringBuffer(chunk);
        StringBuffer result = new StringBuffer(chunk.length());
        String endTag = "</" + tagName + ">";
        String startTag = "<" + tagName;
        int fromIndex = 0;
        int index_e = ori.indexOf(endTag, fromIndex);

        if (index_e != -1)
        {
            int index_s = ori.indexOf(startTag, fromIndex);

            while (index_e != -1 && index_s != -1 && index_s < index_e)
            {
                int endIndex = index_e + endTag.length();
                int index_se = ori.indexOf(">", index_s);
                if (index_se != -1)
                {
                    result.append(ori.substring(fromIndex, index_se + 1));
                    String temp = ori.substring(index_se + 1, index_e);
                    String encoded = encodeStringBasicExceptSub(temp);
                    result.append(encoded);
                    result.append(ori.substring(index_e, endIndex));
                }
                else
                {
                    result.append(ori.substring(fromIndex, endIndex));
                }

                fromIndex = endIndex;
                index_e = ori.indexOf(endTag, fromIndex);
                index_s = ori.indexOf(startTag, fromIndex);
            }

            if (ori.length() > fromIndex)
            {
                result.append(ori.substring(fromIndex));
            }

            return result.toString();
        }
        else
        {
            return chunk;
        }
    }

    private String encodeStringBasicExceptSub(String temp)
    {
        if (temp.indexOf("<") == -1)
        {
            return m_xmlEncoder.encodeStringBasic(temp);
        }
        else
        {
            StringBuffer result = new StringBuffer();
            Pattern p = Pattern
                    .compile("<[^<>]*>[^<>]*</[^<>]*>|<[^<>]*/\\s*>");
            Matcher m = p.matcher(temp);
            int fromIndex = 0;
            if (m.find())
            {
                do
                {
                    int start = m.start();
                    int end = m.end();
                    result.append(m_xmlEncoder.encodeStringBasic(temp
                            .substring(fromIndex, start)));
                    result.append(temp.substring(start, end));
                    fromIndex = end;
                } while (m.find());

                if (fromIndex < temp.length())
                {
                    result.append(m_xmlEncoder.encodeStringBasic(temp
                            .substring(fromIndex)));
                }
            }
            else
            {
                result.append(temp);
            }

            return result.toString();
        }
    }

    /**
     * Processes a GSA comment. If it's GSA snippet, adds the GSA tag to the
     * Output object and returns true.
     */
    private boolean processGsaSnippet(String comments)
            throws ExtractorException
    {
        try
        {
            RegExMatchInterface match = RegEx.matchSubstring(comments,
                    "^\\s*gs\\s", false);

            if (match == null)
            {
                match = RegEx.matchSubstring(comments, "^\\s*/gs", false);
                if (match == null)
                {
                    return false;
                }

                outputGsaEnd();
                return true;
            }

            boolean delete = false;
            String extract = null;
            String description = null;
            String locale = null;
            String add = null;
            String added = null;
            String deleted = null;
            String snippetName = null;
            String snippetId = null;

            match = RegEx.matchSubstring(comments,
                    "\\sadd\\s*=\\s*\"([^\"]+)\"\\s", false);
            if (match != null)
            {
                add = match.group(1);
            }

            match = RegEx.matchSubstring(comments,
                    "\\sextract\\s*=\\s*\"([^\"]+)\"\\s", false);
            if (match != null)
            {
                extract = match.group(1);
            }

            match = RegEx.matchSubstring(comments,
                    "\\sdescription\\s*=\\s*\"([^\"]+)\"\\s", false);
            if (match != null)
            {
                description = match.group(1);
            }

            match = RegEx.matchSubstring(comments,
                    "\\slocale\\s*=\\s*\"([^\"]+)\"\\s", false);
            if (match != null)
            {
                locale = match.group(1);
            }

            match = RegEx.matchSubstring(comments,
                    "\\sname\\s*=\\s*\"([^\"]+)\"\\s", false);
            if (match != null)
            {
                snippetName = match.group(1);
            }

            match = RegEx.matchSubstring(comments,
                    "\\sid\\s*=\\s*\"([^\"]+)\"\\s", false);
            if (match != null)
            {
                snippetId = match.group(1);
            }

            match = RegEx.matchSubstring(comments,
                    "\\sdelete\\s*=\\s*\"?(1|yes|true)\"?\\s", false);
            if (match != null)
            {
                delete = true;
            }

            match = RegEx.matchSubstring(comments,
                    "\\sdeleted\\s*=\\s*\"([^\"]+)\"\\s", false);
            if (match != null)
            {
                deleted = match.group(1);
            }

            match = RegEx.matchSubstring(comments,
                    "\\sadded\\s*=\\s*\"([^\"]+)\"\\s", false);
            if (match != null)
            {
                added = match.group(1);
            }

            outputGsaStart(extract, description, locale, add, delete, added,
                    deleted, snippetName, snippetId);
        }
        catch (RegExException e)
        {
            // Shouldn't reach here.
            System.err.println("Malformed re pattern in XML extractor.");
        }

        return true;
    }

    private void outputGsaStart(String extract, String description,
            String locale, String add, boolean delete, String added,
            String deleted, String snippetName, String snippetId)
            throws ExtractorException
    {
        m_admin.reset(null);

        try
        {
            getOutput().addGsaStart(extract, description, locale, add, delete,
                    added, deleted, snippetName, snippetId);
        }
        catch (DocumentElementException ex)
        {
            throw new ExtractorException(HTML_GS_TAG_ERROR, ex.toString());
        }
    }

    private void outputGsaEnd()
    {
        m_admin.reset(null);

        getOutput().addGsaEnd();
    }

    /**
     * Special treat for InDesign paragraph attributes Because we rely on some
     * attributes to convert XML for adjusting font style information, we could
     * not let them changed by translator.
     * 
     * @param nodeName
     * @param nodeAttrs
     * 
     * @return true | false
     */
    private boolean isShouldNotExtract(String nodeName, NamedNodeMap nodeAttrs)
    {
        //
        boolean shouldNotExtract = false;
        if (nodeName.equals(PARAGRAPH_NODE_NAME))
        {
            for (int i = 0; i < nodeAttrs.getLength(); ++i)
            {
                Node att = nodeAttrs.item(i);
                String attname = att.getNodeName();
                if (attname.equals(PARAGRAPH_HAS_DIFFERENT_STYLE))
                {
                    String value = att.getNodeValue();
                    if (value.equals(VALUE_TRUE))
                    {
                        shouldNotExtract = true;
                        break;
                    }
                }
            }
        }

        return shouldNotExtract;
    }

    public boolean isIdmlXml()
    {
        return m_isIdmlXml;
    }

    public void setIsIdmlXml(boolean mIsIdmlXml)
    {
        m_isIdmlXml = mIsIdmlXml;
    }

    public boolean isOfficeXml()
    {
        return m_isOfficeXml;
    }

    /**
     * Adds a flag attribute to the empty tags in order to be recognized during
     * XML parsing so that they can be preserved as original format while
     * writing to GXML.
     */
    private void preserveEmptyTag()
    {
        File f = getInput().getFile();
        if (f == null)
        {
            return;
        }
        try
        {
            String encoding = FileUtil.guessEncoding(f);
            if (encoding == null)
            {
                encoding = getInput().getCodeset();
            }
            String content = FileUtil.readFile(f, encoding);

            Pattern p = Pattern.compile("<([^>]*)/>");
            Matcher m = p.matcher(content);
            while (m.find())
            {
                String tagName = m.group(1);
                if (!tagName.contains(ATTRIBUTE_PRESERVE_CLOSED_TAG))
                {
                    String newTagName = tagName + " "
                            + ATTRIBUTE_PRESERVE_CLOSED_TAG + "=\"\"";
                    content = StringUtil.replace(content, m.group(), "<"
                            + newTagName + "/>");
                }
            }
            FileUtil.writeFile(f, content, encoding);
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Error happens while handling XML for preserving empty tags.",
                    e);
        }
    }

    /**
     * Checks if the given text contains pure URLs.
     */
    private boolean isPureUrl(String text)
    {
        return StringUtil.isEmpty(text.replaceAll(RE_URL, ""));
    }

    /**
     * Processes the given text with URLs to generate output with inline tags.
     */
    private String processUrlInline(String text)
    {
        StringBuilder output = new StringBuilder();

        Pattern p = Pattern.compile(RE_URL);
        Matcher m = p.matcher(text);

        while (m.find())
        {
            String url = m.group();
            int urlIndex = text.indexOf(url);

            output.append(text.substring(0, urlIndex));
            output.append("<ph type=\"url\">");
            output.append(url);
            output.append("</ph>");

            text = text.substring(urlIndex + url.length());
        }
        output.append(text);

        return output.toString();
    }

    /**
     * Processes the given text to hide the URLs to avoid being handled in next
     * processes if URLs are set not to be translated.
     */
    private Object[] preProcessUrl(String text)
    {
        StringBuilder output = new StringBuilder();
        Map<String, String> urlMap = new HashMap<String, String>();

        Pattern p = Pattern.compile(RE_URL);
        Matcher m = p.matcher(text);

        int i = 0;
        while (m.find())
        {
            String url = m.group();
            int urlIndex = text.indexOf(url);

            output.append(text.substring(0, urlIndex));
            output.append("<ph type=\"url\">");
            output.append(i);
            output.append("</ph>");

            urlMap.put(String.valueOf(i), XmlUtil.escapeString(url));

            text = text.substring(urlIndex + url.length());
            i++;
        }
        output.append(text);

        return new Object[]
        { output.toString(), urlMap };
    }

    /**
     * Adds the original URLs back to the given text.
     */
    private String postProcessUrl(String text, Map<String, String> urlMap)
    {
        Pattern p = Pattern
                .compile("&lt;ph type=&quot;url&quot;&gt;(\\d)&lt;/ph&gt;");
        Matcher m = p.matcher(text);

        while (m.find())
        {
            String key = m.group(1);
            text = text.replace(
                    m.group(),
                    "<ph type=\"url\">"
                            + m_xmlEncoder.encodeStringBasic(urlMap.get(key))
                            + "</ph>");
        }

        return text;
    }

    /**
     * Preserve attributes "version", "encoding", "standalone" in XML
     * declaration if it has.
     * 
     */
    private void preserveAttributesInXmlDeclaration(Document document)
    {
        File f = getInput().getFile();
        if (f != null)
        {
            try
            {
                String encoding = FileUtil.guessEncoding(f);
                if (encoding == null)
                {
                    encoding = getInput().getCodeset();
                }
                String content = FileUtil.readFile(f, encoding);
                // if there is BOM, indexOf returns 1.
                if (content != null
                        && (content.indexOf("<?xml ") == 0 || content
                                .indexOf("<?xml ") == 1))
                {
                    m_haveXMLDecl = true;
                }
                if (m_haveXMLDecl)
                {
                    // encoding
                    m_encoding = document.getXmlEncoding();
                    // version
                    m_version = document.getXmlVersion();

                    String xmlDecl = content.substring(0,
                            content.indexOf(">") + 1);
                    boolean hasStandaloneAttr = (xmlDecl.indexOf("standalone") > -1);
                    if (hasStandaloneAttr)
                    {
                        m_standalone = document.getXmlStandalone() ? "yes"
                                : "no";
                    }
                }
            }
            catch (Exception e)
            {
                s_logger.error(
                        "Error while checking XML declaration attributes in preserveAttributesInXmlDeclaration(..).",
                        e);
            }
        }

    }

    /**
     * @return the rules
     */
    public List<ExtractRule> getRules()
    {
        return rules;
    }

    /**
     * @param rules
     *            the rules to set
     */
    public void setRules(List<ExtractRule> rules)
    {
        this.rules = rules;
    }

    /**
     * @see com.globalsight.cxe.adapter.ling.HasExtractRule#addExtractRule()
     */
    public void addExtractRule(ExtractRule rule)
    {
        rules.add(rule);
    }

}
