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
package com.globalsight.ling.docproc.extractor.msoffice2010;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.globalsight.cxe.entity.filterconfiguration.BaseFilter;
import com.globalsight.cxe.entity.filterconfiguration.BaseFilterManager;
import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.HtmlFilter;
import com.globalsight.cxe.entity.filterconfiguration.InternalText;
import com.globalsight.cxe.entity.filterconfiguration.InternalTextHelper;
import com.globalsight.cxe.entity.filterconfiguration.MSOffice2010Filter;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.extractor.msoffice2010.pptx.SlideUtil;
import com.globalsight.ling.docproc.extractor.xml.OfficeXmlContentPostFilter;
import com.globalsight.ling.docproc.extractor.xml.OfficeXmlContentTag;
import com.globalsight.ling.docproc.extractor.xml.XmlFilterHelper;
import com.globalsight.util.StringUtil;

public class PptxExtractor extends AbstractExtractor
{
    static private final Logger logger = Logger.getLogger(PptxExtractor.class);

    private XmlEntities xmlEncoder = new XmlEntities();

    private String rootName = null;
    private int index = 1;

    private Map<String, List<String>> atts = null;
    private List<String> unchars = null;
    private List<String> unParas = null;
    private List<String> internals = null;
    private Boolean isHiddenTextTranslate = null;
    private Boolean isTableOfContentTranslate = null;

    private static Pattern PATTERN_URL = Pattern
            .compile(
                    "https?://(\\w+(-\\w+)*)(\\.(\\w+(-\\w+)*))*((:\\d+)?)(/(\\w+(-\\w+)*))*(\\.?(\\w)*)(\\?)?(((\\w*%)*(\\w*\\?)*(\\w*:)*(\\w*\\+)*(\\w*\\.)*(\\w*&)*(\\w*-)*(\\w*=)*(\\w*%)*(\\w*\\?)*(\\w*:)*(\\w*\\+)*(\\w*\\.)*(\\w*&)*(\\w*-)*(\\w*=)*)*(\\w*)*)",
                    Pattern.CASE_INSENSITIVE);
    private static Pattern HYPERLINKE_RE = Pattern
            .compile("HYPERLINK \"([^\"]*)\"");
    private static Pattern O_RE = Pattern.compile("\\\\o \"([^\"]*)\"");

    private Boolean isUrlTranslate = null;

    private Map<String, String> options = new HashMap<String, String>();

    public XmlUtil util = new XmlUtil();

    private MSOffice2010Filter filter = null;
    private OfficeXmlContentPostFilter postFilter = null;
    private List<InternalText> internalTexts = null;
    private XmlFilterHelper filterHelp = new XmlFilterHelper(null);

    public static Set<String> EXTRACT_NODE = new HashSet<String>();
    static
    {
        EXTRACT_NODE.add("a:t");
        EXTRACT_NODE.add("p:text");
    }

    public static Set<String> NOT_EXTRACT_NODE = new HashSet<String>();
    static
    {
        NOT_EXTRACT_NODE.add("a:fld");
    }

    public static Set<String> MOVABLE_NODES = new HashSet<String>();
    {
        MOVABLE_NODES.add("bold");
        MOVABLE_NODES.add("italic");
        MOVABLE_NODES.add("ulined");
        MOVABLE_NODES.add("office-sub");
        MOVABLE_NODES.add("office-sup");
    }

    private Map<String, List<String>> getTranslateAttsMaps()
    {
        if (atts == null)
        {
            atts = new HashMap<String, List<String>>();

            if ("true".equals(options.get("isToolTipsTranslate")))
            {
                if ("p:sld".equals(rootName))
                {
                    List<String> att = new ArrayList<String>();
                    att.add("descr");
                    atts.put("p:cNvPr", att);
                }
            }

            if ("p:presentation".equals(rootName))
            {
                List<String> att = new ArrayList<String>();
                att.add("name");
                atts.put("p14:section", att);
            }
        }

        return atts;
    }

    @Override
    public void extract() throws ExtractorException
    {
        setMainFormat(ExtractorRegistry.FORMAT_OFFICE_XML);
        initFilter();

        Reader reader = readInput();
        Document document = util.getDocument(reader);
        Node node = document.getFirstChild();
        rootName = node.getNodeName();
        SlideUtil cUtil = new SlideUtil();
        cUtil.handle(document);

        domNodeVisitor(document);
    }

    public void addOptions(String name, String value)
    {
        options.put(name, value);
    }

    public void outputXMLDeclaration(Document document)
    {
        String encoding = document.getXmlEncoding();
        String version = document.getXmlVersion();
        String standalone = document.getXmlStandalone() ? "yes" : "no";

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml");
        if (version != null)
        {
            sb.append(" version=\"" + version + "\"");
        }
        if (encoding != null)
        {
            sb.append(" encoding=\"" + encoding + "\"");
        }

        sb.append(" standalone=\"" + standalone + "\"");
        sb.append(" ?>\n");

        outputSkeleton(sb.toString());
    }

    public void domNodeVisitor(Document document)
    {
        outputXMLDeclaration(document);
        nodeVisitor(document.getFirstChild(), true);
    }

    private void getTextNode(Node node, List<Node> ns)
    {
        Node n = node.getFirstChild();
        while (n != null)
        {
            if (n.getNodeType() == Node.TEXT_NODE)
                ns.add(n);
            else
                getTextNode(n, ns);

            n = n.getNextSibling();
        }
    }

    private boolean hasContent(Node node)
    {
        if (!isUrlTranslate())
        {
            List<Node> ns = new ArrayList<Node>();
            getTextNode(node, ns);
            if (ns.size() > 0)
            {
                for (Node n : ns)
                {
                    String s = n.getTextContent();
                    StringBuffer sb = new StringBuffer();
                    Matcher m = PATTERN_URL.matcher(s);
                    int i = 0;
                    while (m.find())
                    {
                        sb.append(escapeString(s.substring(i, m.start())));
                        i = m.end();
                    }

                    sb.append(s.substring(i));
                    String content = sb.toString();
                    if (!isEmpty(content))
                        return true;
                }
            }

            return false;
        }
        else
        {
            String content = node.getTextContent();
            return !isEmpty(content);
        }
    }

    private boolean isEmpty(String s)
    {
        if (s == null)
            return true;

        s = s.trim();
        if (s.length() == 0)
            return true;

        s = StringUtil.replace(s, "\u00a0", "");
        if (s.length() == 0)
            return true;

        return false;
    }

    public void handleChild(Node node)
    {
        List<Node> cs = util.getChildNodes(node);
        StringBuffer sb = new StringBuffer();

        for (Node c : cs)
        {
            if (c.getNodeType() == Node.TEXT_NODE)
            {
                sb.append(c.getTextContent());
            }
            else
            {
                if (sb.length() > 0)
                {
                    outGxmlForText(sb.toString());
                    sb = new StringBuffer();
                }

                outGxmlForTranslateNode(c);
            }
        }

        if (sb.length() > 0)
        {
            outGxmlForText(sb.toString());
        }
    }

    public void handleTranslateNode(Node node)
    {
        index = 0;
        handleChild(node);
    }

    private String getType(Node node)
    {
        String nodeName = node.getNodeName();
        if ("atStyle".equals(nodeName))
        {
            NamedNodeMap attrs = node.getAttributes();

            Node n = attrs.getNamedItem("styleType");
            if (n != null)
                return n.getNodeValue();

            if (attrs.getLength() > 0)
            {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < attrs.getLength(); i++)
                {
                    if (sb.length() > 0)
                        sb.append("_");

                    sb.append(attrs.item(i).getNodeName());
                }

                return sb.toString();
            }
        }

        return "";
    }

    public boolean isInternalNode(Node node)
    {
        if ("style".equals(node.getNodeName()))
        {
            NamedNodeMap attrs = node.getAttributes();
            for (int j = 0; j < attrs.getLength(); ++j)
            {
                Node att = attrs.item(j);
                String attname = att.getNodeName();
                String value = att.getNodeValue();

                if ("w:val".equals(attname)
                        && getInternals().indexOf(value) > -1)
                    return true;
            }
        }

        return false;
    }

    public boolean isMovableNode(String type)
    {
        if (MOVABLE_NODES.contains(type))
            return true;

        return false;
    }

    /**
     * Handles internal text.
     */
    private String handleInternalText(String value, boolean isInline,
            boolean isPreserveWS)
    {
        int oriIndex = ++index;
        List<String> handled = InternalTextHelper.handleStringWithListReturn(
                value, internalTexts, getMainFormat());

        for (int i = 0; i < handled.size(); i++)
        {
            String s = handled.get(i);
            if (!s.startsWith(InternalTextHelper.GS_INTERNALT_TAG_START))
            {
                s = filterHelp.processText(s, isInline, isPreserveWS);
                handled.set(i, s);
            }
        }

        int newIndex = InternalTextHelper.assignIndexToBpt(oriIndex, handled);
        index = newIndex;

        return InternalTextHelper.listToString(handled);
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
                if (postFilter.isTranslatableAttribute(attname))
                {
                    stuff.append(createSubTag(true, null, null));
                    String temp = xmlEncoder.encodeStringBasic(strValue);
                    stuff.append(temp);
                    stuff.append("</sub>");
                }
                else
                {
                    stuff.append(xmlEncoder.encodeStringBasic(strValue));
                }

                stuff.append(quote);
            }

            outputTranslatableTmx(stuff.toString());
        }
    }

    private void outGxmlForPureText(String pureText)
    {
        if (postFilter != null)
        {
            List<OfficeXmlContentTag> tagsInContent = postFilter
                    .detectTags(pureText);
            for (OfficeXmlContentTag tag : tagsInContent)
            {
                String tagName = tag.getName();
                if (postFilter.isInlineTag(tagName))
                {
                    // output text
                    int tagIndex = pureText.indexOf(tag.toString());
                    String textBeforeTag = pureText.substring(0, tagIndex);
                    String text = textBeforeTag;

                    // internal text
                    text = handleInternalText(text, true, false);
                    outputTranslatableTmx(text);

                    // output inline element "tag"
                    StringBuilder stuff = new StringBuilder();
                    if (tag.isPaired())
                    {
                        if (tag.isEndTag())
                        {
                            stuff.append("<ept i=\"");
                            stuff.append(tag.getPairedTag().getBptIndex());
                            stuff.append("\"");
                        }
                        else
                        {
                            int bptIndex = ++index;
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
                        stuff.append(filterHelp.processText(tag.toString(),
                                true, false));
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

                    outputTranslatableTmx(stuff.toString());
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

                    outputTranslatableTmx(stuff.toString());

                    // text after tag - new node value
                    pureText = pureText.substring(tagIndex
                            + tag.toString().length());
                }
            }
        }

        outputTranslatableTmx(escapeString(pureText));
    }

    private void outGxmlForText(String s)
    {
        if (!isUrlTranslate())
        {
            StringBuilder sb = new StringBuilder();

            Matcher m = PATTERN_URL.matcher(s);
            int n = 0;
            while (m.find())
            {
                outGxmlForPureText(s.substring(n, m.start()));

                sb.append("<ph type=\"url\" i=\"").append(n).append("\">");
                sb.append(escapeString(escapeString(m.group())));
                sb.append("</ph>");

                outputTranslatableTmx(sb.toString());
                sb = new StringBuilder();

                n = m.end();
            }

            outGxmlForPureText(s.substring(n));
        }
        else
        {
            outGxmlForPureText(s);
        }
    }

    private void outBpt(Node node, int n)
    {
        StringBuilder sb = new StringBuilder();

        String type = getType(node);

        sb.append("<bpt i=\"").append(n).append("\" type=\"").append(type)
                .append("\" ");

        if (isInternalNode(node))
        {
            sb.append("internal=\"yes\" ");
        }
        else if (isMovableNode(type))
        {
            sb.append("erasable=\"yes\" ");
        }

        sb.append(">");

        sb.append("&lt;");

        String name = node.getNodeName();
        sb.append(name);
        NamedNodeMap attrs = node.getAttributes();

        List<String> atts = getTranslateAttsMaps().get(name);

        for (int j = 0; j < attrs.getLength(); ++j)
        {
            Node att = attrs.item(j);
            String attname = att.getNodeName();
            String value = att.getNodeValue();

            sb.append(" ").append(attname).append("=\"");

            if (atts != null && atts.indexOf(attname) > -1)
            {
                sb.append("<sub locType=\"translatable\" id=\"1\">");
                sb.append(escapeString(value)).append("</sub>");
            }
            else
            {
                sb.append(escapeString(escapeString(value)));
            }
            sb.append("\"");
        }
        sb.append("&gt;");

        if ("atStyle".equals(name))
        {
            Node c = node.getFirstChild();
            if (c != null && "atStyleChild".equals(c.getNodeName()))
            {
                util.getXmlString(c, sb);
            }
        }

        sb.append("</bpt>");

        outputTranslatableTmx(sb.toString());
    }

    private void outEpt(Node node, int n)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<ept i=\"").append(n).append("\">");

        sb.append("&lt;/");
        sb.append(node.getNodeName());
        sb.append("&gt;");

        sb.append("</ept>");

        outputTranslatableTmx(sb.toString());
    }

    public void outGxmlForTranslateNode(Node node)
    {
        if (node.getNodeType() == Node.TEXT_NODE)
        {
            outGxmlForText(node.getTextContent());
            return;
        }

        int n = ++index;

        if ("atBr".equals(node.getNodeName()))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("<ph type=\"br\" i=\"").append(++index).append("\">");
            util.getXmlString(node, sb);
            sb.append("</ph>");
            outputTranslatableTmx(sb.toString());
        }
        else
        {
            outBpt(node, n);

            String name = node.getNodeName();
            boolean isAtStyle = "atStyle".equals(name);
            List<Node> cs = util.getChildNodes(node);
            if (isAtStyle && cs.size() > 0)
            {
                Node c = cs.get(0);
                if (c != null && "atStyleChild".equals(c.getNodeName()))
                {
                    cs.remove(0);
                }
            }

            for (Node c : cs)
            {
                outGxmlForTranslateNode(c);
            }

            outEpt(node, n);
        }
    }

    public String escapeString(String s)
    {
        return com.globalsight.diplomat.util.XmlUtil.escapeString(s);
    }

    private List<String> getUnchars()
    {
        if (unchars == null)
        {
            String unCharStyles = options.get("unCharStyles");
            if (unCharStyles != null)
            {
                unchars = MSOffice2010Filter.toList(unCharStyles);
            }
        }

        return unchars;
    }

    private List<String> getUnParas()
    {
        if (unParas == null)
        {
            String unCharStyles = options.get("unParaStyles");
            if (unCharStyles != null)
            {
                unParas = MSOffice2010Filter.toList(unCharStyles);
            }
        }

        return unParas;
    }

    public List<String> getInternals()
    {
        if (internals == null)
        {
            String unCharStyles = options.get("internalCharStyles");
            if (unCharStyles != null)
            {
                internals = MSOffice2010Filter.toList(unCharStyles);
            }
        }

        return internals;
    }

    private boolean isHiddenTextTranslate()
    {
        if (isHiddenTextTranslate == null)
        {
            isHiddenTextTranslate = true;

            String s = options.get("isHiddenTextTranslate");
            if (s != null && !Boolean.parseBoolean(s))
                isHiddenTextTranslate = false;
        }

        return isHiddenTextTranslate;
    }

    private boolean isTableOfContentTranslate()
    {
        if (isTableOfContentTranslate == null)
        {
            isTableOfContentTranslate = true;

            String s = options.get("isTableOfContentTranslate");
            if (s != null && !Boolean.parseBoolean(s))
                isTableOfContentTranslate = false;
        }

        return isTableOfContentTranslate;
    }

    private boolean isToolTipsTranslate()
    {
        return "true".equals(options.get("isToolTipsTranslate"));
    }

    private boolean isUrlTranslate()
    {
        if (isUrlTranslate == null)
        {
            MSOffice2010Filter filter = getFilter();
            if (filter != null)
            {
                isUrlTranslate = filter.isUrlTranslate();
            }
        }

        if (isUrlTranslate == null)
            isUrlTranslate = false;

        return isUrlTranslate;
    }

    private MSOffice2010Filter getFilter()
    {
        if (filter == null)
        {
            Filter mainFilter = getMainFilter();
            if (mainFilter != null)
            {
                if (mainFilter instanceof MSOffice2010Filter)
                {
                    filter = (MSOffice2010Filter) mainFilter;
                }
            }
        }

        return filter;
    }

    private void initFilter()
    {
        initInternalTexts();
        initPostFilter();
        filterHelp.setXmlEntities(xmlEncoder);
    }

    private void initInternalTexts()
    {
        MSOffice2010Filter filter = getFilter();
        if (filter != null)
        {
            BaseFilter baseFilter = BaseFilterManager.getBaseFilterByMapping(
                    filter.getId(), filter.getFilterTableName());
            try
            {
                internalTexts = BaseFilterManager.getInternalTexts(baseFilter);
            }
            catch (Exception e)
            {
                logger.error(e);
            }
        }
    }

    private void initPostFilter()
    {
        MSOffice2010Filter filter = getFilter();
        if (filter != null)
        {
            long contentPostFilterId = filter.getContentPostFilterId();
            if (contentPostFilterId > 0)
            {
                HtmlFilter contentPostFilter = FilterHelper
                        .getHtmlFilter(contentPostFilterId);
                if (contentPostFilter != null)
                {
                    postFilter = new OfficeXmlContentPostFilter(
                            contentPostFilter);
                }
            }
        }
    }

    private boolean isUnextractLink(Node node)
    {
        if (!isTableOfContentTranslate()
                && "w:hyperlink".equals(node.getNodeName()))
        {
            NamedNodeMap attrs = node.getAttributes();
            for (int j = 0; j < attrs.getLength(); ++j)
            {
                Node att = attrs.item(j);
                String attname = att.getNodeName();
                String value = att.getNodeValue();

                if ("w:anchor".equals(attname) && value.startsWith("_Toc"))
                    return true;
            }
        }

        return false;
    }

    public boolean isUnextractWr(Node node)
    {
        if (!"w:r".equals(node.getNodeName()))
            return false;

        Node rPr = util.getNode(node, "w:rPr", false);
        if (rPr != null)
        {
            // w:webHidden
            if (util.getNode(rPr, "w:webHidden", false) != null)
            {
                return true;
            }

            // w:vanish
            if (!isHiddenTextTranslate())
            {
                Node vanish = util.getNode(rPr, "w:vanish", false);
                if (vanish != null)
                    return true;
            }

            // unCharStyles
            List<String> unchar = getUnchars();
            if (unchar != null && unchar.size() > 0)
            {
                List<Node> rStyles = util.getNodes(rPr, "w:rStyle");

                for (Node r : rStyles)
                {
                    NamedNodeMap attrs = r.getAttributes();
                    for (int j = 0; j < attrs.getLength(); ++j)
                    {
                        Node att = attrs.item(j);
                        String attname = att.getNodeName();
                        String value = att.getNodeValue();

                        if ("w:val".equals(attname)
                                && unchar.indexOf(value) > -1)
                            return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isUnextractWp(Node node)
    {
        if (!"w:p".equals(node.getNodeName()))
            return false;

        Node rPr = util.getNode(node, "w:pPr", false);

        if (rPr != null)
        {
            List<String> unParas = getUnParas();
            if (unParas != null && unParas.size() > 0)
            {
                List<Node> rStyles = util.getNodes(rPr, "w:pStyle");

                for (Node r : rStyles)
                {
                    NamedNodeMap attrs = r.getAttributes();
                    for (int j = 0; j < attrs.getLength(); ++j)
                    {
                        Node att = attrs.item(j);
                        String attname = att.getNodeName();
                        String value = att.getNodeValue();

                        if ("w:val".equals(attname)
                                && unParas.indexOf(value) > -1)
                            return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isUnextractNode(Node node)
    {
        String name = node.getNodeName();

        if (NOT_EXTRACT_NODE.contains(name))
            return true;

        if (isUnextractWp(node))
            return true;

        if (isUnextractWr(node))
            return true;

        if (isUnextractLink(node))
            return true;

        return false;
    }

    private boolean handleInstrText(Node node)
    {
        index = 0;
        List<Node> cs = util.getChildNodes(node);

        if (cs.size() != 1)
            return false;

        Node c = cs.get(0);

        if (c.getNodeType() == Node.TEXT_NODE)
        {
            String content = c.getTextContent();
            if (isUrlTranslate())
            {
                Matcher m = HYPERLINKE_RE.matcher(content);
                if (m.find())
                {
                    outputSkeleton(escapeString(content
                            .substring(0, m.start(1))));
                    outputTranslatable(m.group(1));

                    content = content.substring(m.end(1));
                }
            }

            if (content.length() > 0 && isToolTipsTranslate())
            {
                Matcher m = O_RE.matcher(content);
                if (m.find())
                {
                    outputSkeleton(escapeString(content
                            .substring(0, m.start(1))));
                    outputTranslatable(m.group(1));

                    content = content.substring(m.end(1));
                }
            }

            if (content.length() > 0)
            {
                outputSkeleton(escapeString(content));
            }

            return true;
        }

        return false;
    }

    public void nodeVisitor(Node node, boolean extract)
    {
        if (node.getNodeType() == Node.TEXT_NODE)
        {
            outputSkeleton(escapeString(node.getTextContent()));
            return;
        }

        String name = node.getNodeName();

        if (extract)
        {
            if (isUnextractNode(node))
                extract = false;
        }

        outputSkeleton("<" + name);
        List<String> atts = null;
        if (extract)
        {
            atts = getTranslateAttsMaps().get(name);
        }
        outputAttributes(node.getAttributes(), atts);

        Node c = node.getFirstChild();
        if (c != null)
        {
            outputSkeleton(">");

            boolean handle = false;

            if (extract)
            {
                if (EXTRACT_NODE.contains(name))
                {
                    if (hasContent(node))
                    {
                        handleTranslateNode(node);
                        handle = true;
                    }
                    else
                    {
                        extract = false;
                    }
                }
                else if ("w:instrText".equals(name))
                {
                    if (handleInstrText(node))
                        handle = true;
                }
            }

            if (!handle)
            {
                while (c != null)
                {
                    nodeVisitor(c, extract);
                    c = c.getNextSibling();
                }
            }

            outputSkeleton("</" + name + ">");
        }
        else
        {
            outputSkeleton("/>");
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
    public void outputAttributes(NamedNodeMap attrs, List<String> atts)
            throws ExtractorException
    {
        if (attrs == null)
        {
            return;
        }

        if ("Relationships".equals(rootName))
        {
            boolean extract = false;
            Node node = attrs.getNamedItem("TargetMode");
            if (node != null && "External".equals(node.getNodeValue()))
            {
                extract = true;
            }

            for (int i = 0; i < attrs.getLength(); ++i)
            {
                Node att = attrs.item(i);
                String attname = att.getNodeName();
                String value = att.getNodeValue();

                if (extract && "Target".equals(attname))
                {
                    outputSkeleton(" " + attname + "=\"");
                    outputTranslatable(value);
                    outputSkeleton("\"");
                }
                else
                {
                    outputSkeleton(" " + attname + "=\"" + escapeString(value)
                            + "\"");
                }
            }

            return;
        }

        for (int i = 0; i < attrs.getLength(); ++i)
        {
            Node att = attrs.item(i);
            String attname = att.getNodeName();
            String value = att.getNodeValue();

            if (atts != null && atts.indexOf(attname) > -1)
            {
                outputSkeleton(" " + attname + "=\"");
                outputTranslatable(value);
                outputSkeleton("\"");
            }
            else
            {
                outputSkeleton(" " + attname + "=\"" + escapeString(value)
                        + "\"");
            }

        }
    }

    public void outputSkeleton(String s)
    {
        getOutput().addSkeleton(s);

    }

    public void outputTranslatable(String s)
    {
        getOutput().addTranslatable(s);
    }

    public void outputTranslatableTmx(String s)
    {
        getOutput().addTranslatableTmx(s);
    }

    @Override
    public void loadRules() throws ExtractorException
    {

    }
}
