package com.globalsight.ling.docproc.extractor.xml;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class XmlFilterAttribute
{
    private String m_attrName = null;
    private String m_operator = null;
    private String m_attrValue = null;
    
    public XmlFilterAttribute(String attrName, String operator, String attrValue)
    {
        this.m_attrName = attrName;
        this.m_operator = operator;
        this.m_attrValue = attrValue;
    }
    
    public boolean match(Node p_node)
    {
        if (m_attrName != null && m_operator != null && m_attrValue != null
                && p_node.getNodeType() == Node.ELEMENT_NODE)
        {        
            NamedNodeMap attributes = p_node.getAttributes();
            
            if (attributes == null || attributes.getLength() == 0)
            {
                return false;
            }
            
            for(int i = 0; i < attributes.getLength(); i++)
            {
                Node node = attributes.item(i);
                String nodeName = node.getNodeName();
                String localName = node.getLocalName();
                
                if (m_attrName.equals(localName) || m_attrName.equals(nodeName))
                {
                    return isValueMatched(node.getNodeValue());
                }
            }
        }
        
        return false;
    }

    private boolean isValueMatched(String nodeValue)
    {
        if ("equal".equals(m_operator))
        {
            return m_attrValue.equals(nodeValue);
        }
        else if ("not equal".equals(m_operator))
        {
            return !m_attrValue.equals(nodeValue);
        }
        else if ("match".equals(m_operator))
        {
            return nodeValue.matches(m_attrValue);
        }
        else
        {
            // should not here
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        return m_attrName + " " + m_operator + " " + m_attrValue;
    }
}
