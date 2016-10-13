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

package com.globalsight.ling.common.srccomment;

import java.util.ArrayList;
import java.util.List;

import org.apache.xpath.NodeSet;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.globalsight.ling.docproc.extractor.xml.XmlFilterAttribute;

public class SrcCmtXmlTag
{
    private String m_tagName = null;
    private List<XmlFilterAttribute> m_attributes = null;
    private String attributeName = null;
    private boolean fromAttribute = false;

    public SrcCmtXmlTag()
    {
        m_attributes = new ArrayList<XmlFilterAttribute>();
    }

    public SrcCmtXmlTag(Element tagElement)
    {
        Node tagNameElement = tagElement.getElementsByTagName("tagName")
                .item(0);
        m_tagName = tagNameElement.getFirstChild().getNodeValue();

        NodeList attributeNodes = tagElement.getElementsByTagName("attributes");
        m_attributes = buildAttributes(attributeNodes);

        NodeList contentInclTypeNodes = tagElement
                .getElementsByTagName("fromAttribute");
        if (contentInclTypeNodes != null
                && contentInclTypeNodes.getLength() > 0)
        {
            fromAttribute = "true".equalsIgnoreCase(contentInclTypeNodes
                    .item(0).getFirstChild().getNodeValue());

            if (fromAttribute)
            {
                attributeName = tagElement
                        .getElementsByTagName("attributeName").item(0)
                        .getFirstChild().getNodeValue();
            }
        }
    }

    public List<Node> getMatchedNodeList(Document p_doc) throws Exception
    {
        if (m_tagName == null || m_tagName.length() == 0)
        {
            return new ArrayList<Node>();
        }

        String xpath = "//*[name()=\"" + m_tagName
                + "\"] | //*[local-name()=\"" + m_tagName + "\"]";
        NodeList affectedNodes = XPathAPI.selectNodeList(
                p_doc.getDocumentElement(), xpath);

        if (affectedNodes == null || affectedNodes.getLength() == 0)
        {
            affectedNodes = selectNodeListUseMatch(p_doc.getDocumentElement(),
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

    @Override
    public String toString()
    {
        return m_tagName;
    }

    public void setTagName(String tagName)
    {
        m_tagName = tagName;
    }

    public void setAttributes(List<XmlFilterAttribute> attributes)
    {
        m_attributes = attributes;
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
                    String attrName = el.getElementsByTagName("aName").item(0)
                            .getFirstChild().getNodeValue();
                    String operator = el.getElementsByTagName("aOp").item(0)
                            .getFirstChild().getNodeValue();
                    String attrValue = el.getElementsByTagName("aValue")
                            .item(0).getFirstChild().getNodeValue();
                    XmlFilterAttribute xa = new XmlFilterAttribute(attrName,
                            operator, attrValue);
                    ret.add(xa);
                }
            }
        }

        return ret;
    }

    public static NodeList selectNodeListUseMatch(Node node, String tagNameRE)
    {
        NodeSet nodeSet = new NodeSet();
        addMatchedNode(nodeSet, node, tagNameRE);

        return nodeSet;
    }

    private static void addMatchedNode(NodeSet nodeSet, Node node,
            String tagNameRE)
    {
        if (node.getNodeType() == Node.ELEMENT_NODE)
        {
            String tagName = node.getNodeName();
            if (tagName.matches(tagNameRE))
            {
                nodeSet.addNode(node);
            }
            else
            {
                String localName = node.getLocalName();
                if (localName.matches(tagNameRE))
                {
                    nodeSet.addNode(node);
                }
            }
        }

        NodeList childs = node.getChildNodes();

        if (childs != null && childs.getLength() != 0)
        {
            for (int i = 0; i < childs.getLength(); i++)
            {
                Node n = childs.item(i);
                addMatchedNode(nodeSet, n, tagNameRE);
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

    public String getAttributeName()
    {
        return attributeName;
    }

    public void setAttributeName(String attributeName)
    {
        this.attributeName = attributeName;
    }

    public boolean isFromAttribute()
    {
        return fromAttribute;
    }

    public void setFromAttribute(boolean fromAttribute)
    {
        this.fromAttribute = fromAttribute;
    }

    // //////////////////////////////////////////////
    // static methods
    // //////////////////////////////////////////////

    public static SrcCmtXmlTag initFromElement(Element tagElement)
    {
        return new SrcCmtXmlTag(tagElement);
    }
}
