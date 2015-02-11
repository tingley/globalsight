package com.globalsight.ling.docproc.extractor.xml;

import java.util.ArrayList;
import java.util.List;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlFilterSidTag
{
    private String m_tagName = null;
    private String m_attName = null;

    public XmlFilterSidTag(String tagName, String attName)
    {
        m_tagName = tagName;
        m_attName = attName;
    }

    public List<Node> getMatchedNodeList(Document p_doc) throws Exception
    {
        if (m_tagName == null || m_tagName.length() == 0 || m_attName == null
                || m_attName.length() == 0)
        {
            return new ArrayList<Node>();
        }

        String xpath = "//*[name()=\"" + m_tagName + "\"] | //*[local-name()=\"" + m_tagName
                + "\"]";
        NodeList affectedNodes = XPathAPI.selectNodeList(p_doc.getDocumentElement(), xpath);

        if (affectedNodes == null || affectedNodes.getLength() == 0)
        {
            affectedNodes = XmlFilterTag.selectNodeListUseMatch(p_doc.getDocumentElement(),
                    m_tagName);
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

    public static List<Node> getChildNodes(Node node)
    {
        List<Node> result = new ArrayList<Node>();
        
        if (node == null || node.getNodeType() != Node.ELEMENT_NODE)
        {
            return result;
        }
        
        NodeList childNodes = node.getChildNodes();
        
        if (childNodes != null && childNodes.getLength() > 0)
        {
            for(int i = 0; i < childNodes.getLength(); i++)
            {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE)
                {
                    result.add(child);
                    result.addAll(getChildNodes(child));
                }
            }
        }
        
        return result;
    }

    private boolean isAttributesMatch(Node node)
    {
        Node sidAt = getSidAttribute(node);

        return sidAt != null;
    }

    public Node getSidAttribute(Node node)
    {
        NamedNodeMap atts = node.getAttributes();
        Node sidAt = null;

        if (m_attName != null && atts != null && atts.getLength() > 0)
        {
            for (int i = 0; i < atts.getLength(); i++)
            {
                Node att = atts.item(i);
                String localeName = att.getLocalName();
                String name = att.getNodeName();

                if (m_attName.equals(name) || m_attName.equals(localeName))
                {
                    sidAt = att;
                }

                if (name.matches(m_attName) || localeName.matches(m_attName))
                {
                    sidAt = att;
                }

                if (sidAt != null)
                {
                    break;
                }
            }
        }

        return sidAt;
    }

    @Override
    public String toString()
    {
        return m_tagName;
    }
}
