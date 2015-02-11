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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.globalsight.ling.common.srccomment.SrcCmtXmlTag;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.extractor.xml.xmlrule.CommentRuleItem;
import com.globalsight.ling.docproc.extractor.xml.xmlrule.RuleItemManager;

/**
 * Encapsulates all rulesets contained in the rules XML file.
 */
public class RuleSet implements EntityResolver, ErrorHandler
{
	static private final Logger logger = Logger
            .getLogger(RuleSet.class);

	private static int gcCounter = 0;
    private DOMParser m_parser = null;
    private boolean m_useEmptyTag = true;
    public static final String INTERNAL = "internal";
    
    /**
     * Reads an XML ruleset.
     * 
     * @param ruleString
     *            contents of XML rule file
     */
    public void loadRule(String p_ruleString) throws ExtractorException
    {
        m_parser = new DOMParser();

        // ignore external entities
        m_parser.setEntityResolver(this);
        // to detect validation error
        m_parser.setErrorHandler(this);

        // do nothing other than initializing rule parser
        if (p_ruleString == null || p_ruleString.length() == 0)
        {
            return;
        }

        // validate the rule file
        RuleFileValidator validator = new RuleFileValidator();
        if (!validator.validate(p_ruleString))
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.XML_EXTRACTOR_RULES_ERROR,
                    validator.getErrorMessage());
        }

        // parse and create DOM tree
        try
        {
            StringReader in = new StringReader(p_ruleString);
            m_parser.parse(new InputSource(in));
            setUseEmptyTag();
        }
        catch (Exception e)
        {
            throw new ExtractorException(e);
        }
    }

    public Map buildRulesWithFilter(Document toBeExtracted,
            XmlFilterTags xmlFilterTags, String format)
            throws ExtractorException
    {
        Map ruleMap = buildRules(toBeExtracted, format);

        try
        {
            gcCounter++;
            // white space preserve tags
            List<XmlFilterTag> wsPreserveTags = xmlFilterTags
                    .getWhiteSpacePreserveTags();
            List<XmlFilterTag> embTags = xmlFilterTags.getEmbeddedTags();
            List<XmlFilterTag> transAttrTags = xmlFilterTags.getTransAttrTags();
            List<XmlFilterTag> contentInclTags = xmlFilterTags
                    .getContentInclTags();
            List<XmlFilterSidTag> sidTags = xmlFilterTags.getSidTags();
            List<XmlFilterTag> internalTag = xmlFilterTags.getInternalTag();
            List<SrcCmtXmlTag> srcCmtXmlTags = xmlFilterTags.getSrcCmtXmlTag();

            ruleMap = buildPreserveWhitespaceTags(toBeExtracted, ruleMap,
                    wsPreserveTags);
            ruleMap = buildEmbeddedTags(toBeExtracted, ruleMap, embTags);
            ruleMap = buildTransAttrTags(toBeExtracted, ruleMap, transAttrTags);
            ruleMap = buildContentInclTags(toBeExtracted, ruleMap,
                    contentInclTags);
            ruleMap = buildSidTags(toBeExtracted, ruleMap, sidTags);
            ruleMap = buildInternalTag(toBeExtracted, ruleMap, internalTag);
            ruleMap = buildSrcCmtXmlTag(toBeExtracted, ruleMap, srcCmtXmlTags);

            // comment this to translate filter data
            // if (IFormatNames.FORMAT_OPENOFFICE_XML.equals(format))
            // {
            // ruleMap = buildRulesForOpenOffice(toBeExtracted, ruleMap);
            // }
        }
        catch (Exception e)
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.XML_EXTRACTOR_RULES_ERROR,
                    e.toString());
        }

        if (gcCounter > 100)
        {
            // call GC here to free some memory used in building rule
            System.gc();
            gcCounter = 0;
        }

        return ruleMap;
    }

    private Map buildInternalTag(Document toBeExtracted, Map ruleMap,
            List<XmlFilterTag> internalTag) throws Exception
    {
        if (internalTag != null && internalTag.size() > 0)
        {
            if (ruleMap == null)
            {
                ruleMap = new HashMap();
            }

            for (XmlFilterTag xmlFilterTag : internalTag)
            {
                List<Node> matchedNodes = xmlFilterTag
                        .getMatchedNodeList(toBeExtracted);
                for (Node node : matchedNodes)
                {
                    Rule rule = new Rule();
                    rule.setInternal(true);
                    rule.setInline(true);
                    Rule previousRule = (Rule) ruleMap.get(node);
                    Rule newRule = rule;

                    if (previousRule != null)
                    {
                        newRule = previousRule.merge(rule);
                    }

                    ruleMap.put(node, newRule);
                }
            }
        }

        return ruleMap;
    }
    
    private Map buildSrcCmtXmlTag(Document toBeExtracted, Map ruleMap,
            List<SrcCmtXmlTag> srcCmtXmlTags) throws Exception
    {
        if (srcCmtXmlTags != null && srcCmtXmlTags.size() > 0)
        {
            if (ruleMap == null)
            {
                ruleMap = new HashMap();
            }

            for (SrcCmtXmlTag srcCmtXmlTag : srcCmtXmlTags)
            {
                List<Node> matchedNodes = srcCmtXmlTag.getMatchedNodeList(toBeExtracted);
                
                // set src comment nodes' properties
                for (int i = 0; i < matchedNodes.size(); i++)
                {
                    Node node = matchedNodes.get(i);
                    if (srcCmtXmlTag.isFromAttribute())
                    {
                        String atName = srcCmtXmlTag.getAttributeName();
                        Node at = node.getAttributes().getNamedItem(atName);
                        
                        CommentRuleItem.setSrcCommentNodeProperties(at, ruleMap);
                    }
                    else
                    {
                        CommentRuleItem.setSrcCommentNodeProperties(node, ruleMap);
                    }
                }
                
                // set src comments for other nodes
                for (int i = 0; i < matchedNodes.size(); i++)
                {
                    Node node = matchedNodes.get(i);
                    String srcComment = null;
                    if (srcCmtXmlTag.isFromAttribute())
                    {
                        String atName = srcCmtXmlTag.getAttributeName();
                        Node at = node.getAttributes().getNamedItem(atName);

                        if (at != null)
                        {
                            srcComment = at.getNodeValue();
                        }

                        if (srcComment != null)
                        {
                            CommentRuleItem.updateCommendForChildTextNode(ruleMap, node,
                                    srcComment);
                        }
                    }
                    else
                    {
                        try
                        {
                            srcComment = node.getChildNodes().item(0).getNodeValue();
                        }
                        catch (Exception e)
                        {
                            // ignore e
                            srcComment = null;
                        }

                        if (srcComment != null)
                        {
                            CommentRuleItem.updateCommendForNextTextNode(ruleMap, node, srcComment);
                        }
                    }
                }
            }
        }

        return ruleMap;
    }

    private Map buildPreserveWhitespaceTags(Document toBeExtracted,
            Map ruleMap, List<XmlFilterTag> wsPreserveTags) throws Exception
    {
        if (wsPreserveTags != null && wsPreserveTags.size() > 0)
        {
            if (ruleMap == null)
            {
                ruleMap = new HashMap();
            }

            for (XmlFilterTag xmlFilterTag : wsPreserveTags)
            {
                List<Node> matchedNodes = xmlFilterTag
                        .getMatchedNodeList(toBeExtracted);
                for (Node node : matchedNodes)
                {
                    Rule rule = new Rule();
                    rule.setPreserveWhiteSpace("true");
                    Rule previousRule = (Rule) ruleMap.get(node);
                    Rule newRule = rule;

                    if (previousRule != null)
                    {
                        newRule = previousRule.merge(rule);
                    }

                    ruleMap.put(node, newRule);
                }
            }
        }

        return ruleMap;
    }

    private Map buildEmbeddedTags(Document toBeExtracted, Map ruleMap,
            List<XmlFilterTag> embTags) throws Exception
    {
        if (embTags != null && embTags.size() > 0)
        {
            if (ruleMap == null)
            {
                ruleMap = new HashMap();
            }

            for (XmlFilterTag xmlFilterTag : embTags)
            {
                List<Node> matchedNodes = xmlFilterTag
                        .getMatchedNodeList(toBeExtracted);
                for (Node node : matchedNodes)
                {
                    Rule rule = new Rule();
                    rule.setInline(true);
                    Rule previousRule = (Rule) ruleMap.get(node);
                    Rule newRule = rule;

                    if (previousRule != null)
                    {
                        newRule = previousRule.merge(rule);
                    }

                    ruleMap.put(node, newRule);
                }
            }
        }

        return ruleMap;
    }

    private Map buildContentInclTags(Document toBeExtracted, Map ruleMap,
            List<XmlFilterTag> contentInclTags) throws Exception
    {
        if (contentInclTags != null && contentInclTags.size() > 0)
        {
            if (ruleMap == null)
            {
                ruleMap = new HashMap();
            }

            for (XmlFilterTag xmlFilterTag : contentInclTags)
            {
                List<Node> matchedNodes = xmlFilterTag
                        .getMatchedNodeList(toBeExtracted);
                boolean isContentInclude = xmlFilterTag.isContentInclude();
                for (Node node : matchedNodes)
                {
                    Rule rule = new Rule();
                    rule.setTranslate(isContentInclude);
                    Rule previousRule = (Rule) ruleMap.get(node);
                    Rule newRule = rule;

                    if (previousRule != null)
                    {
                        newRule = previousRule.merge(rule);
                    }

                    newRule.setTranslate(isContentInclude);
                    ruleMap.put(node, newRule);
                }
            }
        }

        return ruleMap;
    }

    private Map buildSidTags(Document toBeExtracted, Map ruleMap,
            List<XmlFilterSidTag> sidTags) throws Exception
    {
        if (sidTags != null && sidTags.size() > 0)
        {
            if (ruleMap == null)
            {
                ruleMap = new HashMap();
            }

            for (XmlFilterSidTag sidTag : sidTags)
            {
                List<Node> matchedNodes = sidTag
                        .getMatchedNodeList(toBeExtracted);
                for (Node node : matchedNodes)
                {
                    Node sidAtt = sidTag.getSidAttribute(node);
                    if (sidAtt == null)
                    {
                        continue;
                    }

                    String sidAttName = sidAtt.getNodeName();
                    String sidvalue = sidAtt.getNodeValue();

                    // get all child elements
                    List<Node> enodes = new ArrayList<Node>();
                    enodes.add(node);
                    enodes.addAll(XmlFilterSidTag.getChildNodes(node));

                    for (Node enode : enodes)
                    {
                        // node value
                        NodeList childNodes = enode.getChildNodes();

                        if (childNodes != null && childNodes.getLength() > 0)
                        {
                            for (int i = 0; i < childNodes.getLength(); i++)
                            {
                                Node child = childNodes.item(i);
                                if (child.getNodeType() != Node.TEXT_NODE)
                                {
                                    continue;
                                }

                                Rule rule = new Rule();
                                rule.setSid(sidvalue);
                                Rule previousRule = (Rule) ruleMap.get(child);
                                Rule newRule = rule;

                                if (previousRule != null)
                                {
                                    newRule = previousRule.merge(rule);
                                }

                                ruleMap.put(child, newRule);
                            }
                        }

                        // node attributes
                        NamedNodeMap attributes = enode.getAttributes();

                        if (attributes != null && attributes.getLength() > 1)
                        {
                            for (int i = 0; i < attributes.getLength(); i++)
                            {
                                Node at = attributes.item(i);
                                if (sidAttName.equals(at.getNodeName()))
                                {
                                    continue;
                                }

                                if (Rule.extracts(ruleMap, at))
                                {
                                    Rule atRule = new Rule();
                                    atRule.setSid(sidvalue);
                                    Rule previousAtRule = (Rule) ruleMap
                                            .get(at);
                                    Rule newAtRule = atRule;

                                    if (previousAtRule != null)
                                    {
                                        newAtRule = previousAtRule
                                                .merge(atRule);
                                    }

                                    ruleMap.put(at, newAtRule);
                                }
                            }
                        }
                    }
                }
            }
        }

        return ruleMap;
    }

    private Map buildTransAttrTags(Document toBeExtracted, Map ruleMap,
            List<XmlFilterTag> transAttrTags) throws Exception
    {
        if (transAttrTags != null && transAttrTags.size() > 0)
        {
            if (ruleMap == null)
            {
                ruleMap = new HashMap();
            }

            for (XmlFilterTag xmlFilterTag : transAttrTags)
            {
                List<Node> matchedNodes = xmlFilterTag
                        .getMatchedNodeList(toBeExtracted);
                for (Node node : matchedNodes)
                {
                    NamedNodeMap attributes = node.getAttributes();

                    if (attributes != null && attributes.getLength() > 0)
                    {
                        for (int i = 0; i < attributes.getLength(); i++)
                        {
                            Node att = attributes.item(i);
                            int mode = xmlFilterTag
                                    .getAttributeTranlateMode(att);

                            if (mode != 0)
                            {
                                boolean isInline = (mode != 2);
                                Rule rule = new Rule();
                                rule.setTranslatable(true);
                                rule.setInline(isInline);

                                Rule previousRule = (Rule) ruleMap.get(att);
                                Rule newRule = rule;

                                if (previousRule != null)
                                {
                                    newRule = previousRule.merge(rule);
                                }

                                ruleMap.put(att, newRule);
                            }
                        }
                    }
                }
            }
        }

        return ruleMap;
    }

    /**
     * Build rules for open office xml 1. do not translate filter data
     * 
     * @param toBeExtracted
     * @param ruleMap
     * @return
     * @throws Exception
     */
    private Map buildRulesForOpenOffice(Document toBeExtracted, Map ruleMap)
            throws Exception
    {
        String xpath = "/office:document-content/office:body/office:spreadsheet";
        NodeList odsNodes = XPathAPI.selectNodeList(
                toBeExtracted.getDocumentElement(), xpath);

        // check if it is open office spreadsheet
        if (odsNodes != null && odsNodes.getLength() != 0)
        {
            if (ruleMap == null)
            {
                ruleMap = new HashMap();
            }

            // get filter cell ranges
            String xpathDBRange = "//table:database-range";
            String xpathPilot = "//table:data-pilot-table";
            String attTargetRange = "table:target-range-address";
            NodeList dbRanges = XPathAPI.selectNodeList(
                    toBeExtracted.getDocumentElement(), xpathDBRange);
            NodeList pilots = XPathAPI.selectNodeList(
                    toBeExtracted.getDocumentElement(), xpathPilot);
            List<String> cellRanges = new ArrayList<String>();

            buildCellRanges(attTargetRange, dbRanges, cellRanges);
            buildCellRanges(attTargetRange, pilots, cellRanges);

            // get cells from ranges
            List<OdsCell> cells = buildCellsFromRanges(cellRanges);

            // find each cell and set it to do not extract
            for (OdsCell odsCell : cells)
            {
                String xpathRow = "//table:table[@table:name=\""
                        + odsCell.tableName + "\"]/table:table-row";
                NodeList rows = XPathAPI.selectNodeList(
                        toBeExtracted.getDocumentElement(), xpathRow);
                Element row = null;

                // get row
                int index_r = 0;
                if (rows != null)
                {
                    for (int i = 0; i < rows.getLength(); i++)
                    {
                        Node ndRow = rows.item(i);
                        if (ndRow.getNodeType() == Node.ELEMENT_NODE)
                        {
                            Element temp = (Element) ndRow;
                            if (index_r == odsCell.rowIndex)
                            {
                                row = temp;
                                break;
                            }

                            String rowRepeated = temp
                                    .getAttribute("table:number-rows-repeated");
                            int added = 1;
                            if (rowRepeated != null && rowRepeated.length() > 0)
                            {
                                added = Integer.parseInt(rowRepeated);
                            }

                            index_r += added;
                        }
                    }
                }

                if (row == null)
                {
                    continue;
                }

                // get column
                Element cellElement = null;
                int index_c = 0;
                NodeList cols = row.getChildNodes();
                if (cols != null)
                {
                    for (int i = 0; i < cols.getLength(); i++)
                    {
                        Node col = cols.item(i);
                        if (col.getNodeType() == Node.ELEMENT_NODE)
                        {
                            Element temp = (Element) col;

                            if (index_c == odsCell.colIndex)
                            {
                                cellElement = temp;
                                break;
                            }

                            String colRepeated = temp
                                    .getAttribute("table:number-columns-repeated");
                            String colSpanned = temp
                                    .getAttribute("table:number-columns-spanned");
                            int added = 1;
                            if (colRepeated != null && colRepeated.length() > 0)
                            {
                                added = Integer.parseInt(colRepeated);
                            }
                            else if (colSpanned != null
                                    && colSpanned.length() > 0)
                            {
                                added = Integer.parseInt(colSpanned);
                            }

                            index_c += added;
                        }
                    }
                }

                // set this element to be not extracted if not null
                if (cellElement != null)
                {
                    Node cellNode = cellElement;
                    setTranslatableForNode(ruleMap, cellNode, false);
                    setTranslatableForChild(ruleMap, cellNode, false);
                }
            }
        }

        return ruleMap;
    }

    private void setTranslatableForChild(Map ruleMap, Node node, boolean trans)
    {
        if (node != null)
        {
            NodeList childs = node.getChildNodes();
            if (childs != null && childs.getLength() > 0)
            {
                int clen = childs.getLength();
                for (int i = 0; i < clen; i++)
                {
                    Node child = childs.item(i);
                    setTranslatableForNode(ruleMap, child, trans);
                    setTranslatableForChild(ruleMap, child, trans);
                }
            }
        }
    }

    private void setTranslatableForNode(Map ruleMap, Node cellNode,
            boolean trans)
    {
        Rule rule = new Rule();
        rule.setTranslate(trans);
        Rule previousRule = (Rule) ruleMap.get(cellNode);
        Rule newRule = rule;

        if (previousRule != null)
        {
            newRule = previousRule.merge(rule);
        }

        newRule.setTranslate(trans);
        ruleMap.put(cellNode, newRule);
    }

    /**
     * For open office cell range
     * 
     * @param attTargetRange
     * @param dbRanges
     * @param cellRanges
     */
    private void buildCellRanges(String attTargetRange, NodeList nodes,
            List<String> cellRanges)
    {
        if (nodes != null && nodes.getLength() != 0)
        {
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Element ele = (Element) nodes.item(i);
                String range = ele.getAttribute(attTargetRange);
                if (range != null && !"".equals(range)
                        && !cellRanges.contains(range))
                {
                    cellRanges.add(range);
                }
            }
        }
    }

    /**
     * Build cells from cell ranges
     * 
     * @param cellRanges
     * @return
     */
    private List<OdsCell> buildCellsFromRanges(List<String> cellRanges)
    {
        List<OdsCell> cells = new ArrayList<OdsCell>();

        if (cellRanges != null)
        {
            for (String range : cellRanges)
            {
                int index_0 = -1;
                if (range.startsWith("'"))
                {
                    index_0 = range.indexOf(":", range.indexOf("'."));
                }
                else
                {
                    index_0 = range.indexOf(":");
                }

                if (index_0 == -1)
                {
                    OdsCell cell = buildOdsCell(range);
                    cells.add(cell);
                }
                else
                {
                    String from = range.substring(0, index_0);
                    String to = range.substring(index_0 + 1);
                    List<OdsCell> odcells = buildOdsCells(from, to);
                    cells.addAll(odcells);
                }
            }
        }

        return cells;
    }

    /**
     * from and to have same table name
     * 
     * @param from
     * @param to
     * @return
     */
    private List<OdsCell> buildOdsCells(String from, String to)
    {
        List<OdsCell> cells = new ArrayList<OdsCell>();
        OdsCell fromCell = buildOdsCell(from);
        OdsCell toCell = buildOdsCell(to);

        for (int i = fromCell.rowIndex; i <= toCell.rowIndex; i++)
        {
            for (int j = fromCell.colIndex; j <= toCell.colIndex; j++)
            {
                cells.add(new OdsCell(fromCell.tableName, j, i));
            }
        }

        return cells;
    }

    /**
     * Build one OdsCell from Sheet1.A1
     * 
     * @param cell
     * @return
     */
    private OdsCell buildOdsCell(String cell)
    {
        int index_dot = cell.lastIndexOf(".");
        if (index_dot != -1)
        {
            String tname = cell.substring(0, index_dot);
            String indexStr = cell.substring(index_dot + 1);

            if (tname.startsWith("'") && tname.endsWith("'"))
            {
                tname = tname.substring(1, tname.length() - 1);
            }

            String index_c = "";
            String index_r = "";
            String ddd = "1234567890";
            for (char c : indexStr.toCharArray())
            {
                if (ddd.contains("" + c))
                {
                    index_r += c;
                }
                else
                {
                    index_c += c;
                }
            }

            int rIndex = Integer.parseInt(index_r) - 1;
            int cIndex = 0;
            int clen = index_c.length();
            for (int i = 0; i < clen; i++)
            {
                int c = index_c.charAt(i);

                if (i == clen - 1)
                {
                    cIndex += c - 'A';
                }
                else
                {
                    cIndex += 26 * Math.pow(10, (clen - i - 2)) * (c - 'A' + 1);
                }
            }

            return new OdsCell(tname, cIndex, rIndex);
        }
        else
        {
            return null;
        }
    }

    private class OdsCell
    {
        public int colIndex = -1;
        public int rowIndex = -1;
        public String tableName = null;

        public OdsCell(String tName, int cIndex, int rIndex)
        {
            tableName = tName;
            colIndex = cIndex;
            rowIndex = rIndex;
        }

        public String toString()
        {
            return "'" + tableName + "'.[" + rowIndex + "][" + colIndex + "]";
        }
    }

    /**
     * Returns a hashmap containing nodes (as keys) and the rule matching a node
     * (as value).
     */
    private Map buildRules(Document toBeExtracted, String format) throws ExtractorException
    {
        Document ruleDocument = m_parser.getDocument();

        // rule file is not read yet
        if (ruleDocument == null)
        {
            return null;
        }

        String rulesetXpath = "/schemarules/ruleset[@schema=\""
                + toBeExtracted.getDocumentElement().getTagName() + "\"]";
        Node root = ruleDocument.getDocumentElement();

        try
        {
            // Get the node list of the ruleset that describes the rules
            // for the document, toBeExtracted.
            NodeList nl = XPathAPI.selectNodeList(root, rulesetXpath);
            // if can not find the precise ruleset, use the root ruleset
            if (nl.getLength() == 0)
            {
                String rootTag = toBeExtracted.getDocumentElement()
                        .getTagName();
                rulesetXpath = "/schemarules/ruleset[@schema=\""
                        + rootTag.substring(rootTag.indexOf(":") + 1) + "\"]";
                nl = XPathAPI.selectNodeList(root, rulesetXpath);
            }

            return getRuleMap(nl, toBeExtracted, format);
        }
        catch (Exception e)
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.XML_EXTRACTOR_RULES_ERROR,
                    e.toString());
        }
    }

    private Map getRuleMap(NodeList nl, Document toBeExtracted, String format)
            throws Exception
    {
        Map ruleMap = new HashMap();
        Object[] namespaces = getNamespaces(toBeExtracted.getDocumentElement());

        for (int i = 0; i < nl.getLength(); ++i)
        {
            // Get the all <translate> and <dont-translate>
            NodeList rules = nl.item(i).getChildNodes();
            for (int j = 0; j < rules.getLength(); ++j)
            {
                Node ruleNode = rules.item(j);
                if (ruleNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    RuleItemManager.applyRule(ruleNode, toBeExtracted, ruleMap,
                            namespaces, format);
                }
            }
        }

        return ruleMap;
    }

    /**
     * Gets the namespaces in the xml to be extracted.
     * 
     * @param rootElement
     *            root element of the xml to be extracted
     * @return an array of the namespaces
     */
    private Object[] getNamespaces(Element rootElement)
    {
        List<String> list = new ArrayList<String>();
        NamedNodeMap attributes = rootElement.getAttributes();
        if (attributes != null && attributes.getLength() != 0)
        {
            for (int i = 0; i < attributes.getLength(); i++)
            {
                Node attr = attributes.item(i);
                String attrName = attr.getNodeName();
                if (attrName.startsWith("xmlns:"))
                {
                    list.add(attrName.substring(attrName.indexOf(":") + 1));
                }

            }
        }
        return list.toArray();
    }

    public boolean usesEmptyTag()
    {
        return m_useEmptyTag;
    }

    /**
     * Overrides EntityResolver#resolveEntity.
     * 
     * Don't read external DTDs
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException
    {
        return new InputSource(new ByteArrayInputStream(new byte[0]));
    }

    private void setUseEmptyTag() throws Exception
    {
        // get <schemarules> element
        Node rootElement = m_parser.getDocument().getDocumentElement();

        NamedNodeMap attrs = rootElement.getAttributes();
        if (attrs != null)
        {
            Node useEmptyTagAttr = attrs.getNamedItem("use-empty-tag");

            if (useEmptyTagAttr != null)
            {
                String value = useEmptyTagAttr.getNodeValue();

                if (value.equals("false"))
                {
                    m_useEmptyTag = false;
                }
            }
        }
    }

    // ErrorHandler interface methods

    public void error(SAXParseException e) throws SAXException
    {
        throw new SAXException("XML rule file parse error at\n  line "
                + e.getLineNumber() + "\n  column " + e.getColumnNumber()
                + "\n  Message:" + e.getMessage());
    }

    public void fatalError(SAXParseException e) throws SAXException
    {
        error(e);
    }

    public void warning(SAXParseException e)
    {
        System.err.println("XML rule file parse warning at\n  line "
                + e.getLineNumber() + "\n  column " + e.getColumnNumber()
                + "\n  Message:" + e.getMessage());
    }
}
