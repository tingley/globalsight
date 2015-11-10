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
package com.globalsight.ling.docproc.extractor.xliff20;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.globalsight.ling.common.RegEx;
import com.globalsight.ling.common.RegExException;
import com.globalsight.ling.common.RegExMatchInterface;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DocumentElementException;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorInterface;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.extractor.xml.GsDOMParser;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.SegmentUtil;

/**
 * Xliff 2.0 extractor.
 * 
 * @since GBS-3936
 */
public class Extractor extends AbstractExtractor implements ExtractorInterface,
        EntityResolver, ExtractorExceptionConstants, ErrorHandler, WSConstants
{
    private ExtractorAdmin m_admin = new ExtractorAdmin(null);

    // XML declaration flags
    private boolean m_haveXMLDecl = false;
    private String m_version = null;
    private String m_standalone = null;
    private String m_encoding = null;

    // XML encoder
    private XmlEntities m_xmlEncoder = new XmlEntities();

    // For recording the original source content to write into skeleton.
    private StringBuilder sourceContent = new StringBuilder();
    // For recording the tag transformed source content to write source tuv.
    private StringBuilder tuvSourceContent = new StringBuilder();
    // For recording the source content without tag.
    private StringBuilder sourceContentWithoutTag = new StringBuilder();
    // For recording the original target content to write into skeleton.
    private StringBuilder targetContent = new StringBuilder();
    // For recording the tag transformed source content to write target tuv.
    private StringBuilder tuvTargetContent = new StringBuilder();
    // For recording the alt source content to write into tuv.
    private StringBuilder tuvAltSource = new StringBuilder();
    // For recording the alt target content to write into tuv.
    private StringBuilder tuvAltTarget = new StringBuilder();

    private List<Integer> sourceIndex = new ArrayList<Integer>();
    private int lastIndex = 1;

    private boolean isFromWorldServer = false;
    @SuppressWarnings("serial")
    private static List<String> m_inlineTags = new ArrayList<String>()
    {
        {
            add("it");
            add("ph");
            add("bpt");
            add("ept");
        }
    };

    public Extractor()
    {
        super();

        m_admin = new ExtractorAdmin(null);
        m_haveXMLDecl = false;
        m_version = null;
        m_standalone = null;
        m_encoding = null;
    }

    public void setFormat()
    {
        setMainFormat(IFormatNames.FORMAT_XLIFF20);
    }

    /**
     * Extracts the input document.
     * 
     * Parses the Xliff 2.0 File into DOM. Then invokes domNodeVisitor for the
     * Document 'Node' ('virtual root') to traverse the DOM tree recursively,
     * using the AbstractExtractor API to write out skeleton and segments.
     */
    public void extract() throws ExtractorException
    {
        try
        {
            setFormat();

            GsDOMParser parser = new GsDOMParser();

            // don't read external DTDs
            parser.setEntityResolver(this);
            // provide detailed error report
            parser.setErrorHandler(this);
            // parse and create DOM tree
            Document document = parser.parse(new InputSource(readInput()));

            // preserve the values in the inputs' XML declaration
            m_encoding = document.getXmlEncoding();
            m_version = document.getXmlVersion();
            m_standalone = document.getXmlStandalone() ? "yes" : "no";
            m_haveXMLDecl = m_encoding != null || m_version != null;

            // traverse the DOM tree
            domNodeVisitor(document, true);
        }
        catch (Exception e)
        {
            throw new ExtractorException(e);
        }
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

    public void error(SAXParseException e) throws SAXException
    {
        throw new SAXException("Xliff parse error at\n  line "
                + e.getLineNumber() + "\n  column " + e.getColumnNumber()
                + "\n  Message:" + e.getMessage());
    }

    public void fatalError(SAXParseException e) throws SAXException
    {
        error(e);
    }

    public void warning(SAXParseException e)
    {
        System.err.println("Xliff parse warning at\n  line "
                + e.getLineNumber() + "\n  column " + e.getColumnNumber()
                + "\n  Message:" + e.getMessage());
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
        String systemId = SegmentUtil.restoreEntity(docType.getSystemId());
        String publicId = SegmentUtil.restoreEntity(docType.getPublicId());
        String internalSubset = SegmentUtil.restoreEntity(docType
                .getInternalSubset());

        if (systemId != null || publicId != null || internalSubset != null)
        {
            outputSkeleton("<!DOCTYPE "
                    + SegmentUtil.restoreEntity(docType.getName()) + " ");
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

    private void commentProcessor(Node p_node, boolean isTranslatable)
    {
        if (!processGsaSnippet(p_node.getNodeValue()))
        {
            String comment = "<!--" + p_node.getNodeValue() + "-->";
            outputSkeleton(SegmentUtil.restoreEntity(comment));
        }
    }

    private void outputPi(Node p_node, boolean isTranslatable)
    {
        String piString = "<?" + p_node.getNodeName() + " "
                + p_node.getNodeValue() + "?>";
        outputSkeleton(piString);
    }

    private void cdataProcessor(Node p_node, boolean isTranslatable)
    {
        outputSkeleton("<![CDATA["
                + SegmentUtil.restoreEntity(p_node.getNodeValue()) + "]]>");
    }

    private void entityProcessor(Node p_node, boolean isTranslatable)
    {
        String entityTag = p_node.getNodeName();
        String name = "&" + entityTag + ";";
        outputSkeleton(name);
    }

    /**
     * A visitor that recursively traverses the input document. Element nodes
     * are handed off to domElementProcessor().
     */
    private void domNodeVisitor(Node p_node, boolean isTranslatable)
            throws ExtractorException
    {
        while (true)
        {
            if (p_node == null)
            {
                return;
            }
            switch (p_node.getNodeType())
            {
                case Node.DOCUMENT_NODE:
                    if (m_haveXMLDecl)
                        outputXMLDeclaration();

                    DocumentType docType = ((Document) p_node).getDoctype();
                    if (docType != null)
                        docTypeProcessor(docType);

                    domNodeVisitor(p_node.getFirstChild(), isTranslatable);

                    return;

                case Node.PROCESSING_INSTRUCTION_NODE:
                    outputPi(p_node, isTranslatable);
                    p_node = p_node.getNextSibling();

                    break;

                case Node.ELEMENT_NODE:
                    domElementProcessor(p_node);
                    p_node = p_node.getNextSibling();

                    break;

                case Node.COMMENT_NODE:
                    commentProcessor(p_node, isTranslatable);
                    p_node = p_node.getNextSibling();

                    break;

                case Node.ENTITY_REFERENCE_NODE:
                    entityProcessor(p_node, isTranslatable);
                    p_node = p_node.getNextSibling();

                    break;

                case Node.TEXT_NODE:
                    textProcessor(p_node, isTranslatable);
                    p_node = p_node.getNextSibling();

                    break;

                case Node.CDATA_SECTION_NODE:
                    cdataProcessor(p_node, isTranslatable);
                    p_node = p_node.getNextSibling();

                    break;

                default:
                    // shouldn't reach here.
                    // outputSkeleton(domDumpXML(p_node));
                    domNodeVisitor(p_node.getNextSibling(), isTranslatable);

                    return;
            }
        }
    }

    /**
     * Recursively processes an element node, its attributes and children. The
     * rules are consulted to determine whether the node needs to be extracted
     * etc. Attributes are handed off to outputAttributes(), and all nodes below
     * this node are passed to domNodeVisitor().
     */
    private void domElementProcessor(Node p_node) throws ExtractorException
    {
        String name = p_node.getNodeName();
        int bptIndex = m_admin.getBptIndex();
        Map<String, String> map = getNodeTierInfo(p_node);
        boolean isEmbeddable = isEmbeddedNode(p_node, map);
        boolean isTranslatable = true;
        boolean isEmptyElement = p_node.getFirstChild() == null ? true : false;
        boolean isSource = XliffHelper.SOURCE.equals(name.toLowerCase());
        boolean isTarget = XliffHelper.TARGET.equals(name.toLowerCase());
        boolean isSegment = XliffHelper.SEGMENT.equals(name.toLowerCase());

        isFromWorldServer(p_node);
        if (isSegment)
        {
            lastIndex = m_admin.getBptIndex();
        }
        StringBuilder stuff = new StringBuilder();
        if (isEmbeddable)
        {
            String xliffPart = map.get(XliffHelper.MARK_XLIFF_PART);
            if (isEmptyElement)
            {
                if (XliffHelper.TARGET.equals(xliffPart))
                {
                    // if target has more tags than source, then increase max of
                    // source index for target index
                    if (sourceIndex.size() > 0)
                    {
                        bptIndex = sourceIndex.get(0);
                    }
                    else
                    {
                        bptIndex = m_admin.incrementBptIndex();
                    }
                }
                stuff.append("<ph type=\"");
                stuff.append(name);
                stuff.append("\" id=\"");
                stuff.append(bptIndex);
                stuff.append("\" x=\"");
                stuff.append(bptIndex);
                stuff.append("\"");
                stuff.append(">&lt;");
                stuff.append(name);
                stuff.append(outputAttributesAsString(p_node.getAttributes()));
                stuff.append("&gt;&lt;/");
                stuff.append(name);
                stuff.append("&gt;</ph>");

                if (XliffHelper.SOURCE.equals(xliffPart))
                {
                    bptIndex = m_admin.incrementBptIndex();
                    sourceIndex.add(bptIndex);

                    sourceContent.append("<");
                    sourceContent.append(name);
                    sourceContent.append(outputAttributesAsString(
                            p_node.getAttributes(), false));
                    sourceContent.append(">");

                    tuvSourceContent.append(stuff.toString());
                }
                else if (XliffHelper.TARGET.equals(xliffPart))
                {
                    if (sourceIndex.size() > 0)
                    {
                        sourceIndex.remove(0);
                    }

                    targetContent.append("<");
                    targetContent.append(name);
                    targetContent.append(outputAttributesAsString(
                            p_node.getAttributes(), false));
                    targetContent.append(">");

                    tuvTargetContent.append(stuff.toString());
                }
                else if (XliffHelper.ALT_SOURCE.equals(xliffPart))
                {
                    bptIndex = m_admin.incrementBptIndex();
                    sourceIndex.add(bptIndex);

                    sourceContent.append("<");
                    sourceContent.append(name);
                    sourceContent.append(outputAttributesAsString(
                            p_node.getAttributes(), false));
                    sourceContent.append(">");

                    tuvAltSource.append(stuff.toString());
                }
                else if (XliffHelper.ALT_TARGET.equals(xliffPart))
                {
                    if (sourceIndex.size() > 0)
                    {
                        sourceIndex.remove(0);
                    }

                    targetContent.append("<");
                    targetContent.append(name);
                    targetContent.append(outputAttributesAsString(
                            p_node.getAttributes(), false));
                    targetContent.append(">");

                    tuvAltTarget.append(stuff.toString());
                }
            }
            else
            {
                if (XliffHelper.TARGET.equals(xliffPart))
                {
                    if (sourceIndex.size() > 0)
                    {
                        bptIndex = sourceIndex.get(0);
                        sourceIndex.remove(0);
                    }
                    else
                    {
                        bptIndex = m_admin.incrementBptIndex();
                    }
                }
                if (isInlineTag(name))
                {
                    stuff.append("<ph type=\"");
                    stuff.append(name);
                    stuff.append("\" id=\"");
                    stuff.append(bptIndex);
                    stuff.append("\" x=\"");
                    stuff.append(bptIndex);
                    stuff.append("\"");
                    stuff.append(">&lt;");
                    stuff.append(name);
                    stuff.append(outputAttributesAsString(p_node
                            .getAttributes()));
                    stuff.append("&gt;");
                }
                else
                {
                    boolean isTranslate = checkTextNode(p_node);

                    stuff.append("<bpt i=\"");
                    stuff.append(bptIndex);
                    stuff.append("\" type=\"");
                    stuff.append(name);
                    stuff.append("\" isTranslate=\"");
                    stuff.append(isTranslate);
                    stuff.append("\" x=\"");
                    stuff.append(bptIndex);
                    stuff.append("\"");
                    stuff.append(">&lt;");
                    stuff.append(name);
                    stuff.append(outputAttributesAsString(p_node
                            .getAttributes()));
                    stuff.append("&gt;</bpt>");
                }

                if (XliffHelper.SOURCE.equals(xliffPart))
                {
                    bptIndex = m_admin.incrementBptIndex();
                    sourceIndex.add(bptIndex);

                    sourceContent.append("<");
                    sourceContent.append(name);
                    sourceContent.append(outputAttributesAsString(
                            p_node.getAttributes(), false));
                    sourceContent.append(">");

                    if (isEmbeddedInline(p_node))
                    {
                        tuvSourceContent.append("&lt;");
                        tuvSourceContent.append(name);
                        tuvSourceContent.append(outputAttributesAsString(
                                p_node.getAttributes(), false));
                        tuvSourceContent.append("&gt;");
                    }
                    else
                    {
                        tuvSourceContent.append(stuff.toString());
                    }
                }
                else if (XliffHelper.TARGET.equals(xliffPart))
                {
                    targetContent.append("<");
                    targetContent.append(name);
                    targetContent.append(outputAttributesAsString(
                            p_node.getAttributes(), false));
                    targetContent.append(">");

                    if (isEmbeddedInline(p_node))
                    {
                        tuvTargetContent.append("&lt;");
                        tuvTargetContent.append(name);
                        tuvTargetContent.append(outputAttributesAsString(p_node
                                .getAttributes()));
                        tuvTargetContent.append("&gt;");
                    }
                    else
                    {
                        tuvTargetContent.append(stuff.toString());
                    }
                }
                if (XliffHelper.ALT_SOURCE.equals(xliffPart))
                {
                    bptIndex = m_admin.incrementBptIndex();
                    sourceIndex.add(bptIndex);

                    sourceContent.append("<");
                    sourceContent.append(name);
                    sourceContent.append(outputAttributesAsString(
                            p_node.getAttributes(), false));
                    sourceContent.append(">");

                    if (isEmbeddedInline(p_node))
                    {
                        tuvAltSource.append("&lt;");
                        tuvAltSource.append(name);
                        tuvAltSource.append(outputAttributesAsString(
                                p_node.getAttributes(), false));
                        tuvAltSource.append("&gt;");
                    }
                    else
                    {
                        tuvAltSource.append(stuff.toString());
                    }
                }
                else if (XliffHelper.ALT_TARGET.equals(xliffPart))
                {
                    targetContent.append("<");
                    targetContent.append(name);
                    targetContent.append(outputAttributesAsString(
                            p_node.getAttributes(), false));
                    targetContent.append(">");

                    if (isEmbeddedInline(p_node))
                    {
                        tuvAltTarget.append("&lt;");
                        tuvAltTarget.append(name);
                        tuvAltTarget.append(outputAttributesAsString(p_node
                                .getAttributes()));
                        tuvAltTarget.append("&gt;");
                    }
                    else
                    {
                        tuvAltTarget.append(stuff.toString());
                    }
                }
            }
        }
        else
        {
            outputSkeleton("<" + name);
            outputAttributes(p_node.getAttributes(), isEmbeddable);
            outputSkeleton(">");
        }

        domNodeVisitor(p_node.getFirstChild(), isTranslatable);

        if (isEmbeddable)
        {
            String xliffPart = map.get(XliffHelper.MARK_XLIFF_PART);
            if (XliffHelper.SOURCE.equals(xliffPart))
            {
                sourceContent.append("</");
                sourceContent.append(name);
                sourceContent.append(">");

                if (isEmbeddedInline(p_node) && !isEmptyElement)
                {
                    tuvSourceContent.append("&lt;/");
                    tuvSourceContent.append(name);
                    tuvSourceContent.append("&gt;");
                }
                else
                {
                    if (isInlineTag(name))
                    {
                        if (!isEmptyElement)
                        {
                            tuvSourceContent.append("&lt;/");
                            tuvSourceContent.append(name);
                            tuvSourceContent.append("&gt;</ph>");
                        }
                    }
                    else
                    {
                        if (!isEmptyElement)
                        {
                            tuvSourceContent.append("<ept i=\"");
                            tuvSourceContent.append(bptIndex);
                            tuvSourceContent.append("\">&lt;/");
                            tuvSourceContent.append(name);
                            tuvSourceContent.append("&gt;</ept>");
                        }
                    }
                }
            }
            else if (XliffHelper.TARGET.equals(xliffPart))
            {
                targetContent.append("</");
                targetContent.append(name);
                targetContent.append(">");

                if (isEmbeddedInline(p_node) && !isEmptyElement)
                {
                    tuvTargetContent.append("&lt;/");
                    tuvTargetContent.append(name);
                    tuvTargetContent.append("&gt;");
                }
                else
                {
                    if (isInlineTag(name))
                    {
                        if (!isEmptyElement)
                        {
                            tuvTargetContent.append("&lt;/");
                            tuvTargetContent.append(name);
                            tuvTargetContent.append("&gt;</ph>");
                        }
                    }
                    else
                    {
                        if (!isEmptyElement)
                        {
                            tuvTargetContent.append("<ept i=\"");
                            tuvTargetContent.append(bptIndex);
                            tuvTargetContent.append("\">&lt;/");
                            tuvTargetContent.append(name);
                            tuvTargetContent.append("&gt;</ept>");
                        }
                    }
                }
            }
            else if (XliffHelper.ALT_SOURCE.equals(xliffPart))
            {
                sourceContent.append("</");
                sourceContent.append(name);
                sourceContent.append(">");

                if (isEmbeddedInline(p_node) && !isEmptyElement)
                {
                    tuvAltSource.append("&lt;/");
                    tuvAltSource.append(name);
                    tuvAltSource.append("&gt;");
                }
                else
                {
                    if (isInlineTag(name))
                    {
                        if (!isEmptyElement)
                        {
                            tuvAltSource.append("&lt;/");
                            tuvAltSource.append(name);
                            tuvAltSource.append("&gt;</ph>");
                        }
                    }
                    else
                    {
                        if (!isEmptyElement)
                        {
                            tuvAltSource.append("<ept i=\"");
                            tuvAltSource.append(bptIndex);
                            tuvAltSource.append("\">&lt;/");
                            tuvAltSource.append(name);
                            tuvAltSource.append("&gt;</ept>");
                        }
                    }
                }
            }
            else if (XliffHelper.ALT_TARGET.equals(xliffPart))
            {
                targetContent.append("</");
                targetContent.append(name);
                targetContent.append(">");

                if (isEmbeddedInline(p_node) && !isEmptyElement)
                {
                    tuvAltTarget.append("&lt;/");
                    tuvAltTarget.append(name);
                    tuvAltTarget.append("&gt;");
                }
                else
                {
                    if (isInlineTag(name))
                    {
                        if (!isEmptyElement)
                        {
                            tuvAltTarget.append("&lt;/");
                            tuvAltTarget.append(name);
                            tuvAltTarget.append("&gt;</ph>");
                        }
                    }
                    else
                    {
                        if (!isEmptyElement)
                        {
                            tuvAltTarget.append("<ept i=\"");
                            tuvAltTarget.append(bptIndex);
                            tuvAltTarget.append("\">&lt;/");
                            tuvAltTarget.append(name);
                            tuvAltTarget.append("&gt;</ept>");
                        }
                    }
                }
            }
        }
        else
        {
            String xliffPart = map.get(XliffHelper.MARK_XLIFF_PART);
            if (isEmptyElement)
            {
                if (isSource && XliffHelper.SOURCE.equals(xliffPart))
                {
                    outputSkeleton(sourceContent.toString());
                }
                else if (isTarget && XliffHelper.TARGET.equals(xliffPart))
                {
                    if (!isSourceEmpty(sourceContentWithoutTag.toString()))
                    {
                        outputExtractedStuff(" ", isTranslatable, map, false);
                    }
                    else
                    {
                        outputSkeleton(targetContent.toString());
                        m_admin.setBptIndex(lastIndex);
                    }
                    clearContent();
                }
            }
            else
            {
                if (isSource && XliffHelper.SOURCE.equals(xliffPart))
                {
                    outputSkeleton(sourceContent.toString());
                    if (!isSourceEmpty(sourceContentWithoutTag.toString()))
                    {
                        outputExtractedStuff(tuvSourceContent.toString(),
                                isTranslatable, map, true);
                    }
                }
                else if (isTarget && XliffHelper.TARGET.equals(xliffPart))
                {
                    if (!isSourceEmpty(sourceContentWithoutTag.toString()))
                    {
                        outputExtractedStuff(tuvTargetContent.toString(),
                                isTranslatable, map, false);
                    }
                    else
                    {
                        outputSkeleton(targetContent.toString());
                        m_admin.setBptIndex(lastIndex);
                    }
                    clearContent();
                }
                else if (isSource && XliffHelper.ALT_SOURCE.equals(xliffPart))
                {
                    outputSkeleton(sourceContent.toString());
                    if (!isSourceEmpty(sourceContentWithoutTag.toString()))
                    {
                        outputExtractedStuff(tuvAltSource.toString(),
                                isTranslatable, map, true);
                    }
                }
                else if (isTarget && XliffHelper.ALT_TARGET.equals(xliffPart))
                {
                    if (!isSourceEmpty(sourceContentWithoutTag.toString()))
                    {
                        outputSkeleton(targetContent.toString());
                        outputExtractedStuff(tuvAltTarget.toString(),
                                isTranslatable, map, false);
                    }
                    else
                    {
                        outputSkeleton(targetContent.toString());
                        m_admin.setBptIndex(lastIndex);
                    }
                    clearContent();
                }
            }
            outputSkeleton("</" + name + ">");

            // if a segment has only source part, then add target part and set
            // the target content same as source's
            if (isSource && XliffHelper.SOURCE.equals(xliffPart))
            {
                if (!hasTargetPart(p_node, XliffHelper.SEGMENT))
                {
                    if (!isSourceEmpty(sourceContentWithoutTag.toString()))
                    {
                        Map<String, String> newMap = new HashMap<String, String>();
                        newMap.putAll(map);
                        newMap.put(XliffHelper.MARK_XLIFF_PART,
                                XliffHelper.TARGET);
                        outputSkeleton("<target>");
                        outputExtractedStuff(tuvSourceContent.toString(),
                                isTranslatable, newMap, false);
                        outputSkeleton("</target>");

                        clearContent();
                    }
                    else
                    {
                        m_admin.setBptIndex(lastIndex);
                    }
                }
            }
        }
    }

    private boolean isSourceEmpty(String source)
    {
        return StringUtil.isEmpty(source) || Text.isBlank(source);
    }

    private void clearContent()
    {
        sourceContent = new StringBuilder();
        tuvSourceContent = new StringBuilder();
        sourceContentWithoutTag = new StringBuilder();
        targetContent = new StringBuilder();
        tuvTargetContent = new StringBuilder();
        sourceIndex.clear();
        tuvAltSource = new StringBuilder();
        tuvAltTarget = new StringBuilder();
    }

    private void textProcessor(Node p_node, boolean isTranslatable)
    {
        Map<String, String> map = getNodeTierInfo(p_node);

        String nodeValue = SegmentUtil.restoreEntity(p_node.getNodeValue());

        String xliffPart = map.get(XliffHelper.MARK_XLIFF_PART);
        if (XliffHelper.SOURCE.equals(xliffPart))
        {
            sourceContent.append(nodeValue);
            tuvSourceContent.append(nodeValue);
            sourceContentWithoutTag.append(nodeValue);
        }
        else if (XliffHelper.TARGET.equals(xliffPart))
        {
            targetContent.append(nodeValue);
            tuvTargetContent.append(nodeValue);
        }
        else if (XliffHelper.ALT_SOURCE.equals(xliffPart))
        {
            sourceContent.append(nodeValue);
            tuvAltSource.append(nodeValue);
            sourceContentWithoutTag.append(nodeValue);
        }
        else if (XliffHelper.ALT_TARGET.equals(xliffPart))
        {
            targetContent.append(nodeValue);
            tuvAltTarget.append(nodeValue);
        }
        else
        {
            outputSkeleton(nodeValue);
        }
    }

    private boolean isInlineTag(String tagName)
    {
        return m_inlineTags.contains(tagName);
    }

    /**
     * Checks if a tag inside the inline tag is an inner tag or not.
     * 
     * <pre>
     * For example, "ph" tag is an inline tag. Then for content "<ph> pContent1 <aaa> content </aaa>
     * pContent2 </ph>", the <aaa> tag is an embedded inline tag, which will not be
     * extracted.
     * </pre>
     */
    private boolean isEmbeddedInline(Node node)
    {
        Node parentNode = node.getParentNode();

        while (parentNode != null)
        {
            String name = parentNode.getNodeName();

            if (isInlineTag(name))
            {

                return true;
            }

            parentNode = parentNode.getParentNode();
        }

        return false;
    }

    /**
     * Checks if this node is a child of source or target.
     */
    private boolean isEmbeddedNode(Node node, Map<String, String> map)
    {
        String nodeName = node.getNodeName().toLowerCase();
        if (XliffHelper.SOURCE.equals(nodeName)
                || XliffHelper.TARGET.equals(nodeName))
        {
            return false;
        }

        if (map != null)
        {
            String xliffPart = map.get(XliffHelper.MARK_XLIFF_PART);
            if (XliffHelper.SOURCE.equals(xliffPart)
                    || XliffHelper.TARGET.equals(xliffPart))
            {
                return true;
            }
        }

        return false;
    }

    private Map<String, String> getNodeTierInfo(Node node)
    {
        INodeInfo nodeInfo;

        if (isFromWorldServer)
        {
            nodeInfo = new WSNodeInfo(getMainFormat());
        }
        else
        {
            nodeInfo = new NodeInfo(getMainFormat());
        }

        return nodeInfo.getNodeTierInfo(node);
    }

    private boolean hasTargetPart(Node node, String parentNodeName)
    {
        Node parentNode = node.getParentNode();

        if (parentNodeName.equals(parentNode.getNodeName().toLowerCase()))
        {
            NodeList nodes = parentNode.getChildNodes();

            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node childNode = nodes.item(i);

                if (XliffHelper.TARGET.equals(childNode.getNodeName()
                        .toLowerCase()))
                {
                    return true;
                }
            }
        }
        return false;
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
     * <p>
     * Outputs the attributes of the element node being processed by
     * domElementProcessor().
     * </p>
     * 
     * <p>
     * Note that the <code>isInTranslatable</code> argument is not used.
     * </p>
     */
    private void outputAttributes(NamedNodeMap attrs, boolean isEmbeded)
            throws ExtractorException
    {
        if (attrs == null)
        {
            return;
        }
        for (int i = 0; i < attrs.getLength(); ++i)
        {
            Node att = attrs.item(i);
            String attname = att.getNodeName();
            String value = att.getNodeValue();
            value = SegmentUtil.restoreEntity(value);

            if (isEmbeded)
            {
                String stuff = null;

                stuff = " " + attname + "=\"" + value + "\"";

                m_admin.addContent(stuff);
            }
            else
            {
                outputSkeleton(" " + attname + "=\"" + value + "\"");
            }
        }
    }

    private String outputAttributesAsString(NamedNodeMap attrs)
    {
        return outputAttributesAsString(attrs, true);
    }

    private String outputAttributesAsString(NamedNodeMap attrs, boolean isEncode)
    {
        String output = "";

        if (attrs == null)
        {
            return null;
        }

        for (int i = 0; i < attrs.getLength(); ++i)
        {
            Node att = attrs.item(i);
            String attname = att.getNodeName();
            String value = SegmentUtil.restoreEntity(att.getNodeValue());

            if (isEncode)
            {
                output = output + " " + attname + "=\""
                        + m_xmlEncoder.encodeStringBasic(value) + "\"";
            }
            else
            {
                output = output + " " + attname + "=\"" + value + "\"";
            }
        }

        return output;
    }

    /**
     * Outputs translatable text.
     */
    private void outputTranslatable(String p_ToAdd,
            Map<String, String> xliffAttributes, boolean p_blankTextAsSkeleton)
    {
        if (m_admin.getOutputType() != OutputWriter.TRANSLATABLE)
        {
            m_admin.reset(new TranslatableWriter(getOutput()),
                    p_blankTextAsSkeleton);
            m_admin.setXliffTransPart(xliffAttributes);
        }

        m_admin.addContent(p_ToAdd);
    }

    /**
     * Outputs localizable text.
     */
    private void outputLocalizable(String p_ToAdd)
    {
        if (m_admin.getOutputType() != OutputWriter.LOCALIZABLE)
        {
            m_admin.reset(new LocalizableWriter(getOutput()));
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

    /**
     * Utility function that outputs translatable or localizable text.
     * 
     * @param stuff
     * @param isTranslatable
     * @param xliffAttributes
     * @param p_blankTextAsSkeleton
     *            For target content,"stuff" will be output to "translatable"
     *            whatever it is blank. For others,if "stuff" is blank, it will
     *            be output to "skeleton" part.
     */
    private void outputExtractedStuff(String stuff, boolean isTranslatable,
            Map<String, String> xliffAttributes, boolean p_blankTextAsSkeleton)
    {
        if (isTranslatable)
        {
            outputTranslatable(stuff, xliffAttributes, p_blankTextAsSkeleton);
        }
        else
        {
            outputLocalizable(stuff);
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

    @Override
    public void loadRules() throws ExtractorException
    {
    }

    private void isFromWorldServer(Node p_node)
    {
        if (XliffHelper.FILE.equals(p_node.getNodeName().toLowerCase()))
        {
            Node sNode = p_node.getAttributes().getNamedItem(
                    XliffHelper.ATTR_TOOL);
            if (sNode != null)
            {
                String value = sNode.getNodeValue();
                if (value.indexOf(XliffHelper.ATTR_VALUE_WS) > -1)
                {
                    isFromWorldServer = true;
                }
            }
        }
    }
}
