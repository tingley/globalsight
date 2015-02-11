package com.globalsight.ling.docproc.extractor.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;

import org.apache.xalan.xpath.MutableNodeListImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlFilterTag
{
    private String m_tagName = null;
    private List<XmlFilterAttribute> m_attributes = null;
    private List<String> m_transAttributesEmbed = null;
    private List<String> m_transAttributesSepa = null;
    private boolean m_contentInclude = true;
    
    public XmlFilterTag()
    {
        m_attributes = new ArrayList<XmlFilterAttribute>();
        m_transAttributesEmbed = new ArrayList<String>();
        m_transAttributesSepa = new ArrayList<String>();
    }

    public XmlFilterTag(Element tagElement)
    {
        Node tagNameElement = tagElement.getElementsByTagName("tagName").item(0);
        m_tagName = tagNameElement.getFirstChild().getNodeValue();

        NodeList attributeNodes = tagElement.getElementsByTagName("attributes");
        m_attributes = buildAttributes(attributeNodes);
        
        NodeList contentInclTypeNodes = tagElement.getElementsByTagName("inclType");
        if (contentInclTypeNodes != null && contentInclTypeNodes.getLength() > 0)
        {
            m_contentInclude = "1".equals(contentInclTypeNodes.item(0).getFirstChild().getNodeValue());
        }
        
        String transAttrSegRule = getTransAttrSegRule(tagElement);
        NodeList transAttributeNodes = tagElement.getElementsByTagName("transAttributes");
        buildTransAttributes(transAttributeNodes, transAttrSegRule);
    }

    public List<Node> getMatchedNodeList(Document p_doc) throws Exception
    {
        if (m_tagName == null || m_tagName.length() == 0)
        {
            return new ArrayList<Node>();
        }
        
        String xpath = "//*[name()=\"" + m_tagName + "\"] | //*[local-name()=\"" + m_tagName + "\"]";
        NodeList affectedNodes = XPathAPI.selectNodeList(p_doc.getDocumentElement(), xpath);
        
        if (affectedNodes == null || affectedNodes.getLength() == 0)
        {
            affectedNodes = selectNodeListUseMatch(p_doc.getDocumentElement(), m_tagName);
        }

        List<Node> nodes = new ArrayList<Node>();

        if (affectedNodes != null && affectedNodes.getLength() > 0)
        {
            for (int i = 0; i < affectedNodes.getLength(); i++)
            {
                Node node = affectedNodes.item(i);
                if (isAttributesMatch(node))
                {
                    nodes.add(node);
                }
            }
        }

        return nodes;
    }
    
    /**
     * 0 for non-extract, 1 for embed, 2 for separate
     * @param attrName
     * @return
     */
    public int getAttributeTranlateMode(Node node)
    {
        String nodeName = node.getNodeName();
        String localName = node.getLocalName();
        
        if (m_transAttributesEmbed.contains(nodeName) || m_transAttributesEmbed.contains(localName))
        {
            return 1;
        }
        
        if (m_transAttributesSepa.contains(nodeName) || m_transAttributesSepa.contains(localName))
        {
            return 2;
        }
        
        return 0;
    }
    
    public boolean isContentInclude()
    {
        return m_contentInclude;
    }
    
    @Override
    public String toString()
    {
        return m_tagName;
    }
    
    public void setTagName(String tagName)
    {
        m_tagName = tagName;
    }
    
    public void setContentInclude(boolean contentInclude)
    {
        m_contentInclude = contentInclude;
    }
    
    public void setAttributes(List<XmlFilterAttribute> attributes)
    {
        m_attributes = attributes;
    }

    private String getTransAttrSegRule(Element tagElement)
    {
        NodeList ruleElements = tagElement.getElementsByTagName("transAttrSegRule");
        
        if (ruleElements != null && ruleElements.getLength() > 0)
        {
            Node ele = ruleElements.item(0);
            return ele.getFirstChild().getNodeValue();
        }
        
        return "1";
    }

    private void buildTransAttributes(NodeList transAttributeNodes, String transAttrSegRule)
    {
        m_transAttributesEmbed = new ArrayList<String>();
        m_transAttributesSepa = new ArrayList<String>();
        
        if (transAttributeNodes != null && transAttributeNodes.getLength() > 0)
        {
            int length = transAttributeNodes.getLength();
            for (int i = 0; i < length; i++)
            {
                Node node = transAttributeNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element el = (Element) node;
                    String attrName = el.getElementsByTagName("aName").item(0).getFirstChild()
                            .getNodeValue();
//                    String transAttrSegRule = el.getElementsByTagName("transAttrSegRule").item(0).getFirstChild()
//                            .getNodeValue();
                    
                    // transAttrSegRule 1 emb 2 sepa
                    if ("2".equals(transAttrSegRule))
                    {
                        m_transAttributesSepa.add(attrName);
                    }
                    else
                    {
                        m_transAttributesEmbed.add(attrName);
                    }
                }
            }
        }
    }

    private List<XmlFilterAttribute> buildAttributes(NodeList attributeNodes)
    {
        List<XmlFilterAttribute> ret = new ArrayList<XmlFilterAttribute>();
        if (attributeNodes != null && attributeNodes.getLength() > 0)
        {
            int length = attributeNodes.getLength();
            for (int i = 0; i < length; i++)
            {
                Node node = attributeNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element el = (Element) node;
                    String attrName = el.getElementsByTagName("aName").item(0).getFirstChild()
                            .getNodeValue();
                    String operator = el.getElementsByTagName("aOp").item(0).getFirstChild()
                            .getNodeValue();
                    String attrValue = el.getElementsByTagName("aValue").item(0).getFirstChild()
                            .getNodeValue();
                    XmlFilterAttribute xa = new XmlFilterAttribute(attrName, operator, attrValue);
                    ret.add(xa);
                }
            }
        }

        return ret;
    }

    public static NodeList selectNodeListUseMatch(Node node, String tagNameRE)
    {
        MutableNodeListImpl nodeList = new MutableNodeListImpl();
        addMatchedNode(nodeList, node, tagNameRE);
        
        return nodeList;
    }

    private static void addMatchedNode(MutableNodeListImpl nodeList, Node node, String tagNameRE)
    {
        if (node.getNodeType() == Node.ELEMENT_NODE)
        {
            String tagName = node.getNodeName();
            if (tagName.matches(tagNameRE))
            {
                nodeList.addNode(node);
            }
            else
            {
                String localName = node.getLocalName();
                if (localName.matches(tagNameRE))
                {
                    nodeList.addNode(node);
                }
            }
        }
        
        NodeList childs = node.getChildNodes();
        
        if (childs != null && childs.getLength() != 0)
        {
            for (int i = 0; i < childs.getLength(); i++)
            {
                Node n = childs.item(i);
                addMatchedNode(nodeList, n, tagNameRE);
            }
        }
    }

    private boolean isAttributesMatch(Node p_node)
    {
        if (m_attributes == null || m_attributes.size() == 0)
        {
            return true;
        }

        for (XmlFilterAttribute attr : m_attributes)
        {
            if (!attr.match(p_node))
            {
                return false;
            }
        }

        return true;
    }

    private boolean isTagNameMatch(Node p_node)
    {
        // TODO Auto-generated method stub
        return false;
    }
}
